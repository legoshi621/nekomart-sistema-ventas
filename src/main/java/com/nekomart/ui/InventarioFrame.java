package com.nekomart.ui;

import com.nekomart.Main;
import com.nekomart.dao.MovimientoDAO;
import com.nekomart.dao.ProductoDAO;
import com.nekomart.models.Movimiento;
import com.nekomart.models.Producto;
import com.nekomart.models.Usuario;
import com.nekomart.services.ProductoService;
import com.nekomart.utils.SessionManager;
import com.nekomart.utils.ThemeManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class InventarioFrame extends JPanel {

    private static final Color LAVANDA = ThemeManager.AZUL_PRIMARIO;
    private static final Color LAVANDA_CLARO = ThemeManager.AZUL_MUY_CLARO;
    private static final Color CORAL = ThemeManager.AZUL_PRIMARIO;
    private static final Color CORAL_HOVER = ThemeManager.AZUL_OSCURO;
    private static final Color FONDO = ThemeManager.FONDO_PRINCIPAL;
    private static final Color TEXTO_OSCURO = ThemeManager.GRIS_OSCURO;
    private static final Color TEXTO_GRIS = ThemeManager.GRIS_MEDIO;
    private static final Color BORDE = ThemeManager.GRIS_CLARO;
    private static final Color FILA_ALTERNA = ThemeManager.FONDO_PRINCIPAL;
    private static final Color HOVER_LAVANDA = ThemeManager.AZUL_MUY_CLARO;
    private static final Color STOCK_BAJO_BG = new Color(254, 226, 226);
    private static final Color STOCK_BAJO_TEXT = ThemeManager.PELIGRO;
    private static final Color STOCK_BAJO_BORDE = ThemeManager.PELIGRO;
    private static final Color CADUCIDAD_BG = new Color(254, 243, 199);
    private static final Color CADUCIDAD_TEXT = ThemeManager.ADVERTENCIA;
    private static final Color MENTA = ThemeManager.EXITO;

    private ProductoService productoService;
    private MovimientoDAO movimientoDAO;
    private ProductoDAO productoDAO;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
private JButton btnNuevo, btnEditar, btnEliminar, btnEntradaStock, btnExportar;
    // ← NUEVO: Columna "Imagen" agregada al inicio
    private final String[] COLUMNAS = {"Imagen", "ID", "Código", "Nombre", "Precio", "Stock", "Stock Mín.", "Categoría", "Lote", "Caducidad"};

    public InventarioFrame() {
    productoService = new ProductoService();
    movimientoDAO = new MovimientoDAO();
    productoDAO = new ProductoDAO();
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    setBackground(FONDO);

    initComponents();
    cargarProductos();
    
    // ← AGREGAR: Detectar rol y ocultar botones si es empleado
    Usuario usuarioActual = SessionManager.getInstancia().getUsuarioActual();
    if (usuarioActual != null && "EMPLEADO".equals(usuarioActual.getRol().toUpperCase())) {
        // Ocultar botones de modificación
        btnNuevo.setVisible(false);
        btnEditar.setVisible(false);
        btnEliminar.setVisible(false);
        btnEntradaStock.setVisible(false);
        btnExportar.setVisible(false);
    }
}
    private void initComponents() {
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelSuperior.setOpaque(false);

        JLabel lblBuscar = new JLabel("🔍");
        lblBuscar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        txtBuscar = new JTextField(22);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por nombre o código...");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.setPreferredSize(new Dimension(280, 45));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
btnNuevo = crearBotonPastel("➕ Nuevo", ThemeManager.AZUL_PRIMARIO, Color.WHITE);
btnEditar = crearBotonPastel("✏️ Editar", ThemeManager.AZUL_PRIMARIO, Color.WHITE);
btnEliminar = crearBotonPastel("🗑️ Eliminar", ThemeManager.PELIGRO, Color.WHITE);
btnEntradaStock = crearBotonPastel("📥 Entrada de Stock", ThemeManager.EXITO, Color.WHITE);
btnExportar = crearBotonPastel("📤 Exportar a Excel", ThemeManager.EXITO, Color.WHITE);
        panelSuperior.add(lblBuscar);
        panelSuperior.add(txtBuscar);
       
        panelSuperior.add(Box.createHorizontalStrut(10));
        panelSuperior.add(btnNuevo);
        panelSuperior.add(btnEditar);
        panelSuperior.add(btnEntradaStock);
        panelSuperior.add(btnEliminar);
       
        panelSuperior.add(btnExportar);

        // ← NUEVO: Modelo con soporte para ImageIcon
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return ImageIcon.class; // ← Columna de imagen
                if (column == 5 || column == 6) return Integer.class; // Stock y Stock Mín.
                return String.class;
            }
        };

        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setAutoCreateRowSorter(true);
        tablaProductos.setRowHeight(60); // ← Aumentado para mostrar imágenes
        tablaProductos.setShowGrid(false);
        tablaProductos.setIntercellSpacing(new Dimension(0, 0));
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaProductos.setForeground(TEXTO_OSCURO);
        tablaProductos.setSelectionBackground(HOVER_LAVANDA);
        tablaProductos.setSelectionForeground(TEXTO_OSCURO);

        JTableHeader header = tablaProductos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(ThemeManager.GRIS_MUY_CLARO);
        header.setForeground(ThemeManager.GRIS_OSCURO);
        header.setPreferredSize(new Dimension(0, 48));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // ← NUEVO: Renderer para la columna de imagen
        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaProductos.getColumnModel().getColumn(0).setMaxWidth(80);
        tablaProductos.getColumnModel().getColumn(0).setCellRenderer(new ImageCellRenderer());

        // Aplicar renderer personalizado para alertas (desde columna 1 en adelante)
        CustomTableCellRenderer renderer = new CustomTableCellRenderer();
        for (int i = 1; i < tablaProductos.getColumnCount(); i++) {
            tablaProductos.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JPanel panelTabla = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Main.isDarkMode ? new Color(60, 60, 60) : BORDE);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panelTabla.setOpaque(false);
        panelTabla.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panelTabla.add(scrollPane, BorderLayout.CENTER);

        btnNuevo.addActionListener(e -> abrirDialogoProducto(null));
        btnEditar.addActionListener(e -> editarProductoSeleccionado());
        btnEntradaStock.addActionListener(e -> abrirDialogoEntradaStock());
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
       
        btnExportar.addActionListener(e -> exportarInventario());

        tablaProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarProductoSeleccionado();
                }
            }
        });

        txtBuscar.addActionListener(e -> buscarProductos());

        add(panelSuperior, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
    }

    // ← NUEVO: Renderer para mostrar imágenes en la tabla
    private class ImageCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = new JLabel();
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(HOVER_LAVANDA);
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
            }

            if (value instanceof ImageIcon) {
                label.setIcon((ImageIcon) value);
                label.setText("");
            } else {
                label.setIcon(null);
                label.setText("📦");
                label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            }

            return label;
        }
    }

    private JButton crearBotonPastel(String texto, Color bgColor, Color fgColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    // ← MODIFICADO: Ahora carga imágenes
    private void cargarProductos() {
        modeloTabla.setRowCount(0);
        List<Producto> productos = productoService.obtenerTodos();

        for (Producto p : productos) {
            modeloTabla.addRow(new Object[]{
                    cargarImagenMiniatura(p.getImagenRuta()), // Columna 0: Imagen
                    p.getId(),                                 // Columna 1: ID
                    p.getCodigo(),                             // Columna 2: Código
                    p.getNombre(),                             // Columna 3: Nombre
                    String.format("$%.2f", p.getPrecio()),    // Columna 4: Precio
                    p.getStock(),                              // Columna 5: Stock
                    p.getStockMinimo(),                        // Columna 6: Stock Mín.
                    p.getCategoria(),                          // Columna 7: Categoría
                    p.getLote() != null ? p.getLote() : "",   // Columna 8: Lote
                    p.getFechaCaducidad() != null ? p.getFechaCaducidad() : "" // Columna 9: Caducidad
            });
        }
    }

    // ← NUEVO: Método para cargar imagen en miniatura
    private ImageIcon cargarImagenMiniatura(String ruta) {
        if (ruta == null || ruta.isEmpty()) return null;
        try {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                ImageIcon icon = new ImageIcon(archivo.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen: " + e.getMessage());
        }
        return null;
    }

    // ← MODIFICADO: Ahora carga imágenes
    private void buscarProductos() {
        String texto = txtBuscar.getText().trim();
        modeloTabla.setRowCount(0);

        if (texto.isEmpty()) {
            cargarProductos();
            return;
        }

        List<Producto> resultados = productoService.buscar(texto);
        for (Producto p : resultados) {
            modeloTabla.addRow(new Object[]{
                    cargarImagenMiniatura(p.getImagenRuta()),
                    p.getId(),
                    p.getCodigo(),
                    p.getNombre(),
                    String.format("$%.2f", p.getPrecio()),
                    p.getStock(),
                    p.getStockMinimo(),
                    p.getCategoria(),
                    p.getLote() != null ? p.getLote() : "",
                    p.getFechaCaducidad() != null ? p.getFechaCaducidad() : ""
            });
        }
    }

    private void exportarInventario() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Inventario como Excel");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos Excel", "xlsx"));
        fileChooser.setSelectedFile(new java.io.File("inventario_nekomart.xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String ruta = fileChooser.getSelectedFile().getAbsolutePath();
            if (!ruta.endsWith(".xlsx")) ruta += ".xlsx";

            List<Producto> productos = productoService.obtenerTodos();
            com.nekomart.services.ExcelService excelService = new com.nekomart.services.ExcelService();

            if (excelService.exportarInventario(productos, ruta)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Inventario exportado exitosamente a:\n" + ruta,
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Error al exportar el inventario",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarVistaPrevia(JLabel lbl, String ruta) {
        lbl.setText("");
        com.nekomart.utils.ImageLoader.cargarImagenAsync(ruta, 150, 150, lbl);
    }

    private void abrirDialogoProducto(Producto producto) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow,
                producto != null ? "Editar Producto" : "Nuevo Producto",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 450);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setResizable(false);

        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        panelContenido.setBackground(FONDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setOpaque(false);
        panelForm.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.fill = GridBagConstraints.HORIZONTAL;
        gbcForm.insets = new Insets(5, 5, 5, 5);

        JTextField txtCodigo = new JTextField(producto != null ? producto.getCodigo() : "");
        txtCodigo.putClientProperty("JTextField.placeholderText", "Ej: PROD-001");

        JTextField txtNombre = new JTextField(producto != null ? producto.getNombre() : "");
        txtNombre.putClientProperty("JTextField.placeholderText", "Ej: Crema Facial");

        JTextField txtPrecio = new JTextField(producto != null ? String.valueOf(producto.getPrecio()) : "");
        txtPrecio.putClientProperty("JTextField.placeholderText", "Ej: 15.50");

        JTextField txtStock = new JTextField(producto != null ? String.valueOf(producto.getStock()) : "");
        txtStock.putClientProperty("JTextField.placeholderText", "Ej: 50");

        JTextField txtStockMin = new JTextField(producto != null ? String.valueOf(producto.getStockMinimo()) : "5");
        txtStockMin.putClientProperty("JTextField.placeholderText", "Ej: 5");

        JTextField txtCategoria = new JTextField(producto != null ? producto.getCategoria() : "");
        txtCategoria.putClientProperty("JTextField.placeholderText", "Ej: Cuidado Facial");

        String[] etiquetas = {"Código:", "Nombre:", "Precio ($):", "Stock:", "Stock Mín.:", "Categoría:"};
        JTextField[] campos = {txtCodigo, txtNombre, txtPrecio, txtStock, txtStockMin, txtCategoria};

        for (int i = 0; i < etiquetas.length; i++) {
            gbcForm.gridx = 0;
            gbcForm.gridy = i;
            gbcForm.weightx = 0.3;
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(TEXTO_OSCURO);
            panelForm.add(lbl, gbcForm);

            gbcForm.gridx = 1;
            gbcForm.weightx = 0.7;
            panelForm.add(campos[i], gbcForm);
        }

        JPanel panelImgCol = new JPanel(new GridBagLayout());
        panelImgCol.setOpaque(false);
        panelImgCol.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbcImg = new GridBagConstraints();
        gbcImg.fill = GridBagConstraints.NONE;
        gbcImg.insets = new Insets(10, 10, 10, 10);

        JLabel lblPreview = new JLabel("", SwingConstants.CENTER);
        lblPreview.setPreferredSize(new Dimension(150, 150));
        lblPreview.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setVerticalAlignment(SwingConstants.CENTER);
        lblPreview.setOpaque(true);
        lblPreview.setBackground(FILA_ALTERNA);

        JButton btnSeleccionar = crearBotonPastel("📷 Seleccionar", LAVANDA, Color.WHITE);

        if (producto != null && producto.getImagenRuta() != null) {
            mostrarVistaPrevia(lblPreview, producto.getImagenRuta());
        } else {
            lblPreview.setText("<html><center>Sin Vista<br>Previa</center></html>");
            lblPreview.setForeground(TEXTO_GRIS);
        }

        gbcImg.gridx = 0;
        gbcImg.gridy = 0;
        panelImgCol.add(lblPreview, gbcImg);
        gbcImg.gridy = 1;
        panelImgCol.add(btnSeleccionar, gbcImg);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 1.0;
        panelContenido.add(panelForm, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        panelContenido.add(panelImgCol, gbc);

        final File[] archivoImagenSeleccionada = new File[1];

        btnSeleccionar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Seleccionar Imagen de Producto");
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes (PNG, JPG, JPEG)", "png", "jpg", "jpeg"));
            int res = fc.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                File selected = fc.getSelectedFile();
                archivoImagenSeleccionada[0] = selected;
                mostrarVistaPrevia(lblPreview, selected.getAbsolutePath());
            }
        });

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        panelInferior.setBackground(FONDO);

        JLabel lblError = new JLabel("", SwingConstants.LEFT);
        lblError.setForeground(STOCK_BAJO_TEXT);
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelAcciones.setOpaque(false);

        JButton btnCancelar = crearBotonPastel("Cancelar", FILA_ALTERNA, TEXTO_OSCURO);
        JButton btnGuardar = crearBotonPastel("Guardar", CORAL, Color.WHITE);

        panelAcciones.add(btnCancelar);
        panelAcciones.add(btnGuardar);

        panelInferior.add(lblError, BorderLayout.WEST);
        panelInferior.add(panelAcciones, BorderLayout.EAST);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnGuardar.addActionListener(e -> {
            txtCodigo.putClientProperty("JComponent.outline", null);
            txtNombre.putClientProperty("JComponent.outline", null);
            txtPrecio.putClientProperty("JComponent.outline", null);
            txtStock.putClientProperty("JComponent.outline", null);
            txtStockMin.putClientProperty("JComponent.outline", null);
            txtCategoria.putClientProperty("JComponent.outline", null);
            lblError.setText("");

            String codigo = txtCodigo.getText().trim();
            String nombre = txtNombre.getText().trim();
            String precioStr = txtPrecio.getText().trim();
            String stockStr = txtStock.getText().trim();
            String stockMinStr = txtStockMin.getText().trim();
            String categoria = txtCategoria.getText().trim();

            boolean valid = true;

            if (codigo.isEmpty()) {
                txtCodigo.putClientProperty("JComponent.outline", "error");
                valid = false;
            }
            if (nombre.isEmpty()) {
                txtNombre.putClientProperty("JComponent.outline", "error");
                valid = false;
            }

            double precio = 0;
            try {
                precio = Double.parseDouble(precioStr);
                if (precio < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                txtPrecio.putClientProperty("JComponent.outline", "error");
                valid = false;
            }

            int stock = 0;
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                txtStock.putClientProperty("JComponent.outline", "error");
                valid = false;
            }

            int stockMin = 0;
            try {
                stockMin = Integer.parseInt(stockMinStr);
                if (stockMin < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                txtStockMin.putClientProperty("JComponent.outline", "error");
                valid = false;
            }

            if (categoria.isEmpty()) {
                txtCategoria.putClientProperty("JComponent.outline", "error");
                valid = false;
            }

            if (!valid) {
                lblError.setText("Por favor, corrige los campos resaltados en rojo.");
                return;
            }

            String finalRutaImagen = producto != null ? producto.getImagenRuta() : null;

            if (archivoImagenSeleccionada[0] != null) {
                try {
                    File folderDestino = new File("imagenes/productos");
                    if (!folderDestino.exists()) {
                        folderDestino.mkdirs();
                    }
                    String origName = archivoImagenSeleccionada[0].getName();
                    String nuevoNombre = System.currentTimeMillis() + "_" + origName;
                    File archivoDestino = new File(folderDestino, nuevoNombre);
                    Files.copy(archivoImagenSeleccionada[0].toPath(), archivoDestino.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    finalRutaImagen = "imagenes/productos/" + nuevoNombre;
                } catch (Exception ex) {
                    System.err.println("Error al copiar imagen del producto: " + ex.getMessage());
                    lblError.setText("No se pudo copiar la imagen del producto.");
                    return;
                }
            }

            Producto p = new Producto();
            if (producto != null) {
                p.setId(producto.getId());
            }
            p.setCodigo(codigo);
            p.setNombre(nombre);
            p.setPrecio(precio);
            p.setStock(stock);
            p.setStockMinimo(stockMin);
            p.setCategoria(categoria);
            p.setImagenRuta(finalRutaImagen);

            if (productoService.guardarProducto(p)) {
                JOptionPane.showMessageDialog(dialog, "Producto guardado exitosamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                cargarProductos();
            } else {
                lblError.setText("Error al guardar el producto en el sistema.");
            }
        });

        dialog.setLayout(new BorderLayout());
        dialog.add(panelContenido, BorderLayout.CENTER);
        dialog.add(panelInferior, BorderLayout.SOUTH);
        dialog.getContentPane().setBackground(FONDO);

        dialog.setVisible(true);
    }

    private void editarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila >= 0) {
            int filaModelo = tablaProductos.convertRowIndexToModel(fila);
            // ← MODIFICADO: ID ahora está en columna 1
            int id = (int) modeloTabla.getValueAt(filaModelo, 1);
            Producto producto = productoService.obtenerTodos().stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
            if (producto != null) {
                abrirDialogoProducto(producto);
            }
        }
    }

    private void eliminarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para eliminar",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(fila);
        // ← MODIFICADO: ID en columna 1, Nombre en columna 3
        int id = (int) modeloTabla.getValueAt(filaModelo, 1);
        String nombre = (String) modeloTabla.getValueAt(filaModelo, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar el producto '" + nombre + "'?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (productoService.eliminarProducto(id)) {
                JOptionPane.showMessageDialog(this, "Producto eliminado",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el producto",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirDialogoEntradaStock() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para registrar entrada",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(fila);
        // ← MODIFICADO: ID en columna 1, Nombre en columna 3
        int productoId = (int) modeloTabla.getValueAt(filaModelo, 1);
        String nombreProducto = (String) modeloTabla.getValueAt(filaModelo, 3);

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow,
                "📥 Entrada de Stock — " + nombreProducto,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setResizable(false);

        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        panelContenido.setBackground(FONDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        JTextField txtCantidad = new JTextField();
        txtCantidad.putClientProperty("JTextField.placeholderText", "Ej: 50");

        JTextField txtLote = new JTextField();
        txtLote.putClientProperty("JTextField.placeholderText", "Ej: LOTE-2026-06");

        JTextField txtFechaCad = new JTextField();
        txtFechaCad.putClientProperty("JTextField.placeholderText", "YYYY-MM-DD (Ej: 2027-01-15)");

        String[] etiquetas = {"Cantidad a ingresar:", "Lote:", "Fecha de Caducidad:"};
        JTextField[] campos = {txtCantidad, txtLote, txtFechaCad};

        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.35;
            JLabel lbl = new JLabel(etiquetas[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(TEXTO_OSCURO);
            panelContenido.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.65;
            panelContenido.add(campos[i], gbc);
        }

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotones.setBackground(FONDO);

        JButton btnCancelar = crearBotonPastel("Cancelar", FILA_ALTERNA, TEXTO_OSCURO);
        JButton btnGuardar = crearBotonPastel("Guardar Entrada", MENTA, TEXTO_OSCURO);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnGuardar.addActionListener(e -> {
            String cantidadStr = txtCantidad.getText().trim();
            String lote = txtLote.getText().trim();
            String fechaCad = txtFechaCad.getText().trim();

            if (cantidadStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "La cantidad es obligatoria",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int cantidad;
            try {
                cantidad = Integer.parseInt(cantidadStr);
                if (cantidad <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Ingresa una cantidad válida (número positivo)",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (productoDAO.sumarStock(productoId, cantidad)) {
                Producto prod = productoDAO.buscarPorId(productoId);
                if (prod != null) {
                    if (!lote.isEmpty()) prod.setLote(lote);
                    if (!fechaCad.isEmpty()) prod.setFechaCaducidad(fechaCad);
                    productoDAO.actualizar(prod);
                }

                Movimiento mov = new Movimiento();
                mov.setProductoId(productoId);
                mov.setTipo("ENTRADA");
                mov.setCantidad(cantidad);
                mov.setFecha(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        + " " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

                int usuarioId = 0;
                if (SessionManager.getInstancia().getUsuarioActual() != null) {
                    usuarioId = SessionManager.getInstancia().getUsuarioActual().getId();
                }
                mov.setUsuarioId(usuarioId);

                movimientoDAO.registrarMovimiento(mov);

                JOptionPane.showMessageDialog(dialog,
                        "✅ Entrada registrada: +" + cantidad + " unidades",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                cargarProductos();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "❌ Error al registrar la entrada de stock",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        dialog.setLayout(new BorderLayout());
        dialog.add(panelContenido, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);
        dialog.getContentPane().setBackground(FONDO);
        dialog.setVisible(true);
    }

    private void verKardexProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para ver su Kardex",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(fila);
        // ← MODIFICADO: ID en columna 1, Nombre en columna 3
        int productoId = (int) modeloTabla.getValueAt(filaModelo, 1);
        String nombreProducto = (String) modeloTabla.getValueAt(filaModelo, 3);

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        KardexFrame kardex = new KardexFrame(parentWindow, productoId, nombreProducto);
        kardex.setVisible(true);
    }

    private boolean esCaducidadProxima(String fechaCaducidad) {
        if (fechaCaducidad == null || fechaCaducidad.isEmpty()) return false;
        try {
            LocalDate fecha = LocalDate.parse(fechaCaducidad, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate hoy = LocalDate.now();
            long diasRestantes = ChronoUnit.DAYS.between(hoy, fecha);
            return diasRestantes <= 30;
        } catch (Exception e) {
            return false;
        }
    }

    // ← MODIFICADO: Índices de columnas ajustados
    private class CustomTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int modelRow = table.convertRowIndexToModel(row);

            // ← MODIFICADO: Stock ahora es columna 5, Stock Mín. es columna 6
            Object stockObj = table.getModel().getValueAt(modelRow, 5);
            Object stockMinObj = table.getModel().getValueAt(modelRow, 6);

            int stock = 0;
            int stockMin = 0;
            try {
                stock = Integer.parseInt(stockObj.toString());
                stockMin = Integer.parseInt(stockMinObj.toString());
            } catch (Exception e) {}

            boolean esStockBajo = stock <= stockMin;

            // ← MODIFICADO: Caducidad ahora es columna 9
            Object caducidadObj = table.getModel().getValueAt(modelRow, 9);
            String fechaCad = caducidadObj != null ? caducidadObj.toString() : "";
            boolean esCaducProxima = esCaducidadProxima(fechaCad);

            if (isSelected) {
                c.setBackground(Main.isDarkMode ? new Color(70, 60, 100) : HOVER_LAVANDA);
                c.setForeground(Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO);
            } else if (esStockBajo) {
                c.setBackground(Main.isDarkMode ? new Color(0x5C, 0x2D, 0x2D) : STOCK_BAJO_BG);
                c.setForeground(Main.isDarkMode ? new Color(0xFE, 0xB2, 0xB2) : STOCK_BAJO_TEXT);

                if (column == 2) { // ← Código (antes 1, ahora 2)
                    setText("⚠️ " + (value != null ? value.toString() : ""));
                }

                if (column == 1) { // ← ID (antes 0, ahora 1)
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, STOCK_BAJO_BORDE),
                            BorderFactory.createEmptyBorder(0, 8, 0, 8)
                    ));
                } else {
                    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                }
            } else if (esCaducProxima) {
                c.setBackground(Main.isDarkMode ? new Color(0x5C, 0x3C, 0x15) : CADUCIDAD_BG);
                c.setForeground(Main.isDarkMode ? new Color(0xFE, 0xEB, 0xC8) : CADUCIDAD_TEXT);

                if (column == 9) { // ← Caducidad (antes 8, ahora 9)
                    setText("⏰ " + (value != null ? value.toString() : ""));
                }

                if (column == 1) { // ← ID (antes 0, ahora 1)
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(255, 183, 77)),
                            BorderFactory.createEmptyBorder(0, 8, 0, 8)
                    ));
                } else {
                    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                }
            } else {
                c.setBackground(row % 2 == 0 ? (Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE) : (Main.isDarkMode ? new Color(0x24, 0x24, 0x24) : FILA_ALTERNA));
                c.setForeground(Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            }

            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            return c;
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        reaplicarTemaInventario();
    }

    public void reaplicarTemaInventario() {
        Color fondo = Main.isDarkMode ? new Color(0x1E, 0x1E, 0x1E) : FONDO;
        Color card = Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE;
        Color textClaro = Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO;
        Color selectionBg = Main.isDarkMode ? new Color(70, 60, 100) : HOVER_LAVANDA;
        Color border = Main.isDarkMode ? new Color(60, 60, 60) : BORDE;

        setBackground(fondo);
        if (tablaProductos != null) {
            tablaProductos.setForeground(textClaro);
            tablaProductos.setSelectionBackground(selectionBg);
            tablaProductos.setSelectionForeground(Main.isDarkMode ? Color.WHITE : ThemeManager.GRIS_OSCURO);

            JTableHeader header = tablaProductos.getTableHeader();
            if (header != null) {
                header.setBackground(Main.isDarkMode ? card : ThemeManager.GRIS_MUY_CLARO);
                header.setForeground(Main.isDarkMode ? Color.WHITE : ThemeManager.GRIS_OSCURO);
            }
        }
        if (txtBuscar != null) {
            txtBuscar.setBackground(Main.isDarkMode ? new Color(0x3D, 0x3D, 0x3D) : Color.WHITE);
            txtBuscar.setForeground(textClaro);
            txtBuscar.setCaretColor(textClaro);
            txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(border, 1, true),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
        }

        actualizarComponentesHijos(this, fondo, card, textClaro, border);
    }

    private void actualizarComponentesHijos(Component comp, Color fondo, Color card, Color textClaro, Color border) {
        if (comp instanceof JScrollPane) {
            ((JScrollPane) comp).getViewport().setBackground(card);
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                actualizarComponentesHijos(child, fondo, card, textClaro, border);
            }
        }
    }
}