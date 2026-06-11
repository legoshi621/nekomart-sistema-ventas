package com.nekomart.ui;

import com.nekomart.models.Producto;
import com.nekomart.services.ProductoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        JButton btnBuscar = new JButton("🔍 Buscar");
        JButton btnNuevo = new JButton("+ Nuevo");
        JButton btnEliminar = new JButton(" Eliminar");
        JButton btnRefrescar = new JButton("🔄 Refrescar");

        btnNuevo.putClientProperty("JButton.buttonType", "default");

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
        tablaProductos.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);

        // Eventos
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
     * Abre un diálogo para crear o editar un producto.
     */
    private void abrirDialogoProducto(Producto producto) {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        JTextField txtCodigo = new JTextField(producto != null ? producto.getCodigo() : "");
        JTextField txtNombre = new JTextField(producto != null ? producto.getNombre() : "");
        JTextField txtPrecio = new JTextField(producto != null ? String.valueOf(producto.getPrecio()) : "");
        JTextField txtStock = new JTextField(producto != null ? String.valueOf(producto.getStock()) : "");
        JTextField txtStockMin = new JTextField(producto != null ? String.valueOf(producto.getStockMinimo()) : "5");
        JTextField txtCategoria = new JTextField(producto != null ? producto.getCategoria() : "");

        panel.add(new JLabel("Código:"));
        panel.add(txtCodigo);
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Precio:"));
        panel.add(txtPrecio);
        panel.add(new JLabel("Stock:"));
        panel.add(txtStock);
        panel.add(new JLabel("Stock Mínimo:"));
        panel.add(txtStockMin);
        panel.add(new JLabel("Categoría:"));
        panel.add(txtCategoria);

        String titulo = producto != null ? "Editar Producto" : "Nuevo Producto";
        int opcion = JOptionPane.showConfirmDialog(this, panel, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opcion == JOptionPane.OK_OPTION) {
            try {
                Producto p = new Producto();
                if (producto != null)
                    p.setId(producto.getId());
                p.setCodigo(txtCodigo.getText().trim());
                p.setNombre(txtNombre.getText().trim());
                p.setPrecio(Double.parseDouble(txtPrecio.getText().trim()));
                p.setStock(Integer.parseInt(txtStock.getText().trim()));
                p.setStockMinimo(Integer.parseInt(txtStockMin.getText().trim()));
                p.setCategoria(txtCategoria.getText().trim());

                if (productoService.guardarProducto(p)) {
                    JOptionPane.showMessageDialog(this, "Producto guardado exitosamente",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarProductos();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar el producto",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Datos inválidos. Verifica precio y stock.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Edita el producto seleccionado en la tabla.
     */
    private void editarProductoSeleccionado() {
        int fila = tablaProductos.getSelectedRow();
        if (fila >= 0) {
            int id = (int) modeloTabla.getValueAt(fila, 0);
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

        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar el producto '" + nombre + "'?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

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