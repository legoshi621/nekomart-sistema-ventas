package com.nekomart.ui;

import com.nekomart.models.Usuario;
import com.nekomart.utils.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Pantalla principal del sistema NekoMart.
 * Incorpora un diseño moderno con cabecera corporativa, alternador de tema claro/oscuro
 * y pestaña de bienvenida con estadísticas en tiempo real.
 * Todo el código está comentado en español.
 */
public class MainFrame extends JFrame {
    private JLabel lblBienvenido;
    private JLabel lblRol;
    private JButton btnCerrarSesion;
    private JTabbedPane tabbedPane;
    private DashboardFrame dashboard;

    // Estado del tema oscuro
    private boolean isDarkMode = false;

    public MainFrame() {
        setTitle("NekoMart - Sistema de Ventas");
        setSize(1150, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    /**
     * Inicializa y organiza los componentes principales de la interfaz.
     */
    private void initComponents() {
        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        
        // 1. Logotipo pequeño para la barra superior
        JLabel lblLogo = new JLabel();
        java.net.URL logoURL = getClass().getResource("/nekomart_logo.png");
        if (logoURL != null) {
            ImageIcon logoIcon = new ImageIcon(logoURL);
            Image scaledImg = logoIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(scaledImg));
        } else {
            lblLogo.setText("🐱");
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        }

        // 2. Título de la aplicación
        JLabel lblAppName = new JLabel("NekoMart POS");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAppName.setForeground(Color.WHITE);

        // 3. Cabecera moderna con diseño azul corporativo
        JPanel panelSuperior = new JPanel(new GridBagLayout());
        panelSuperior.setBackground(new Color(30, 58, 138)); // Azul corporativo (#1e3a8a)
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        
        // Agregar logo
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        panelSuperior.add(lblLogo, gbc);

        // Agregar nombre
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panelSuperior.add(lblAppName, gbc);

        // Espaciador expansivo para empujar elementos al extremo derecho
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        panelSuperior.add(Box.createGlue(), gbc);

        // Información de usuario
        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 20);
        JLabel lblUserInfo = new JLabel("👤 " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
        lblUserInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUserInfo.setForeground(new Color(229, 231, 235)); // Gris claro
        panelSuperior.add(lblUserInfo, gbc);

        // Botón de alternancia de tema (☀️ / 🌙)
        gbc.gridx = 4;
        gbc.insets = new Insets(0, 0, 0, 15);
        JButton btnTheme = new JButton("🌙");
        btnTheme.setToolTipText("Alternar Tema Claro/Oscuro");
        btnTheme.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTheme.putClientProperty("JButton.buttonType", "roundRect");
        panelSuperior.add(btnTheme, gbc);

        // Botón de Cerrar Sesión
        gbc.gridx = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setBackground(new Color(220, 38, 38)); // Rojo para advertencia/cierre
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrarSesion.putClientProperty("JButton.buttonType", "roundRect");
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelSuperior.add(btnCerrarSesion, gbc);

        // 4. Instanciar el Dashboard
        dashboard = new DashboardFrame();

        // 5. Crear el Panel de Inicio / Bienvenida
        JPanel panelInicio = new JPanel(new BorderLayout(15, 15));
        panelInicio.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel panelWelcome = new JPanel(new GridLayout(2, 1, 5, 5));
        panelWelcome.setOpaque(false);
        
        JLabel lblWelcomeTitle = new JLabel("¡Hola, " + usuario.getNombreCompleto() + "!");
        lblWelcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel lblWelcomeSub = new JLabel("Te damos la bienvenida al panel de administración de NekoMart. Aquí tienes el resumen financiero de hoy:");
        lblWelcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWelcomeSub.setForeground(Color.GRAY);
        
        panelWelcome.add(lblWelcomeTitle);
        panelWelcome.add(lblWelcomeSub);
        
        panelInicio.add(panelWelcome, BorderLayout.NORTH);
        panelInicio.add(dashboard, BorderLayout.CENTER);

        // 6. Configuración del TabbedPane con FlatLaf
        tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("JTabbedPane.showTabSeparators", true);
        tabbedPane.putClientProperty("JTabbedPane.tabHeight", 38);
        tabbedPane.putClientProperty("JTabbedPane.tabType", "card");

        // Pestaña principal de Inicio (Dashboard) visible para todos
        tabbedPane.addTab("🏠 Inicio", panelInicio);

        String rol = usuario.getRol().toUpperCase();
        if (rol.equals("ADMIN")) {
            // El administrador tiene acceso total
            tabbedPane.addTab("📦 Inventario", new InventarioFrame());
            tabbedPane.addTab("👥 Usuarios", new UsuariosFrame());
            tabbedPane.addTab("🛒 Ventas (POS)", new VentasFrame());
            tabbedPane.addTab("📋 Historial", new HistorialVentasFrame());
        } else {
            // El empleado solo ve Ventas e Historial
            tabbedPane.addTab("🛒 Ventas (POS)", new VentasFrame());
            tabbedPane.addTab("📋 Historial", new HistorialVentasFrame());
        }

        // Listener para refrescar automáticamente el Dashboard al seleccionar la pestaña "Inicio"
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                dashboard.refrescar();
            }
        });

        // 7. Organizar en la ventana
        setLayout(new BorderLayout());
        add(panelSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // 8. Evento del botón de cerrar sesión
        btnCerrarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarSesion();
            }
        });

        // 9. Evento para alternancia de tema (Claro / Oscuro)
        btnTheme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (isDarkMode) {
                        UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                        btnTheme.setText("🌙");
                        isDarkMode = false;
                    } else {
                        UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                        btnTheme.setText("☀️");
                        isDarkMode = true;
                    }
                    
                    // Actualizar todas las ventanas abiertas en tiempo real
                    for (Window window : Window.getWindows()) {
                        SwingUtilities.updateComponentTreeUI(window);
                    }
                    
                    // Actualizar estilos internos en caliente
                    dashboard.refrescar();
                } catch (Exception ex) {
                    System.err.println("Error al cambiar de tema: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * Termina la sesión actual y vuelve a la pantalla de Login.
     */
    private void cerrarSesion() {
        SessionManager.getInstancia().cerrarSesion();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        this.dispose();
    }
}