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
import java.util.List;

/**
 * Panel que muestra el historial de ventas realizadas.
 * Permite ver los detalles de cada venta al hacer doble clic.
 * IMPORTANTE: Debe extender de JPanel para poder integrarse en JTabbedPane.
 * Todo el código está comentado en español.
 */
public class HistorialVentasFrame extends JPanel {

    private VentaService ventaService;
    private ProductoService productoService;
    private JTable tablaVentas;
    private DefaultTableModel modeloVentas;

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
        // Panel superior con botón de refrescar
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.addActionListener(e -> cargarVentas());
        panelSuperior.add(btnRefrescar);
        panelSuperior.add(new JLabel("  Doble clic en una venta para ver detalles"));

        // Tabla de ventas
        modeloVentas = new DefaultTableModel(COLS_VENTAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaVentas = new JTable(modeloVentas);
        tablaVentas.setRowHeight(25);

        // Doble clic para ver detalles
        tablaVentas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    verDetallesVenta();
                }
            }
        });

        add(panelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tablaVentas), BorderLayout.CENTER);
    }

    /**
     * Carga todas las ventas desde el servicio.
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
     * Muestra los detalles de la venta seleccionada en un diálogo.
     */
    private void verDetallesVenta() {
        int fila = tablaVentas.getSelectedRow();
        if (fila < 0)
            return;

        String folio = (String) modeloVentas.getValueAt(fila, 0);
        Venta venta = ventaService.obtenerTodasLasVentas().stream()
                .filter(v -> v.getFolio().equals(folio))
                .findFirst().orElse(null);

        if (venta == null)
            return;

        List<DetalleVenta> detalles = ventaService.obtenerDetalles(venta.getId());

        // Crear panel con los detalles
        JPanel panel = new JPanel(new BorderLayout());
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
        tablaDetalles.setRowHeight(25);

        panel.add(new JLabel("Detalles de la venta: " + folio, SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaDetalles), BorderLayout.CENTER);
        panel.add(new JLabel("Total: $" + String.format("%.2f", venta.getTotal()), SwingConstants.RIGHT),
                BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Detalles de Venta", JOptionPane.INFORMATION_MESSAGE);
    }
}