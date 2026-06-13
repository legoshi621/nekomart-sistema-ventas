package com.nekomart.ui;

import com.nekomart.Main;
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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VentasFrame es la interfaz principal del Punto de Venta (POS) para NekoMart.
 * Implementa un diseño responsivo con colores pastel, paneles redondeados,
 * pestañas de categorías, un buscador en tiempo real, atajos de teclado y
 * una lista de productos en el carrito con controles visuales modernos.
 * 
 * Todo el código está en español y documentado.
 */
public class VentasFrame extends JPanel {

    // Servicios de negocio
    private final ProductoService productoService;
    private final VentaService ventaService;

    // Colores pastel para la interfaz (no final para soportar cambio de tema)
    private static Color LAVANDA = new Color(184, 169, 232);    // #B8A9E8
    private static Color CORAL = new Color(255, 139, 148);      // #FF8B94
    private static Color MENTA = new Color(168, 230, 207);      // #A8E6CF
    private static Color FONDO = new Color(250, 250, 250);      // #FAFAFA
    private static Color BLANCO = Color.WHITE;                  // #FFFFFF
    private static Color TEXTO_OSCURO = new Color(45, 55, 72);  // #2D3748
    private static Color GRIS = new Color(113, 128, 150);       // #718096
    private static Color STOCK_BAJO = new Color(252, 129, 129); // #FC8181
    private static Color BORDE = new Color(226, 232, 240);      // #E2E8F0

    // Componente expuesto para el cambio de tema
    private RoundedPanel panelDerecho;

    // Componentes del Panel Izquierdo (Selección de productos)
    private JPanel panelCategorias;
    private JPanel panelGridProductos;
    private JTextField txtBuscar;
    private String categoriaSeleccionada = "Todos";
    private final List<CategoryButton> botonesCategorias = new ArrayList<>();

    // Componentes del Panel Derecho (Carrito de Ventas)
    private JLabel lblBadgeCount;
    private JPanel panelItemsCarrito;
    private JLabel lblSubtotal;
    private JLabel lblIva;
    private JLabel lblTotal;

    // Métodos de Pago y Efectivo
    private String metodoPagoSeleccionado = "Efectivo";
    private PastelButton btnMetodoEfectivo;
    private PastelButton btnMetodoTarjeta;
    private JPanel panelEfectivoDetails;
    private JTextField txtMontoRecibido;
    private JLabel lblCambioValor;
    private JPanel panelSouthCart;

    // Datos del Carrito
    private final List<CarritoItem> carrito;

    /**
     * Clase interna que modela un elemento en el carrito de compras.
     */
    private static class CarritoItem {
        Producto producto;
        int cantidad;

        public CarritoItem(Producto producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }

        public double getSubtotal() {
            return producto.getPrecio() * cantidad;
        }
    }

    /**
     * Constructor principal de la interfaz POS de ventas.
     */
    public VentasFrame() {
        this.productoService = new ProductoService();
        this.ventaService = new VentaService();
        this.carrito = new ArrayList<>();

        // Configuración básica del panel contenedor
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(FONDO);

        initComponents();
        if (panelGridProductos != null) {
            actualizarGridProductos();
        }
        configurarAtajosTeclado();
    }

    /**
     * Inicializa y organiza los componentes del frame.
     */
    private void initComponents() {
        // --- 1. CONTENEDOR CENTRAL CON PROPORCIONES 70% / 30% ---
        JPanel panelContenedor = new JPanel(new GridBagLayout());
        panelContenedor.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // --- COLUMNA IZQUIERDA (70%) ---
        JPanel panelIzquierdo = new JPanel(new BorderLayout(15, 15));
        panelIzquierdo.setOpaque(false);

        // Barra superior de la izquierda: Categorías y Buscador
        JPanel panelNorteIzquierdo = new JPanel(new BorderLayout(10, 10));
        panelNorteIzquierdo.setOpaque(false);

        panelCategorias = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panelCategorias.setOpaque(false);

        String[] categorias = {"Todos", "Cuidado Facial", "Maquillaje", "Limpieza", "Mascarillas", "Accesorios"};
        for (String cat : categorias) {
            boolean active = cat.equals(categoriaSeleccionada);
            CategoryButton btnCat = new CategoryButton(cat, active);
            btnCat.addActionListener(e -> {
                categoriaSeleccionada = cat;
                for (CategoryButton b : botonesCategorias) {
                    b.setActive(b.getText().equals(categoriaSeleccionada));
                }
                actualizarGridProductos();
            });
            botonesCategorias.add(btnCat);
            panelCategorias.add(btnCat);
        }
        panelNorteIzquierdo.add(panelCategorias, BorderLayout.CENTER);

        // Buscador de producto sutil
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        panelBusqueda.setOpaque(false);
        txtBuscar = new JTextField(15);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar producto... (F2)");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        txtBuscar.setPreferredSize(new Dimension(200, 32));
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            private void filtrar() { actualizarGridProductos(); }
        });
        panelBusqueda.add(txtBuscar);
        panelNorteIzquierdo.add(panelBusqueda, BorderLayout.EAST);

        panelIzquierdo.add(panelNorteIzquierdo, BorderLayout.NORTH);

        // Grid de productos central scrollable
        panelGridProductos = new JPanel(new GridLayout(0, 3, 15, 15));
        panelGridProductos.setBackground(FONDO);

        JScrollPane scrollGrid = new JScrollPane(panelGridProductos);
        scrollGrid.setBorder(null);
        scrollGrid.setOpaque(false);
        scrollGrid.getViewport().setOpaque(false);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16); // Scroll suave

        panelIzquierdo.add(scrollGrid, BorderLayout.CENTER);

        // --- COLUMNA DERECHA (30%) ---
        panelDerecho = new RoundedPanel(12, BLANCO, BORDE);
        panelDerecho.setLayout(new BorderLayout(15, 15));
        panelDerecho.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cabecera del Carrito
        JPanel panelHeaderCart = new JPanel(new BorderLayout());
        panelHeaderCart.setOpaque(false);

        JLabel lblCartTitle = new JLabel("🛒 Carrito de Venta");
        lblCartTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCartTitle.setForeground(TEXTO_OSCURO);
        panelHeaderCart.add(lblCartTitle, BorderLayout.WEST);

        lblBadgeCount = new JLabel("0 items", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadgeCount.setOpaque(false);
        lblBadgeCount.setBackground(LAVANDA);
        lblBadgeCount.setForeground(BLANCO);
        lblBadgeCount.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadgeCount.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        panelHeaderCart.add(lblBadgeCount, BorderLayout.EAST);

        panelDerecho.add(panelHeaderCart, BorderLayout.NORTH);

        // Lista de items del carrito scrollable
        panelItemsCarrito = new JPanel();
        panelItemsCarrito.setLayout(new BoxLayout(panelItemsCarrito, BoxLayout.Y_AXIS));
        panelItemsCarrito.setBackground(BLANCO);

        JScrollPane scrollCart = new JScrollPane(panelItemsCarrito);
        scrollCart.setBorder(null);
        scrollCart.setBackground(BLANCO);
        scrollCart.getViewport().setBackground(BLANCO);
        scrollCart.getVerticalScrollBar().setUnitIncrement(12);

        panelDerecho.add(scrollCart, BorderLayout.CENTER);

        // Parte inferior del carrito (Totales, Pagos, Acciones)
        panelSouthCart = new JPanel();
        panelSouthCart.setLayout(new BoxLayout(panelSouthCart, BoxLayout.Y_AXIS));
        panelSouthCart.setOpaque(false);

        // Separador visual inicial
        panelSouthCart.add(Box.createRigidArea(new Dimension(0, 10)));
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(BORDE);
        panelSouthCart.add(sep1);
        panelSouthCart.add(Box.createRigidArea(new Dimension(0, 10)));

        // Panel de totales
        JPanel panelTotales = new JPanel(new GridBagLayout());
        panelTotales.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);

        c.gridx = 0; c.gridy = 0; c.weightx = 0.5;
        JLabel lblSubTitle = new JLabel("Subtotal");
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubTitle.setForeground(GRIS);
        panelTotales.add(lblSubTitle, c);

        c.gridx = 1; c.weightx = 0.5;
        lblSubtotal = new JLabel("$0.00", SwingConstants.RIGHT);
        lblSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSubtotal.setForeground(TEXTO_OSCURO);
        panelTotales.add(lblSubtotal, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0.5;
        JLabel lblIvaTitle = new JLabel("IVA (16%)");
        lblIvaTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIvaTitle.setForeground(GRIS);
        panelTotales.add(lblIvaTitle, c);

        c.gridx = 1; c.weightx = 0.5;
        lblIva = new JLabel("$0.00", SwingConstants.RIGHT);
        lblIva.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIva.setForeground(TEXTO_OSCURO);
        panelTotales.add(lblIva, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0.5;
        JLabel lblTotalTitle = new JLabel("TOTAL");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalTitle.setForeground(TEXTO_OSCURO);
        panelTotales.add(lblTotalTitle, c);

        c.gridx = 1; c.weightx = 0.5;
        lblTotal = new JLabel("$0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(CORAL);
        panelTotales.add(lblTotal, c);

        panelSouthCart.add(panelTotales);

        // Separador y Métodos de pago
        panelSouthCart.add(Box.createRigidArea(new Dimension(0, 10)));
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(BORDE);
        panelSouthCart.add(sep2);
        panelSouthCart.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel lblPagoTitle = new JLabel("Método de Pago");
        lblPagoTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPagoTitle.setForeground(GRIS);
        lblPagoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelSouthCart.add(lblPagoTitle);
        panelSouthCart.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel panelMetodos = new JPanel(new GridLayout(1, 2, 10, 0));
        panelMetodos.setOpaque(false);
        panelMetodos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnMetodoEfectivo = new PastelButton("💵 Efectivo", LAVANDA, BLANCO, 12);
        btnMetodoEfectivo.addActionListener(e -> setMetodoPago("Efectivo"));

        btnMetodoTarjeta = new PastelButton("💳 Tarjeta", BLANCO, GRIS, 12);
        btnMetodoTarjeta.setBorderColor(BORDE);
        btnMetodoTarjeta.addActionListener(e -> setMetodoPago("Tarjeta"));

        panelMetodos.add(btnMetodoEfectivo);
        panelMetodos.add(btnMetodoTarjeta);
        panelSouthCart.add(panelMetodos);

        // Panel de detalles del efectivo (Monto recibido y Cambio)
        panelEfectivoDetails = new JPanel(new GridBagLayout());
        panelEfectivoDetails.setOpaque(false);
        panelEfectivoDetails.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        GridBagConstraints ec = new GridBagConstraints();
        ec.fill = GridBagConstraints.HORIZONTAL;
        ec.insets = new Insets(4, 0, 4, 0);

        ec.gridx = 0; ec.gridy = 0; ec.weightx = 0.5;
        JLabel lblRecibido = new JLabel("Monto Recibido");
        lblRecibido.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRecibido.setForeground(GRIS);
        panelEfectivoDetails.add(lblRecibido, ec);

        ec.gridx = 1; ec.weightx = 0.5;
        txtMontoRecibido = new JTextField(8);
        txtMontoRecibido.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtMontoRecibido.setHorizontalAlignment(JTextField.RIGHT);
        txtMontoRecibido.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE, 1, true),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        txtMontoRecibido.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calcularCambio(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calcularCambio(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calcularCambio(); }
        });
        panelEfectivoDetails.add(txtMontoRecibido, ec);

        ec.gridx = 0; ec.gridy = 1; ec.weightx = 0.5;
        JLabel lblCambioText = new JLabel("Cambio");
        lblCambioText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCambioText.setForeground(GRIS);
        panelEfectivoDetails.add(lblCambioText, ec);

        ec.gridx = 1; ec.weightx = 0.5;
        lblCambioValor = new JLabel("$0.00", SwingConstants.RIGHT);
        lblCambioValor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCambioValor.setForeground(new Color(74, 185, 140)); // Verde éxito
        panelEfectivoDetails.add(lblCambioValor, ec);

        panelSouthCart.add(panelEfectivoDetails);

        // Separador final antes de acciones
        panelSouthCart.add(Box.createRigidArea(new Dimension(0, 5)));
        JSeparator sep3 = new JSeparator();
        sep3.setForeground(BORDE);
        panelSouthCart.add(sep3);
        panelSouthCart.add(Box.createRigidArea(new Dimension(0, 12)));

        // Botones de Checkout
        PastelButton btnCobrar = new PastelButton("COBRAR", CORAL, BLANCO, 12);
        btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCobrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnCobrar.addActionListener(e -> procesarCobro());
        panelSouthCart.add(btnCobrar);

        panelSouthCart.add(Box.createRigidArea(new Dimension(0, 8)));

        PastelButton btnCancelar = new PastelButton("Cancelar Venta", BLANCO, GRIS, 12);
        btnCancelar.setBorderColor(GRIS);
        btnCancelar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnCancelar.addActionListener(e -> limpiarCarrito());
        panelSouthCart.add(btnCancelar);

        panelDerecho.add(panelSouthCart, BorderLayout.SOUTH);

        // Agregar al GridBagLayout principal
        gbc.gridx = 0;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 0, 15);
        panelContenedor.add(panelIzquierdo, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 0, 0);
        panelContenedor.add(panelDerecho, gbc);

        add(panelContenedor, BorderLayout.CENTER);
    }

    /**
     * Registra atajos de teclado globales a nivel de la pestaña seleccionada.
     */
    private void configurarAtajosTeclado() {
        // F2: enfocar cuadro de búsqueda
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "focusBuscador");
        this.getActionMap().put("focusBuscador", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtBuscar.requestFocusInWindow();
                txtBuscar.selectAll();
            }
        });

        // F3: accionar botón de Cobrar
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "ejecutarCobro");
        this.getActionMap().put("ejecutarCobro", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarCobro();
            }
        });
    }

    /**
     * Alterna y estiliza el método de pago seleccionado.
     */
    private void setMetodoPago(String metodo) {
        this.metodoPagoSeleccionado = metodo;
        if (metodo.equals("Efectivo")) {
            btnMetodoEfectivo.setBackground(LAVANDA);
            btnMetodoEfectivo.setForeground(BLANCO);
            btnMetodoEfectivo.setBorderColor(null);

            btnMetodoTarjeta.setBackground(BLANCO);
            btnMetodoTarjeta.setForeground(GRIS);
            btnMetodoTarjeta.setBorderColor(BORDE);

            panelEfectivoDetails.setVisible(true);
            txtMontoRecibido.requestFocusInWindow();
        } else {
            btnMetodoTarjeta.setBackground(LAVANDA);
            btnMetodoTarjeta.setForeground(BLANCO);
            btnMetodoTarjeta.setBorderColor(null);

            btnMetodoEfectivo.setBackground(BLANCO);
            btnMetodoEfectivo.setForeground(GRIS);
            btnMetodoEfectivo.setBorderColor(BORDE);

            panelEfectivoDetails.setVisible(false);
        }
        calcularTotal();
        panelSouthCart.revalidate();
        panelSouthCart.repaint();
    }

    /**
     * Calcula los totales del carrito e IVA, y actualiza las etiquetas correspondientes.
     */
    private void calcularTotal() {
        double subtotal = 0;
        for (CarritoItem item : carrito) {
            subtotal += item.getSubtotal();
        }
        double iva = subtotal * 0.16;
        double total = subtotal + iva;

        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblIva.setText(String.format("$%.2f", iva));
        lblTotal.setText(String.format("$%.2f", total));

        calcularCambio();
    }

    /**
     * Calcula el cambio correspondiente en base al monto recibido en efectivo.
     */
    private void calcularCambio() {
        if (!metodoPagoSeleccionado.equals("Efectivo")) {
            lblCambioValor.setText("$0.00");
            return;
        }

        try {
            double total = obtenerTotalNumerico();
            String recibidoTexto = txtMontoRecibido.getText().trim();
            if (recibidoTexto.isEmpty()) {
                lblCambioValor.setText("$0.00");
                return;
            }
            double recibido = Double.parseDouble(recibidoTexto);
            double cambio = recibido - total;
            if (cambio < 0) {
                lblCambioValor.setText("$0.00");
            } else {
                lblCambioValor.setText(String.format("$%.2f", cambio));
            }
        } catch (NumberFormatException e) {
            lblCambioValor.setText("$0.00");
        }
    }

    /**
     * Retorna el monto total numérico actual con IVA incluido.
     */
    private double obtenerTotalNumerico() {
        double subtotal = 0;
        for (CarritoItem item : carrito) {
            subtotal += item.getSubtotal();
        }
        return subtotal * 1.16;
    }

    /**
     * Agrega un producto seleccionado al carrito, validando disponibilidad de stock.
     */
    private void agregarAlCarrito(Producto prod) {
        if (prod.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, 
                "El producto " + prod.getNombre() + " no tiene stock disponible en almacén.", 
                "Aviso - Sin Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CarritoItem existente = null;
        for (CarritoItem item : carrito) {
            if (item.producto.getId() == prod.getId()) {
                existente = item;
                break;
            }
        }

        if (existente != null) {
            if (existente.cantidad + 1 > prod.getStock()) {
                JOptionPane.showMessageDialog(this, 
                    "No es posible agregar más unidades. El stock máximo disponible es: " + prod.getStock(), 
                    "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
            existente.cantidad++;
        } else {
            carrito.add(new CarritoItem(prod, 1));
        }

        actualizarVistaCarrito();
    }

    /**
     * Elimina un producto seleccionado del carrito de compras.
     */
    private void eliminarDelCarrito(Producto prod) {
        carrito.removeIf(item -> item.producto.getId() == prod.getId());
        actualizarVistaCarrito();
    }

    /**
     * Actualiza la cantidad de un producto específico en el carrito.
     */
    private void actualizarCantidad(Producto prod, int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            eliminarDelCarrito(prod);
            return;
        }

        if (nuevaCantidad > prod.getStock()) {
            JOptionPane.showMessageDialog(this, 
                "Cantidad supera el stock actual disponible en el almacén. Máximo: " + prod.getStock(), 
                "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (CarritoItem item : carrito) {
            if (item.producto.getId() == prod.getId()) {
                item.cantidad = nuevaCantidad;
                break;
            }
        }

        actualizarVistaCarrito();
    }

    /**
     * Vacía el carrito de compras y limpia todos los campos de cobro.
     */
    private void limpiarCarrito() {
        carrito.clear();
        txtMontoRecibido.setText("");
        actualizarVistaCarrito();
    }

    /**
     * Reconstruye visualmente la lista de items agregados al carrito de ventas.
     */
    private void actualizarVistaCarrito() {
        panelItemsCarrito.removeAll();

        for (CarritoItem item : carrito) {
            Producto prod = item.producto;

            RoundedPanel rowPanel = new RoundedPanel(8, BLANCO, new Color(243, 244, 246));
            rowPanel.setLayout(new GridBagLayout());
            rowPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            GridBagConstraints rc = new GridBagConstraints();
            rc.fill = GridBagConstraints.HORIZONTAL;
            rc.insets = new Insets(0, 4, 0, 4);

            // Nombre y Precio c/u
            rc.gridx = 0; rc.weightx = 0.40;
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel lblItemName = new JLabel(prod.getNombre());
            lblItemName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblItemName.setForeground(TEXTO_OSCURO);

            JLabel lblItemPrice = new JLabel(String.format("$%.2f c/u", prod.getPrecio()));
            lblItemPrice.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblItemPrice.setForeground(GRIS);

            infoPanel.add(lblItemName);
            infoPanel.add(lblItemPrice);
            rowPanel.add(infoPanel, rc);

            // Controles de cantidad [-] [1] [+]
            rc.gridx = 1; rc.weightx = 0.30;
            JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            qtyPanel.setOpaque(false);

            PastelButton btnMinus = new PastelButton("-", new Color(243, 244, 246), TEXTO_OSCURO, 8);
            btnMinus.setPreferredSize(new Dimension(22, 22));
            btnMinus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnMinus.addActionListener(e -> {
                if (item.cantidad > 1) {
                    actualizarCantidad(prod, item.cantidad - 1);
                } else {
                    eliminarDelCarrito(prod);
                }
            });

            JLabel lblQty = new JLabel(String.valueOf(item.cantidad), SwingConstants.CENTER);
            lblQty.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblQty.setPreferredSize(new Dimension(20, 20));

            PastelButton btnPlus = new PastelButton("+", new Color(243, 244, 246), TEXTO_OSCURO, 8);
            btnPlus.setPreferredSize(new Dimension(22, 22));
            btnPlus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnPlus.addActionListener(e -> actualizarCantidad(prod, item.cantidad + 1));

            qtyPanel.add(btnMinus);
            qtyPanel.add(lblQty);
            qtyPanel.add(btnPlus);
            rowPanel.add(qtyPanel, rc);

            // Subtotal
            rc.gridx = 2; rc.weightx = 0.20;
            JLabel lblSub = new JLabel(String.format("$%.2f", item.getSubtotal()), SwingConstants.RIGHT);
            lblSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblSub.setForeground(TEXTO_OSCURO);
            rowPanel.add(lblSub, rc);

            // Eliminar item individual 🗑️
            rc.gridx = 3; rc.weightx = 0.10;
            PastelButton btnTrash = new PastelButton("🗑️", BLANCO, CORAL, 6);
            btnTrash.setPreferredSize(new Dimension(26, 26));
            btnTrash.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnTrash.addActionListener(e -> eliminarDelCarrito(prod));
            rowPanel.add(btnTrash, rc);

            panelItemsCarrito.add(rowPanel);
            panelItemsCarrito.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        panelItemsCarrito.add(Box.createVerticalGlue());

        // Actualizar número en el badge
        int totalItems = 0;
        for (CarritoItem item : carrito) {
            totalItems += item.cantidad;
        }
        lblBadgeCount.setText(totalItems + (totalItems == 1 ? " item" : " items"));

        panelItemsCarrito.revalidate();
        panelItemsCarrito.repaint();

        calcularTotal();
    }

    /**
     * Recarga el panel del Grid de productos filtrando por categoría activa y barra de búsqueda.
     */
    private void actualizarGridProductos() {
        // Validar que el panel de productos no sea null antes de manipularlo
        if (panelGridProductos == null) return;
        panelGridProductos.removeAll();

        String query = txtBuscar.getText().trim().toLowerCase();
        List<Producto> productos = productoService.obtenerTodos().stream()
            .filter(p -> {
                boolean matchesCategory = categoriaSeleccionada.equalsIgnoreCase("Todos") 
                    || (p.getCategoria() != null && p.getCategoria().equalsIgnoreCase(categoriaSeleccionada));
                boolean matchesSearch = query.isEmpty() 
                    || (p.getNombre() != null && p.getNombre().toLowerCase().contains(query))
                    || (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(query));
                return matchesCategory && matchesSearch;
            })
            .collect(Collectors.toList());

        for (Producto prod : productos) {
            panelGridProductos.add(new ProductoCardPanel(prod));
        }

        // Rellenar con paneles transparentes invisibles si hay pocos items 
        // para que no se estiren de forma antiestética en el GridLayout
        int itemsCount = productos.size();
        if (itemsCount > 0 && itemsCount < 6) {
            int fillCount = 6 - itemsCount;
            for (int i = 0; i < fillCount; i++) {
                JPanel emptyPanel = new JPanel();
                emptyPanel.setOpaque(false);
                panelGridProductos.add(emptyPanel);
            }
        }

        panelGridProductos.revalidate();
        panelGridProductos.repaint();
    }

    /**
     * Procesa la venta total del carrito llamando al servicio, levantando 
     * el diálogo de facturación y limpiando el carrito.
     */
    private void procesarCobro() {
        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No hay productos en el carrito actual.", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String metodoPago = metodoPagoSeleccionado;
        double montoRecibido = 0;

        if (metodoPago.equals("Efectivo")) {
            try {
                montoRecibido = Double.parseDouble(txtMontoRecibido.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Por favor, ingresa el monto recibido en efectivo.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                txtMontoRecibido.requestFocusInWindow();
                return;
            }

            double total = obtenerTotalNumerico();
            if (montoRecibido < total) {
                JOptionPane.showMessageDialog(this, 
                    "El monto recibido es insuficiente para cubrir el total de la venta.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                txtMontoRecibido.requestFocusInWindow();
                return;
            }
        }

        int empleadoId = 1;
        if (SessionManager.getInstancia().getUsuarioActual() != null) {
            empleadoId = SessionManager.getInstancia().getUsuarioActual().getId();
        }

        double totalVenta = obtenerTotalNumerico();
        double cambio = metodoPago.equals("Efectivo") ? montoRecibido - totalVenta : 0;

        // Construir lista de DetalleVenta
        List<DetalleVenta> detalles = new ArrayList<>();
        for (CarritoItem item : carrito) {
            DetalleVenta d = new DetalleVenta();
            d.setProductoId(item.producto.getId());
            d.setCantidad(item.cantidad);
            d.setPrecioUnitario(item.producto.getPrecio());
            detalles.add(d);
        }

        List<DetalleVenta> detallesCopia = new ArrayList<>(detalles);

        // Guardar venta en la BD
        if (ventaService.procesarVenta(detalles, metodoPago, montoRecibido, empleadoId)) {
            // Construir modelo Venta para el PDF de facturación
            Venta ventaRegistrada = new Venta();
            int numVentas = ventaService.obtenerTodasLasVentas().size();
            ventaRegistrada.setFolio("VENTA-" + (numVentas));
            ventaRegistrada.setFecha(java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ventaRegistrada.setTotal(totalVenta);
            ventaRegistrada.setMetodoPago(metodoPago);
            ventaRegistrada.setMontoRecibido(metodoPago.equals("Efectivo") ? montoRecibido : totalVenta);
            ventaRegistrada.setCambio(cambio);
            ventaRegistrada.setEmpleadoId(empleadoId);

            // Desplegar diálogo post-venta
            mostrarDialogoFacturacion(ventaRegistrada, detallesCopia, totalVenta, cambio);

            // Resetear carrito y recargar productos (para actualizar stock en el Grid)
            limpiarCarrito();
            actualizarGridProductos();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Ocurrió un error inesperado al procesar la venta en la base de datos.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra el diálogo post-venta para imprimir, enviar por correo o guardar PDF de la factura.
     */
    private void mostrarDialogoFacturacion(Venta venta, List<DetalleVenta> detalles, double total, double cambio) {
        JPanel panelInfo = new JPanel(new BorderLayout(0, 8));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        JLabel lblTitulo = new JLabel("✅ Venta registrada exitosamente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(74, 185, 140));

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

        Object[] opciones = { "🖨️ Imprimir Factura", "📧 Enviar por Correo", "💾 Guardar PDF", "Cerrar" };

        int eleccion = JOptionPane.showOptionDialog(
                this,
                panelInfo,
                "Facturación — Folio: " + venta.getFolio(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[2]
        );

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
                case 0:
                    ImpresionService impresionService = new ImpresionService();
                    impresionService.imprimirPDF(rutaPDF);
                    break;

                case 1:
                    if (!ConfiguracionCorreo.estaConfigurado()) {
                        int resp = JOptionPane.showConfirmDialog(this,
                                "El correo SMTP no está configurado aún.\n¿Desea configurarlo ahora?",
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

                case 2:
                    JOptionPane.showMessageDialog(this,
                            "PDF guardado correctamente en:\n" + rutaPDF,
                            "PDF Guardado", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
        }
    }

    /**
     * Diálogo para ingresar el correo del cliente y enviar la factura.
     */
    private void enviarFacturaPorCorreo(Venta venta, String rutaPDF) {
        JPanel panelEmail = new JPanel(new GridLayout(2, 2, 8, 8));
        panelEmail.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTextField txtEmail = new JTextField(25);
        JTextField txtAsunto = new JTextField("Tu factura de NekoMart — Folio: " + venta.getFolio(), 25);

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

        String email = txtEmail.getText().trim();
        String asunto = txtAsunto.getText().trim();

        if (email.isEmpty() || !email.contains("@")) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, ingresa un correo electrónico válido.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

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
                                "❌ No se pudo enviar el correo.\nVerifica tu configuración SMTP.",
                                "Error de Envío", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(VentasFrame.this,
                            "Error inesperado al enviar: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        JOptionPane.showMessageDialog(this,
                "Enviando correo a " + email + "...\nEspera un momento.",
                "Enviando...", JOptionPane.INFORMATION_MESSAGE);
    }

    // ======================================================================
    // CLASES AUXILIARES DE RENDERIZADO VISUAL
    // ======================================================================

    /**
     * Panel con bordes redondeados y opción de un color de borde.
     */
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color borderCol;

        public RoundedPanel(int radius, Color bg) {
            this(radius, bg, null);
        }

        public RoundedPanel(int radius, Color bg, Color borderCol) {
            super();
            this.radius = radius;
            this.borderCol = borderCol;
            setBackground(bg);
            setOpaque(false);
        }

        public void setBorderColor(Color borderCol) {
            this.borderCol = borderCol;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (borderCol != null) {
                g2.setColor(borderCol);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
        }
    }

    /**
     * Botón circular perfecto, ideal para agregar productos.
     */
    private static class CircleButton extends JButton {
        public CircleButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(30, 30));
            setMinimumSize(new Dimension(30, 30));
            setMaximumSize(new Dimension(30, 30));
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /**
     * Botón de categoría con diseño de pastilla ovalada (Activo/Inactivo).
     */
    private static class CategoryButton extends JButton {
        private boolean active;

        public CategoryButton(String text, boolean active) {
            super(text);
            this.active = active;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            updateStyle();
        }

        public void setActive(boolean active) {
            this.active = active;
            updateStyle();
            repaint();
        }

        private void updateStyle() {
            if (active) {
                setBackground(LAVANDA);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else {
                setBackground(Color.WHITE);
                setForeground(GRIS);
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            if (!active) {
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }

            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /**
     * Botón pastel multiusos con bordes redondeados y estado de hover.
     */
    private static class PastelButton extends JButton {
        private final int arc;
        private Color borderColor = null;

        public PastelButton(String text, Color bg, Color fg, int arc) {
            super(text);
            this.arc = arc;
            setBackground(bg);
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        public void setBorderColor(Color border) {
            this.borderColor = border;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }

            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /**
     * Tarjeta visual para representar individualmente a un Producto.
     */
    private class ProductoCardPanel extends RoundedPanel {
        public ProductoCardPanel(Producto producto) {
            super(12, Color.WHITE, BORDE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            // Superior Derecha: Botón de agregar "+"
            JPanel panelSuperior = new JPanel(new BorderLayout());
            panelSuperior.setOpaque(false);

            CircleButton btnAdd = new CircleButton("+");
            btnAdd.setBackground(CORAL);
            btnAdd.addActionListener(e -> agregarAlCarrito(producto));
            panelSuperior.add(btnAdd, BorderLayout.EAST);
            add(panelSuperior, BorderLayout.NORTH);

            // Centro: Imagen del Producto o Placeholder
            JLabel lblImage = new JLabel();
            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
            lblImage.setVerticalAlignment(SwingConstants.CENTER);
            lblImage.setPreferredSize(new Dimension(100, 100));

            ImageIcon icon = null;
            if (producto.getImagenRuta() != null && !producto.getImagenRuta().trim().isEmpty()) {
                try {
                    File file = new File(producto.getImagenRuta());
                    if (file.exists()) {
                        icon = new ImageIcon(producto.getImagenRuta());
                    } else {
                        // Intentar rutas relativas
                        File relFile = new File("../" + producto.getImagenRuta());
                        if (relFile.exists()) {
                            icon = new ImageIcon(relFile.getAbsolutePath());
                        } else {
                            // Intentar ruta absoluta de workspace
                            File wsFile = new File("c:/Users/igeri/Desktop/SistemaNekoMart/" + producto.getImagenRuta());
                            if (wsFile.exists()) {
                                icon = new ImageIcon(wsFile.getAbsolutePath());
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Error al buscar imagen de " + producto.getNombre());
                }
            }

            if (icon != null) {
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                lblImage.setIcon(new ImageIcon(scaledImg));
            } else {
                // Placeholder gato neko
                lblImage.setFont(new Font("Segoe UI", Font.PLAIN, 40));
                lblImage.setText("🐱");
                lblImage.setForeground(LAVANDA);
                lblImage.setOpaque(true);
                lblImage.setBackground(new Color(243, 244, 246));
                lblImage.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true));
            }
            add(lblImage, BorderLayout.CENTER);

            // Abajo: Información del Producto (Nombre, Precio y Stock)
            JPanel panelInfo = new JPanel();
            panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
            panelInfo.setOpaque(false);
            panelInfo.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

            JLabel lblName = new JLabel(producto.getNombre());
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblName.setForeground(TEXTO_OSCURO);
            lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblPrice = new JLabel(String.format("$%.2f", producto.getPrecio()));
            lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblPrice.setForeground(CORAL);
            lblPrice.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblStock = new JLabel();
            lblStock.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblStock.setAlignmentX(Component.LEFT_ALIGNMENT);

            int stock = producto.getStock();
            if (stock > 10) {
                lblStock.setText("Stock: " + stock);
                lblStock.setForeground(new Color(74, 185, 140)); // Verde
            } else if (stock >= 5) {
                lblStock.setText("Stock: " + stock + " (Bajo)");
                lblStock.setForeground(new Color(245, 158, 11)); // Naranja
            } else {
                lblStock.setText("Stock: " + stock + " (Crítico)");
                lblStock.setForeground(STOCK_BAJO); // Rojo stock bajo (#FC8181)
            }

            panelInfo.add(lblName);
            panelInfo.add(Box.createRigidArea(new Dimension(0, 4)));
            panelInfo.add(lblPrice);
            panelInfo.add(Box.createRigidArea(new Dimension(0, 4)));
            panelInfo.add(lblStock);

            add(panelInfo, BorderLayout.SOUTH);

            // Doble clic agrega automáticamente al carrito
            MouseAdapter cardDoubleClickListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        agregarAlCarrito(producto);
                    }
                }
            };
            this.addMouseListener(cardDoubleClickListener);
            lblImage.addMouseListener(cardDoubleClickListener);
            panelInfo.addMouseListener(cardDoubleClickListener);
            lblName.addMouseListener(cardDoubleClickListener);
            lblPrice.addMouseListener(cardDoubleClickListener);
            lblStock.addMouseListener(cardDoubleClickListener);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        actualizarColores();
        // Solo actualizar estilos si los componentes han sido inicializados
        if (panelGridProductos != null) {
            actualizarComponentesEstilos();
        }
    }

    private void actualizarColores() {
        if (Main.isDarkMode) {
            FONDO = new Color(0x1E, 0x1E, 0x1E);
            BLANCO = new Color(0x2D, 0x2D, 0x2D);
            TEXTO_OSCURO = Color.WHITE;
            GRIS = new Color(170, 170, 170);
            BORDE = new Color(60, 60, 60);
        } else {
            FONDO = new Color(250, 250, 250);
            BLANCO = Color.WHITE;
            TEXTO_OSCURO = new Color(45, 55, 72);
            GRIS = new Color(113, 128, 150);
            BORDE = new Color(226, 232, 240);
        }
    }

    private void actualizarComponentesEstilos() {
        Color fondo = Main.isDarkMode ? new Color(0x1E, 0x1E, 0x1E) : new Color(250, 250, 250);
        Color card = Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE;
        Color text = Main.isDarkMode ? Color.WHITE : new Color(45, 55, 72);
        Color textGris = Main.isDarkMode ? new Color(170, 170, 170) : new Color(113, 128, 150);
        Color border = Main.isDarkMode ? new Color(60, 60, 60) : new Color(226, 232, 240);

        setBackground(fondo);

        if (txtBuscar != null) {
            txtBuscar.setBackground(Main.isDarkMode ? new Color(0x3D, 0x3D, 0x3D) : Color.WHITE);
            txtBuscar.setForeground(text);
            txtBuscar.setCaretColor(text);
            txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
            ));
        }
        if (txtMontoRecibido != null) {
            txtMontoRecibido.setBackground(Main.isDarkMode ? new Color(0x3D, 0x3D, 0x3D) : Color.WHITE);
            txtMontoRecibido.setForeground(text);
            txtMontoRecibido.setCaretColor(text);
            txtMontoRecibido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
            ));
        }

        if (lblSubtotal != null) lblSubtotal.setForeground(text);
        if (lblIva != null) lblIva.setForeground(text);
        if (lblTotal != null) lblTotal.setForeground(CORAL);
        if (lblCambioValor != null) lblCambioValor.setForeground(new Color(74, 185, 140));

        if (botonesCategorias != null) {
            for (CategoryButton btn : botonesCategorias) {
                btn.setActive(btn.active);
            }
        }

        if (btnMetodoEfectivo != null) {
            btnMetodoEfectivo.setBackground(metodoPagoSeleccionado.equals("Efectivo") ? LAVANDA : card);
            btnMetodoEfectivo.setForeground(metodoPagoSeleccionado.equals("Efectivo") ? Color.WHITE : textGris);
            btnMetodoEfectivo.setBorderColor(metodoPagoSeleccionado.equals("Efectivo") ? null : border);
        }
        if (btnMetodoTarjeta != null) {
            btnMetodoTarjeta.setBackground(metodoPagoSeleccionado.equals("Tarjeta") ? LAVANDA : card);
            btnMetodoTarjeta.setForeground(metodoPagoSeleccionado.equals("Tarjeta") ? Color.WHITE : textGris);
            btnMetodoTarjeta.setBorderColor(metodoPagoSeleccionado.equals("Tarjeta") ? null : border);
        }

        if (panelDerecho != null) {
            panelDerecho.setBackground(card);
            panelDerecho.setBorderColor(border);
            panelDerecho.repaint();
        }

        if (panelGridProductos != null) {
            panelGridProductos.setBackground(fondo);
        }

        actualizarViewports(this, card);

        // Volver a pintar catálogo de productos y carrito con los nuevos colores
        if (panelGridProductos != null) {
            actualizarGridProductos();
        }
        actualizarVistaCarrito();
    }

    private void actualizarViewports(Component comp, Color cardBg) {
        if (comp instanceof JScrollPane) {
            ((JScrollPane) comp).getViewport().setBackground(cardBg);
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                actualizarViewports(child, cardBg);
            }
        }
    }
}