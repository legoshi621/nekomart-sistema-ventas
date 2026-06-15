package com.nekomart.ui;

import com.nekomart.dao.ProductoDAO;
import com.nekomart.models.Venta;
import com.nekomart.models.DetalleVenta;
import com.nekomart.models.Producto;
import com.nekomart.services.DevolucionService;
import com.nekomart.services.VentaService;
import com.nekomart.utils.SessionManager;
import com.nekomart.utils.ThemeManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo modal (JDialog) que recibe un idVenta y permite registrar las devoluciones
 * de los productos vendidos asociados a esa venta.
 * Todo el código está comentado y documentado en español.
 */
public class DevolucionesDialog extends JDialog {

    private final int idVenta;
    private final DevolucionService devolucionService;
    private final VentaService ventaService;

    // Componentes de la interfaz
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> cbMotivo;
    private JComboBox<String> cbReembolso;
    
    // Lista para asociar las filas de la tabla con los detalles de venta correspondientes
    private List<DetalleVenta> listaDetalles;
    private Venta ventaActual;

    /**
     * Constructor del diálogo de devoluciones.
     *
     * @param parent  El Frame padre del cual dependerá este JDialog modal.
     * @param idVenta El ID de la venta sobre la cual se aplicarán las devoluciones.
     */
    public DevolucionesDialog(Frame parent, int idVenta) {
        super(parent, "Devolución de Venta", true);
        this.idVenta = idVenta;
        this.devolucionService = new DevolucionService();
        this.ventaService = new VentaService();
        this.listaDetalles = new ArrayList<>();

        // Cargar los datos de la venta para poder poner el título correcto
        obtenerVentaInfo();

        if (ventaActual != null) {
            setTitle("Devolución de Venta #" + ventaActual.getFolio());
        }

        setSize(700, 500);
        setResizable(false);
        setLocationRelativeTo(parent);

        // Layout principal
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(ThemeManager.FONDO_PRINCIPAL);
        setContentPane(contentPane);

        initComponents();
        cargarItemsVenta();
    }

    /**
     * Obtiene el objeto Venta desde la base de datos para recuperar el folio y otros datos.
     */
    private void obtenerVentaInfo() {
        try {
            List<Venta> todas = ventaService.obtenerTodasLasVentas();
            for (Venta v : todas) {
                if (v.getId() == idVenta) {
                    ventaActual = v;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener información de la venta: " + e.getMessage());
        }
    }

    /**
     * Inicializa los componentes visuales del diálogo.
     */
    private void initComponents() {
        // --- 1. PANEL SUPERIOR (Información de la venta) ---
        JPanel panelSuperior = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.AZUL_MUY_CLARO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.AZUL_CLARO);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panelSuperior.setOpaque(false);
        panelSuperior.setLayout(new GridBagLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String infoTexto = "Cargando información de venta...";
        if (ventaActual != null) {
            infoTexto = String.format(
                "<html><body style='font-family:Segoe UI; font-size:11px;'>"
                + "<font color='#1E293B'><b>Folio:</b> %s &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b>Fecha:</b> %s &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b>Total:</b> <font color='#10B981'><b>$%s</b></font> &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "<b>Empleado ID:</b> %d</font>"
                + "</body></html>",
                ventaActual.getFolio(),
                ventaActual.getFecha(),
                String.format("%.2f", ventaActual.getTotal()),
                ventaActual.getEmpleadoId()
            );
        }

        JLabel lblInfo = new JLabel(infoTexto);
        lblInfo.setFont(ThemeManager.TEXTO_NORMAL);
        panelSuperior.add(lblInfo);
        add(panelSuperior, BorderLayout.NORTH);

        // --- 2. PANEL CENTRAL (Tabla de artículos) ---
        String[] columnas = { "Seleccionar", "Producto", "Cantidad Vendida", "Precio Unitario", "Subtotal", "Cantidad a Devolver" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                } else if (columnIndex == 2 || columnIndex == 5) {
                    return Integer.class;
                } else if (columnIndex == 3 || columnIndex == 4) {
                    return Double.class;
                }
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo son editables la columna de selección y la de cantidad a devolver
                return column == 0 || column == 5;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(48); // Row height 48px
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setFont(ThemeManager.TEXTO_NORMAL);
        tabla.setForeground(ThemeManager.GRIS_OSCURO);
        tabla.setSelectionBackground(ThemeManager.AZUL_MUY_CLARO);
        tabla.setSelectionForeground(ThemeManager.GRIS_OSCURO);

        // Estilo del Header
        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249)); // Header con color #F1F5F9
        header.setForeground(new Color(30, 41, 59));   // Texto #1E293B
        header.setPreferredSize(new Dimension(0, 48));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // Configurar renderers de las celdas
        configurarRenderersYEditores();

        // Contenedor de la tabla con bordes redondeados
        JPanel panelTabla = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.GRIS_CLARO);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panelTabla.setOpaque(false);
        panelTabla.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panelTabla.add(scrollPane, BorderLayout.CENTER);
        add(panelTabla, BorderLayout.CENTER);

        // --- 3. PANEL INFERIOR (Motivos, tipo de reembolso y acciones) ---
        JPanel panelInferior = new JPanel(new GridBagLayout());
        panelInferior.setOpaque(false);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label Motivo
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel lblMotivo = new JLabel("Motivo:");
        lblMotivo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMotivo.setForeground(ThemeManager.GRIS_OSCURO);
        panelInferior.add(lblMotivo, gbc);

        // JComboBox Motivo
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        cbMotivo = new JComboBox<>(new String[] { "Producto defectuoso", "Error en cobro", "Cambio de opinión", "Otro" });
        cbMotivo.setFont(ThemeManager.TEXTO_NORMAL);
        cbMotivo.setPreferredSize(new Dimension(180, 38));
        panelInferior.add(cbMotivo, gbc);

        // Label Reembolso
        gbc.gridx = 2;
        gbc.weightx = 0;
        JLabel lblReembolso = new JLabel("Tipo Reembolso:");
        lblReembolso.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblReembolso.setForeground(ThemeManager.GRIS_OSCURO);
        panelInferior.add(lblReembolso, gbc);

        // JComboBox Reembolso
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        cbReembolso = new JComboBox<>(new String[] { "EFECTIVO", "CREDITO" });
        cbReembolso.setFont(ThemeManager.TEXTO_NORMAL);
        cbReembolso.setPreferredSize(new Dimension(140, 38));
        panelInferior.add(cbReembolso, gbc);

        // Panel de Botones de Acción en fila inferior
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 15, 0));
        panelBotones.setOpaque(false);

        // Confirmar Devolución (background #1E88E5, texto blanco, height 45px)
        DevolucionButton btnConfirmar = new DevolucionButton("Confirmar Devolución", ThemeManager.AZUL_PRIMARIO, Color.WHITE, 10);
        btnConfirmar.setPreferredSize(new Dimension(0, 45));
        btnConfirmar.addActionListener(e -> procesarDevoluciones());

        // Cancelar (background #EF4444, texto blanco, height 45px)
        DevolucionButton btnCancelar = new DevolucionButton("Cancelar", ThemeManager.PELIGRO, Color.WHITE, 10);
        btnCancelar.setPreferredSize(new Dimension(0, 45));
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnConfirmar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(12, 0, 0, 0);
        panelInferior.add(panelBotones, gbc);

        add(panelInferior, BorderLayout.SOUTH);
    }

    /**
     * Aplica el renderizado y editores personalizados a las columnas.
     */
    private void configurarRenderersYEditores() {
        // Renderer para columnas de texto e imports monetarios
        DefaultTableCellRenderer standardRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Color de fondo alternado o de selección
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                } else {
                    setBackground(row % 2 == 0 ? ThemeManager.BLANCO : ThemeManager.FONDO_PRINCIPAL);
                    setForeground(ThemeManager.GRIS_OSCURO);
                }

                // Alinear y dar formato a los campos correspondientes
                if (column == 3 || column == 4) { // Precio unitario y Subtotal
                    if (value instanceof Double) {
                        setText(String.format("$%.2f", (Double) value));
                    }
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (column == 2) { // Cantidad vendida
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else { // Producto
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return this;
            }
        };

        // Asignar los renderers y editores correspondientes a cada columna
        TableColumnModel columnModel = tabla.getColumnModel();
        
        // Columna 0: CheckBox
        columnModel.getColumn(0).setCellRenderer(new CheckBoxRenderer());
        columnModel.getColumn(0).setPreferredWidth(90);

        // Columna 1: Producto
        columnModel.getColumn(1).setCellRenderer(standardRenderer);
        columnModel.getColumn(1).setPreferredWidth(210);

        // Columna 2: Cantidad Vendida
        columnModel.getColumn(2).setCellRenderer(standardRenderer);
        columnModel.getColumn(2).setPreferredWidth(110);

        // Columna 3: Precio Unitario
        columnModel.getColumn(3).setCellRenderer(standardRenderer);
        columnModel.getColumn(3).setPreferredWidth(110);

        // Columna 4: Subtotal
        columnModel.getColumn(4).setCellRenderer(standardRenderer);
        columnModel.getColumn(4).setPreferredWidth(110);

        // Columna 5: Cantidad a Devolver
        columnModel.getColumn(5).setCellRenderer(new SpinnerRenderer());
        columnModel.getColumn(5).setCellEditor(new SpinnerEditor());
        columnModel.getColumn(5).setPreferredWidth(150);
    }

    /**
     * Consulta el detalle de venta y productos asociados de la venta actual
     * para poblarlos en el JTable del diálogo.
     */
    private void cargarItemsVenta() {
        try {
            listaDetalles = ventaService.obtenerDetalles(idVenta);
            modeloTabla.setRowCount(0);
            ProductoDAO productoDAO = new ProductoDAO();

            for (DetalleVenta d : listaDetalles) {
                Producto prod = productoDAO.buscarPorId(d.getProductoId());
                String nombreProd = (prod != null) ? prod.getNombre() : "Producto #" + d.getProductoId();
                double subtotal = d.getCantidad() * d.getPrecioUnitario();

                modeloTabla.addRow(new Object[] {
                    Boolean.FALSE,        // Seleccionar (CheckBox)
                    nombreProd,           // Producto (String)
                    d.getCantidad(),      // Cantidad Vendida (Integer)
                    d.getPrecioUnitario(),// Precio Unitario (Double)
                    subtotal,             // Subtotal (Double)
                    0                     // Cantidad a Devolver (Integer)
                });
            }
        } catch (Exception e) {
            System.err.println("Error al cargar los ítems de la venta: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Ocurrió un error al intentar cargar los artículos de la venta.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Verifica que haya al menos un ítem marcado y que tenga una cantidad a devolver
     * mayor a cero.
     *
     * @return true si la selección es válida, false de lo contrario.
     */
    private boolean validarSeleccion() {
        int itemsParaDevolver = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Boolean seleccionado = (Boolean) modeloTabla.getValueAt(i, 0);
            if (seleccionado != null && seleccionado) {
                Integer cant = (Integer) modeloTabla.getValueAt(i, 5);
                if (cant != null && cant > 0) {
                    itemsParaDevolver++;
                }
            }
        }
        return itemsParaDevolver > 0;
    }

    /**
     * Itera los artículos marcados por el usuario y los procesa en la capa de negocio
     * a través de `DevolucionService`.
     */
    private void procesarDevoluciones() {
        // Guardar cualquier edición actual en la tabla si está editándose
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }

        // Validar que se haya marcado al menos una fila con cantidad mayor a 0
        if (!validarSeleccion()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione al menos un artículo marcando la casilla de 'Seleccionar' y asigne una cantidad a devolver mayor a 0.", 
                "Selección inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener el ID del administrador actual desde SessionManager (o 1 por defecto)
        int idAdmin = 1;
        if (SessionManager.getInstancia().getUsuarioActual() != null) {
            idAdmin = SessionManager.getInstancia().getUsuarioActual().getId();
        }

        String motivo = (String) cbMotivo.getSelectedItem();
        String tipoReembolso = (String) cbReembolso.getSelectedItem();

        boolean exitoCompleto = true;
        int cantidadProcesada = 0;

        for (int i = 0; i < tabla.getRowCount(); i++) {
            int modelRow = tabla.convertRowIndexToModel(i);
            Boolean seleccionado = (Boolean) modeloTabla.getValueAt(modelRow, 0);

            if (seleccionado != null && seleccionado) {
                Integer cantidad = (Integer) modeloTabla.getValueAt(modelRow, 5);
                if (cantidad != null && cantidad > 0) {
                    DetalleVenta d = listaDetalles.get(modelRow);

                    // Registrar devolución de este artículo
                    boolean resultado = devolucionService.procesarDevolucion(
                        idVenta,
                        d.getId(),
                        cantidad,
                        motivo,
                        tipoReembolso,
                        idAdmin
                    );

                    if (resultado) {
                        cantidadProcesada++;
                    } else {
                        exitoCompleto = false;
                    }
                }
            }
        }

        if (exitoCompleto && cantidadProcesada > 0) {
            JOptionPane.showMessageDialog(this, 
                "¡Devolución registrada exitosamente!\nSe reintegró el stock de los productos correspondientes.", 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else if (cantidadProcesada > 0) {
            JOptionPane.showMessageDialog(this, 
                "Se procesaron algunas devoluciones, pero hubo errores en otras. Revise la consola para más detalles.", 
                "Advertencia - Error parcial", JOptionPane.WARNING_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Ocurrió un error al intentar procesar la devolución. Intente nuevamente.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======================================================================
    // CLASES AUXILIARES DE RENDERIZADO VISUAL
    // ======================================================================

    /**
     * Botón con esquinas redondeadas y tipografía moderna adaptado para el JDialog.
     */
    private static class DevolucionButton extends JButton {
        private final int arc;

        public DevolucionButton(String text, Color bg, Color fg, int arc) {
            super(text);
            this.arc = arc;
            setBackground(bg);
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

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
     * Renderer de casilla de verificación (JCheckBox) que aplica
     * los colores de filas alternados y el color de selección de la JTable.
     */
    private static class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
        public CheckBoxRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            setBorder(null);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Boolean) {
                setSelected((Boolean) value);
            } else {
                setSelected(false);
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(row % 2 == 0 ? ThemeManager.BLANCO : ThemeManager.FONDO_PRINCIPAL);
                setForeground(ThemeManager.GRIS_OSCURO);
            }
            return this;
        }
    }

    /**
     * Renderer del control numérico (JSpinner) que aplica
     * los colores de filas alternados y sincroniza los límites numéricos.
     */
    private static class SpinnerRenderer extends JSpinner implements TableCellRenderer {
        public SpinnerRenderer() {
            super(new SpinnerNumberModel(0, 0, 100, 1));
            setBorder(null);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            int maxVal = (Integer) table.getModel().getValueAt(modelRow, 2);
            int currentVal = 0;
            if (value instanceof Integer) {
                currentVal = (Integer) value;
            }
            setModel(new SpinnerNumberModel(currentVal, 0, maxVal, 1));

            Color bg = isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? ThemeManager.BLANCO : ThemeManager.FONDO_PRINCIPAL);
            setBackground(bg);
            if (getEditor() instanceof DefaultEditor) {
                ((DefaultEditor) getEditor()).getTextField().setBackground(bg);
                ((DefaultEditor) getEditor()).getTextField().setForeground(ThemeManager.GRIS_OSCURO);
                ((DefaultEditor) getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
            }
            return this;
        }
    }

    /**
     * Editor de celda personalizado que utiliza un JSpinner
     * limitando el valor máximo a la cantidad originalmente vendida.
     */
    private static class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
        private final JSpinner spinner;

        public SpinnerEditor() {
            spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
            spinner.setBorder(null);
            if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            int maxVal = (Integer) table.getModel().getValueAt(modelRow, 2);
            
            int currentVal = 0;
            if (value instanceof Integer) {
                currentVal = (Integer) value;
            }
            
            spinner.setModel(new SpinnerNumberModel(currentVal, 0, maxVal, 1));
            return spinner;
        }

        @Override
        public Object getCellEditorValue() {
            try {
                spinner.commitEdit();
            } catch (Exception e) {
                // Si hay error al procesar la entrada de texto manual, omitir.
            }
            return spinner.getValue();
        }
    }
}
