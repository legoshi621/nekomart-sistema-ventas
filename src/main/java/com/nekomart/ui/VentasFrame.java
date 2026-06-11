package com.nekomart.ui;

import com.nekomart.models.Producto;
import com.nekomart.models.DetalleVenta;
import com.nekomart.services.ProductoService;
import com.nekomart.services.VentaService;
import com.nekomart.utils.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel del módulo de ventas (Punto de Venta - POS).
 * Permite buscar productos, agregarlos a la venta actual y procesar el cobro.
 * Todo el código está comentado en español.
 */
public class VentasFrame extends JPanel {

    private ProductoService productoService;
    private VentaService ventaService;

    // Componentes del panel izquierdo (búsqueda)
    private JTextField txtBuscarProducto;
    private JTable tablaProductosDisponibles;
    private DefaultTableModel modeloProductos;

    // Componentes del panel derecho (ticket)
    private JLabel lblFolio;
    private JTable tablaVentaActual;
    private DefaultTableModel modeloVenta;
    private JTextField txtCantidad;
    private JLabel lblTotal;

    // Componentes del panel inferior (cobro)
    private JComboBox<String> cmbMetodoPago;
    private JTextField txtMontoRecibido;
    private JLabel lblCambio;
    private JButton btnCobrar;

    // Lista de detalles de la venta actual
    private List<DetalleVenta> detallesVenta;

    // Columnas de las tablas
    private final String[] COLS_PRODUCTOS = { "Código", "Nombre", "Precio", "Stock" };
    private final String[] COLS_VENTA = { "Producto", "Cantidad", "Precio Unit.", "Subtotal" };

    public VentasFrame() {
        productoService = new ProductoService();
        ventaService = new VentaService();
        detallesVenta = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        cargarProductosDisponibles();
        generarNuevoFolio();
    }

    /**
     * Inicializa los componentes visuales.
     */
    private void initComponents() {
        // Panel principal dividido en dos
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);

        // Panel izquierdo: Búsqueda de productos
        JPanel panelIzquierdo = new JPanel(new BorderLayout(5, 5));

        txtBuscarProducto = new JTextField();
        txtBuscarProducto.putClientProperty("JTextField.placeholderText", "Buscar por código o nombre...");
        txtBuscarProducto.addActionListener(e -> buscarProducto());

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarProducto());

        JPanel panelBusqueda = new JPanel(new BorderLayout(5, 0));
        panelBusqueda.add(txtBuscarProducto, BorderLayout.CENTER);
        panelBusqueda.add(btnBuscar, BorderLayout.EAST);

        modeloProductos = new DefaultTableModel(COLS_PRODUCTOS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProductosDisponibles = new JTable(modeloProductos);
        tablaProductosDisponibles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductosDisponibles.setRowHeight(25);

        tablaProductosDisponibles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    agregarProductoSeleccionado();
                }
            }
        });

        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.putClientProperty("JButton.buttonType", "default");
        btnAgregar.addActionListener(e -> agregarProductoSeleccionado());

        panelIzquierdo.add(panelBusqueda, BorderLayout.NORTH);
        panelIzquierdo.add(new JScrollPane(tablaProductosDisponibles), BorderLayout.CENTER);
        panelIzquierdo.add(btnAgregar, BorderLayout.SOUTH);

        // Panel derecho: Ticket de venta
        JPanel panelDerecho = new JPanel(new BorderLayout(5, 5));

        lblFolio = new JLabel("Folio: VENTA-000", SwingConstants.CENTER);
        lblFolio.setFont(new Font("Segoe UI", Font.BOLD, 16));

        modeloVenta = new DefaultTableModel(COLS_VENTA, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaVentaActual = new JTable(modeloVenta);
        tablaVentaActual.setRowHeight(25);

        JPanel panelCantidad = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCantidad.add(new JLabel("Cantidad:"));
        txtCantidad = new JTextField(5);
        txtCantidad.setText("1");
        panelCantidad.add(txtCantidad);

        JButton btnQuitar = new JButton("Quitar");
        btnQuitar.addActionListener(e -> quitarProductoSeleccionado());
        panelCantidad.add(btnQuitar);

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarVenta());
        panelCantidad.add(btnLimpiar);

        lblTotal = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(new Color(0, 128, 0));

        panelDerecho.add(lblFolio, BorderLayout.NORTH);
        panelDerecho.add(new JScrollPane(tablaVentaActual), BorderLayout.CENTER);
        panelDerecho.add(panelCantidad, BorderLayout.SOUTH);

        splitPane.setLeftComponent(panelIzquierdo);
        splitPane.setRightComponent(panelDerecho);

        // Panel inferior: Cobro
        JPanel panelCobro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelCobro.setBorder(BorderFactory.createTitledBorder("Cobro"));

        panelCobro.add(new JLabel("Método de pago:"));
        cmbMetodoPago = new JComboBox<>(new String[] { "Efectivo", "Tarjeta" });
        cmbMetodoPago.addActionListener(e -> toggleMontoRecibido());
        panelCobro.add(cmbMetodoPago);

        panelCobro.add(new JLabel("Recibido:"));
        txtMontoRecibido = new JTextField(10);
        txtMontoRecibido.addActionListener(e -> calcularCambio());
        panelCobro.add(txtMontoRecibido);

        lblCambio = new JLabel("Cambio: $0.00");
        lblCambio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelCobro.add(lblCambio);

        btnCobrar = new JButton("COBRAR");
        btnCobrar.putClientProperty("JButton.buttonType", "default");
        btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCobrar.addActionListener(e -> procesarCobro());
        panelCobro.add(btnCobrar);

        add(splitPane, BorderLayout.CENTER);
        add(panelCobro, BorderLayout.SOUTH);
    }

    /**
     * Carga todos los productos disponibles en la tabla izquierda.
     */
    private void cargarProductosDisponibles() {
        modeloProductos.setRowCount(0);
        List<Producto> productos = productoService.obtenerTodos();
        for (Producto p : productos) {
            if (p.getStock() > 0) {
                modeloProductos.addRow(new Object[] {
                        p.getCodigo(), p.getNombre(),
                        String.format("$%.2f", p.getPrecio()), p.getStock()
                });
            }
        }
    }

    /**
     * Busca productos según el texto ingresado.
     */
    private void buscarProducto() {
        String texto = txtBuscarProducto.getText().trim();
        modeloProductos.setRowCount(0);

        if (texto.isEmpty()) {
            cargarProductosDisponibles();
            return;
        }

        List<Producto> resultados = productoService.buscar(texto);
        for (Producto p : resultados) {
            if (p.getStock() > 0) {
                modeloProductos.addRow(new Object[] {
                        p.getCodigo(), p.getNombre(),
                        String.format("$%.2f", p.getPrecio()), p.getStock()
                });
            }
        }
    }

    /**
     * Agrega el producto seleccionado a la venta actual.
     */
    private void agregarProductoSeleccionado() {
        int fila = tablaProductosDisponibles.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String codigo = (String) modeloProductos.getValueAt(fila, 0);
        Producto producto = productoService.obtenerTodos().stream()
                .filter(p -> p.getCodigo().equals(codigo))
                .findFirst().orElse(null);

        if (producto == null)
            return;

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cantidad > producto.getStock()) {
            JOptionPane.showMessageDialog(this, "Stock insuficiente. Disponible: " + producto.getStock(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si ya está en la venta
        for (DetalleVenta d : detallesVenta) {
            if (d.getProductoId() == producto.getId()) {
                int nuevaCantidad = d.getCantidad() + cantidad;
                if (nuevaCantidad > producto.getStock()) {
                    JOptionPane.showMessageDialog(this, "Stock insuficiente al sumar cantidades",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                d.setCantidad(nuevaCantidad);
                actualizarTablaVenta();
                return;
            }
        }

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId(producto.getId());
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());
        detallesVenta.add(detalle);

        actualizarTablaVenta();
        txtCantidad.setText("1");
    }

    /**
     * Actualiza la tabla de la venta actual y el total.
     */
    private void actualizarTablaVenta() {
        modeloVenta.setRowCount(0);
        double total = 0;

        for (DetalleVenta d : detallesVenta) {
            Producto p = productoService.obtenerTodos().stream()
                    .filter(prod -> prod.getId() == d.getProductoId())
                    .findFirst().orElse(null);

            if (p != null) {
                double subtotal = d.getCantidad() * d.getPrecioUnitario();
                total += subtotal;
                modeloVenta.addRow(new Object[] {
                        p.getNombre(), d.getCantidad(),
                        String.format("$%.2f", d.getPrecioUnitario()),
                        String.format("$%.2f", subtotal)
                });
            }
        }

        lblTotal.setText("Total: $" + String.format("%.2f", total));
        calcularCambio();
    }

    /**
     * Quita el producto seleccionado de la venta actual.
     */
    private void quitarProductoSeleccionado() {
        int fila = tablaVentaActual.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la venta",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        detallesVenta.remove(fila);
        actualizarTablaVenta();
    }

    /**
     * Limpia la venta actual.
     */
    private void limpiarVenta() {
        detallesVenta.clear();
        actualizarTablaVenta();
        generarNuevoFolio();
    }

    /**
     * Genera un nuevo folio para la venta.
     */
    private void generarNuevoFolio() {
        lblFolio.setText("Folio: " + ventaService.obtenerTodasLasVentas().size() + 1);
    }

    /**
     * Muestra/oculta el campo de monto recibido según el método de pago.
     */
    private void toggleMontoRecibido() {
        boolean esEfectivo = cmbMetodoPago.getSelectedItem().equals("Efectivo");
        txtMontoRecibido.setEnabled(esEfectivo);
        if (!esEfectivo) {
            txtMontoRecibido.setText("");
            lblCambio.setText("Cambio: $0.00");
        }
    }

    /**
     * Calcula el cambio automáticamente.
     */
    private void calcularCambio() {
        if (!cmbMetodoPago.getSelectedItem().equals("Efectivo"))
            return;

        try {
            double total = Double.parseDouble(lblTotal.getText().replace("Total: $", "").replace(",", ""));
            double recibido = Double.parseDouble(txtMontoRecibido.getText().trim());
            double cambio = recibido - total;
            lblCambio.setText("Cambio: $" + String.format("%.2f", Math.max(0, cambio)));
        } catch (NumberFormatException e) {
            lblCambio.setText("Cambio: $0.00");
        }
    }

    /**
     * Procesa el cobro de la venta.
     */
    private void procesarCobro() {
        if (detallesVenta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en la venta",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String metodoPago = (String) cmbMetodoPago.getSelectedItem();
        double montoRecibido = 0;

        if (metodoPago.equals("Efectivo")) {
            try {
                montoRecibido = Double.parseDouble(txtMontoRecibido.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Ingresa el monto recibido",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double total = Double.parseDouble(lblTotal.getText().replace("Total: $", "").replace(",", ""));
            if (montoRecibido < total) {
                JOptionPane.showMessageDialog(this, "Monto recibido insuficiente",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int empleadoId = SessionManager.getInstancia().getUsuarioActual().getId();

        if (ventaService.procesarVenta(detallesVenta, metodoPago, montoRecibido, empleadoId)) {
            double total = Double.parseDouble(lblTotal.getText().replace("Total: $", "").replace(",", ""));
            double cambio = metodoPago.equals("Efectivo") ? montoRecibido - total : 0;

            JOptionPane.showMessageDialog(this,
                    "Venta registrada exitosamente\nTotal: $" + String.format("%.2f", total) +
                            "\nCambio: $" + String.format("%.2f", cambio),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

            limpiarVenta();
            cargarProductosDisponibles();
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar la venta",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}