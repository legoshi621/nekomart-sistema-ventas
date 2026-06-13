package com.nekomart.ui;

import com.nekomart.Main;
import com.nekomart.models.Usuario;
import com.nekomart.services.AuthService;
import com.nekomart.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.io.File;

/**
 * Pantalla de inicio de sesión (Login) con diseño pastel moderno.
 * Cuenta con gradiente adaptable a temas claro/oscuro, panel de tarjeta redondeado con sombra,
 * carga de logo (con fallback a gatito dibujado si no se encuentra), campos estilizados y botón coral.
 * Incluye un botón para cambiar entre tema claro y oscuro de manera dinámica.
 * Todo el código está comentado en español.
 */
public class LoginFrame extends JFrame {

    // ── Colores de la paleta pastel ──────────────────────────────────────
    private static final Color LAVANDA = new Color(184, 169, 232);
    private static final Color LAVANDA_CLARO = new Color(212, 196, 240);
    private static final Color ROSA_PASTEL = new Color(255, 209, 220);
    private static final Color CORAL = new Color(255, 139, 148);
    private static final Color CORAL_HOVER = new Color(255, 107, 116);
    private static final Color TEXTO_OSCURO = new Color(45, 55, 72);
    private static final Color TEXTO_GRIS = new Color(113, 128, 150);
    private static final Color BORDE_CAMPO = new Color(226, 232, 240);

    // ── Componentes de la interfaz ───────────────────────────────────────
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIniciarSesion;
    private JPanel panelFondo;
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private JButton btnSalir;
    private JButton btnTema;

    // Instancia del servicio de autenticación
    private AuthService authService;

    // Panel central con animación de entrada
    private ShadowPanel panelCard;

    // Variables para arrastrar la ventana sin decoración
    private Point puntoInicialArrastre;

    public LoginFrame() {
        authService = new AuthService();

        // Configuración básica del JFrame sin decoración para look moderno
        setTitle("NekoMart - Iniciar Sesión");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
        setResizable(false);
        setUndecorated(true); // Sin borde del sistema operativo

        // Inicializar la interfaz de usuario
        initComponents();

        // Aplicar el tema actual inicial en los componentes
        reaplicarTemaLogin();

        // Iniciar animación de deslizamiento vertical al cargar
        iniciarAnimacionCarga();
    }

    /**
     * Intenta cargar el archivo de logo desde recursos o ruta relativa.
     *
     * @return ImageIcon cargado o null si no existe.
     */
    private ImageIcon obtenerLogo() {
        try {
            // Intentar cargar desde el classpath (JAR / clases compiladas)
            java.net.URL logoUrl = getClass().getResource("/nekomart_logo.png");
            if (logoUrl != null) {
                return new ImageIcon(logoUrl);
            }
            
            // Intentar cargar directo de la ruta física en desarrollo
            File file = new File("src/main/resources/nekomart_logo.png");
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }

            // Fallback para archivo JPG
            logoUrl = getClass().getResource("/nekomart_logo.jpg");
            if (logoUrl != null) {
                return new ImageIcon(logoUrl);
            }
            file = new File("src/main/resources/nekomart_logo.jpg");
            if (file.exists()) {
                return new ImageIcon(file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error al intentar buscar el logo: " + e.getMessage());
        }
        return null;
    }

    /**
     * Construye y organiza todos los componentes visuales del login.
     */
    private void initComponents() {
        // ── Panel de fondo con gradiente dinámico según tema ─────────────
        panelFondo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (Main.isDarkMode) {
                    // Gradiente oscuro sutil
                    GradientPaint gp = new GradientPaint(
                            0, 0, new Color(0x12, 0x12, 0x12),
                            getWidth(), getHeight(), new Color(0x24, 0x24, 0x24)
                    );
                    g2.setPaint(gp);
                } else {
                    // Gradiente diagonal suave de lavanda claro a rosa pastel
                    GradientPaint gp = new GradientPaint(
                            0, 0, LAVANDA_CLARO,
                            getWidth(), getHeight(), ROSA_PASTEL
                    );
                    g2.setPaint(gp);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panelFondo.setLayout(new GridBagLayout());

        // Permitir arrastrar la ventana sin decoración
        panelFondo.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                puntoInicialArrastre = e.getPoint();
            }
        });
        panelFondo.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Calcular nueva posición de la ventana
                int x = getLocation().x + e.getX() - puntoInicialArrastre.x;
                int y = getLocation().y + e.getY() - puntoInicialArrastre.y;
                setLocation(x, y);
            }
        });

        // ── Botón superior para cambiar de tema (Claro / Oscuro) ─────────
        btnTema = new JButton("🌓");
        btnTema.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btnTema.setContentAreaFilled(false);
        btnTema.setBorderPainted(false);
        btnTema.setFocusPainted(false);
        btnTema.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTema.addActionListener(e -> {
            Main.cambiarTema();
            reaplicarTemaLogin();
        });

        // ── Panel tipo tarjeta blanca/gris con sombra suave ───────────────────
        panelCard = new ShadowPanel();
        panelCard.setLayout(new GridBagLayout());
        panelCard.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        // ── 1. Logo de la aplicación (Cargado de recursos o dibujado) ────
        JPanel panelLogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ImageIcon logo = obtenerLogo();
                if (logo != null) {
                    // Escalar el logo a dimensiones máximas de 80x80 manteniendo relación de aspecto
                    int iw = logo.getIconWidth();
                    int ih = logo.getIconHeight();
                    int maxDim = 80;
                    int nw = iw;
                    int nh = ih;
                    if (iw > maxDim || ih > maxDim) {
                        if (iw > ih) {
                            nw = maxDim;
                            nh = (ih * maxDim) / iw;
                        } else {
                            nh = maxDim;
                            nw = (iw * maxDim) / ih;
                        }
                    }
                    int x = (getWidth() - nw) / 2;
                    int y = (getHeight() - nh) / 2;
                    g2.drawImage(logo.getImage(), x, y, nw, nh, null);
                } else {
                    // Fallback a dibujo del gatito si no existe el archivo
                    dibujarGatito(g2, getWidth() / 2, getHeight() / 2, 35);
                }
                g2.dispose();
            }
        };
        panelLogo.setOpaque(false);
        panelLogo.setPreferredSize(new Dimension(100, 90));
        gbc.gridy = 0;
        panelCard.add(panelLogo, gbc);

        // ── 2. Título "NekoMart" ─────────────────────────────────────────
        ImageIcon imgLogo = obtenerLogo();
        lblTitulo = new JLabel(imgLogo != null ? "NekoMart" : "🐱 NekoMart", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 0, 0);
        panelCard.add(lblTitulo, gbc);

        // ── 3. Subtítulo "Sistema de Ventas" ─────────────────────────────
        lblSubtitulo = new JLabel("Sistema de Ventas", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 25, 0);
        panelCard.add(lblSubtitulo, gbc);

        // ── 4. Campo de usuario ──────────────────────────────────────────
        txtUsuario = new JTextField(20);
        txtUsuario.putClientProperty("JTextField.placeholderText", "Nombre de usuario");
        txtUsuario.putClientProperty("JTextField.showClearButton", true);
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 0, 4, 0);
        panelCard.add(txtUsuario, gbc);

        // ── 5. Campo de contraseña ───────────────────────────────────────
        txtPassword = new JPasswordField(20);
        txtPassword.putClientProperty("JTextField.placeholderText", "Contraseña");
        txtPassword.putClientProperty("JTextField.showRevealButton", true);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 4;
        panelCard.add(txtPassword, gbc);

        // Atajo: Enter en campo de contraseña para iniciar sesión
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnIniciarSesion.doClick();
                }
            }
        });

        // ── 6. Botón "Iniciar Sesión" con estilo coral ───────────────────
        btnIniciarSesion = new JButton("Iniciar Sesión") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar fondo redondeado (25px de arco)
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(CORAL_HOVER);
                } else {
                    g2.setColor(CORAL);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Dibujar texto centrado
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnIniciarSesion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIniciarSesion.setForeground(Color.WHITE);
        btnIniciarSesion.setContentAreaFilled(false);
        btnIniciarSesion.setBorderPainted(false);
        btnIniciarSesion.setFocusPainted(false);
        btnIniciarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIniciarSesion.setPreferredSize(new Dimension(0, 45));
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 0, 5, 0);
        panelCard.add(btnIniciarSesion, gbc);

        // ── 7. Botón "Salir" discreto ────────────────────────────────────
        btnSalir = new JButton("Salir");
        btnSalir.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSalir.setContentAreaFilled(false);
        btnSalir.setBorderPainted(false);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 0, 0, 0);
        panelCard.add(btnSalir, gbc);

        // ── Eventos de clic ──────────────────────────────────────────────
        btnIniciarSesion.addActionListener(e -> realizarLogin());
        btnSalir.addActionListener(e -> System.exit(0));

        // ── GridBagConstraints para botón Tema y Tarjeta en fondo ─────────
        GridBagConstraints gbcTema = new GridBagConstraints();
        gbcTema.gridx = 0;
        gbcTema.gridy = 0;
        gbcTema.anchor = GridBagConstraints.NORTHEAST;
        gbcTema.insets = new Insets(15, 15, 0, 15);
        panelFondo.add(btnTema, gbcTema);

        GridBagConstraints gbcCard = new GridBagConstraints();
        gbcCard.gridx = 0;
        gbcCard.gridy = 1;
        gbcCard.insets = new Insets(0, 0, 20, 0);
        panelFondo.add(panelCard, gbcCard);

        // Añadir el panel de fondo al JFrame
        add(panelFondo);
    }

    /**
     * Adapta la apariencia de la pantalla según `Main.isDarkMode` esté activo.
     */
    public void reaplicarTemaLogin() {
        Color fgColor = Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO;
        Color textGris = Main.isDarkMode ? new Color(170, 170, 170) : TEXTO_GRIS;
        Color bgField = Main.isDarkMode ? new Color(0x3D, 0x3D, 0x3D) : Color.WHITE;
        Color borderField = Main.isDarkMode ? new Color(0x55, 0x55, 0x55) : BORDE_CAMPO;

        // Estilos para el campo Usuario
        txtUsuario.setBackground(bgField);
        txtUsuario.setForeground(fgColor);
        txtUsuario.setCaretColor(fgColor);
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderField, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Estilos para el campo Password
        txtPassword.setBackground(bgField);
        txtPassword.setForeground(fgColor);
        txtPassword.setCaretColor(fgColor);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderField, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Estilos de textos y botones
        lblTitulo.setForeground(Main.isDarkMode ? Color.WHITE : new Color(120, 100, 180));
        lblSubtitulo.setForeground(textGris);
        btnSalir.setForeground(textGris);
        btnTema.setForeground(Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO);

        // Repintar fondo y tarjeta
        panelFondo.repaint();
        panelCard.repaint();
    }

    /**
     * Dibuja un gatito estilizado con Graphics2D.
     * Usa formas geométricas simples en tonos lavanda.
     *
     * @param g2   Contexto gráfico 2D
     * @param cx   Centro X del dibujo
     * @param cy   Centro Y del dibujo
     * @param size Tamaño base del gatito
     */
    private void dibujarGatito(Graphics2D g2, int cx, int cy, int size) {
        // Cara del gato (círculo principal)
        g2.setColor(LAVANDA);
        g2.fillOval(cx - size, cy - size + 5, size * 2, size * 2);

        // Oreja izquierda (triángulo)
        Path2D orejaIzq = new Path2D.Double();
        orejaIzq.moveTo(cx - size + 5, cy - size + 10);
        orejaIzq.lineTo(cx - size / 2 - 5, cy - size - 18);
        orejaIzq.lineTo(cx - 5, cy - size + 10);
        orejaIzq.closePath();
        g2.fill(orejaIzq);

        // Oreja derecha (triángulo)
        Path2D orejaDer = new Path2D.Double();
        orejaDer.moveTo(cx + 5, cy - size + 10);
        orejaDer.lineTo(cx + size / 2 + 5, cy - size - 18);
        orejaDer.lineTo(cx + size - 5, cy - size + 10);
        orejaDer.closePath();
        g2.fill(orejaDer);

        // Interior de orejas (rosa pastel)
        g2.setColor(ROSA_PASTEL);
        Path2D intIzq = new Path2D.Double();
        intIzq.moveTo(cx - size + 12, cy - size + 12);
        intIzq.lineTo(cx - size / 2 - 3, cy - size - 10);
        intIzq.lineTo(cx - 10, cy - size + 12);
        intIzq.closePath();
        g2.fill(intIzq);

        Path2D intDer = new Path2D.Double();
        intDer.moveTo(cx + 10, cy - size + 12);
        intDer.lineTo(cx + size / 2 + 3, cy - size - 10);
        intDer.lineTo(cx + size - 12, cy - size + 12);
        intDer.closePath();
        g2.fill(intDer);

        // Ojos (blancos con pupilas oscuras)
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 16, cy - 8, 14, 14);
        g2.fillOval(cx + 2, cy - 8, 14, 14);
        g2.setColor(TEXTO_OSCURO);
        g2.fillOval(cx - 12, cy - 4, 7, 7);
        g2.fillOval(cx + 6, cy - 4, 7, 7);
        // Brillos en los ojos
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 11, cy - 3, 3, 3);
        g2.fillOval(cx + 7, cy - 3, 3, 3);

        // Nariz (triángulo rosa pequeño)
        g2.setColor(CORAL);
        Path2D nariz = new Path2D.Double();
        nariz.moveTo(cx, cy + 4);
        nariz.lineTo(cx - 4, cy + 10);
        nariz.lineTo(cx + 4, cy + 10);
        nariz.closePath();
        g2.fill(nariz);

        // Boca (dos líneas curvas)
        g2.setColor(TEXTO_OSCURO);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(cx - 8, cy + 6, 8, 8, 0, -180);
        g2.drawArc(cx, cy + 6, 8, 8, 0, -180);

        // Bigotes (3 a cada lado)
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(TEXTO_GRIS);
        // Izquierda
        g2.drawLine(cx - 18, cy + 4, cx - size - 8, cy);
        g2.drawLine(cx - 18, cy + 8, cx - size - 10, cy + 8);
        g2.drawLine(cx - 18, cy + 12, cx - size - 8, cy + 16);
        // Derecha
        g2.drawLine(cx + 18, cy + 4, cx + size + 8, cy);
        g2.drawLine(cx + 18, cy + 8, cx + size + 10, cy + 8);
        g2.drawLine(cx + 18, cy + 12, cx + size + 8, cy + 16);
    }

    /**
     * Lógica principal que se ejecuta al presionar "Iniciar Sesión".
     * Valida credenciales y abre la ventana principal si son correctas.
     */
    private void realizarLogin() {
        String username = txtUsuario.getText();
        String password = new String(txtPassword.getPassword());

        Usuario usuarioValidado = authService.login(username, password);

        if (usuarioValidado != null) {
            // Guardar sesión del usuario autenticado
            SessionManager.getInstancia().setUsuarioActual(usuarioValidado);

            // Abrir la ventana principal
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);

            // Cerrar la ventana de login
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos",
                    "Error de acceso",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicia un temporizador para animación de deslizamiento vertical (slide-in).
     * El panel central se desliza suavemente de arriba hacia su posición final.
     */
    private void iniciarAnimacionCarga() {
        Timer timer = new Timer(15, new ActionListener() {
            private double currentY = 50.0; // Desplazamiento inicial en píxeles

            @Override
            public void actionPerformed(ActionEvent e) {
                currentY = currentY * 0.85; // Disminución progresiva (ease-out)
                if (currentY < 0.5) {
                    panelCard.setAnimYOffset(0);
                    panelCard.repaint();
                    ((Timer) e.getSource()).stop();
                } else {
                    panelCard.setAnimYOffset((int) currentY);
                    panelCard.repaint();
                }
            }
        });
        timer.start();
    }

    /**
     * Panel personalizado que dibuja una tarjeta con bordes redondeados (20px)
     * y sombra suave difuminada. Soporta animación de desplazamiento vertical y
     * color de fondo adaptable al tema.
     */
    private class ShadowPanel extends JPanel {
        // Desplazamiento vertical para la animación de entrada
        private int animYOffset = 50;

        public ShadowPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(340, 460));
        }

        /**
         * Establece el desplazamiento vertical para la animación.
         *
         * @param offset Píxeles de desplazamiento desde la posición final
         */
        public void setAnimYOffset(int offset) {
            this.animYOffset = offset;
        }

        @Override
        public void paint(Graphics g) {
            // Aplicar traslación vertical para la animación de slide-in
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(0, animYOffset);
            super.paint(g2);
            g2.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Dibujar sombra suave difuminada (múltiples capas semi-transparentes)
            // En modo oscuro se hace una sombra mucho más tenue
            int maxSombra = Main.isDarkMode ? 4 : 10;
            for (int i = 0; i < 8; i++) {
                int alpha = Math.max(0, maxSombra - i);
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRoundRect(i, i + 2, width - (i * 2), height - (i * 2), 24, 24);
            }

            // Dibujar fondo de la tarjeta con color dependiente del tema
            g2.setColor(Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE);
            g2.fillRoundRect(6, 6, width - 12, height - 12, 20, 20);

            g2.dispose();
        }
    }
}
