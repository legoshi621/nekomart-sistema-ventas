package com.nekomart.ui;

import com.nekomart.services.EstadisticasService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Panel de Dashboard que muestra indicadores clave de ventas,
 * un gráfico de barras de los últimos 7 días y la tabla de los 5 productos más vendidos.
 * Diseñado con una estética profesional basada en colores pastel.
 * Todo el código está comentado en español.
 */
public class DashboardFrame extends JPanel {

    // ── Colores de la paleta pastel ──────────────────────────────────────
    private static final Color LAVANDA = new Color(184, 169, 232);
    private static final Color LAVANDA_CLARO = new Color(212, 196, 240);
    private static final Color MENTA = new Color(168, 230, 207);
    private static final Color DURAZNO = new Color(255, 211, 182);
    private static final Color CORAL = new Color(255, 139, 148);
    private static final Color ROSA_PASTEL = new Color(255, 209, 220);
    private static final Color AZUL_CLARO = new Color(168, 216, 234);
    private static final Color FONDO = new Color(250, 250, 250);
    private static final Color TEXTO_OSCURO = new Color(45, 55, 72);
    private static final Color TEXTO_GRIS = new Color(113, 128, 150);
    private static final Color BORDE = new Color(226, 232, 240);
    private static final Color FILA_ALTERNA = new Color(247, 250, 252);

    private final EstadisticasService estadisticasService;
    private final DecimalFormat df = new DecimalFormat("$#,##0.00");

    // Componentes visuales
    private JLabel lblVentasHoy;
    private JLabel lblVentasMes;
    private JLabel lblProductosHoy;
    private JLabel lblIngresosTotales;
    private GraficoBarrasPanel panelGrafico;
    private DefaultTableModel modeloTablaProductos;

    public DashboardFrame() {
        this.estadisticasService = new EstadisticasService();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(FONDO);

        initComponents();
        cargarDatos();
    }

    /**
     * Inicializa la interfaz y distribuye las secciones del Dashboard.
     */
    private void initComponents() {
        // ══════════════════════════════════════════════════════════════════
        // TÍTULO DEL DASHBOARD
        // ══════════════════════════════════════════════════════════════════
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelTitulo.setOpaque(false);
        JLabel lblTitulo = new JLabel("📊 Resumen de Estadísticas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(TEXTO_OSCURO);
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);

        // ══════════════════════════════════════════════════════════════════
        // CONTENEDOR CENTRAL: Tarjetas superiores y paneles inferiores
        // ══════════════════════════════════════════════════════════════════
        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 15, 0);

        // ── 1. Tarjetas de Estadísticas (Fila Superior) ──────────────────
        JPanel panelTarjetas = new JPanel(new GridLayout(1, 4, 15, 0));
        panelTarjetas.setOpaque(false);

        panelTarjetas.add(crearTarjeta("💰 Ventas de Hoy", lblVentasHoy = new JLabel("$0.00"), AZUL_CLARO));
        panelTarjetas.add(crearTarjeta("📊 Ventas del Mes", lblVentasMes = new JLabel("$0.00"), MENTA));
        panelTarjetas.add(crearTarjeta("📦 Prod. Vendidos Hoy", lblProductosHoy = new JLabel("0 unidades"), DURAZNO));
        panelTarjetas.add(crearTarjeta("💵 Ingresos Totales", lblIngresosTotales = new JLabel("$0.00"), ROSA_PASTEL));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.15;
        panelCentro.add(panelTarjetas, gbc);

        // ── 2. Gráfico de Barras (Fila Inferior - Izquierda) ─────────────
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.6;
        gbc.weighty = 0.85;
        gbc.insets = new Insets(0, 0, 0, 15);

        JPanel panelContenedorGrafico = crearPanelContenedorRedondeado("📈 Historial de Ventas (Últimos 7 Días)");
        panelGrafico = new GraficoBarrasPanel();
        panelContenedorGrafico.add(panelGrafico, BorderLayout.CENTER);
        panelCentro.add(panelContenedorGrafico, gbc);

        // ── 3. Top 5 Productos (Fila Inferior - Derecha) ─────────────────
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel panelContenedorProductos = crearPanelContenedorRedondeado("🏆 Top 5 Productos Más Vendidos");

        String[] columnas = {"#", "Producto", "Cant. Vendida", "Ingresos"};
        modeloTablaProductos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable tablaProductos = new JTable(modeloTablaProductos);
        tablaProductos.setRowHeight(35);
        tablaProductos.setShowGrid(false);
        tablaProductos.setIntercellSpacing(new Dimension(0, 0));
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaProductos.setSelectionBackground(LAVANDA_CLARO);
        tablaProductos.setSelectionForeground(TEXTO_OSCURO);

        // Personalización del encabezado de la tabla de productos
        JTableHeader header = tablaProductos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(LAVANDA);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 32));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // Configuración de renderers y alineación de la tabla
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val,
                                                           boolean isSel, boolean hasFoc, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, val, isSel, hasFoc, row, col);
                c.setBackground(row % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
                c.setForeground(TEXTO_OSCURO);
                if (isSel) c.setBackground(LAVANDA_CLARO);
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
                        if (isS) c.setBackground(LAVANDA_CLARO);
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
                        if (isS) c.setBackground(LAVANDA_CLARO);
                        return c;
                    }
                });
            } else {
                tablaProductos.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
            }
        }

        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(30);
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(140);
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panelContenedorProductos.add(scrollPane, BorderLayout.CENTER);
        panelCentro.add(panelContenedorProductos, gbc);

        add(panelCentro, BorderLayout.CENTER);
    }

    /**
     * Recupera y carga las estadísticas de ventas y productos en los controles visuales.
     */
    public void cargarDatos() {
        // Ejecución en segundo plano para evitar congelar la interfaz Swing
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private double ventasHoy;
            private double ventasMes;
            private int productosHoy;
            private double ingresosTotales;
            private List<Map<String, Object>> topProductos;
            private Map<String, Double> ventas7Dias;

            @Override
            protected Void doInBackground() {
                ventasHoy = estadisticasService.getVentasHoy();
                ventasMes = estadisticasService.getVentasMes();
                productosHoy = estadisticasService.getProductosVendidosHoy();
                ingresosTotales = estadisticasService.getIngresosTotales();
                topProductos = estadisticasService.getTopProductos();
                ventas7Dias = estadisticasService.getVentasUltimos7Dias();
                return null;
            }

            @Override
            protected void done() {
                // Actualizar las etiquetas superiores
                lblVentasHoy.setText(df.format(ventasHoy));
                lblVentasMes.setText(df.format(ventasMes));
                lblProductosHoy.setText(productosHoy + " unidades");
                lblIngresosTotales.setText(df.format(ingresosTotales));

                // Actualizar el gráfico de barras
                panelGrafico.setDatos(ventas7Dias);

                // Actualizar la tabla del Top 5
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

    /**
     * Helper para crear tarjetas con bordes redondeados y un fondo de color pastel.
     */
    private JPanel crearTarjeta(String titulo, JLabel lblValor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 18, 15, 18));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(TEXTO_OSCURO);

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblValor.setForeground(TEXTO_OSCURO);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);

        return card;
    }

    /**
     * Helper para crear paneles contenedores con bordes redondeados y un título interno.
     */
    private JPanel crearPanelContenedorRedondeado(String titulo) {
        JPanel container = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(15, 15, 15, 15));
        container.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXTO_OSCURO);
        lblTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));

        container.add(lblTitle, BorderLayout.NORTH);
        return container;
    }

    /**
     * Panel gráfico personalizado para renderizar las ventas de los últimos 7 días como un gráfico de barras.
     */
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

            // Mensaje si no hay datos o están vacíos
            if (datos == null || datos.isEmpty()) {
                g2.setColor(TEXTO_GRIS);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                String msg = "No hay ventas registradas";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
                g2.dispose();
                return;
            }

            // Calcular el valor máximo para el escalado de barras
            double maxValor = 0.0;
            boolean todoCero = true;
            for (double val : datos.values()) {
                if (val > maxValor) {
                    maxValor = val;
                }
                if (val > 0) {
                    todoCero = false;
                }
            }

            // Si todas las ventas son 0.0, pintar mensaje de sin ventas
            if (todoCero) {
                g2.setColor(TEXTO_GRIS);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                String msg = "No hay ventas registradas";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
                g2.dispose();
                return;
            }

            // Margen y área útil del gráfico
            int paddingIzq = 50;
            int paddingDer = 20;
            int paddingSup = 30;
            int paddingInf = 40;

            int areaW = width - paddingIzq - paddingDer;
            int areaH = height - paddingSup - paddingInf;

            // Dibujar Ejes X e Y
            g2.setColor(BORDE);
            g2.setStroke(new BasicStroke(1));
            // Eje Y
            g2.drawLine(paddingIzq, paddingSup, paddingIzq, height - paddingInf);
            // Eje X
            g2.drawLine(paddingIzq, height - paddingInf, width - paddingDer, height - paddingInf);

            // Dibujar líneas de referencia horizontal en el eje Y
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(TEXTO_GRIS);
            int divisiones = 4;
            for (int i = 0; i <= divisiones; i++) {
                int yLine = height - paddingInf - (i * areaH / divisiones);
                double valorLine = (i * maxValor / divisiones);

                // Línea de cuadrícula tenue
                g2.setColor(new Color(240, 240, 240));
                g2.drawLine(paddingIzq + 1, yLine, width - paddingDer, yLine);

                // Etiqueta del valor en el Eje Y
                g2.setColor(TEXTO_GRIS);
                String labelVal = String.format("$%.0f", valorLine);
                FontMetrics fmVal = g2.getFontMetrics();
                g2.drawString(labelVal, paddingIzq - fmVal.stringWidth(labelVal) - 8, yLine + 3);
            }

            // Dibujar las barras para cada día
            int numBarras = datos.size();
            int gap = 15;
            int barW = (areaW - (gap * (numBarras - 1))) / numBarras;

            int i = 0;
            DateTimeFormatter parseador = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter formateadorX = DateTimeFormatter.ofPattern("dd/MM");

            for (Map.Entry<String, Double> entry : datos.entrySet()) {
                String fechaStr = entry.getKey();
                double valor = entry.getValue();

                // Calcular dimensiones de la barra
                int barH = (int) ((valor / maxValor) * areaH);
                int bx = paddingIzq + i * (barW + gap) + (gap / 2);
                int by = height - paddingInf - barH;

                // Dibujar barra con bordes superiores redondeados (usando clip)
                g2.setColor(CORAL);
                Shape barShape = new RoundRectangle2D.Double(bx, by, barW, barH + 10, 6, 6);
                Shape oldClip = g2.getClip();
                // Limitar el dibujo al área superior al eje X
                g2.clipRect(paddingIzq, paddingSup - 5, areaW + 10, areaH + 5);
                g2.fill(barShape);
                g2.setClip(oldClip);

                // Dibujar valor sobre la barra
                if (valor > 0) {
                    g2.setColor(TEXTO_OSCURO);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    String valStr = String.format("$%.0f", valor);
                    FontMetrics fmText = g2.getFontMetrics();
                    g2.drawString(valStr, bx + (barW - fmText.stringWidth(valStr)) / 2, by - 6);
                }

                // Formatear fecha para el Eje X
                String fechaLabel = fechaStr;
                try {
                    LocalDate fecha = LocalDate.parse(fechaStr, parseador);
                    fechaLabel = fecha.format(formateadorX);
                } catch (Exception ex) {
                    // Si falla el casteo, dejamos la fecha tal cual viene
                }

                // Dibujar etiqueta de fecha abajo de la barra
                g2.setColor(TEXTO_OSCURO);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                FontMetrics fmLabel = g2.getFontMetrics();
                int lx = bx + (barW - fmLabel.stringWidth(fechaLabel)) / 2;
                int ly = height - paddingInf + fmLabel.getAscent() + 8;
                g2.drawString(fechaLabel, lx, ly);

                i++;
            }

            g2.dispose();
        }
    }
}