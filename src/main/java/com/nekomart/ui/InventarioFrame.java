package com.nekomart.ui;

import com.nekomart.models.Producto;
import com.nekomart.services.ProductoService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Panel de gestión de inventario con tabla de productos y operaciones CRUD.
 * Se integra dentro del JTabbedPane del MainFrame.
 * Todo el código está comentado en español.
 */
public class InventarioFrame extends JPanel {

    private ProductoService productoService;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;

    // Columnas de la tabla
    private final String[] COLUMNAS = { "ID", "Código", "Nombre", "Precio", "Stock", "Stock Mín.", "Categoría" };

    public InventarioFrame() {
        productoService = new ProductoService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        cargarProductos();
    }

    /**
     * Inicializa los componentes visuales.
     */
    private void initComponents() {
        // Panel superior con búsqueda y botones
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JLabel lblBuscar = new JLabel("Buscar:");
        txtBuscar = new JTextField(20);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Nombre o código...");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);

        JButton btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.setToolTipText("Buscar productos por código o nombre");
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnNuevo = new JButton("➕ Nuevo");
        btnNuevo.setToolTipText("Registrar un nuevo producto en el inventario");
        btnNuevo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNuevo.putClientProperty("JButton.buttonType", "default");

        JButton btnEliminar = new JButton("🗑️ Eliminar");
        btnEliminar.setToolTipText("Eliminar el producto seleccionado de la base de datos");
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.setToolTipText("Refrescar y recargar la lista de productos");
        btnRefrescar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelSuperior.add(lblBuscar);
        panelSuperior.add(txtBuscar);
        panelSuperior.add(btnBuscar);
        panelSuperior.add(Box.createHorizontalStrut(20));
        panelSuperior.add(btnNuevo);
        panelSuperior.add(btnEliminar);
        panelSuperior.add(btnRefrescar);

        // Tabla de productos
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }
        };

        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setAutoCreateRowSorter(true);
        tablaProductos.setRowHeight(28);

        // Estilo moderno de tabla alternada en FlatLaf
        tablaProductos.putClientProperty("JTable.alternateRowColor", true);
        tablaProductos.putClientProperty("JTable.showHorizontalLines", true);
        tablaProductos.putClientProperty("JTable.showVerticalLines", false);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);

        // Eventos de botones
        btnBuscar.addActionListener(e -> buscarProductos());
        btnNuevo.addActionListener(e -> abrirDialogoProducto(null));
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
        btnRefrescar.addActionListener(e -> cargarProductos());

        // Doble clic para editar
        tablaProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarProductoSeleccionado();
                }
            }
        });

        // Enter en búsqueda
        txtBuscar.addActionListener(e -> buscarProductos());

        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Carga todos los productos en la tabla.
     */
    private void cargarProductos() {
        modeloTabla.setRowCount(0);
        List<Producto> productos = productoService.obtenerTodos();

        for (Producto p : productos) {
            modeloTabla.addRow(new Object[] {
                    p.getId(),
                    p.getCodigo(),
                    p.getNombre(),
                    String.format("$%.2f", p.getPrecio()),
                    p.getStock(),
                    p.getStockMinimo(),
                    p.getCategoria()
            });
        }
    }

    /**
     * Busca productos según el texto ingresado.
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
            modeloTabla.addRow(new Object[] {
                    p.getId(),
                    p.getCodigo(),
                    p.getNombre(),
                    String.format("$%.2f", p.getPrecio()),
                    p.getStock(),
                    p.getStockMinimo(),
                    p.getCategoria()
            });
        }
    }

    /**
     * Auxiliar para mostrar de forma segura una imagen previsualizada en un JLabel.
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
     * Abre un diálogo modal de doble columna para crear o editar un producto con imágenes.
     */
    private void abrirDialogoProducto(Producto producto) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, producto != null ? "Editar Producto" : "Nuevo Producto", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 420);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setResizable(false);

        // Panel de Contenido Principal (Divide datos a la izquierda e imagen a la derecha)
        JPanel panelContenido = new JPanel(new GridBagLayout());
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // --- COLUMNA IZQUIERDA: Formulario de Datos ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.fill = GridBagConstraints.HORIZONTAL;
        gbcForm.insets = new Insets(6, 6, 6, 6);

        JTextField txtCodigo = new JTextField(producto != null ? producto.getCodigo() : "");
        txtCodigo.putClientProperty("JTextField.placeholderText", "Ej: PROD-001");
        
        JTextField txtNombre = new JTextField(producto != null ? producto.getNombre() : "");
        txtNombre.putClientProperty("JTextField.placeholderText", "Ej: Croquetas Gato");
        
        JTextField txtPrecio = new JTextField(producto != null ? String.valueOf(producto.getPrecio()) : "");
        txtPrecio.putClientProperty("JTextField.placeholderText", "Ej: 15.50");
        
        JTextField txtStock = new JTextField(producto != null ? String.valueOf(producto.getStock()) : "");
        txtStock.putClientProperty("JTextField.placeholderText", "Ej: 50");
        
        JTextField txtStockMin = new JTextField(producto != null ? String.valueOf(producto.getStockMinimo()) : "5");
        txtStockMin.putClientProperty("JTextField.placeholderText", "Ej: 5");
        
        JTextField txtCategoria = new JTextField(producto != null ? producto.getCategoria() : "");
        txtCategoria.putClientProperty("JTextField.placeholderText", "Ej: Alimentos");

        // Añadir elementos al panel del formulario
        gbcForm.gridx = 0; gbcForm.gridy = 0; gbcForm.weightx = 0.3;
        panelForm.add(new JLabel("Código:"), gbcForm);
        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        panelForm.add(txtCodigo, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 1; gbcForm.weightx = 0.3;
        panelForm.add(new JLabel("Nombre:"), gbcForm);
        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        panelForm.add(txtNombre, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 2; gbcForm.weightx = 0.3;
        panelForm.add(new JLabel("Precio ($):"), gbcForm);
        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        panelForm.add(txtPrecio, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 3; gbcForm.weightx = 0.3;
        panelForm.add(new JLabel("Stock:"), gbcForm);
        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        panelForm.add(txtStock, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 4; gbcForm.weightx = 0.3;
        panelForm.add(new JLabel("Stock Mín.:"), gbcForm);
        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        panelForm.add(txtStockMin, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 5; gbcForm.weightx = 0.3;
        panelForm.add(new JLabel("Categoría:"), gbcForm);
        gbcForm.gridx = 1; gbcForm.weightx = 0.7;
        panelForm.add(txtCategoria, gbcForm);

        // --- COLUMNA DERECHA: Imagen ---
        JPanel panelImgCol = new JPanel(new GridBagLayout());
        panelImgCol.setBorder(BorderFactory.createTitledBorder("Imagen de Producto"));
        GridBagConstraints gbcImg = new GridBagConstraints();
        gbcImg.fill = GridBagConstraints.NONE;
        gbcImg.insets = new Insets(10, 10, 10, 10);

        JLabel lblPreview = new JLabel("", SwingConstants.CENTER);
        lblPreview.setPreferredSize(new Dimension(150, 150));
        lblPreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setVerticalAlignment(SwingConstants.CENTER);

        JButton btnSeleccionar = new JButton("📷 Seleccionar Imagen");
        btnSeleccionar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Cargar vista previa inicial
        if (producto != null && producto.getImagenRuta() != null) {
            mostrarVistaPrevia(lblPreview, producto.getImagenRuta());
        } else {
            lblPreview.setText("<html><center>Sin Vista<br>Previa</center></html>");
        }

        // Estructura interna de imagen
        gbcImg.gridx = 0; gbcImg.gridy = 0;
        panelImgCol.add(lblPreview, gbcImg);
        gbcImg.gridy = 1;
        panelImgCol.add(btnSeleccionar, gbcImg);

        // Agregar formulario e imagen al contenedor de contenido
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.65; gbc.weighty = 1.0;
        panelContenido.add(panelForm, gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        panelContenido.add(panelImgCol, gbc);

        // Almacenar el archivo seleccionado usando un array para acceso en listener
        final File[] archivoImagenSeleccionada = new File[1];

        btnSeleccionar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Seleccionar Imagen de Producto");
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes (PNG, JPG, JPEG)", "png", "jpg", "jpeg"));
            int res = fc.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                File selected = fc.getSelectedFile();
                archivoImagenSeleccionada[0] = selected;
                // Mostrar vista previa local
                mostrarVistaPrevia(lblPreview, selected.getAbsolutePath());
            }
        });

        // --- PANEL DE BOTONES Y ERRORES ---
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JLabel lblError = new JLabel("", SwingConstants.LEFT);
        lblError.setForeground(new Color(220, 38, 38));
        lblError.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(30, 58, 138));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.putClientProperty("JButton.buttonType", "default");
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelAcciones.add(btnCancelar);
        panelAcciones.add(btnGuardar);

        panelInferior.add(lblError, BorderLayout.WEST);
        panelInferior.add(panelAcciones, BorderLayout.EAST);

        // Cancelar
        btnCancelar.addActionListener(e -> dialog.dispose());

        // Guardar
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

            // Copiar la imagen si se seleccionó una nueva
            String finalRutaImagen = producto != null ? producto.getImagenRuta() : null;

            if (archivoImagenSeleccionada[0] != null) {
                try {
                    File folderDestino = new File("imagenes/productos");
                    if (!folderDestino.exists()) {
                        folderDestino.mkdirs();
                    }
                    
                    // Generar un nombre único con timestamp
                    String origName = archivoImagenSeleccionada[0].getName();
                    String nuevoNombre = System.currentTimeMillis() + "_" + origName;
                    File archivoDestino = new File(folderDestino, nuevoNombre);
                    
                    // Copiar el archivo al directorio de destino
                    Files.copy(archivoImagenSeleccionada[0].toPath(), archivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    // Guardamos la ruta relativa usando barras normales para uniformidad en BD
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
     * Elimina el producto seleccionado.
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
}