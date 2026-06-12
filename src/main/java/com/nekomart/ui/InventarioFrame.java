package com.nekomart.ui;

import com.nekomart.Main;
import com.nekomart.dao.MovimientoDAO;
import com.nekomart.dao.ProductoDAO;
import com.nekomart.models.Movimiento;
import com.nekomart.models.Producto;
import com.nekomart.services.ProductoService;
import com.nekomart.utils.SessionManager;

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

/**
 * Panel de gestión de inventario con diseño pastel moderno.
 * Tabla con header lavanda, filas alternadas, hover sutil y alertas
 * visuales para productos con stock bajo (fondo rojo claro, borde izquierdo rojo).
 * Todo el código está comentado en español.
 */
public class InventarioFrame extends JPanel {

    // ── Colores de la paleta pastel ──────────────────────────────────────
    private static final Color LAVANDA = new Color(184, 169, 232);
    private static final Color LAVANDA_CLARO = new Color(212, 196, 240);
    private static final Color CORAL = new Color(255, 139, 148);
    private static final Color CORAL_HOVER = new Color(255, 107, 116);
    private static final Color FONDO = new Color(250, 250, 250);
    private static final Color TEXTO_OSCURO = new Color(45, 55, 72);
    private static final Color TEXTO_GRIS = new Color(113, 128, 150);
    private static final Color BORDE = new Color(226, 232, 240);
    private static final Color FILA_ALTERNA = new Color(247, 250, 252);
    private static final Color HOVER_LAVANDA = new Color(240, 235, 255);
    private static final Color STOCK_BAJO_BG = new Color(254, 215, 215);
    private static final Color STOCK_BAJO_TEXT = new Color(197, 48, 48);
    private static final Color STOCK_BAJO_BORDE = new Color(252, 129, 129);
    private static final Color CADUCIDAD_BG = new Color(255, 237, 213);
    private static final Color CADUCIDAD_TEXT = new Color(146, 100, 22);
    private static final Color MENTA = new Color(168, 230, 207);

    // ── Servicio, DAO y componentes de la tabla ──────────────────────────
    private ProductoService productoService;
    private MovimientoDAO movimientoDAO;
    private ProductoDAO productoDAO;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;

    // Columnas de la tabla (incluye Lote y Caducidad)
    private final String[] COLUMNAS = {"ID", "Código", "Nombre", "Precio", "Stock", "Stock Mín.", "Categoría", "Lote", "Caducidad"};

    public InventarioFrame() {
        productoService = new ProductoService();
        movimientoDAO = new MovimientoDAO();
        productoDAO = new ProductoDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(FONDO);

        initComponents();
        cargarProductos();
    }

    /**
     * Inicializa los componentes visuales del panel de inventario.
     */
    private void initComponents() {
        // ══════════════════════════════════════════════════════════════════
        // PANEL SUPERIOR — Búsqueda y botones de acción
        // ══════════════════════════════════════════════════════════════════
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelSuperior.setOpaque(false);

        // Campo de búsqueda estilizado
        JLabel lblBuscar = new JLabel("🔍");
        lblBuscar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        txtBuscar = new JTextField(22);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por nombre o código...");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Botón buscar
        JButton btnBuscar = crearBotonPastel("Buscar", LAVANDA, Color.WHITE);
        btnBuscar.setToolTipText("Buscar productos por código o nombre");

        // Botón nuevo producto
        JButton btnNuevo = crearBotonPastel("➕ Nuevo", CORAL, Color.WHITE);
        btnNuevo.setToolTipText("Registrar un nuevo producto en el inventario");

        // Botón editar producto seleccionado
        JButton btnEditar = crearBotonPastel("✏️ Editar", LAVANDA, Color.WHITE);
        btnEditar.setToolTipText("Editar el producto seleccionado");

        // Botón entrada de stock (nueva mercancía)
        JButton btnEntradaStock = crearBotonPastel("📥 Entrada de Stock", MENTA, TEXTO_OSCURO);
        btnEntradaStock.setToolTipText("Registrar entrada de mercancía al inventario");

        // Botón eliminar
        JButton btnEliminar = crearBotonPastel("🗑️ Eliminar", new Color(252, 129, 129), Color.WHITE);
        btnEliminar.setToolTipText("Eliminar el producto seleccionado");

        // Botón ver Kardex
        JButton btnKardex = crearBotonPastel("📋 Kardex", new Color(255, 183, 77), TEXTO_OSCURO);
        btnKardex.setToolTipText("Ver historial de movimientos del producto seleccionado");

        // Botón refrescar
        JButton btnRefrescar = crearBotonPastel("🔄 Refrescar", LAVANDA_CLARO, TEXTO_OSCURO);
        btnRefrescar.setToolTipText("Recargar la lista de productos");

        // Botón exportar a Excel
        JButton btnExportar = crearBotonPastel("📤 Exportar a Excel", new Color(168, 230, 207), TEXTO_OSCURO);
        btnExportar.setToolTipText("Exportar inventario a archivo Excel (.xlsx)");

        panelSuperior.add(lblBuscar);
        panelSuperior.add(txtBuscar);
        panelSuperior.add(btnBuscar);
        panelSuperior.add(Box.createHorizontalStrut(10));
        panelSuperior.add(btnNuevo);
        panelSuperior.add(btnEditar);
        panelSuperior.add(btnEntradaStock);
        panelSuperior.add(btnEliminar);
        panelSuperior.add(btnKardex);
        panelSuperior.add(btnRefrescar);
        panelSuperior.add(btnExportar);

        // ══════════════════════════════════════════════════════════════════
        // TABLA DE PRODUCTOS — Con renderer personalizado para stock bajo
        // ══════════════════════════════════════════════════════════════════
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // La tabla no es editable directamente
            }
        };

        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setAutoCreateRowSorter(true);
        tablaProductos.setRowHeight(45);
        tablaProductos.setShowGrid(false);
        tablaProductos.setIntercellSpacing(new Dimension(0, 0));
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaProductos.setForeground(TEXTO_OSCURO);
        tablaProductos.setSelectionBackground(HOVER_LAVANDA);
        tablaProductos.setSelectionForeground(TEXTO_OSCURO);

        // Estilo del header de la tabla (fondo lavanda, texto blanco)
        JTableHeader header = tablaProductos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(LAVANDA);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // Aplicar renderer personalizado para alertas de stock bajo
        CustomTableCellRenderer renderer = new CustomTableCellRenderer();
        for (int i = 0; i < tablaProductos.getColumnCount(); i++) {
            tablaProductos.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Panel contenedor con bordes redondeados para la tabla
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

        // ══════════════════════════════════════════════════════════════════
        // EVENTOS
        // ══════════════════════════════════════════════════════════════════
        btnBuscar.addActionListener(e -> buscarProductos());
        btnNuevo.addActionListener(e -> abrirDialogoProducto(null));
        btnEditar.addActionListener(e -> editarProductoSeleccionado());
        btnEntradaStock.addActionListener(e -> abrirDialogoEntradaStock());
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
        btnKardex.addActionListener(e -> verKardexProducto());
        btnRefrescar.addActionListener(e -> cargarProductos());
        btnExportar.addActionListener(e -> exportarInventario());

        // Doble clic para editar un producto
        tablaProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarProductoSeleccionado();
                }
            }
        });

        // Enter en el campo de búsqueda para buscar
        txtBuscar.addActionListener(e -> buscarProductos());

        // ── Organizar layout ─────────────────────────────────────────────
        add(panelSuperior, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
    }

    /**
     * Crea un botón estilizado con colores pastel y bordes redondeados.
     *
     * @param texto  Texto del botón
     * @param bgColor Color de fondo
     * @param fgColor Color del texto
     * @return Botón estilizado
     */
    private JButton crearBotonPastel(String texto, Color bgColor, Color fgColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    /**
     * Carga todos los productos en la tabla desde el servicio.
     */
    private void cargarProductos() {
        modeloTabla.setRowCount(0);
        List<Producto> productos = productoService.obtenerTodos();

        for (Producto p : productos) {
            modeloTabla.addRow(new Object[]{
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

    /**
     * Busca productos según el texto ingresado en el campo de búsqueda.
     */
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

    /**
     * Exporta el inventario actual a un archivo Excel (.xlsx).
     * Abre un JFileChooser para que el usuario elija dónde guardar el archivo.
     * Utiliza ExcelService para generar el Excel con formato profesional.
     */
    private void exportarInventario() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Inventario como Excel");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos Excel", "xlsx"));
        fileChooser.setSelectedFile(new java.io.File("inventario_nekomart.xlsx"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String ruta = fileChooser.getSelectedFile().getAbsolutePath();
            // Asegurar extensión .xlsx
            if (!ruta.endsWith(".xlsx")) ruta += ".xlsx";

            // Obtener todos los productos activos
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

    /**
     * Muestra de forma segura una imagen previsualizada en un JLabel.
     *
     * @param lbl  JLabel donde mostrar la imagen
     * @param ruta Ruta de la imagen
     */
    private void mostrarVistaPrevia(JLabel lbl, String ruta) {
        if (ruta != null && !ruta.isEmpty()) {
            File f = new File(ruta);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lbl.setIcon(new ImageIcon(scaled));
                lbl.setText("");
                return;
            }
        }
        lbl.setIcon(null);
        lbl.setText("<html><center>Sin Vista<br>Previa</center></html>");
    }

    /**
     * Abre un diálogo modal estilizado para crear o editar un producto.
     * Diseño de doble columna: datos a la izquierda, imagen a la derecha.
     *
     * @param producto Producto a editar, o null para crear uno nuevo
     */
    private void abrirDialogoProducto(Producto producto) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow,
                producto != null ? "Editar Producto" : "Nuevo Producto",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 450);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setResizable(false);

        // ── Panel de contenido principal (doble columna) ─────────────────
        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        panelContenido.setBackground(FONDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // ── COLUMNA IZQUIERDA: Formulario de datos ───────────────────────
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

        // Agregar campos al formulario
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

        // ── COLUMNA DERECHA: Imagen del producto ─────────────────────────
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

        // Cargar vista previa si el producto tiene imagen
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

        // Agregar columnas al panel de contenido
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 1.0;
        panelContenido.add(panelForm, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        panelContenido.add(panelImgCol, gbc);

        // Almacenar archivo de imagen seleccionada
        final File[] archivoImagenSeleccionada = new File[1];

        // Evento de seleccionar imagen
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

        // ── PANEL INFERIOR: Botones de acción y errores ──────────────────
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

        // Evento cancelar
        btnCancelar.addActionListener(e -> dialog.dispose());

        // Evento guardar
        btnGuardar.addActionListener(e -> {
            // Limpiar indicadores de error
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

            // Copiar imagen si se seleccionó una nueva
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

            // Guardar en base de datos
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

        // Integrar paneles al JDialog
        dialog.setLayout(new BorderLayout());
        dialog.add(panelContenido, BorderLayout.CENTER);
        dialog.add(panelInferior, BorderLayout.SOUTH);
        dialog.getContentPane().setBackground(FONDO);

        dialog.setVisible(true);
    }

    /**
     * Edita el producto seleccionado en la tabla.
     */
    private void editarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila >= 0) {
            int filaModelo = tablaProductos.convertRowIndexToModel(fila);
            int id = (int) modeloTabla.getValueAt(filaModelo, 0);
            Producto producto = productoService.obtenerTodos().stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
            if (producto != null) {
                abrirDialogoProducto(producto);
            }
        }
    }

    /**
     * Elimina el producto seleccionado de la base de datos.
     */
    private void eliminarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para eliminar",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(fila);
        int id = (int) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = (String) modeloTabla.getValueAt(filaModelo, 2);

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

    /**
     * Abre un diálogo para registrar una entrada de stock (mercancía nueva).
     * Pide: Cantidad, Lote y Fecha de Caducidad.
     * Al guardar, suma la cantidad al stock y registra el movimiento en el Kardex.
     */
    private void abrirDialogoEntradaStock() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para registrar entrada",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(fila);
        int productoId = (int) modeloTabla.getValueAt(filaModelo, 0);
        String nombreProducto = (String) modeloTabla.getValueAt(filaModelo, 2);

        // Crear diálogo de entrada de stock
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

        // Campo: Cantidad a ingresar
        JTextField txtCantidad = new JTextField();
        txtCantidad.putClientProperty("JTextField.placeholderText", "Ej: 50");

        // Campo: Lote
        JTextField txtLote = new JTextField();
        txtLote.putClientProperty("JTextField.placeholderText", "Ej: LOTE-2026-06");

        // Campo: Fecha de Caducidad
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

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotones.setBackground(FONDO);

        JButton btnCancelar = crearBotonPastel("Cancelar", FILA_ALTERNA, TEXTO_OSCURO);
        JButton btnGuardar = crearBotonPastel("Guardar Entrada", MENTA, TEXTO_OSCURO);

        btnCancelar.addActionListener(e -> dialog.dispose());
        btnGuardar.addActionListener(e -> {
            // Validar cantidad
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

            // Sumar stock al producto
            if (productoDAO.sumarStock(productoId, cantidad)) {
                // Actualizar lote y fecha de caducidad del producto si se proporcionaron
                Producto prod = productoDAO.buscarPorId(productoId);
                if (prod != null) {
                    if (!lote.isEmpty()) prod.setLote(lote);
                    if (!fechaCad.isEmpty()) prod.setFechaCaducidad(fechaCad);
                    productoDAO.actualizar(prod);
                }

                // Registrar movimiento en el Kardex
                Movimiento mov = new Movimiento();
                mov.setProductoId(productoId);
                mov.setTipo("ENTRADA");
                mov.setCantidad(cantidad);
                mov.setFecha(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        + " " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

                // Obtener ID del usuario actual de la sesión
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

    /**
     * Abre el diálogo Kardex para ver el historial de movimientos del producto seleccionado.
     */
    private void verKardexProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para ver su Kardex",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(fila);
        int productoId = (int) modeloTabla.getValueAt(filaModelo, 0);
        String nombreProducto = (String) modeloTabla.getValueAt(filaModelo, 2);

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        KardexFrame kardex = new KardexFrame(parentWindow, productoId, nombreProducto);
        kardex.setVisible(true);
    }

    /**
     * Verifica si una fecha de caducidad está próxima a vencer (dentro de 30 días).
     *
     * @param fechaCaducidad Fecha en formato YYYY-MM-DD
     * @return true si la fecha está a 30 días o menos de la fecha actual
     */
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

    // ══════════════════════════════════════════════════════════════════════
    // RENDERER PERSONALIZADO PARA ALERTAS DE STOCK BAJO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Renderer de celda personalizado que:
     * - Pinta filas con stock bajo en fondo rojo claro (#FED7D7)
     * - Pinta filas con caducidad próxima (≤30 días) en fondo naranja (#FFEDD5)
     * - Agrega icono ⚠️ y texto rojo oscuro para stock bajo
     * - Alterna colores blanco / #F7FAFC para filas normales
     * - Aplica borde izquierdo rojo de 4px en filas con alerta
     */
    private class CustomTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Obtener índice del modelo real (puede diferir si se ordena la tabla)
            int modelRow = table.convertRowIndexToModel(row);

            // Leer stock y stock mínimo del modelo de datos
            Object stockObj = table.getModel().getValueAt(modelRow, 4);
            Object stockMinObj = table.getModel().getValueAt(modelRow, 5);

            int stock = 0;
            int stockMin = 0;
            try {
                stock = Integer.parseInt(stockObj.toString());
                stockMin = Integer.parseInt(stockMinObj.toString());
            } catch (Exception e) {
                // Ignorar errores de parseo
            }

            boolean esStockBajo = stock <= stockMin;

            // Verificar caducidad próxima (columna 8 = Caducidad)
            Object caducidadObj = table.getModel().getValueAt(modelRow, 8);
            String fechaCad = caducidadObj != null ? caducidadObj.toString() : "";
            boolean esCaducProxima = esCaducidadProxima(fechaCad);

            if (isSelected) {
                c.setBackground(Main.isDarkMode ? new Color(70, 60, 100) : HOVER_LAVANDA);
                c.setForeground(Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO);
            } else if (esStockBajo) {
                c.setBackground(Main.isDarkMode ? new Color(0x5C, 0x2D, 0x2D) : STOCK_BAJO_BG);
                c.setForeground(Main.isDarkMode ? new Color(0xFE, 0xB2, 0xB2) : STOCK_BAJO_TEXT);

                if (column == 1) {
                    setText("⚠️ " + (value != null ? value.toString() : ""));
                }

                if (column == 0) {
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

                if (column == 8) {
                    setText("⏰ " + (value != null ? value.toString() : ""));
                }

                if (column == 0) {
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
            tablaProductos.setSelectionForeground(textClaro);
            
            JTableHeader header = tablaProductos.getTableHeader();
            if (header != null) {
                header.setBackground(Main.isDarkMode ? card : LAVANDA);
                header.setForeground(Color.WHITE);
            }
        }
        if (txtBuscar != null) {
            txtBuscar.setBackground(Main.isDarkMode ? new Color(0x3D, 0x3D, 0x3D) : Color.WHITE);
            txtBuscar.setForeground(textClaro);
            txtBuscar.setCaretColor(textClaro);
            txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(border, 1, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
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