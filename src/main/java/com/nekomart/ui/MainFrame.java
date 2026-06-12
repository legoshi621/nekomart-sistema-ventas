package com.nekomart.ui;

import com.nekomart.Main;
import com.nekomart.models.Usuario;
import com.nekomart.utils.SessionManager;
import com.nekomart.services.UsuarioService;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;

/**
 * Pantalla principal del sistema NekoMart con diseño pastel moderno.
 * Barra superior con logo, información de usuario, botón de tema claro/oscuro y botones de contraseña/cerrar sesión.
 * Navegación por pestañas con colores adaptables lavanda (activa) y blanco/gris (inactiva).
 * Todo el código está comentado en español.
 */
public class MainFrame extends JFrame {

    // ── Colores de la paleta pastel ──────────────────────────────────────
    private static final Color LAVANDA = new Color(184, 169, 232);
    private static final Color LAVANDA_CLARO = new Color(212, 196, 240);
    private static final Color CORAL = new Color(255, 139, 148);
    private static final Color CORAL_HOVER = new Color(255, 107, 116);
    private static final Color FONDO = new Color(250, 250, 250);
    private static final Color TEXTO_OSCURO = new Color(45, 55, 72);
    private static final Color TEXTO_GRIS = new Color(113, 128, 150);
    private static final Color BORDE = new Color(226, 232, 240);

    // ── Componentes de la interfaz ───────────────────────────────────────
    private JButton btnCerrarSesion;
    private JButton btnCambiarPassword;
    private JButton btnTema;
    private JTabbedPane tabbedPane;
    private DashboardFrame dashboard;
    
    // Paneles y etiquetas principales expuestos para actualizaciones de tema
    private JPanel panelSuperior;
    private JPanel panelInicio;
    private JLabel lblAppName;
    private JLabel lblUserInfo;
    private JLabel lblWelcomeTitle;
    private JLabel lblWelcomeSub;

    public MainFrame() {
        setTitle("NekoMart - Sistema de Ventas");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        
        // Aplicar el tema inicial
        reaplicarTemaMainFrame();
    }

    /**
     * Inicializa y organiza los componentes principales de la interfaz.
     */
    private void initComponents() {
        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();

        // ══════════════════════════════════════════════════════════════════
        // 1. BARRA SUPERIOR — Fondo adaptable, altura 60px, borde inferior
        // ══════════════════════════════════════════════════════════════════
        panelSuperior = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Dibujar borde inferior sutil con color adaptado
                g.setColor(Main.isDarkMode ? new Color(60, 60, 60) : BORDE);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setPreferredSize(new Dimension(0, 60));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;

        // ── Logo: gatito pequeño dibujado ─────────────────────────────────
        JPanel panelLogoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                dibujarGatitoPequeno(g2, 16, 16, 12);
                g2.dispose();
            }
        };
        panelLogoIcon.setOpaque(false);
        panelLogoIcon.setPreferredSize(new Dimension(32, 32));
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        panelSuperior.add(panelLogoIcon, gbc);

        // ── Nombre de la aplicación ──────────────────────────────────────
        lblAppName = new JLabel("NekoMart");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblAppName.setForeground(new Color(120, 100, 180)); // Lavanda oscuro
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panelSuperior.add(lblAppName, gbc);

        // ── Espaciador expansivo ─────────────────────────────────────────
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        panelSuperior.add(Box.createGlue(), gbc);

        // ── Información del usuario ──────────────────────────────────────
        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 15);
        lblUserInfo = new JLabel("👤 " + (usuario != null ? usuario.getNombreCompleto() : "Invitado") + 
                " (" + (usuario != null ? usuario.getRol() : "Ninguno") + ")");
        lblUserInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUserInfo.setForeground(TEXTO_OSCURO);
        panelSuperior.add(lblUserInfo, gbc);

        // ── Botón "🌓" para alternar tema ─────────────────────────────
        btnTema = new JButton(Main.isDarkMode ? "☀️" : "🌙") {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color accentColor = Main.isDarkMode ? new Color(255, 183, 77) : LAVANDA;
                if (hover) {
                    g2.setColor(accentColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(accentColor);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnTema.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btnTema.setContentAreaFilled(false);
        btnTema.setBorderPainted(false);
        btnTema.setFocusPainted(false);
        btnTema.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTema.setPreferredSize(new Dimension(50, 35));
        btnTema.addActionListener(e -> {
            Main.cambiarTema();
            reaplicarTemaMainFrame();
        });

        // ── Botón "Cambiar Contraseña" con estilo lavanda outline ─────────
        btnCambiarPassword = new JButton("🔒 Cambiar Contraseña") {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover) {
                    g2.setColor(LAVANDA);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(LAVANDA);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnCambiarPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCambiarPassword.setForeground(LAVANDA);
        btnCambiarPassword.setContentAreaFilled(false);
        btnCambiarPassword.setBorderPainted(false);
        btnCambiarPassword.setFocusPainted(false);
        btnCambiarPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCambiarPassword.setPreferredSize(new Dimension(160, 35));
        btnCambiarPassword.addActionListener(e -> mostrarDialogoCambiarPassword(usuario));

        // ── Botón "Cerrar Sesión" con estilo coral outline ───────────────
        btnCerrarSesion = new JButton("Cerrar Sesión") {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hover) {
                    g2.setColor(CORAL);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(CORAL);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrarSesion.setForeground(CORAL);
        btnCerrarSesion.setContentAreaFilled(false);
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setPreferredSize(new Dimension(130, 35));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        // Colocar botones en la barra superior
        gbc.gridx = 4;
        gbc.insets = new Insets(0, 0, 0, 10);
        panelSuperior.add(btnTema, gbc);

        gbc.gridx = 5;
        gbc.insets = new Insets(0, 0, 0, 10);
        panelSuperior.add(btnCambiarPassword, gbc);

        gbc.gridx = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        panelSuperior.add(btnCerrarSesion, gbc);

        // ══════════════════════════════════════════════════════════════════
        // 2. DASHBOARD — Instanciar panel de estadísticas
        // ══════════════════════════════════════════════════════════════════
        dashboard = new DashboardFrame();

        // ══════════════════════════════════════════════════════════════════
        // 3. PANEL DE INICIO — Bienvenida + Dashboard
        // ══════════════════════════════════════════════════════════════════
        panelInicio = new JPanel(new BorderLayout(15, 15));
        panelInicio.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelInicio.setBackground(FONDO);

        // Panel de bienvenida con saludo y descripción
        JPanel panelWelcome = new JPanel(new GridLayout(2, 1, 5, 5));
        panelWelcome.setOpaque(false);

        lblWelcomeTitle = new JLabel("¡Hola, " + (usuario != null ? usuario.getNombreCompleto() : "Usuario") + "! 👋");
        lblWelcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcomeTitle.setForeground(TEXTO_OSCURO);

        lblWelcomeSub = new JLabel("Bienvenido al panel de administración de NekoMart. Aquí tienes el resumen del día.");
        lblWelcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWelcomeSub.setForeground(TEXTO_GRIS);

        panelWelcome.add(lblWelcomeTitle);
        panelWelcome.add(lblWelcomeSub);

        panelInicio.add(panelWelcome, BorderLayout.NORTH);
        panelInicio.add(dashboard, BorderLayout.CENTER);

        // ══════════════════════════════════════════════════════════════════
        // 4. TABBEDPANE — Pestañas con colores personalizados
        // ══════════════════════════════════════════════════════════════════
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(FONDO);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Aplicar UI personalizada para pestañas lavanda/blanco/gris oscuro
        tabbedPane.setUI(new PastelTabbedPaneUI());

        String rol = (usuario != null) ? usuario.getRol().toUpperCase() : "EMPLEADO";

        if (rol.equals("ADMIN")) {
            // ADMIN ve: Dashboard, Inventario, Usuarios, Ventas, Historial
            tabbedPane.addTab("📊 Dashboard", panelInicio);
            tabbedPane.addTab("📦 Inventario", new InventarioFrame());
            tabbedPane.addTab("👥 Usuarios", new UsuariosFrame());
            tabbedPane.addTab("🛒 Ventas (POS)", new VentasFrame());
            tabbedPane.addTab("📋 Historial", new HistorialVentasFrame());
        } else {
            // EMPLEADO ve: Dashboard, Ventas, Historial
            tabbedPane.addTab("📊 Dashboard", panelInicio);
            tabbedPane.addTab("🛒 Ventas (POS)", new VentasFrame());
            tabbedPane.addTab("📋 Historial", new HistorialVentasFrame());
        }

        // Refrescar el Dashboard al seleccionar la pestaña correspondiente (índice 0)
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                dashboard.cargarDatos();
            }
        });

        // ══════════════════════════════════════════════════════════════════
        // 5. ORGANIZAR LAYOUT PRINCIPAL
        // ══════════════════════════════════════════════════════════════════
        setLayout(new BorderLayout());
        add(panelSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Re-aplica colores a los componentes de MainFrame que anulan el L&F por defecto.
     */
    public void reaplicarTemaMainFrame() {
        Color fondo = Main.isDarkMode ? new Color(0x1E, 0x1E, 0x1E) : FONDO;
        Color card = Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE;
        Color textClaro = Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO;
        Color textGris = Main.isDarkMode ? new Color(170, 170, 170) : TEXTO_GRIS;

        if (panelSuperior != null) {
            panelSuperior.setBackground(card);
        }
        if (lblAppName != null) {
            lblAppName.setForeground(Main.isDarkMode ? new Color(184, 169, 232) : new Color(120, 100, 180));
        }
        if (lblUserInfo != null) {
            lblUserInfo.setForeground(textClaro);
        }
        if (panelInicio != null) {
            panelInicio.setBackground(fondo);
        }
        if (lblWelcomeTitle != null) {
            lblWelcomeTitle.setForeground(textClaro);
        }
        if (lblWelcomeSub != null) {
            lblWelcomeSub.setForeground(textGris);
        }
        if (tabbedPane != null) {
            tabbedPane.setBackground(fondo);
        }
        if (btnTema != null) {
            btnTema.setText(Main.isDarkMode ? "☀️" : "🌙");
            btnTema.repaint();
        }
    }



    /**
     * Dibuja un gatito pequeño para el logo de la barra superior.
     *
     * @param g2   Contexto gráfico 2D
     * @param cx   Centro X
     * @param cy   Centro Y
     * @param size Tamaño base
     */
    private void dibujarGatitoPequeno(Graphics2D g2, int cx, int cy, int size) {
        // Cara del gato (círculo)
        g2.setColor(LAVANDA);
        g2.fillOval(cx - size, cy - size + 3, size * 2, size * 2);

        // Oreja izquierda
        Path2D orejaIzq = new Path2D.Double();
        orejaIzq.moveTo(cx - size + 3, cy - size + 6);
        orejaIzq.lineTo(cx - size / 2 - 2, cy - size - 7);
        orejaIzq.lineTo(cx - 2, cy - size + 6);
        orejaIzq.closePath();
        g2.fill(orejaIzq);

        // Oreja derecha
        Path2D orejaDer = new Path2D.Double();
        orejaDer.moveTo(cx + 2, cy - size + 6);
        orejaDer.lineTo(cx + size / 2 + 2, cy - size - 7);
        orejaDer.lineTo(cx + size - 3, cy - size + 6);
        orejaDer.closePath();
        g2.fill(orejaDer);

        // Ojos
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 7, cy - 3, 6, 6);
        g2.fillOval(cx + 1, cy - 3, 6, 6);
        g2.setColor(TEXTO_OSCURO);
        g2.fillOval(cx - 5, cy - 1, 3, 3);
        g2.fillOval(cx + 3, cy - 1, 3, 3);

        // Nariz
        g2.setColor(CORAL);
        g2.fillOval(cx - 2, cy + 3, 4, 3);
    }

    /**
     * Termina la sesión actual y regresa a la pantalla de Login.
     */
    private void cerrarSesion() {
        SessionManager.getInstancia().cerrarSesion();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        this.dispose();
    }

    /**
     * Muestra un diálogo emergente con diseño moderno para cambiar la contraseña del usuario.
     *
     * @param usuario El usuario actual en sesión.
     */
    private void mostrarDialogoCambiarPassword(Usuario usuario) {
        if (usuario == null) return;
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(320, 150));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 6, 6, 6);

        JLabel lblActual = new JLabel("Contraseña Actual:");
        lblActual.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblActual.setForeground(Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO);
        JPasswordField txtActual = new JPasswordField(15);
        txtActual.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblNueva = new JLabel("Nueva Contraseña:");
        lblNueva.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNueva.setForeground(Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO);
        JPasswordField txtNueva = new JPasswordField(15);
        txtNueva.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblConfirmar = new JLabel("Confirmar Nueva:");
        lblConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblConfirmar.setForeground(Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO);
        JPasswordField txtConfirmar = new JPasswordField(15);
        txtConfirmar.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        c.gridx = 0; c.gridy = 0; c.weightx = 0.4;
        panel.add(lblActual, c);
        c.gridx = 1; c.weightx = 0.6;
        panel.add(txtActual, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0.4;
        panel.add(lblNueva, c);
        c.gridx = 1; c.weightx = 0.6;
        panel.add(txtNueva, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0.4;
        panel.add(lblConfirmar, c);
        c.gridx = 1; c.weightx = 0.6;
        panel.add(txtConfirmar, c);

        int opcion = JOptionPane.showConfirmDialog(
                this,
                panel,
                "🔒 Cambiar Contraseña",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion == JOptionPane.OK_OPTION) {
            String actual = new String(txtActual.getPassword()).trim();
            String nueva = new String(txtNueva.getPassword()).trim();
            String confirmar = new String(txtConfirmar.getPassword()).trim();

            // 1. Validar campos no vacíos
            if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Todos los campos son obligatorios.",
                        "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Validar longitud de la nueva contraseña
            if (nueva.length() < 4) {
                JOptionPane.showMessageDialog(this,
                        "La nueva contraseña debe tener al menos 4 caracteres.",
                        "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Validar que la nueva y confirmación coincidan
            if (!nueva.equals(confirmar)) {
                JOptionPane.showMessageDialog(this,
                        "La nueva contraseña y la confirmación no coinciden.",
                        "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 4. Llamar al servicio para cambiar la contraseña
            UsuarioService service = new UsuarioService();
            if (service.cambiarPassword(usuario.getId(), actual, nueva)) {
                JOptionPane.showMessageDialog(this,
                        "Contraseña cambiada exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo cambiar la contraseña. Verifica que la contraseña actual sea correcta.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI PERSONALIZADA PARA EL TABBEDPANE CON COLORES PASTEL ADAPTABLES
    // ══════════════════════════════════════════════════════════════════════

    /**
     * UI personalizada para JTabbedPane que pinta pestañas con colores pastel adaptables:
     * - Pestaña activa: fondo lavanda (#B8A9E8), texto blanco
     * - Pestaña inactiva: fondo blanco (claro) / #2D2D2D (oscuro), texto gris
     */
    private class PastelTabbedPaneUI extends BasicTabbedPaneUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();
            // Configurar alturas y márgenes de las pestañas
            tabAreaInsets = new Insets(8, 15, 0, 15);
            contentBorderInsets = new Insets(0, 0, 0, 0);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabInsets = new Insets(10, 16, 10, 16);
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
            // No pintar borde por defecto (usamos paintTabBackground)
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                // Pestaña activa: fondo lavanda con bordes redondeados arriba
                g2.setColor(LAVANDA);
                g2.fillRoundRect(x + 2, y + 2, w - 4, h, 12, 12);
            } else {
                // Pestaña inactiva: fondo adaptado al tema actual
                g2.setColor(Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE);
                g2.fillRoundRect(x + 2, y + 2, w - 4, h, 12, 12);
            }
            g2.dispose();
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                 int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (isSelected) {
                g2.setColor(Color.WHITE); // Texto blanco en pestaña activa
            } else {
                // Texto gris en pestaña inactiva adaptable
                g2.setColor(Main.isDarkMode ? new Color(170, 170, 170) : TEXTO_GRIS);
            }

            g2.setFont(font);
            g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            g2.dispose();
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // No pintar borde del contenido para un look más limpio
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                           int tabIndex, Rectangle iconRect, Rectangle textRect,
                                           boolean isSelected) {
            // No pintar indicador de foco
        }

        @Override
        protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
            return 42; // Altura fija para todas las pestañas
        }
    }
}