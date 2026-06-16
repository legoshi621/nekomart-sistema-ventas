package com.nekomart.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.nekomart.models.Usuario;
import com.nekomart.services.EstadisticasService;
import com.nekomart.utils.DisenoSystem;

public class DashboardFrame extends JPanel {

    private static final Color FONDO = DisenoSystem.FONDO_PRINCIPAL;
    private static final Color TEXTO_OSCURO = DisenoSystem.GRIS_OSCURO;
    private static final Color TEXTO_GRIS = DisenoSystem.GRIS_MEDIO;
    private static final Color BORDE = DisenoSystem.GRIS_CLARO;
    private static final Color FILA_ALTERNA = DisenoSystem.GRIS_MUY_CLARO;

    // Colores de tarjetas KPI
    private static final Color VERDE_KPI = new Color(16, 185, 129);
    private static final Color AZUL_KPI = new Color(59, 130, 246);
    private static final Color AMARILLO_KPI = new Color(245, 158, 11);
    private static final Color ROJO_KPI = new Color(239, 68, 68);

    private final EstadisticasService estadisticasService;
    private final DecimalFormat df = new DecimalFormat("$#,##0.00");

    private JLabel lblVentasHoy;
    private JLabel lblVentasMes;
    private JLabel lblProductosHoy;
    private JLabel lblIngresosTotales;
    private GraficoBarrasPanel panelGrafico;
    private DefaultTableModel modeloTablaProductos;
    
    private final Usuario usuarioActual;
public DashboardFrame(Usuario usuario) {
    this.usuarioActual = usuario;
    this.estadisticasService = new EstadisticasService();

    setLayout(new BorderLayout(20, 20));
    setBorder(new EmptyBorder(30, 30, 30, 30));
    setBackground(FONDO);

    initComponents();
    cargarDatos();
    
    // Auto-refresh cada 30 segundos
    javax.swing.Timer timer = new javax.swing.Timer(30000, e -> cargarDatos());
    timer.start();
}

    public DashboardFrame() {
        this(null);
    }

    private void initComponents() {
        // TÍTULO
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelTitulo.setOpaque(false);
        JLabel lblTitulo = new JLabel("Resumen de Estadísticas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(TEXTO_OSCURO);
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);

        // CONTENIDO CENTRAL
        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 20, 0);

        // TARJETAS KPI
        JPanel panelTarjetas = new JPanel(new GridLayout(1, 4, 20, 0));
        panelTarjetas.setOpaque(false);

        panelTarjetas.add(crearTarjetaKPI("Ventas de Hoy", lblVentasHoy = new JLabel("$0.00"), VERDE_KPI, "ventas"));
        panelTarjetas.add(crearTarjetaKPI("Ventas del Mes", lblVentasMes = new JLabel("$0.00"), AZUL_KPI, "mes"));
        panelTarjetas.add(crearTarjetaKPI("Prod. Vendidos Hoy", lblProductosHoy = new JLabel("0 unidades"), AMARILLO_KPI, "productos"));
        panelTarjetas.add(crearTarjetaKPI("Ingresos Totales", lblIngresosTotales = new JLabel("$0.00"), ROJO_KPI, "ingresos"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.4;
        panelCentro.add(panelTarjetas, gbc);

        // GRÁFICO DE BARRAS
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.6;
        gbc.weighty = 0.6;
        gbc.insets = new Insets(0, 0, 0, 20);

        JPanel panelContenedorGrafico = crearPanelContenedor("Historial de Ventas (Últimos 7 Días)");
        panelGrafico = new GraficoBarrasPanel();
        panelContenedorGrafico.add(panelGrafico, BorderLayout.CENTER);
        panelCentro.add(panelContenedorGrafico, gbc);

        // TABLA DE PRODUCTOS
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel panelContenedorProductos = crearPanelContenedor("Top 5 Productos Más Vendidos");

        String[] columnas = {"#", "Producto", "Cant. Vendida", "Ingresos"};
        modeloTablaProductos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable tablaProductos = new JTable(modeloTablaProductos);
        tablaProductos.setRowHeight(40);
        tablaProductos.setShowGrid(false);
        tablaProductos.setIntercellSpacing(new Dimension(0, 0));
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader header = tablaProductos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(59, 130, 246));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, val, isSel, hasFoc, row, col);
                c.setBackground(row % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
                c.setForeground(TEXTO_OSCURO);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        };

        for (int i = 0; i < tablaProductos.getColumnCount(); i++) {
            if (i == 0 || i == 2) {
                tablaProductos.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    { setHorizontalAlignment(SwingConstants.CENTER); }
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int col) {
                        Component c = super.getTableCellRendererComponent(t, v, isS, hasF, r, col);
                        c.setBackground(r % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
                        c.setForeground(TEXTO_OSCURO);
                        ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                        return c;
                    }
                });
            } else if (i == 3) {
                tablaProductos.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    { setHorizontalAlignment(SwingConstants.RIGHT); }
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int col) {
                        Component c = super.getTableCellRendererComponent(t, v, isS, hasF, r, col);
                        c.setBackground(r % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
                        c.setForeground(TEXTO_OSCURO);
                        ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                        return c;
                    }
                });
            } else {
                tablaProductos.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
            }
        }

        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(180);
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panelContenedorProductos.add(scrollPane, BorderLayout.CENTER);
        panelCentro.add(panelContenedorProductos, gbc);

        add(panelCentro, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaKPI(String titulo, JLabel lblValor, Color color, String tipo) {
        JPanel card = new JPanel(new BorderLayout(15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradiente de color
                GradientPaint gp = new GradientPaint(
                    0, 0, color,
                    getWidth(), getHeight(), color.brighter()
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(0, 120));

        // Título
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitulo.setForeground(Color.WHITE);
        card.add(lblTitulo, BorderLayout.NORTH);

        // Valor
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(Color.WHITE);
        card.add(lblValor, BorderLayout.CENTER);

        // Icono
        JPanel panelIcono = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(2));
                
                if (tipo.equals("ventas")) {
                    g2.fillOval(10, 10, 20, 20);
                } else if (tipo.equals("mes")) {
                    g2.drawRect(10, 10, 20, 20);
                } else if (tipo.equals("productos")) {
                    g2.fillRoundRect(10, 10, 20, 20, 5, 5);
                } else {
                    g2.drawOval(10, 10, 20, 20);
                }
            }
        };
        panelIcono.setOpaque(false);
        panelIcono.setPreferredSize(new Dimension(40, 40));
        card.add(panelIcono, BorderLayout.EAST);

        return card;
    }

    private JPanel crearPanelContenedor(String titulo) {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXTO_OSCURO);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        container.add(lblTitle, BorderLayout.NORTH);

        return container;
    }

    public void cargarDatos() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private double ventasHoy;
            private double ventasMes;
            private int productosHoy;
            private double ingresosTotales;
            private List<Map<String, Object>> topProductos;
            private Map<String, Double> ventas7Dias;

            @Override
            protected Void doInBackground() {
                if (usuarioActual != null && !"ADMIN".equals(usuarioActual.getRol().toUpperCase())) {
                    ventasHoy = estadisticasService.getVentasHoyPorEmpleado(usuarioActual.getId());
                    ventasMes = estadisticasService.getVentasMesPorEmpleado(usuarioActual.getId());
                    productosHoy = estadisticasService.getProductosVendidosHoyPorEmpleado(usuarioActual.getId());
                    ingresosTotales = estadisticasService.getIngresosTotalesPorEmpleado(usuarioActual.getId());
                    topProductos = estadisticasService.getTopProductosPorEmpleado(usuarioActual.getId());
                    ventas7Dias = estadisticasService.getVentasUltimos7DiasPorEmpleado(usuarioActual.getId());
                } else {
                    ventasHoy = estadisticasService.getVentasHoy();
                    ventasMes = estadisticasService.getVentasMes();
                    productosHoy = estadisticasService.getProductosVendidosHoy();
                    ingresosTotales = estadisticasService.getIngresosTotales();
                    topProductos = estadisticasService.getTopProductos();
                    ventas7Dias = estadisticasService.getVentasUltimos7Dias();
                }
                return null;
            }

            @Override
            protected void done() {
                lblVentasHoy.setText(df.format(ventasHoy));
                lblVentasMes.setText(df.format(ventasMes));
                lblProductosHoy.setText(productosHoy + " unidades");
                lblIngresosTotales.setText(df.format(ingresosTotales));

                panelGrafico.setDatos(ventas7Dias);

                modeloTablaProductos.setRowCount(0);
                int puesto = 1;
                for (Map<String, Object> p : topProductos) {
                    modeloTablaProductos.addRow(new Object[]{
                            puesto++,
                            p.get("producto"),
                            p.get("cantidad_sold") != null ? p.get("cantidad_sold") : p.get("cantidad_vendida"),
                            df.format(p.get("ingresos"))
                    });
                }
            }
        };
        worker.execute();
    }

    private class GraficoBarrasPanel extends JPanel {
        private Map<String, Double> datos;

        public GraficoBarrasPanel() {
            setBackground(Color.WHITE);
            setOpaque(true);
        }

        public void setDatos(Map<String, Double> datos) {
            this.datos = datos;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (datos == null || datos.isEmpty()) {
                g2.setColor(TEXTO_GRIS);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                String msg = "No hay ventas registradas";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
                g2.dispose();
                return;
            }

            double maxValor = 0.0;
            boolean todoCero = true;
            for (double val : datos.values()) {
                if (val > maxValor) maxValor = val;
                if (val > 0) todoCero = false;
            }

            if (todoCero) {
                g2.setColor(TEXTO_GRIS);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                String msg = "No hay ventas registradas";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
                g2.dispose();
                return;
            }

            int paddingIzq = 60, paddingDer = 20, paddingSup = 30, paddingInf = 50;
            int areaW = width - paddingIzq - paddingDer;
            int areaH = height - paddingSup - paddingInf;

            // Líneas de grid
            g2.setColor(new Color(226, 232, 240));
            g2.setStroke(new BasicStroke(1));
            int divisiones = 4;
            for (int i = 0; i <= divisiones; i++) {
                int yLine = height - paddingInf - (i * areaH / divisiones);
                g2.drawLine(paddingIzq, yLine, width - paddingDer, yLine);
                
                double valorLine = (i * maxValor / divisiones);
                g2.setColor(TEXTO_GRIS);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String labelVal = String.format("$%.0f", valorLine);
                FontMetrics fmVal = g2.getFontMetrics();
                g2.drawString(labelVal, paddingIzq - fmVal.stringWidth(labelVal) - 10, yLine + 4);
            }

            // Barras
            int numBarras = datos.size();
            int gap = 20;
            int barW = (areaW - (gap * (numBarras - 1))) / numBarras;

            int i = 0;
            DateTimeFormatter parseador = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter formateadorX = DateTimeFormatter.ofPattern("dd/MM");

            for (Map.Entry<String, Double> entry : datos.entrySet()) {
                String fechaStr = entry.getKey();
                double valor = entry.getValue();

                int barH = (int) ((valor / maxValor) * areaH);
                int bx = paddingIzq + i * (barW + gap) + (gap / 2);
                int by = height - paddingInf - barH;

                // Gradiente de barra
                GradientPaint gp = new GradientPaint(bx, by, new Color(59, 130, 246), bx, by + barH, new Color(147, 197, 253));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(bx, by, barW, barH, 8, 8));

                // Valor encima de la barra
                if (valor > 0) {
                    g2.setColor(TEXTO_OSCURO);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String valStr = String.format("$%.0f", valor);
                    FontMetrics fmText = g2.getFontMetrics();
                    g2.drawString(valStr, bx + (barW - fmText.stringWidth(valStr)) / 2, by - 8);
                }

                // Fecha debajo
                String fechaLabel = fechaStr;
                try {
                    LocalDate fecha = LocalDate.parse(fechaStr, parseador);
                    fechaLabel = fecha.format(formateadorX);
                } catch (Exception ex) {}

                g2.setColor(TEXTO_GRIS);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                FontMetrics fmLabel = g2.getFontMetrics();
                int lx = bx + (barW - fmLabel.stringWidth(fechaLabel)) / 2;
                int ly = height - paddingInf + fmLabel.getAscent() + 10;
                g2.drawString(fechaLabel, lx, ly);

                i++;
            }

            g2.dispose();
        }
    }
}