package com.nekomart.ui;

import com.nekomart.models.Venta;
import com.nekomart.models.DetalleVenta;
import com.nekomart.models.Producto;
import com.nekomart.services.VentaService;
import com.nekomart.services.ProductoService;
import com.nekomart.models.Usuario;
import com.nekomart.services.UsuarioService;
import com.nekomart.utils.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel que muestra el historial de ventas realizadas.
 * Permite filtrar por fechas, ver detalles con doble clic y exportar datos a Excel (CSV).
 * Todo el código está comentado en español.
 */
public class HistorialVentasFrame extends JPanel {

    private VentaService ventaService;
    private ProductoService productoService;
    private JTable tablaVentas;
    private DefaultTableModel modeloVentas;

    // Componentes de filtros por fecha y empleado
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JComboBox<EmpleadoComboItem> cmbEmpleados;
    private JButton btnFiltrar;
    private JButton btnExportar;
    private JButton btnRefrescar;

    private final String[] COLS_VENTAS = { "Folio", "Fecha", "Total", "Método Pago", "Empleado ID" };

    public HistorialVentasFrame() {
        ventaService = new VentaService();
        productoService = new ProductoService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.FONDO_PRINCIPAL);

        initComponents();
        cargarVentas();
    }

    /**
     * Inicializa los componentes visuales del panel.
     */
    private void initComponents() {
        // Panel superior con filtros y botones de acción
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        panelSuperior.setOpaque(false);

        // Filtro de fecha inicio
        JLabel lblDesde = new JLabel("Desde (YYYY-MM-DD):");
        lblDesde.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDesde.setForeground(ThemeManager.GRIS_OSCURO);
        panelSuperior.add(lblDesde);

        txtFechaInicio = new JTextField(10);
        txtFechaInicio.putClientProperty("JTextField.placeholderText", "AAAA-MM-DD");
        txtFechaInicio.setText(LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        txtFechaInicio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtFechaInicio.setPreferredSize(new Dimension(120, 45));
        txtFechaInicio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.GRIS_CLARO, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panelSuperior.add(txtFechaInicio);

        // Filtro de fecha fin
        JLabel lblHasta = new JLabel("Hasta (YYYY-MM-DD):");
        lblHasta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHasta.setForeground(ThemeManager.GRIS_OSCURO);
        panelSuperior.add(lblHasta);

        txtFechaFin = new JTextField(10);
        txtFechaFin.putClientProperty("JTextField.placeholderText", "AAAA-MM-DD");
        txtFechaFin.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        txtFechaFin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtFechaFin.setPreferredSize(new Dimension(120, 45));
        txtFechaFin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.GRIS_CLARO, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panelSuperior.add(txtFechaFin);

        // Filtro por empleado
        JLabel lblEmpleado = new JLabel("Empleado:");
        lblEmpleado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmpleado.setForeground(ThemeManager.GRIS_OSCURO);
        panelSuperior.add(lblEmpleado);

        cmbEmpleados = new JComboBox<>();
        cmbEmpleados.setPreferredSize(new Dimension(180, 45));
        cmbEmpleados.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbEmpleados.setBackground(Color.WHITE);
        cmbEmpleados.setForeground(ThemeManager.GRIS_OSCURO);
        cmbEmpleados.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.GRIS_CLARO, 1, true),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        
        // Cargar empleados
        cmbEmpleados.addItem(new EmpleadoComboItem(-1, "Todos los empleados"));
        try {
            List<Usuario> empleados = new UsuarioService().obtenerTodos();
            for (Usuario emp : empleados) {
                cmbEmpleados.addItem(new EmpleadoComboItem(emp.getId(), emp.getNombreCompleto()));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar empleados en el filtro: " + e.getMessage());
        }
        panelSuperior.add(cmbEmpleados);

        // Botón de Filtrar
        btnFiltrar = new JButton("🔍 Filtrar");
        btnFiltrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFiltrar.setBackground(ThemeManager.AZUL_PRIMARIO);
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setBorderPainted(true);
        btnFiltrar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnFiltrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFiltrar.putClientProperty("JButton.buttonType", "roundRect");
        btnFiltrar.setToolTipText("Filtrar ventas por el rango de fechas especificado");
        panelSuperior.add(btnFiltrar);

        // Botón de Refrescar
        btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefrescar.setBackground(ThemeManager.AZUL_MUY_CLARO);
        btnRefrescar.setForeground(ThemeManager.GRIS_OSCURO);
        btnRefrescar.setBorderPainted(true);
        btnRefrescar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnRefrescar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefrescar.putClientProperty("JButton.buttonType", "roundRect");
        btnRefrescar.setToolTipText("Limpiar filtros y recargar el historial completo de ventas");
        panelSuperior.add(btnRefrescar);

        // Botón de Exportar
        btnExportar = new JButton("📥 Exportar a Excel");
        btnExportar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportar.setBackground(ThemeManager.AZUL_PRIMARIO);
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setBorderPainted(true);
        btnExportar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnExportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportar.putClientProperty("JButton.buttonType", "roundRect");
        btnExportar.setToolTipText("Exportar los datos actualmente mostrados a un archivo CSV compatible con Excel");
        panelSuperior.add(btnExportar);

        // Tabla de ventas
        modeloVentas = new DefaultTableModel(COLS_VENTAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaVentas = new JTable(modeloVentas);
        tablaVentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVentas.setAutoCreateRowSorter(true);
        tablaVentas.setRowHeight(48); // Fila más ancha y moderna (48px)
        tablaVentas.setShowGrid(false);
        tablaVentas.setIntercellSpacing(new Dimension(0, 0));
        tablaVentas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaVentas.setForeground(ThemeManager.GRIS_OSCURO);
        tablaVentas.setSelectionBackground(ThemeManager.AZUL_MUY_CLARO);
        tablaVentas.setSelectionForeground(ThemeManager.GRIS_OSCURO);

        // Estilo del header (fondo #F1F5F9, texto #1E293B 14px bold)
        JTableHeader header = tablaVentas.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(ThemeManager.GRIS_MUY_CLARO);
        header.setForeground(ThemeManager.GRIS_OSCURO);
        header.setPreferredSize(new Dimension(0, 48));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // Custom TableCellRenderer to paint alternating row backgrounds (#FFFFFF and #F8FAFC)
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(ThemeManager.AZUL_MUY_CLARO);
                    c.setForeground(ThemeManager.GRIS_OSCURO);
                } else {
                    c.setBackground(row % 2 == 0 ? ThemeManager.BLANCO : ThemeManager.FONDO_PRINCIPAL);
                    c.setForeground(ThemeManager.GRIS_OSCURO);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return c;
            }
        };
        for (int i = 0; i < tablaVentas.getColumnCount(); i++) {
            tablaVentas.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        // Panel contenedor con bordes redondeados para la tabla
        JPanel panelTabla = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(com.nekomart.Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(com.nekomart.Main.isDarkMode ? new Color(60, 60, 60) : ThemeManager.GRIS_CLARO);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panelTabla.setOpaque(false);
        panelTabla.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JScrollPane scrollPane = new JScrollPane(tablaVentas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panelTabla.add(scrollPane, BorderLayout.CENTER);

        // Eventos
        btnFiltrar.addActionListener(e -> filtrarVentas());
        btnRefrescar.addActionListener(e -> {
            // Limpiar inputs y recargar todo
            txtFechaInicio.setText(LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
            txtFechaFin.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            if (cmbEmpleados != null) {
                cmbEmpleados.setSelectedIndex(0);
            }
            cargarVentas();
        });
        btnExportar.addActionListener(e -> exportarAExcel());

        // Doble clic para ver detalles de la venta
        tablaVentas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    verDetallesVenta();
                }
            }
        });

        // Panel inferior con instrucciones
        JLabel lblHint = new JLabel("💡 Doble clic en una venta para inspeccionar sus artículos.", SwingConstants.LEFT);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(ThemeManager.GRIS_MEDIO);

        add(panelSuperior, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
        add(lblHint, BorderLayout.SOUTH);
    }

    /**
     * Carga todas las ventas desde el servicio sin filtros de fecha, pero respetando el filtro de empleado.
     */
    private void cargarVentas() {
        modeloVentas.setRowCount(0);
        int empleadoId = -1;
        if (cmbEmpleados != null && cmbEmpleados.getSelectedItem() != null) {
            empleadoId = ((EmpleadoComboItem) cmbEmpleados.getSelectedItem()).getId();
        }

        List<Venta> ventas = ventaService.obtenerTodasLasVentas(empleadoId);
        for (Venta v : ventas) {
            modeloVentas.addRow(new Object[] {
                    v.getFolio(), v.getFecha(),
                    String.format("$%.2f", v.getTotal()),
                    v.getMetodoPago(), v.getEmpleadoId()
            });
        }
    }

    /**
     * Filtra las ventas consultando la base de datos por un rango de fechas y empleado seleccionado.
     */
    private void filtrarVentas() {
        String inicio = txtFechaInicio.getText().trim();
        String fin = txtFechaFin.getText().trim();

        if (inicio.isEmpty() || fin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, especifica tanto la fecha de inicio como la de fin.",
                    "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación de formato YYYY-MM-DD
        if (!inicio.matches("\\d{4}-\\d{2}-\\d{2}") || !fin.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "El formato de fecha debe ser AAAA-MM-DD (ej: 2026-06-11)",
                    "Formato incorrecto", JOptionPane.ERROR_MESSAGE);
            return;
        }

        modeloVentas.setRowCount(0);
        int empleadoId = -1;
        if (cmbEmpleados != null && cmbEmpleados.getSelectedItem() != null) {
            empleadoId = ((EmpleadoComboItem) cmbEmpleados.getSelectedItem()).getId();
        }

        List<Venta> ventas = ventaService.obtenerVentasPorFecha(inicio, fin, empleadoId);
        for (Venta v : ventas) {
            modeloVentas.addRow(new Object[] {
                    v.getFolio(), v.getFecha(),
                    String.format("$%.2f", v.getTotal()),
                    v.getMetodoPago(), v.getEmpleadoId()
            });
        }
        
        if (modeloVentas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron ventas en el rango de fechas seleccionado.",
                    "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Exporta los datos actualmente visualizados en la tabla a un archivo CSV compatible con Excel.
     */
    private void exportarAExcel() {
        if (modeloVentas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay registros en la tabla para exportar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Ventas");
        fileChooser.setSelectedFile(new File("Reporte_Ventas_NekoMart_" + fechaHoy + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            // Validar extensión .csv
            if (!path.toLowerCase().endsWith(".csv")) {
                fileToSave = new File(path + ".csv");
            }

            try (FileWriter fw = new FileWriter(fileToSave);
                 BufferedWriter bw = new BufferedWriter(fw)) {

                // Escribir cabecera usando punto y coma como separador (estándar de Excel en español)
                bw.write("Folio;Fecha;Total;Metodo Pago;Empleado ID");
                bw.newLine();

                // Escribir las filas de datos
                for (int i = 0; i < modeloVentas.getRowCount(); i++) {
                    String folio = modeloVentas.getValueAt(i, 0).toString();
                    String fecha = modeloVentas.getValueAt(i, 1).toString();
                    // Limpiar el símbolo de moneda para Excel
                    String total = modeloVentas.getValueAt(i, 2).toString().replace("$", "").replace(",", "");
                    String metodo = modeloVentas.getValueAt(i, 3).toString();
                    String empleado = modeloVentas.getValueAt(i, 4).toString();

                    bw.write(String.format("%s;%s;%s;%s;%s", folio, fecha, total, metodo, empleado));
                    bw.newLine();
                }

                JOptionPane.showMessageDialog(this, "Reporte exportado exitosamente en:\n" + fileToSave.getAbsolutePath(),
                        "Exportación Completada", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ocurrió un error al intentar escribir el archivo:\n" + ex.getMessage(),
                        "Error de I/O", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra los detalles de la venta seleccionada en un diálogo modal estilizado.
     */
    private void verDetallesVenta() {
        int fila = tablaVentas.getSelectedRow();
        if (fila < 0)
            return;

        int modelRow = tablaVentas.convertRowIndexToModel(fila);
        String folio = (String) modeloVentas.getValueAt(modelRow, 0);
        Venta venta = ventaService.obtenerTodasLasVentas().stream()
                .filter(v -> v.getFolio().equals(folio))
                .findFirst().orElse(null);

        if (venta == null)
            return;

        List<DetalleVenta> detalles = ventaService.obtenerDetalles(venta.getId());

        // Crear panel contenedor con GridBagLayout
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(ThemeManager.FONDO_PRINCIPAL);

        String[] cols = { "Producto", "Cantidad", "Precio Unit.", "Subtotal" };
        DefaultTableModel modeloDetalles = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (DetalleVenta d : detalles) {
            Producto p = productoService.obtenerTodos().stream()
                    .filter(prod -> prod.getId() == d.getProductoId())
                    .findFirst().orElse(null);
            String nombre = p != null ? p.getNombre() : "Producto #" + d.getProductoId();
            double subtotal = d.getCantidad() * d.getPrecioUnitario();
            modeloDetalles.addRow(new Object[] {
                    nombre, d.getCantidad(),
                    String.format("$%.2f", d.getPrecioUnitario()),
                    String.format("$%.2f", subtotal)
            });
        }

        JTable tablaDetalles = new JTable(modeloDetalles);
        tablaDetalles.setRowHeight(36);
        tablaDetalles.setShowGrid(false);
        tablaDetalles.setIntercellSpacing(new Dimension(0, 0));
        tablaDetalles.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaDetalles.setForeground(ThemeManager.GRIS_OSCURO);

        JTableHeader detailsHeader = tablaDetalles.getTableHeader();
        detailsHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailsHeader.setBackground(ThemeManager.GRIS_MUY_CLARO);
        detailsHeader.setForeground(ThemeManager.GRIS_OSCURO);
        detailsHeader.setPreferredSize(new Dimension(0, 36));
        detailsHeader.setOpaque(true);
        detailsHeader.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer detailsRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? ThemeManager.BLANCO : ThemeManager.FONDO_PRINCIPAL);
                c.setForeground(ThemeManager.GRIS_OSCURO);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return c;
            }
        };
        for (int i = 0; i < tablaDetalles.getColumnCount(); i++) {
            tablaDetalles.getColumnModel().getColumn(i).setCellRenderer(detailsRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tablaDetalles);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.GRIS_CLARO, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(480, 250));

        JLabel lblHeader = new JLabel("Artículos de la venta: " + folio, SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeader.setForeground(ThemeManager.AZUL_PRIMARIO);

        JLabel lblFooter = new JLabel("Total Cobrado: $" + String.format("%.2f", venta.getTotal()), SwingConstants.RIGHT);
        lblFooter.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFooter.setForeground(ThemeManager.EXITO);

        panel.add(lblHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(lblFooter, BorderLayout.SOUTH);

        // Mostrar diálogo
        JOptionPane.showMessageDialog(this, panel, "Detalles de Venta", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Clase auxiliar para representar un Empleado en el combo box de filtrado.
     */
    private static class EmpleadoComboItem {
        private final int id;
        private final String nombre;

        public EmpleadoComboItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}