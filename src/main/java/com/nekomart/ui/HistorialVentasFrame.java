package com.nekomart.ui;

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

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.nekomart.models.DetalleVenta;
import com.nekomart.models.Producto;
import com.nekomart.models.Usuario;
import com.nekomart.models.Venta;
import com.nekomart.services.ProductoService;
import com.nekomart.services.UsuarioService;
import com.nekomart.services.VentaService;
import com.nekomart.utils.SessionManager;
import com.nekomart.utils.ThemeManager;

public class HistorialVentasFrame extends JPanel {

    private VentaService ventaService;
    private ProductoService productoService;
    private UsuarioService usuarioService;
    private JTable tablaVentas;
    private DefaultTableModel modeloVentas;

    private JTextField txtFechaInicio;
    private JTextField txtFechaFin;
    private JComboBox<Usuario> cmbEmpleado;
    private JButton btnFiltrar;
    private JButton btnExportar;
    private JButton btnRefrescar;
    private JButton btnDevolver;
    private JPanel panelFiltroEmpleado;

    private final String[] COLS_VENTAS = { "ID", "Folio", "Fecha", "Total", "Método Pago", "Empleado" };

    public HistorialVentasFrame() {
        ventaService = new VentaService();
        productoService = new ProductoService();
        usuarioService = new UsuarioService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(ThemeManager.FONDO_PRINCIPAL);

        initComponents();
        cargarVentas();
    }
private void initComponents() {
    JPanel panelSuperior = new JPanel();
    panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
    panelSuperior.setOpaque(false);

    // Panel de filtros de fecha
    JPanel panelFechas = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
    panelFechas.setOpaque(false);

    JLabel lblDesde = new JLabel("Desde:");
    lblDesde.setFont(new Font("Segoe UI", Font.BOLD, 12));
    panelFechas.add(lblDesde);

    txtFechaInicio = new JTextField(10);
    txtFechaInicio.setText(LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
    txtFechaInicio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    txtFechaInicio.setPreferredSize(new Dimension(120, 35));
    panelFechas.add(txtFechaInicio);

    JLabel lblHasta = new JLabel("Hasta:");
    lblHasta.setFont(new Font("Segoe UI", Font.BOLD, 12));
    panelFechas.add(lblHasta);

    txtFechaFin = new JTextField(10);
    txtFechaFin.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    txtFechaFin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    txtFechaFin.setPreferredSize(new Dimension(120, 35));
    panelFechas.add(txtFechaFin);

    btnFiltrar = new JButton("🔍 Filtrar");
    btnFiltrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
    btnFiltrar.setBackground(ThemeManager.AZUL_PRIMARIO);
    btnFiltrar.setForeground(Color.WHITE);
    btnFiltrar.setFocusPainted(false);
    btnFiltrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    panelFechas.add(btnFiltrar);

    btnRefrescar = new JButton("🔄 Refrescar");
    btnRefrescar.setFont(new Font("Segoe UI", Font.BOLD, 12));
    btnRefrescar.setBackground(ThemeManager.AZUL_MUY_CLARO);
    btnRefrescar.setForeground(ThemeManager.GRIS_OSCURO);
    btnRefrescar.setFocusPainted(false);
    btnRefrescar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    panelFechas.add(btnRefrescar);

    btnExportar = new JButton("📥 Exportar");
    btnExportar.setFont(new Font("Segoe UI", Font.BOLD, 12));
    btnExportar.setBackground(ThemeManager.EXITO);
    btnExportar.setForeground(Color.WHITE);
    btnExportar.setFocusPainted(false);
    btnExportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
    panelFechas.add(btnExportar);

    btnDevolver = new JButton("🔄 Devolver");
    btnDevolver.setBackground(new Color(245, 158, 11));
    btnDevolver.setForeground(Color.WHITE);
    btnDevolver.setFont(new Font("Segoe UI", Font.BOLD, 12));
    btnDevolver.setFocusPainted(false);
    btnDevolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
    panelFechas.add(btnDevolver);

    panelSuperior.add(panelFechas);

    // Panel de filtro por empleado (solo visible para ADMIN)
    panelFiltroEmpleado = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
    panelFiltroEmpleado.setOpaque(false);

    JLabel lblEmpleado = new JLabel("Empleado:");
    lblEmpleado.setFont(new Font("Segoe UI", Font.BOLD, 12));
    panelFiltroEmpleado.add(lblEmpleado);

    cmbEmpleado = new JComboBox<>();
    cmbEmpleado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    cmbEmpleado.setPreferredSize(new Dimension(200, 35));
    panelFiltroEmpleado.add(cmbEmpleado);

    panelSuperior.add(panelFiltroEmpleado);

    // Ocultar filtro de empleado si no es admin
    Usuario usuarioActual = SessionManager.getInstancia().getUsuarioActual();
    if (usuarioActual != null && !"ADMIN".equals(usuarioActual.getRol().toUpperCase())) {
        panelFiltroEmpleado.setVisible(false);
    }

    // ══════════════════════════════════════════════════════════════════
    // TABLA (DEBE IR ANTES DE cargarEmpleados)
    // ══════════════════════════════════════════════════════════════════
    modeloVentas = new DefaultTableModel(COLS_VENTAS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    tablaVentas = new JTable(modeloVentas);
    tablaVentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tablaVentas.setAutoCreateRowSorter(true);
    tablaVentas.setRowHeight(48);
    tablaVentas.setShowGrid(false);
    tablaVentas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    tablaVentas.setSelectionBackground(ThemeManager.AZUL_MUY_CLARO);

    tablaVentas.removeColumn(tablaVentas.getColumnModel().getColumn(0));

    JTableHeader header = tablaVentas.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    header.setBackground(ThemeManager.GRIS_MUY_CLARO);
    header.setForeground(ThemeManager.GRIS_OSCURO);
    header.setPreferredSize(new Dimension(0, 48));

    DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                c.setBackground(ThemeManager.AZUL_MUY_CLARO);
            } else {
                c.setBackground(row % 2 == 0 ? Color.WHITE : ThemeManager.FONDO_PRINCIPAL);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return c;
        }
    };
    for (int i = 0; i < tablaVentas.getColumnCount(); i++) {
        tablaVentas.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
    }

    JPanel panelTabla = new JPanel(new BorderLayout());
    panelTabla.setOpaque(false);
    JScrollPane scrollPane = new JScrollPane(tablaVentas);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    panelTabla.add(scrollPane, BorderLayout.CENTER);

    // ══════════════════════════════════════════════════════════════════
    // AHORA SÍ: Cargar empleados (después de crear modeloVentas)
    // ══════════════════════════════════════════════════════════════════
    cargarEmpleados();
    
    // AGREGAR LISTENER DESPUÉS DE CARGAR EMPLEADOS (para evitar el error)
    cmbEmpleado.addActionListener(e -> filtrarVentas());

    // Eventos
    btnFiltrar.addActionListener(e -> filtrarVentas());
    btnRefrescar.addActionListener(e -> {
        txtFechaInicio.setText(LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        txtFechaFin.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (cmbEmpleado.getItemCount() > 0) cmbEmpleado.setSelectedIndex(0);
        cargarVentas();
    });
    btnExportar.addActionListener(e -> exportarAExcel());
    btnDevolver.addActionListener(e -> {
        int fila = tablaVentas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una venta", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tablaVentas.convertRowIndexToModel(fila);
        int idVenta = (int) modeloVentas.getValueAt(modelRow, 0);
        DevolucionesDialog dialog = new DevolucionesDialog((Frame) SwingUtilities.getWindowAncestor(this), idVenta);
        dialog.setModal(true);
        dialog.setVisible(true);
        cargarVentas();
    });

    tablaVentas.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) verDetallesVenta();
        }
    });

    add(panelSuperior, BorderLayout.NORTH);
    add(panelTabla, BorderLayout.CENTER);
}

  private void cargarEmpleados() {
    cmbEmpleado.removeAllItems();
    
    // Opción "Todos"
    Usuario todos = new Usuario();
    todos.setId(-1);
    todos.setNombreCompleto("Todos los empleados");
    cmbEmpleado.addItem(todos);

    // Cargar empleados
    List<Usuario> empleados = usuarioService.obtenerTodos();
    for (Usuario u : empleados) {
        if ("EMPLEADO".equals(u.getRol().toUpperCase())) {
            cmbEmpleado.addItem(u);
        }
    }
    
    // ← AGREGAR: Renderer personalizado para mostrar solo el nombre
    cmbEmpleado.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Usuario) {
                Usuario u = (Usuario) value;
                setText(u.getNombreCompleto());
            }
            
            return this;
        }
    });
}

    private void cargarVentas() {
        modeloVentas.setRowCount(0);
        Usuario usuarioActual = SessionManager.getInstancia().getUsuarioActual();
        List<Venta> ventas;

        if (usuarioActual != null && "EMPLEADO".equals(usuarioActual.getRol().toUpperCase())) {
            // Empleado: solo sus ventas
            ventas = ventaService.obtenerTodasLasVentas(usuarioActual.getId());
        } else {
            // Admin: todas las ventas
            ventas = ventaService.obtenerTodasLasVentas();
        }

        for (Venta v : ventas) {
            String nombreEmpleado = obtenerNombreEmpleado(v.getEmpleadoId());
            modeloVentas.addRow(new Object[]{
                    v.getId(), v.getFolio(), v.getFecha(),
                    String.format("$%.2f", v.getTotal()),
                    v.getMetodoPago(), nombreEmpleado
            });
        }
    }

    private void filtrarVentas() {
        String inicio = txtFechaInicio.getText().trim();
        String fin = txtFechaFin.getText().trim();

        if (inicio.isEmpty() || fin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Especifica ambas fechas", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloVentas.setRowCount(0);
        Usuario usuarioActual = SessionManager.getInstancia().getUsuarioActual();
        Usuario empleadoSeleccionado = (Usuario) cmbEmpleado.getSelectedItem();
        int empleadoId = (empleadoSeleccionado != null) ? empleadoSeleccionado.getId() : -1;

        List<Venta> ventas;

        if (usuarioActual != null && "EMPLEADO".equals(usuarioActual.getRol().toUpperCase())) {
            // Empleado: solo sus ventas (ignorar el combo)
            ventas = ventaService.obtenerVentasPorFecha(inicio, fin, usuarioActual.getId());
        } else {
            // Admin: filtrar por empleado seleccionado
            ventas = ventaService.obtenerVentasPorFecha(inicio, fin, empleadoId);
        }

        for (Venta v : ventas) {
            String nombreEmpleado = obtenerNombreEmpleado(v.getEmpleadoId());
            modeloVentas.addRow(new Object[]{
                    v.getId(), v.getFolio(), v.getFecha(),
                    String.format("$%.2f", v.getTotal()),
                    v.getMetodoPago(), nombreEmpleado
            });
        }

        if (modeloVentas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron ventas", "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String obtenerNombreEmpleado(int empleadoId) {
        if (empleadoId <= 0) return "N/A";
        Usuario u = usuarioService.obtenerPorId(empleadoId);
        return u != null ? u.getNombreCompleto() : "Empleado #" + empleadoId;
    }

    private void exportarAExcel() {
        if (modeloVentas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay registros", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Reporte_Ventas.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".csv")) path += ".csv";

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
                bw.write("Folio;Fecha;Total;Metodo Pago;Empleado");
                bw.newLine();

                for (int i = 0; i < modeloVentas.getRowCount(); i++) {
                    bw.write(String.format("%s;%s;%s;%s;%s",
                            modeloVentas.getValueAt(i, 1),
                            modeloVentas.getValueAt(i, 2),
                            modeloVentas.getValueAt(i, 3).toString().replace("$", ""),
                            modeloVentas.getValueAt(i, 4),
                            modeloVentas.getValueAt(i, 5)));
                    bw.newLine();
                }

                JOptionPane.showMessageDialog(this, "Exportado a:\n" + path, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void verDetallesVenta() {
        int fila = tablaVentas.getSelectedRow();
        if (fila < 0) return;

        int modelRow = tablaVentas.convertRowIndexToModel(fila);
        String folio = (String) modeloVentas.getValueAt(modelRow, 1);
        Venta venta = ventaService.obtenerTodasLasVentas().stream()
                .filter(v -> v.getFolio().equals(folio))
                .findFirst().orElse(null);

        if (venta == null) return;

        List<DetalleVenta> detalles = ventaService.obtenerDetalles(venta.getId());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

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
            modeloDetalles.addRow(new Object[]{
                    nombre, d.getCantidad(),
                    String.format("$%.2f", d.getPrecioUnitario()),
                    String.format("$%.2f", subtotal)
            });
        }

        JTable tablaDetalles = new JTable(modeloDetalles);
        tablaDetalles.setRowHeight(36);
        JScrollPane scrollPane = new JScrollPane(tablaDetalles);
        scrollPane.setPreferredSize(new Dimension(480, 250));

        JLabel lblHeader = new JLabel("Artículos de la venta: " + folio);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblFooter = new JLabel("Total: $" + String.format("%.2f", venta.getTotal()));
        lblFooter.setFont(new Font("Segoe UI", Font.BOLD, 16));

        panel.add(lblHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(lblFooter, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Detalles de Venta", JOptionPane.PLAIN_MESSAGE);
    }
}