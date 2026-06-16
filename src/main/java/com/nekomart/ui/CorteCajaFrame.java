package com.nekomart.ui;

import com.nekomart.models.CorteCaja;
import com.nekomart.models.Usuario;
import com.nekomart.services.CorteCajaService;
import com.nekomart.utils.SessionManager;
import com.nekomart.utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panel de administración para el módulo de Corte de Caja de NekoMart.
 * Permite realizar aperturas de caja, cierres transaccionales y ver el historial de cortes.
 * Utiliza la paleta de colores y componentes del sistema NekoMart.
 * Todo el código está comentado en español.
 */
public class CorteCajaFrame extends JPanel {

    // ── Colores de la paleta centralizada usando ThemeManager ────────────────
    private static final Color LAVANDA = ThemeManager.AZUL_PRIMARIO;
    private static final Color FONDO = ThemeManager.FONDO_PRINCIPAL;
    private static final Color TEXTO_OSCURO = ThemeManager.GRIS_OSCURO;
    private static final Color TEXTO_GRIS = ThemeManager.GRIS_MEDIO;
    private static final Color BORDE = ThemeManager.GRIS_CLARO;
    private static final Color FILA_ALTERNA = ThemeManager.FONDO_PRINCIPAL;
    private static final Color HOVER_LAVANDA = ThemeManager.AZUL_MUY_CLARO;
    private static final Color BLANCO = ThemeManager.BLANCO;

    // ── Servicios y Componentes ───────────────────────────────────────────
    private final CorteCajaService corteCajaService;
    private CorteCaja corteActual;

    private JPanel panelEstadoActual;
    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;

    // Columnas de la tabla
    private final String[] COLUMNAS = {
            "ID", "Fecha Apertura", "Fecha Cierre", "Monto Inicial", "Monto Esperado", "Monto Real", "Diferencia", "Estado"
    };

    /**
     * Constructor del panel. Inicializa servicios, layout y componentes.
     */
    public CorteCajaFrame() {
        this.corteCajaService = new CorteCajaService();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(FONDO);

        initComponents();
        cargarEstado();
    }

    /**
     * Inicializa la estructura visual principal.
     */
    private void initComponents() {
        // ── Panel Superior Dinámico (Apertura o Cierre) ────────────────────
        panelEstadoActual = new JPanel(new BorderLayout());
        panelEstadoActual.setOpaque(false);
        add(panelEstadoActual, BorderLayout.NORTH);

        // ── Panel Inferior (Historial de Cortes) ───────────────────────────
        JPanel panelHistorial = new JPanel(new BorderLayout(5, 5));
        panelHistorial.setOpaque(false);

        JLabel lblTituloHistorial = new JLabel("📋 Historial de Cortes de Caja");
        lblTituloHistorial.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTituloHistorial.setForeground(TEXTO_OSCURO);
        lblTituloHistorial.setBorder(new EmptyBorder(0, 0, 5, 0));
        panelHistorial.add(lblTituloHistorial, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editable
            }
        };

        tablaHistorial = new JTable(modeloTabla);
        tablaHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaHistorial.setRowHeight(48);
        tablaHistorial.setShowGrid(false);
        tablaHistorial.setIntercellSpacing(new Dimension(0, 0));
        tablaHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaHistorial.setForeground(TEXTO_OSCURO);
        tablaHistorial.setSelectionBackground(HOVER_LAVANDA);
        tablaHistorial.setSelectionForeground(TEXTO_OSCURO);

        // Estilo del header (fondo #F1F5F9, texto #1E293B 14px bold)
        JTableHeader header = tablaHistorial.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(ThemeManager.GRIS_MUY_CLARO);
        header.setForeground(ThemeManager.GRIS_OSCURO);
        header.setPreferredSize(new Dimension(0, 48));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // Configurar renderers para alineación y coloreado de columnas
        CorteCajaCellRenderer rendererGeneral = new CorteCajaCellRenderer();
        for (int i = 0; i < tablaHistorial.getColumnCount(); i++) {
            tablaHistorial.getColumnModel().getColumn(i).setCellRenderer(rendererGeneral);
        }

        // Panel contenedor de la tabla con bordes redondeados
        JPanel panelTablaWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panelTablaWrapper.setOpaque(false);
        panelTablaWrapper.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JScrollPane scrollPane = new JScrollPane(tablaHistorial);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panelTablaWrapper.add(scrollPane, BorderLayout.CENTER);

        panelHistorial.add(panelTablaWrapper, BorderLayout.CENTER);
        add(panelHistorial, BorderLayout.CENTER);
    }

    /**
     * Consulta el estado de caja y recarga los paneles correspondientes.
     */
    public void cargarEstado() {
        corteActual = corteCajaService.obtenerCorteAbierto();

        panelEstadoActual.removeAll();

        if (corteActual == null) {
            panelEstadoActual.add(crearPanelApertura(), BorderLayout.CENTER);
        } else {
            panelEstadoActual.add(crearPanelCierre(), BorderLayout.CENTER);
        }

        panelEstadoActual.revalidate();
        panelEstadoActual.repaint();

        cargarHistorial();
    }

    /**
     * Crea la vista del formulario para la Apertura de Caja.
     */
    private JPanel crearPanelApertura() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título de la tarjeta
        JLabel lblTitulo = new JLabel("💵 Apertura de Caja");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(LAVANDA);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Subtítulo instructivo
        JLabel lblSub = new JLabel("La caja se encuentra CERRADA. Ingrese el monto inicial para comenzar el turno.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXTO_GRIS);
        gbc.gridy = 1;
        panel.add(lblSub, gbc);

        // Campo de entrada
        JLabel lblMonto = new JLabel("Monto Inicial en Efectivo ($):");
        lblMonto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMonto.setForeground(TEXTO_OSCURO);
        gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(lblMonto, gbc);

        JTextField txtMonto = new JTextField(12);
        txtMonto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtMonto.setHorizontalAlignment(JTextField.RIGHT);
        txtMonto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        gbc.gridx = 1;
        panel.add(txtMonto, gbc);

        // Botón abrir
        JButton btnAbrir = crearBotonPastel("🔑 Abrir Caja", ThemeManager.AZUL_PRIMARIO, Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnAbrir, gbc);

        // Evento botón abrir
        btnAbrir.addActionListener(e -> {
            String txt = txtMonto.getText().trim();
            if (txt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor ingrese un monto inicial.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double monto = Double.parseDouble(txt);
                if (monto < 0) {
                    JOptionPane.showMessageDialog(this, "El monto inicial no puede ser negativo.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
                int adminId = usuario != null ? usuario.getId() : 1; // ID 1 por defecto

                if (corteCajaService.abrirCaja(adminId, monto)) {
                    JOptionPane.showMessageDialog(this, "Caja abierta correctamente con " + formatearMoneda(monto) + ".",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarEstado();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo abrir la caja.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Monto inicial inválido. Ingrese un valor numérico.",
                        "Validación", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    /**
     * Crea la vista del formulario para el Cierre de Caja en progreso.
     */
    private JPanel crearPanelCierre() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título de la sección
        JLabel lblTitulo = new JLabel("💵 Cierre de Caja (Turno Activo)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(ThemeManager.PELIGRO);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        panel.add(lblTitulo, gbc);

        // Subtítulo informativo
        JLabel lblSub = new JLabel("Caja abierta en la fecha: " + corteActual.getFechaApertura());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXTO_GRIS);
        gbc.gridy = 1;
        panel.add(lblSub, gbc);

        // Fila 2: Monto Inicial e Monto Esperado
        gbc.gridwidth = 1;

        JLabel lblMontoInicialText = new JLabel("Monto Inicial:");
        lblMontoInicialText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMontoInicialText.setForeground(TEXTO_OSCURO);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblMontoInicialText, gbc);

        JTextField txtMontoInicial = new JTextField(formatearMoneda(corteActual.getMontoInicial()), 10);
        txtMontoInicial.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMontoInicial.setEditable(false);
        txtMontoInicial.setHorizontalAlignment(JTextField.RIGHT);
        txtMontoInicial.setBackground(ThemeManager.GRIS_MUY_CLARO);
        txtMontoInicial.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        panel.add(txtMontoInicial, gbc);

        // Calcular ventas acumuladas actuales
        String fechaFinActual = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        double totalVentas = corteCajaService.obtenerVentasPeriodo(corteActual.getFechaApertura(), fechaFinActual);
        corteActual.setMontoEsperado(totalVentas); // Guardar temporalmente en el bean

        JLabel lblMontoEsperadoText = new JLabel("Ventas del Periodo:");
        lblMontoEsperadoText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMontoEsperadoText.setForeground(TEXTO_OSCURO);
        gbc.gridx = 2;
        panel.add(lblMontoEsperadoText, gbc);

        JTextField txtMontoEsperado = new JTextField(formatearMoneda(totalVentas), 10);
        txtMontoEsperado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMontoEsperado.setEditable(false);
        txtMontoEsperado.setHorizontalAlignment(JTextField.RIGHT);
        txtMontoEsperado.setBackground(ThemeManager.GRIS_MUY_CLARO);
        txtMontoEsperado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 3;
        panel.add(txtMontoEsperado, gbc);

        // Fila 3: Monto Real (Editable) y Diferencia
        JLabel lblMontoRealText = new JLabel("Efectivo Real Contado ($):");
        lblMontoRealText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMontoRealText.setForeground(TEXTO_OSCURO);
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(lblMontoRealText, gbc);

        JTextField txtMontoReal = new JTextField(10);
        txtMontoReal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtMontoReal.setHorizontalAlignment(JTextField.RIGHT);
        txtMontoReal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LAVANDA, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        panel.add(txtMontoReal, gbc);

        JLabel lblDiferenciaText = new JLabel("Diferencia:");
        lblDiferenciaText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDiferenciaText.setForeground(TEXTO_OSCURO);
        gbc.gridx = 2;
        panel.add(lblDiferenciaText, gbc);

        JLabel lblDiferencia = new JLabel("—", SwingConstants.CENTER);
        lblDiferencia.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDiferencia.setOpaque(true);
        lblDiferencia.setBackground(ThemeManager.GRIS_MUY_CLARO);
        lblDiferencia.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        gbc.gridx = 3;
        panel.add(lblDiferencia, gbc);

        // Escucha en tiempo real de la diferencia al escribir el monto real
        txtMontoReal.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarDiff();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarDiff();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarDiff();
            }

            private void actualizarDiff() {
                SwingUtilities.invokeLater(() -> {
                    String texto = txtMontoReal.getText().trim();
                    if (texto.isEmpty()) {
                        lblDiferencia.setText("—");
                        lblDiferencia.setForeground(TEXTO_OSCURO);
                        return;
                    }

                    try {
                        double montoRealVal = Double.parseDouble(texto);
                        double diff = montoRealVal - totalVentas; // Diferencia = Real - Esperado en ventas
                        lblDiferencia.setText(formatearMoneda(diff));
                        if (diff < 0) {
                            lblDiferencia.setForeground(ThemeManager.PELIGRO);
                        } else {
                            lblDiferencia.setForeground(ThemeManager.EXITO);
                        }
                    } catch (NumberFormatException ex) {
                        lblDiferencia.setText("Error");
                        lblDiferencia.setForeground(ThemeManager.PELIGRO);
                    }
                });
            }
        });

        // Fila 4: Botón Confirmar Cierre
        JButton btnCerrar = crearBotonPastel("🔒 Confirmar Cierre de Caja", ThemeManager.PELIGRO, Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 5, 10);
        panel.add(btnCerrar, gbc);

        // Evento botón cerrar
        btnCerrar.addActionListener(e -> {
            String txt = txtMontoReal.getText().trim();
            if (txt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor ingrese el monto real contado en caja.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double montoRealVal = Double.parseDouble(txt);
                if (montoRealVal < 0) {
                    JOptionPane.showMessageDialog(this, "El monto real no puede ser negativo.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int opcion = JOptionPane.showConfirmDialog(this,
                        "¿Está seguro de realizar el Cierre de Caja?\nEsta operación finalizará el turno actual.",
                        "Confirmar Cierre", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (opcion == JOptionPane.YES_OPTION) {
                    if (corteCajaService.cerrarCaja(corteActual.getId(), montoRealVal)) {
                        JOptionPane.showMessageDialog(this, "Caja cerrada exitosamente.",
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        cargarEstado();
                    } else {
                        JOptionPane.showMessageDialog(this, "Ocurrió un error al cerrar la caja.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Monto real inválido. Ingrese un valor numérico.",
                        "Validación", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    /**
     * Carga el historial de todos los cortes registrados en la base de datos.
     */
    private void cargarHistorial() {
        modeloTabla.setRowCount(0);
        List<CorteCaja> cortes = corteCajaService.obtenerHistorial();

        for (CorteCaja c : cortes) {
            modeloTabla.addRow(new Object[]{
                    c.getId(),
                    c.getFechaApertura(),
                    c.getFechaCierre() != null ? c.getFechaCierre() : "—",
                    c.getMontoInicial(),
                    c.getMontoEsperado(),
                    c.getEstado().equals("ABIERTO") ? "—" : c.getMontoReal(),
                    c.getEstado().equals("ABIERTO") ? "—" : c.getDiferencia(),
                    c.getEstado()
            });
        }
    }

    /**
     * Formatea un valor double a un string de moneda local ($X,XXX.XX).
     */
    private String formatearMoneda(double valor) {
        return String.format("$%,.2f", valor);
    }

    /**
     * Crea un botón estilizado con colores pastel y bordes redondeados.
     */
    private JButton crearBotonPastel(String texto, Color bgColor, Color fgColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════
    // RENDERER PERSONALIZADO PARA LA TABLA DEL HISTORIAL
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Renderer personalizado para las celdas de la tabla de cortes.
     * Aplica alineaciones, formatos de moneda, y resalta colores para diferencias y estados.
     */
    private class CorteCajaCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int modelRow = table.convertRowIndexToModel(row);
            Object estadoObj = table.getModel().getValueAt(modelRow, 7);
            String estado = estadoObj != null ? estadoObj.toString() : "";

            // Formato alterno de fondos por filas
            if (isSelected) {
                c.setBackground(HOVER_LAVANDA);
                c.setForeground(TEXTO_OSCURO);
            } else {
                c.setBackground(row % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
                c.setForeground(TEXTO_OSCURO);
            }

            // Alineación por defecto
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            // Formatear valores numéricos de dinero
            if (column >= 3 && column <= 6) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Double) {
                    double val = (Double) value;
                    setText(formatearMoneda(val));
                }
            }

            // Colorear columna de Diferencia (Columna 6)
            if (column == 6 && !estado.equals("ABIERTO")) {
                Object diffObj = table.getModel().getValueAt(modelRow, 6);
                if (diffObj instanceof Double) {
                    double diffVal = (Double) diffObj;
                    if (diffVal < 0) {
                        c.setForeground(ThemeManager.PELIGRO);
                    } else if (diffVal > 0) {
                        c.setForeground(ThemeManager.EXITO);
                    }
                }
            }

            // Colorear badge de Estado (Columna 7)
            if (column == 7) {
                if (estado.equalsIgnoreCase("ABIERTO")) {
                    c.setForeground(ThemeManager.EXITO);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    c.setForeground(TEXTO_GRIS);
                }
            }

            return c;
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        reaplicarTemaCorteCaja();
    }

    public void reaplicarTemaCorteCaja() {
        Color fondo = com.nekomart.Main.isDarkMode ? new Color(0x1E, 0x1E, 0x1E) : FONDO;
        Color card = com.nekomart.Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE;
        Color textClaro = com.nekomart.Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO;
        Color selectionBg = com.nekomart.Main.isDarkMode ? new Color(70, 60, 100) : HOVER_LAVANDA;
        Color border = com.nekomart.Main.isDarkMode ? new Color(60, 60, 60) : BORDE;

        setBackground(fondo);
        if (tablaHistorial != null) {
            tablaHistorial.setForeground(textClaro);
            tablaHistorial.setSelectionBackground(selectionBg);
            tablaHistorial.setSelectionForeground(com.nekomart.Main.isDarkMode ? Color.WHITE : ThemeManager.GRIS_OSCURO);
            
            JTableHeader header = tablaHistorial.getTableHeader();
            if (header != null) {
                header.setBackground(com.nekomart.Main.isDarkMode ? card : ThemeManager.GRIS_MUY_CLARO);
                header.setForeground(com.nekomart.Main.isDarkMode ? Color.WHITE : ThemeManager.GRIS_OSCURO);
            }
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
