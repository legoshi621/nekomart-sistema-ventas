package com.nekomart.ui;

import com.nekomart.models.Venta;
import com.nekomart.models.DetalleVenta;
import com.nekomart.models.Producto;
import com.nekomart.services.VentaService;
import com.nekomart.services.ProductoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    // Componentes de filtros por fecha
    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JButton btnFiltrar;
    private JButton btnExportar;
    private JButton btnRefrescar;

    private final String[] COLS_VENTAS = { "Folio", "Fecha", "Total", "Método Pago", "Empleado ID" };

    public HistorialVentasFrame() {
        ventaService = new VentaService();
        productoService = new ProductoService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        cargarVentas();
    }

    /**
     * Inicializa los componentes visuales del panel.
     */
    private void initComponents() {
        // Panel superior con filtros y botones de acción
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));

        // Filtro de fecha inicio
        panelSuperior.add(new JLabel("Desde (YYYY-MM-DD):"));
        txtFechaInicio = new JTextField(8);
        txtFechaInicio.putClientProperty("JTextField.placeholderText", "AAAA-MM-DD");
        // Inicializar con la fecha de hace 7 días por conveniencia
        txtFechaInicio.setText(LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        panelSuperior.add(txtFechaInicio);

        // Filtro de fecha fin
        panelSuperior.add(new JLabel("Hasta (YYYY-MM-DD):"));
        txtFechaFin = new JTextField(8);
        txtFechaFin.putClientProperty("JTextField.placeholderText", "AAAA-MM-DD");
        // Inicializar con la fecha de hoy
        txtFechaFin.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        panelSuperior.add(txtFechaFin);

        // Botón de Filtrar
        btnFiltrar = new JButton("🔍 Filtrar");
        btnFiltrar.setToolTipText("Filtrar ventas por el rango de fechas especificado");
        btnFiltrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelSuperior.add(btnFiltrar);

        // Botón de Refrescar
        btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.setToolTipText("Limpiar filtros y recargar el historial completo de ventas");
        btnRefrescar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelSuperior.add(btnRefrescar);

        // Botón de Exportar
        btnExportar = new JButton("📥 Exportar a Excel");
        btnExportar.setToolTipText("Exportar los datos actualmente mostrados a un archivo CSV compatible con Excel");
        btnExportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExportar.putClientProperty("JButton.buttonType", "default");
        panelSuperior.add(btnExportar);

        // Tabla de ventas
        modeloVentas = new DefaultTableModel(COLS_VENTAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaVentas = new JTable(modeloVentas);
        tablaVentas.setRowHeight(28); // Fila más ancha y moderna
        
        // Estilos alternados de FlatLaf
        tablaVentas.putClientProperty("JTable.alternateRowColor", true);
        tablaVentas.putClientProperty("JTable.showHorizontalLines", true);
        tablaVentas.putClientProperty("JTable.showVerticalLines", false);

        // Eventos
        btnFiltrar.addActionListener(e -> filtrarVentas());
        btnRefrescar.addActionListener(e -> {
            // Limpiar inputs y recargar todo
            txtFechaInicio.setText(LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
            txtFechaFin.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
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
        lblHint.setForeground(Color.GRAY);

        add(panelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tablaVentas), BorderLayout.CENTER);
        add(lblHint, BorderLayout.SOUTH);
    }

    /**
     * Carga todas las ventas desde el servicio sin filtros.
     */
    private void cargarVentas() {
        modeloVentas.setRowCount(0);
        List<Venta> ventas = ventaService.obtenerTodasLasVentas();
        for (Venta v : ventas) {
            modeloVentas.addRow(new Object[] {
                    v.getFolio(), v.getFecha(),
                    String.format("$%.2f", v.getTotal()),
                    v.getMetodoPago(), v.getEmpleadoId()
            });
        }
    }

    /**
     * Filtra las ventas consultando la base de datos por un rango de fechas.
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
        List<Venta> ventas = ventaService.obtenerVentasPorFecha(inicio, fin);
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

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Ventas");
        fileChooser.setSelectedFile(new File("Reporte_NekoMart_Ventas.csv"));

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
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        tablaDetalles.setRowHeight(26);
        tablaDetalles.putClientProperty("JTable.alternateRowColor", true);
        tablaDetalles.putClientProperty("JTable.showHorizontalLines", true);
        tablaDetalles.putClientProperty("JTable.showVerticalLines", false);

        JScrollPane scrollPane = new JScrollPane(tablaDetalles);
        scrollPane.setPreferredSize(new Dimension(450, 250));

        JLabel lblHeader = new JLabel("Artículos de la venta: " + folio, SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeader.setForeground(new Color(30, 58, 138));

        JLabel lblFooter = new JLabel("Total Cobrado: $" + String.format("%.2f", venta.getTotal()), SwingConstants.RIGHT);
        lblFooter.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFooter.setForeground(new Color(22, 163, 74));

        panel.add(lblHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(lblFooter, BorderLayout.SOUTH);

        // Mostrar diálogo
        JOptionPane.showMessageDialog(this, panel, "Detalles de Venta", JOptionPane.PLAIN_MESSAGE);
    }
}