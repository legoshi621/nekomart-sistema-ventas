package com.nekomart.ui;

import com.nekomart.models.Producto;
import com.nekomart.models.DetalleVenta;
import com.nekomart.models.Venta;
import com.nekomart.services.ProductoService;
import com.nekomart.services.VentaService;
import com.nekomart.services.FacturaService;
import com.nekomart.services.ImpresionService;
import com.nekomart.services.CorreoService;
import com.nekomart.utils.ConfiguracionCorreo;
import com.nekomart.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel del módulo de ventas (Punto de Venta - POS).
 * Permite buscar productos, agregarlos a la venta actual y procesar el cobro.
 * Incluye atajos de teclado (F2, F3), animaciones y diseño responsivo.
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
        
        // Registrar atajos de teclado
        configurarAtajosTeclado();
    }

    /**
     * Inicializa los componentes visuales.
     */
    private void initComponents() {
        // Panel principal dividido en dos (Búsqueda a la izquierda, Ticket a la derecha)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(520);
        splitPane.setResizeWeight(0.5);

        // --- PANEL IZQUIERDO: Búsqueda de productos ---
        JPanel panelIzquierdo = new JPanel(new BorderLayout(8, 8));
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        txtBuscarProducto = new JTextField();
        txtBuscarProducto.putClientProperty("JTextField.placeholderText", "Buscar por código o nombre (F2)...");
        txtBuscarProducto.putClientProperty("JTextField.showClearButton", true);
        txtBuscarProducto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscarProducto.addActionListener(e -> buscarProducto());

        JButton btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.setToolTipText("Buscar productos");
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        tablaProductosDisponibles.setRowHeight(28);
        tablaProductosDisponibles.putClientProperty("JTable.alternateRowColor", true);

        // Doble clic para añadir al carrito
        tablaProductosDisponibles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    agregarProductoSeleccionado();
                }
            }
        });

        JButton btnAgregar = new JButton("➕ Agregar al Carrito");
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAgregar.putClientProperty("JButton.buttonType", "default");
        btnAgregar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgregar.addActionListener(e -> agregarProductoSeleccionado());

        panelIzquierdo.add(panelBusqueda, BorderLayout.NORTH);
        panelIzquierdo.add(new JScrollPane(tablaProductosDisponibles), BorderLayout.CENTER);
        panelIzquierdo.add(btnAgregar, BorderLayout.SOUTH);

        // --- PANEL DERECHO: Ticket de venta ---
        JPanel panelDerecho = new JPanel(new BorderLayout(8, 8));
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        lblFolio = new JLabel("Folio: VENTA-000", SwingConstants.CENTER);
        lblFolio.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFolio.setForeground(new Color(30, 58, 138)); // Azul corporativo

        modeloVenta = new DefaultTableModel(COLS_VENTA, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaVentaActual = new JTable(modeloVenta);
        tablaVentaActual.setRowHeight(28);
        tablaVentaActual.putClientProperty("JTable.alternateRowColor", true);

        // Panel de cantidad y control del carrito
        JPanel panelCantidad = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelCantidad.add(new JLabel("Cantidad:"));
        
        txtCantidad = new JTextField(5);
        txtCantidad.setText("1");
        txtCantidad.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtCantidad.setHorizontalAlignment(JTextField.CENTER);
        panelCantidad.add(txtCantidad);

        JButton btnQuitar = new JButton("🗑️ Quitar");
        btnQuitar.setToolTipText("Quitar producto seleccionado de la venta");
        btnQuitar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuitar.addActionListener(e -> quitarProductoSeleccionado());
        panelCantidad.add(btnQuitar);

        JButton btnLimpiar = new JButton("🔄 Limpiar");
        btnLimpiar.setToolTipText("Cancelar venta y limpiar carrito");
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.addActionListener(e -> limpiarVenta());
        panelCantidad.add(btnLimpiar);

        // Total grande en verde esmeralda
        lblTotal = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTotal.setForeground(new Color(22, 163, 74)); // Verde esmeralda (#16a34a)
        lblTotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 10));

        panelDerecho.add(lblFolio, BorderLayout.NORTH);
        panelDerecho.add(new JScrollPane(tablaVentaActual), BorderLayout.CENTER);
        
        JPanel panelDerechoInferior = new JPanel(new BorderLayout());
        panelDerechoInferior.add(panelCantidad, BorderLayout.NORTH);
        panelDerechoInferior.add(lblTotal, BorderLayout.SOUTH);
        panelDerecho.add(panelDerechoInferior, BorderLayout.SOUTH);

        splitPane.setLeftComponent(panelIzquierdo);
        splitPane.setRightComponent(panelDerecho);

        // --- PANEL INFERIOR: Cobro ---
        JPanel panelCobro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelCobro.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 210), 1, true),
            "Procesar Cobro (F3)", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 12)
        ));

        JLabel lblPago = new JLabel("Método de pago:");
        lblPago.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelCobro.add(lblPago);
        
        cmbMetodoPago = new JComboBox<>(new String[] { "Efectivo", "Tarjeta" });
        cmbMetodoPago.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbMetodoPago.addActionListener(e -> toggleMontoRecibido());
        panelCobro.add(cmbMetodoPago);

        JLabel lblRecibido = new JLabel("Monto Recibido ($):");
        lblRecibido.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelCobro.add(lblRecibido);
        
        txtMontoRecibido = new JTextField(10);
        txtMontoRecibido.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtMontoRecibido.setHorizontalAlignment(JTextField.RIGHT);
        txtMontoRecibido.addActionListener(e -> calcularCambio());
        panelCobro.add(txtMontoRecibido);

        lblCambio = new JLabel("Cambio: $0.00");
        lblCambio.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCambio.setForeground(new Color(30, 58, 138));
        panelCobro.add(lblCambio);

        btnCobrar = new JButton("COBRAR (F3)");
        btnCobrar.putClientProperty("JButton.buttonType", "default");
        btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCobrar.setBackground(new Color(22, 163, 74)); // Verde esmeralda
        btnCobrar.setForeground(Color.WHITE);
        btnCobrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCobrar.addActionListener(e -> procesarCobro());
        panelCobro.add(btnCobrar);

        add(splitPane, BorderLayout.CENTER);
        add(panelCobro, BorderLayout.SOUTH);
    }

    /**
     * Configura los atajos de teclado F2 (Buscar) y F3 (Cobrar) a nivel de ventana.
     */
    private void configurarAtajosTeclado() {
        // F2: Enfoca el cuadro de búsqueda de productos
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "focusBuscador");
        this.getActionMap().put("focusBuscador", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtBuscarProducto.requestFocusInWindow();
                txtBuscarProducto.selectAll();
            }
        });

        // F3: Ejecuta la acción de Cobrar
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "ejecutarCobro");
        this.getActionMap().put("ejecutarCobro", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnCobrar.doClick();
            }
        });
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
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la lista izquierda primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convertir índice por si el usuario reordenó las columnas de la tabla
        int modelRow = tablaProductosDisponibles.convertRowIndexToModel(fila);
        String codigo = (String) modeloProductos.getValueAt(modelRow, 0);
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
            JOptionPane.showMessageDialog(this, "Cantidad ingresada inválida. Debe ser un entero mayor a cero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cantidad > producto.getStock()) {
            JOptionPane.showMessageDialog(this, "Stock insuficiente en almacén. Disponible: " + producto.getStock(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si ya está en la venta para sumar la cantidad
        for (DetalleVenta d : detallesVenta) {
            if (d.getProductoId() == producto.getId()) {
                int nuevaCantidad = d.getCantidad() + cantidad;
                if (nuevaCantidad > producto.getStock()) {
                    JOptionPane.showMessageDialog(this, "Stock insuficiente al sumar cantidades. Máximo disponible: " + producto.getStock(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                d.setCantidad(nuevaCantidad);
                actualizarTablaVenta();
                animarTotalAlAgregar(); // Lanzar animación de parpadeo
                return;
            }
        }

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId(producto.getId());
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());
        detallesVenta.add(detalle);

        actualizarTablaVenta();
        animarTotalAlAgregar(); // Lanzar animación de parpadeo
        txtCantidad.setText("1");
    }

    /**
     * Hace parpadear el color del total de la venta para dar feedback inmediato al agregar artículos.
     */
    private void animarTotalAlAgregar() {
        Timer timer = new Timer(60, new ActionListener() {
            private int count = 0;
            private final Color originalColor = new Color(22, 163, 74); // Verde esmeralda
            private final Color flashColor = new Color(245, 158, 11);    // Dorado/Naranja
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count % 2 == 0) {
                    lblTotal.setForeground(flashColor);
                } else {
                    lblTotal.setForeground(originalColor);
                }
                count++;
                if (count >= 6) {
                    lblTotal.setForeground(originalColor);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
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
            JOptionPane.showMessageDialog(this, "Selecciona un producto del ticket de venta a la derecha para quitarlo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tablaVentaActual.convertRowIndexToModel(fila);
        detallesVenta.remove(modelRow);
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
        lblFolio.setText("Folio: " + (ventaService.obtenerTodasLasVentas().size() + 1));
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
        } else {
            txtMontoRecibido.requestFocusInWindow();
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
     * Procesa el cobro de la venta y, si tiene éxito, muestra el diálogo
     * de opciones de facturación (imprimir, enviar por correo, guardar PDF).
     */
    private void procesarCobro() {
        if (detallesVenta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos agregados en la venta actual.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String metodoPago    = (String) cmbMetodoPago.getSelectedItem();
        double montoRecibido = 0;

        if (metodoPago.equals("Efectivo")) {
            try {
                montoRecibido = Double.parseDouble(txtMontoRecibido.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa el monto recibido en efectivo.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                txtMontoRecibido.requestFocusInWindow();
                return;
            }

            double total = Double.parseDouble(lblTotal.getText().replace("Total: $", "").replace(",", ""));
            if (montoRecibido < total) {
                JOptionPane.showMessageDialog(this, "El monto recibido es insuficiente para cubrir el total de la venta.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                txtMontoRecibido.requestFocusInWindow();
                return;
            }
        }

        int    empleadoId = SessionManager.getInstancia().getUsuarioActual().getId();
        double totalVenta = Double.parseDouble(lblTotal.getText().replace("Total: $", "").replace(",", ""));
        double cambio     = metodoPago.equals("Efectivo") ? montoRecibido - totalVenta : 0;

        // Guardar copia de los detalles antes de limpiar el carrito
        List<DetalleVenta> detallesCopia = new ArrayList<>(detallesVenta);

        if (ventaService.procesarVenta(detallesVenta, metodoPago, montoRecibido, empleadoId)) {

            // Construir objeto Venta para la generación del PDF
            // Obtenemos el folio del label (formato "Folio: XXXX")
            String folioTexto = lblFolio.getText().replace("Folio: ", "").trim();
            Venta ventaRegistrada = new Venta();
            ventaRegistrada.setFolio(folioTexto);
            ventaRegistrada.setFecha(java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ventaRegistrada.setTotal(totalVenta);
            ventaRegistrada.setMetodoPago(metodoPago);
            ventaRegistrada.setMontoRecibido(montoRecibido);
            ventaRegistrada.setCambio(cambio);
            ventaRegistrada.setEmpleadoId(empleadoId);

            // Mostrar diálogo de facturación
            mostrarDialogoFacturacion(ventaRegistrada, detallesCopia, totalVenta, cambio);

            // Limpiar la pantalla para la siguiente venta
            limpiarVenta();
            cargarProductosDisponibles();

        } else {
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al procesar la venta.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra el diálogo post-venta con 3 opciones:
     * 🖨️ Imprimir Factura | 📧 Enviar por Correo | 💾 Guardar PDF.
     *
     * @param venta      Objeto Venta registrado.
     * @param detalles   Lista de detalles (productos) de la venta.
     * @param total      Total cobrado.
     * @param cambio     Cambio entregado al cliente.
     */
    private void mostrarDialogoFacturacion(Venta venta, List<DetalleVenta> detalles,
                                           double total, double cambio) {
        // ── Panel de confirmación de venta ────────────────────────────────
        JPanel panelInfo = new JPanel(new BorderLayout(0, 8));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        JLabel lblTitulo = new JLabel("✅ Venta registrada exitosamente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(22, 163, 74));

        String infoTexto = String.format(
                "<html><center>"
                + "Total cobrado: <b>$%.2f</b><br>"
                + "Cambio: <b>$%.2f</b><br><br>"
                + "<i>¿Desea generar la factura?</i>"
                + "</center></html>", total, cambio);
        JLabel lblInfo = new JLabel(infoTexto, SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panelInfo.add(lblTitulo, BorderLayout.NORTH);
        panelInfo.add(lblInfo, BorderLayout.CENTER);

        // ── Botones del diálogo ───────────────────────────────────────────
        Object[] opciones = { "🖨️ Imprimir Factura", "📧 Enviar por Correo", "💾 Guardar PDF", "Cerrar" };

        int eleccion = JOptionPane.showOptionDialog(
                this,
                panelInfo,
                "Facturación — Folio: " + venta.getFolio(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[2] // Opción por defecto: Guardar PDF
        );

        // ── Generar PDF (necesario para imprimir o adjuntar en correo) ────
        if (eleccion == 0 || eleccion == 1 || eleccion == 2) {
            FacturaService facturaService = new FacturaService();
            String rutaPDF = facturaService.generarPDF(venta, detalles);

            if (rutaPDF == null) {
                JOptionPane.showMessageDialog(this,
                        "Error al generar el PDF de la factura.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            switch (eleccion) {
                case 0: // 🖨️ Imprimir Factura
                    ImpresionService impresionService = new ImpresionService();
                    impresionService.imprimirPDF(rutaPDF);
                    break;

                case 1: // 📧 Enviar por Correo
                    // Verificar configuración de correo antes de abrir el diálogo
                    if (!ConfiguracionCorreo.estaConfigurado()) {
                        int resp = JOptionPane.showConfirmDialog(this,
                                "El correo SMTP no está configurado aún.\n"
                                + "¿Desea configurarlo ahora?",
                                "Configuración requerida", JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (resp == JOptionPane.YES_OPTION) {
                            ConfiguracionCorreo.mostrarDialogoConfiguracion(this);
                        } else {
                            break;
                        }
                    }
                    enviarFacturaPorCorreo(venta, rutaPDF);
                    break;

                case 2: // 💾 Guardar PDF
                    JOptionPane.showMessageDialog(this,
                            "PDF guardado correctamente en:\n" + rutaPDF,
                            "PDF Guardado", JOptionPane.INFORMATION_MESSAGE);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Abre un diálogo para ingresar el email del cliente y envía la factura.
     *
     * @param venta    Venta registrada (para construir el cuerpo del correo).
     * @param rutaPDF  Ruta del PDF ya generado.
     */
    private void enviarFacturaPorCorreo(Venta venta, String rutaPDF) {
        // ── Diálogo para ingresar el email del cliente ─────────────────────
        JPanel panelEmail = new JPanel(new GridLayout(2, 2, 8, 8));
        panelEmail.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTextField txtEmail   = new JTextField(25);
        JTextField txtAsunto  = new JTextField("Tu factura de NekoMart — Folio: " + venta.getFolio(), 25);

        panelEmail.add(new JLabel("Correo del cliente:"));
        panelEmail.add(txtEmail);
        panelEmail.add(new JLabel("Asunto:"));
        panelEmail.add(txtAsunto);

        int resp = JOptionPane.showConfirmDialog(
                this, panelEmail,
                "📧 Enviar Factura por Correo",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (resp != JOptionPane.OK_OPTION) return;

        String email  = txtEmail.getText().trim();
        String asunto = txtAsunto.getText().trim();

        if (email.isEmpty() || !email.contains("@")) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, ingresa un correo electrónico válido.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Enviar en hilo separado para no bloquear la UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                CorreoService correoService = new CorreoService();
                String cuerpoHTML = CorreoService.generarCuerpoHTML(
                        venta.getFolio(),
                        String.format("$%.2f", venta.getTotal()),
                        venta.getMetodoPago());
                return correoService.enviarFactura(email, asunto, cuerpoHTML, rutaPDF);
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (exito) {
                        JOptionPane.showMessageDialog(VentasFrame.this,
                                "✅ Factura enviada correctamente a:\n" + email,
                                "Correo Enviado", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(VentasFrame.this,
                                "❌ No se pudo enviar el correo.\n"
                                + "Verifica la configuración SMTP en:\n"
                                + "Configuración → Correo SMTP",
                                "Error de Envío", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(VentasFrame.this,
                            "Error inesperado: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // Mostrar indicador de progreso mientras se envía
        worker.execute();
        JOptionPane.showMessageDialog(this,
                "Enviando correo a " + email + "...\nEspera un momento.",
                "Enviando...", JOptionPane.INFORMATION_MESSAGE);
    }
}