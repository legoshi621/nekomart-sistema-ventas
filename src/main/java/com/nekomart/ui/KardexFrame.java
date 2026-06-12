package com.nekomart.ui;

import com.nekomart.dao.MovimientoDAO;
import com.nekomart.models.Movimiento;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Panel/Diálogo que muestra el historial de movimientos (Kardex) de un producto.
 * Presenta una tabla con las columnas: Fecha, Tipo, Cantidad, Usuario.
 * Usa la paleta de colores pastel del sistema NekoMart.
 * Todo el código está comentado en español.
 */
public class KardexFrame extends JDialog {

    // ── Colores de la paleta pastel ──────────────────────────────────────
    private static final Color LAVANDA = new Color(184, 169, 232);
    private static final Color FONDO = new Color(250, 250, 250);
    private static final Color TEXTO_OSCURO = new Color(45, 55, 72);
    private static final Color BORDE = new Color(226, 232, 240);
    private static final Color FILA_ALTERNA = new Color(247, 250, 252);
    private static final Color HOVER_LAVANDA = new Color(240, 235, 255);
    private static final Color MENTA = new Color(168, 230, 207);
    private static final Color CORAL = new Color(255, 139, 148);
    private static final Color NARANJA = new Color(255, 183, 77);

    // ── DAO y componentes ────────────────────────────────────────────────
    private MovimientoDAO movimientoDAO;
    private DefaultTableModel modeloTabla;
    private JTable tablaKardex;

    // Columnas de la tabla del Kardex
    private final String[] COLUMNAS = {"Fecha", "Tipo", "Cantidad", "Usuario ID"};

    /**
     * Constructor del diálogo Kardex.
     *
     * @param parent       Ventana padre para centrar el diálogo
     * @param productoId   ID del producto a consultar
     * @param nombreProducto Nombre del producto (para el título)
     */
    public KardexFrame(Window parent, int productoId, String nombreProducto) {
        super(parent, "📋 Kardex — " + nombreProducto, ModalityType.APPLICATION_MODAL);
        this.movimientoDAO = new MovimientoDAO();

        setSize(650, 450);
        setLocationRelativeTo(parent);
        setResizable(true);
        getContentPane().setBackground(FONDO);

        initComponents(productoId, nombreProducto);
    }

    /**
     * Inicializa los componentes visuales del Kardex.
     *
     * @param productoId     ID del producto
     * @param nombreProducto Nombre del producto
     */
    private void initComponents(int productoId, String nombreProducto) {
        setLayout(new BorderLayout(10, 10));

        // ── PANEL SUPERIOR — Título informativo ──────────────────────────
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelTitulo.setBackground(LAVANDA);

        JLabel lblTitulo = new JLabel("📋 Historial de Movimientos — " + nombreProducto);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        panelTitulo.add(lblTitulo);

        // ── TABLA DE MOVIMIENTOS ─────────────────────────────────────────
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // La tabla no es editable
            }
        };

        tablaKardex = new JTable(modeloTabla);
        tablaKardex.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaKardex.setRowHeight(40);
        tablaKardex.setShowGrid(false);
        tablaKardex.setIntercellSpacing(new Dimension(0, 0));
        tablaKardex.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaKardex.setForeground(TEXTO_OSCURO);
        tablaKardex.setSelectionBackground(HOVER_LAVANDA);
        tablaKardex.setSelectionForeground(TEXTO_OSCURO);

        // Estilo del header (fondo lavanda, texto blanco)
        JTableHeader header = tablaKardex.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(LAVANDA);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // Renderer personalizado para colorear por tipo de movimiento
        KardexCellRenderer renderer = new KardexCellRenderer();
        for (int i = 0; i < tablaKardex.getColumnCount(); i++) {
            tablaKardex.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // ScrollPane con bordes redondeados
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setOpaque(false);
        panelTabla.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JScrollPane scrollPane = new JScrollPane(tablaKardex);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panelTabla.add(scrollPane, BorderLayout.CENTER);

        // ── PANEL INFERIOR — Botón cerrar ────────────────────────────────
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelInferior.setBackground(FONDO);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrar.setBackground(LAVANDA);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.putClientProperty("JButton.buttonType", "roundRect");
        btnCerrar.addActionListener(e -> dispose());
        panelInferior.add(btnCerrar);

        // ── Organizar layout ─────────────────────────────────────────────
        add(panelTitulo, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        // Cargar datos del historial
        cargarHistorial(productoId);
    }

    /**
     * Carga el historial de movimientos del producto en la tabla.
     *
     * @param productoId ID del producto a consultar
     */
    private void cargarHistorial(int productoId) {
        modeloTabla.setRowCount(0);
        List<Movimiento> historial = movimientoDAO.obtenerHistorial(productoId);

        if (historial.isEmpty()) {
            // Mostrar mensaje si no hay movimientos registrados
            modeloTabla.addRow(new Object[]{"—", "Sin movimientos registrados", "—", "—"});
        } else {
            for (Movimiento m : historial) {
                modeloTabla.addRow(new Object[]{
                        m.getFecha(),
                        m.getTipo(),
                        m.getCantidad(),
                        m.getUsuarioId()
                });
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // RENDERER PERSONALIZADO PARA COLOREAR FILAS POR TIPO DE MOVIMIENTO
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Renderer de celda que colorea las filas según el tipo de movimiento:
     * - ENTRADA: fondo verde menta suave
     * - SALIDA: fondo coral suave
     * - AJUSTE: fondo naranja suave
     * - Normal: colores alternados blanco / gris claro
     */
    private class KardexCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Obtener el tipo de movimiento de la columna 1 (Tipo)
            int modelRow = table.convertRowIndexToModel(row);
            Object tipoObj = table.getModel().getValueAt(modelRow, 1);
            String tipo = tipoObj != null ? tipoObj.toString() : "";

            if (isSelected) {
                c.setBackground(HOVER_LAVANDA);
                c.setForeground(TEXTO_OSCURO);
            } else {
                switch (tipo.toUpperCase()) {
                    case "ENTRADA":
                        // Verde menta suave para entradas
                        c.setBackground(new Color(220, 245, 233));
                        c.setForeground(new Color(39, 103, 73));
                        break;
                    case "SALIDA":
                        // Coral suave para salidas
                        c.setBackground(new Color(254, 226, 226));
                        c.setForeground(new Color(155, 44, 44));
                        break;
                    case "AJUSTE":
                        // Naranja suave para ajustes
                        c.setBackground(new Color(254, 235, 200));
                        c.setForeground(new Color(146, 100, 22));
                        break;
                    default:
                        // Filas normales con colores alternados
                        c.setBackground(row % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
                        c.setForeground(TEXTO_OSCURO);
                        break;
                }
            }

            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return c;
        }
    }
}
