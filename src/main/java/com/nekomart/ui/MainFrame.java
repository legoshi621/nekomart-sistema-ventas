package com.nekomart.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.nekomart.models.Usuario;
import com.nekomart.utils.SessionManager;

public class MainFrame extends JFrame {

    private static final Color AZUL_POS = new Color(59, 130, 246);
    private static final Color SIDEBAR_COLOR = new Color(30, 41, 59);
    private static final Color SIDEBAR_HOVER = new Color(51, 65, 85);
    private static final Color FONDO = new Color(241, 245, 249);
    private static final Color TEXTO_OSCURO = new Color(30, 41, 59);
    private static final Color TEXTO_GRIS = new Color(100, 116, 139);
    private static final Color BORDE = new Color(226, 232, 240);

    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private DashboardFrame dashboard;
    private CorteCajaFrame corteCajaFrame;
    private Usuario usuarioActual;

    public MainFrame() {
        setTitle("NekoMart - Sistema de Ventas");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        usuarioActual = SessionManager.getInstancia().getUsuarioActual();
        String rol = (usuarioActual != null) ? usuarioActual.getRol().toUpperCase() : "EMPLEADO";

        setLayout(new BorderLayout());

        // ══════════════════════════════════════════════════════════════════
        // SIDEBAR
        // ══════════════════════════════════════════════════════════════════
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(240, 800));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // HEADER: Avatar + Nombre + Rol
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
        panelHeader.setOpaque(false);
        panelHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelHeader.setMaximumSize(new Dimension(240, 80));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Avatar centrado
        JLabel lblAvatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AZUL_POS);
                g2.fillOval(0, 0, 44, 44);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String iniciales = obtenerIniciales(usuarioActual);
                FontMetrics fm = g2.getFontMetrics();
                int width = fm.stringWidth(iniciales);
                g2.drawString(iniciales, (44 - width) / 2, 30);
            }
        };
        lblAvatar.setPreferredSize(new Dimension(44, 44));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelHeader.add(lblAvatar);

        panelHeader.add(Box.createVerticalStrut(8));

        // Nombre del usuario
        JLabel lblNombre = new JLabel(usuarioActual != null ? usuarioActual.getNombreCompleto() : "Usuario");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelHeader.add(lblNombre);

        // Rol del usuario
        JLabel lblRol = new JLabel(rol.equals("ADMIN") ? "Administrador" : "Empleado");
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRol.setForeground(new Color(148, 163, 184));
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelHeader.add(lblRol);

        sidebarPanel.add(panelHeader);
        sidebarPanel.add(Box.createVerticalStrut(15));

        // LOGO: Icono + Nombre
        JPanel panelLogo = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        panelLogo.setOpaque(false);
        panelLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLogo.setMaximumSize(new Dimension(240, 45));

        JLabel lblLogoIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                dibujarCarritoSidebar(g2, 0, 5, 28);
                g2.dispose();
            }
        };
        lblLogoIcon.setPreferredSize(new Dimension(35, 35));
        panelLogo.add(lblLogoIcon);

        JLabel lblAppName = new JLabel("NekoMart");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblAppName.setForeground(Color.WHITE);
        panelLogo.add(lblAppName);
        sidebarPanel.add(panelLogo);
        sidebarPanel.add(Box.createVerticalStrut(10));

        // Línea separadora
        JPanel linea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(51, 65, 85));
                g2.fillRect(15, 0, 210, 1);
            }
        };
        linea.setOpaque(false);
        linea.setMaximumSize(new Dimension(240, 1));
        linea.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPanel.add(linea);
        sidebarPanel.add(Box.createVerticalStrut(10));

        // MENÚ DE NAVEGACIÓN
        agregarBotonSidebar("Dashboard", "dashboard", "dashboard", true);
        agregarBotonSidebar("Punto de Venta", "ventas", "ventas", false);

        if (rol.equals("ADMIN")) {
            agregarBotonSidebar("Inventario", "inventario", "inventario", false);
            agregarBotonSidebar("Usuarios", "usuarios", "usuarios", false);
            agregarBotonSidebar("Historial", "historial", "historial", false);
            agregarBotonSidebar("Corte de Caja", "corte", "corte", false);
        } else {
            agregarBotonSidebar("Consulta Inventario", "inventario", "inventario", false);
            agregarBotonSidebar("Mis Ventas", "historial", "historial", false);
        }

        // Espacio flexible
        sidebarPanel.add(Box.createVerticalGlue());

        // Botón Mi Perfil
        JButton btnPerfil = crearBotonPerfil(usuarioActual);
        sidebarPanel.add(btnPerfil);
        sidebarPanel.add(Box.createVerticalStrut(10));

        add(sidebarPanel, BorderLayout.WEST);

        // ══════════════════════════════════════════════════════════════════
        // TOPBAR
        // ══════════════════════════════════════════════════════════════════
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setPreferredSize(new Dimension(0, 55));
        panelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));

        // Notificaciones
        JButton btnNotif = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillOval(0, 0, 32, 32);
                }
                g2.setColor(TEXTO_GRIS);
                g2.setStroke(new BasicStroke(2));
                g2.drawArc(8, 6, 16, 16, 0, 180);
                g2.drawLine(8, 14, 8, 20);
                g2.drawLine(24, 14, 24, 20);
                g2.drawLine(6, 20, 26, 20);
                g2.fillOval(14, 22, 4, 4);
                g2.dispose();
            }
        };
        btnNotif.setPreferredSize(new Dimension(32, 32));
        btnNotif.setContentAreaFilled(false);
        btnNotif.setBorderPainted(false);
        btnNotif.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelSuperior.add(btnNotif);

        // Cerrar Sesión
        JButton btnCerrar = new JButton("Cerrar Sesión") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(220, 38, 38) : new Color(239, 68, 68));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setPreferredSize(new Dimension(105, 32));
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> {
            SessionManager.getInstancia().cerrarSesion();
            new LoginFrame().setVisible(true);
            dispose();
        });
        panelSuperior.add(btnCerrar);

        add(panelSuperior, BorderLayout.NORTH);

        // ══════════════════════════════════════════════════════════════════
        // CONTENIDO
        // ══════════════════════════════════════════════════════════════════
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(FONDO);

        dashboard = new DashboardFrame(usuarioActual);
        InventarioFrame inventarioFrame = new InventarioFrame();
        UsuariosFrame usuariosFrame = new UsuariosFrame();
        VentasFrame ventasFrame = new VentasFrame();
        HistorialVentasFrame historialFrame = new HistorialVentasFrame();
        ProfilePanel profilePanel = new ProfilePanel(usuarioActual); // ← AGREGADO

        if (rol.equals("ADMIN")) {
            corteCajaFrame = new CorteCajaFrame();
        }

        JPanel panelWelcome = new JPanel(new GridLayout(2, 1, 5, 5));
        panelWelcome.setOpaque(false);
        panelWelcome.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel lblWelcomeTitle = new JLabel("¡Hola, " + (usuarioActual != null ? usuarioActual.getNombreCompleto() : "Usuario") + "!");
        lblWelcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcomeTitle.setForeground(TEXTO_OSCURO);

        JLabel lblWelcomeSub = new JLabel("Bienvenido al panel de administración de NekoMart.");
        lblWelcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblWelcomeSub.setForeground(TEXTO_GRIS);

        panelWelcome.add(lblWelcomeTitle);
        panelWelcome.add(lblWelcomeSub);

        JPanel panelDashboard = new JPanel(new BorderLayout());
        panelDashboard.setOpaque(false);
        panelDashboard.add(panelWelcome, BorderLayout.NORTH);
        panelDashboard.add(dashboard, BorderLayout.CENTER);

        // Agregar todos los paneles al CardLayout
        contentPanel.add(panelDashboard, "dashboard");
        contentPanel.add(ventasFrame, "ventas");
        contentPanel.add(inventarioFrame, "inventario");
        contentPanel.add(usuariosFrame, "usuarios");
        contentPanel.add(historialFrame, "historial");
        contentPanel.add(profilePanel, "perfil"); // ← AGREGADO

        if (rol.equals("ADMIN") && corteCajaFrame != null) {
            contentPanel.add(corteCajaFrame, "corte");
        }

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "dashboard");
    }

    private void agregarBotonSidebar(String texto, String idPanel, String tipoIcono, boolean activo) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getBackground().equals(AZUL_POS)) {
                    g2.setColor(AZUL_POS);
                } else if (getModel().isRollover()) {
                    g2.setColor(SIDEBAR_HOVER);
                } else {
                    g2.setColor(SIDEBAR_COLOR);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());

                dibujarIconoMenu(g2, tipoIcono, 18, 11);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.drawString(texto, 48, 25);
                g2.dispose();
            }
        };

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(240, 40));
        btn.setMinimumSize(new Dimension(240, 40));
        btn.setPreferredSize(new Dimension(240, 40));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBackground(activo ? AZUL_POS : SIDEBAR_COLOR);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addActionListener(e -> {
            for (Component c : sidebarPanel.getComponents()) {
                if (c instanceof JButton) {
                    c.setBackground(SIDEBAR_COLOR);
                    c.repaint();
                }
            }
            btn.setBackground(AZUL_POS);
            btn.repaint();
            cardLayout.show(contentPanel, idPanel);
            
              // ← AGREGAR ESTAS LÍNEAS: Forzar actualización al cambiar de panel
    if (idPanel.equals("dashboard") && dashboard != null) {
        dashboard.cargarDatos();
    } else if (idPanel.equals("corte") && corteCajaFrame != null) {
        corteCajaFrame.cargarEstado();
    }
});

        sidebarPanel.add(btn);
    }

    private JButton crearBotonPerfil(Usuario usuario) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getModel().isRollover() ? new Color(71, 85, 105) : new Color(51, 65, 85));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.fillOval(18, 10, 16, 16);
                g2.fillArc(12, 26, 28, 16, 0, 180);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.drawString("Mi Perfil", 50, 25);
                g2.dispose();
            }
        };

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(240, 40));
        btn.setMinimumSize(new Dimension(240, 40));
        btn.setPreferredSize(new Dimension(240, 40));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBackground(new Color(51, 65, 85));
        
        // ← CAMBIADO: Ahora navega al panel en lugar de abrir ventana
       btn.addActionListener(e -> {
    cardLayout.show(contentPanel, "perfil");
});

        return btn;
    }

    private void dibujarIconoMenu(Graphics2D g2, String tipo, int x, int y) {
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.WHITE);

        switch (tipo) {
            case "dashboard":
                g2.fillRect(x, y, 9, 9);
                g2.fillRect(x + 11, y, 9, 9);
                g2.fillRect(x, y + 11, 9, 9);
                g2.fillRect(x + 11, y + 11, 9, 9);
                break;
            case "ventas":
                g2.drawLine(x, y + 6, x + 6, y + 6);
                g2.drawLine(x + 6, y + 6, x + 8, y + 14);
                g2.drawLine(x + 8, y + 14, x + 18, y + 14);
                g2.drawLine(x + 18, y + 14, x + 20, y + 6);
                g2.drawLine(x + 20, y + 6, x + 8, y + 6);
                g2.fillOval(x + 10, y + 16, 4, 4);
                g2.fillOval(x + 16, y + 16, 4, 4);
                break;
            case "inventario":
                g2.drawRect(x + 2, y + 4, 18, 14);
                g2.drawLine(x + 2, y + 10, x + 20, y + 10);
                g2.drawLine(x + 11, y + 4, x + 11, y + 18);
                break;
            case "usuarios":
                g2.fillOval(x + 4, y + 2, 7, 7);
                g2.fillArc(x, y + 10, 14, 10, 0, 180);
                g2.fillOval(x + 11, y + 4, 5, 5);
                g2.fillArc(x + 9, y + 11, 10, 8, 0, 180);
                break;
            case "historial":
                g2.drawRect(x + 3, y + 2, 14, 18);
                g2.drawLine(x + 6, y + 6, x + 14, y + 6);
                g2.drawLine(x + 6, y + 10, x + 14, y + 10);
                g2.drawLine(x + 6, y + 14, x + 12, y + 14);
                break;
            case "corte":
                g2.drawRect(x + 2, y + 6, 16, 10);
                g2.fillOval(x + 8, y + 9, 4, 4);
                g2.drawLine(x + 6, y + 6, x + 6, y + 4);
                g2.drawLine(x + 14, y + 6, x + 14, y + 4);
                break;
        }
    }

    private void dibujarCarritoSidebar(Graphics2D g2, int x, int y, int size) {
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x, y + 8, x + 8, y + 8);
        g2.drawLine(x + 8, y + 8, x + 12, y + 24);
        g2.drawLine(x + 12, y + 24, x + size - 8, y + 24);
        g2.drawLine(x + size - 8, y + 24, x + size, y + 10);
        g2.drawLine(x + size, y + 10, x + 8, y + 10);
        g2.fillOval(x + 14, y + 26, 6, 6);
        g2.fillOval(x + size - 18, y + 26, 6, 6);
    }

    private String obtenerIniciales(Usuario usuario) {
        if (usuario == null || usuario.getNombreCompleto() == null) return "U";
        String[] partes = usuario.getNombreCompleto().split(" ");
        if (partes.length >= 2) {
            return (partes[0].charAt(0) + "" + partes[1].charAt(0)).toUpperCase();
        }
        return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
    }
}
