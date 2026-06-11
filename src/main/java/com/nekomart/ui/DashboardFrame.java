package com.nekomart.ui;

import com.nekomart.dao.EstadisticasDAO;
import javax.swing.*;
import java.awt.*;

/**
 * Panel de Dashboard que muestra estadísticas de ventas y facturación en tiempo real.
 * Cuenta con un diseño premium adaptable a temas claros y oscuros.
 * Todo el código está comentado en español.
 */
public class DashboardFrame extends JPanel {
    private EstadisticasDAO dao = new EstadisticasDAO();

    // Etiquetas para actualización dinámica
    private JLabel lblVentasHoyValor;
    private JLabel lblTicketsHoyValor;
    private JLabel lblIngresosTotalesValor;

    public DashboardFrame() {
        setLayout(new GridLayout(1, 3, 20, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // El fondo será heredado para adaptarse correctamente al tema claro/oscuro
        setOpaque(false);

        // Inicializar componentes
        initComponents();
        
        // Cargar los datos iniciales
        refrescar();
    }

    /**
     * Inicializa los tres paneles de tarjetas con sus respectivas etiquetas y colores.
     */
    private void initComponents() {
        // Tarjeta 1: Ventas Hoy
        JPanel cardVentas = crearTarjeta("💰 Ventas Hoy", new Color(59, 130, 246));
        lblVentasHoyValor = (JLabel) cardVentas.getClientProperty("valLabel");
        add(cardVentas);

        // Tarjeta 2: Tickets del Día
        JPanel cardTickets = crearTarjeta("🎫 Tickets del Día", new Color(16, 185, 129));
        lblTicketsHoyValor = (JLabel) cardTickets.getClientProperty("valLabel");
        add(cardTickets);

        // Tarjeta 3: Ingresos Totales
        JPanel cardIngresos = crearTarjeta("📈 Ingresos Totales", new Color(245, 158, 11));
        lblIngresosTotalesValor = (JLabel) cardIngresos.getClientProperty("valLabel");
        add(cardIngresos);
    }

    /**
     * Crea un panel estilizado tipo tarjeta para mostrar una estadística.
     */
    private JPanel crearTarjeta(String titulo, Color accentColor) {
        JPanel p = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                // Dibujar fondo redondeado premium adaptado a FlatLaf (blanco en claro, gris en oscuro)
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Usar color del fondo del panel de FlatLaf (generalmente blanco en tema claro, gris en oscuro)
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                // Pintar una barra de color en la parte superior para indicar el acento
                g2.setColor(accentColor);
                g2.fillRect(0, 0, getWidth(), 6);
                
                g2.dispose();
            }
        };
        p.setOpaque(false);
        // Borde con padding y línea muy sutil
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 228, 232, 100), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Configurar color de fondo según el Look & Feel activo
        p.setBackground(UIManager.getColor("Panel.background"));
        
        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblT.setForeground(Color.GRAY);
        
        JLabel lblV = new JLabel("$0.00");
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblV.setForeground(accentColor);
        
        p.add(lblT, BorderLayout.NORTH);
        p.add(lblV, BorderLayout.CENTER);
        
        // Guardar referencia al JLabel de valor en las propiedades del panel
        p.putClientProperty("valLabel", lblV);
        
        return p;
    }

    /**
     * Consulta el DAO y actualiza los valores de las etiquetas con formato correcto.
     */
    public void refrescar() {
        // Asegurarse de que el color de fondo de las tarjetas se adapte al tema actual
        for (Component c : getComponents()) {
            if (c instanceof JPanel) {
                c.setBackground(UIManager.getColor("Panel.background"));
            }
        }
        
        double ventas = dao.ventasHoy();
        int tickets = dao.totalVentasHoy();
        double ingresos = dao.ingresosTotales();

        lblVentasHoyValor.setText(String.format("$%.2f", ventas));
        lblTicketsHoyValor.setText(String.valueOf(tickets));
        lblIngresosTotalesValor.setText(String.format("$%.2f", ingresos));
        
        repaint();
    }
}