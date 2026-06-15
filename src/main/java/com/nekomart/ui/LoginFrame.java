package com.nekomart.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.nekomart.Main;
import com.nekomart.models.Usuario;
import com.nekomart.services.AuthService;
import com.nekomart.utils.SessionManager;
import com.nekomart.utils.ThemeManager;

/**
 * Pantalla de inicio de sesión (Login) con diseño POS profesional.
 * Gradiente azul de fondo (#BBDEFB → #E3F2FD), panel blanco con sombra,
 * logo grande (200x200px) o fallback de texto, campos estilizados y botón azul.
 * Incluye un botón para cambiar entre tema claro y oscuro de manera dinámica.
 * Todo el código está comentado en español.
 */
public class LoginFrame extends JFrame {

    // ── Colores de la paleta POS (usando ThemeManager) ──────────────────
    private static final Color AZUL_PRIMARIO = ThemeManager.AZUL_PRIMARIO;        // #1E88E5 - Azul principal
    private static final Color AZUL_OSCURO = ThemeManager.AZUL_OSCURO;            // #1565C0 - Hover botones
    private static final Color AZUL_CLARO = ThemeManager.AZUL_CLARO;              // #87CEFA - Acentos
    private static final Color AZUL_MUY_CLARO = ThemeManager.AZUL_MUY_CLARO;      // #E3F2FD - Fondos suaves
    private static final Color GRIS_OSCURO = ThemeManager.GRIS_OSCURO;            // #1E293B - Texto principal
    private static final Color GRIS_MEDIO = ThemeManager.GRIS_MEDIO;              // #64748B - Texto secundario
    private static final Color GRIS_CLARO = ThemeManager.GRIS_CLARO;              // #CBD5E1 - Bordes
    private static final Color GRIS_MUY_CLARO = ThemeManager.GRIS_MUY_CLARO;      // #F1F5F9 - Fondos secciones
    private static final Color FONDO_PRINCIPAL = ThemeManager.FONDO_PRINCIPAL;    // #F8FAFC - Fondo general
    private static final Color BLANCO = ThemeManager.BLANCO;                      // #FFFFFF - Tarjetas

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
        // ── Panel de fondo con gradiente azul claro (#BBDEFB → #E3F2FD) ────
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
                    // Gradiente diagonal azul claro: #BBDEFB → #E3F2FD (135 grados)
                    GradientPaint gp = new GradientPaint(
                            0, 0, new Color(187, 222, 251),  // #BBDEFB
                            getWidth(), getHeight(), AZUL_MUY_CLARO  // #E3F2FD
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

        // ── Panel tipo tarjeta blanca con sombra suave (border radius 16px) ──
        panelCard = new ShadowPanel();
        panelCard.setLayout(new GridBagLayout());
        panelCard.setBorder(BorderFactory.createEmptyBorder(40, 35, 40, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        // ── 1. Logo de la aplicación (200x200px desde recursos o fallback) ──
        JPanel panelLogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ImageIcon logo = obtenerLogo();
                if (logo != null) {
                    // Escalar el logo a dimensiones máximas de 200x200 manteniendo relación de aspecto
                    int iw = logo.getIconWidth();
                    int ih = logo.getIconHeight();
                    int maxDim = 200;
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
                    // Fallback: no dibujar nada aquí, el texto se maneja en lblTitulo
                }
                g2.dispose();
            }
        };
        panelLogo.setOpaque(false);
        panelLogo.setPreferredSize(new Dimension(210, 210));
        gbc.gridy = 0;
        panelCard.add(panelLogo, gbc);

        // ── 2. Título "NekoMart" (fallback con emoji si no hay logo) ─────
        ImageIcon imgLogo = obtenerLogo();
        lblTitulo = new JLabel(imgLogo != null ? "NekoMart" : "🐱 NekoMart", SwingConstants.CENTER);
        // Si no carga el logo, usar fuente bold 32px azul primario según requisitos
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, imgLogo != null ? 28 : 32));
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 0, 0);
        panelCard.add(lblTitulo, gbc);

        // ── 3. Subtítulo "Sistema de Ventas" ─────────────────────────────
        lblSubtitulo = new JLabel("Sistema de Ventas", SwingConstants.CENTER);
        lblSubtitulo.setFont(ThemeManager.TEXTO_PEQUENO);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 25, 0);
        panelCard.add(lblSubtitulo, gbc);

        // ── 4. Campo de usuario (height 45px, border #CBD5E1, border radius 8px) ──
        txtUsuario = new JTextField(20);
        txtUsuario.putClientProperty("JTextField.placeholderText", "Nombre de usuario");
        txtUsuario.putClientProperty("JTextField.showClearButton", true);
        txtUsuario.setFont(ThemeManager.TEXTO_NORMAL);
        txtUsuario.setPreferredSize(new Dimension(0, 45));
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 0, 4, 0);
        panelCard.add(txtUsuario, gbc);

        // ── 5. Campo de contraseña (height 45px, border #CBD5E1, border radius 8px) ──
        txtPassword = new JPasswordField(20);
        txtPassword.putClientProperty("JTextField.placeholderText", "Contraseña");
        txtPassword.putClientProperty("JTextField.showRevealButton", true);
        txtPassword.setFont(ThemeManager.TEXTO_NORMAL);
        txtPassword.setPreferredSize(new Dimension(0, 45));
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

        // ── 6. Botón "Iniciar Sesión" azul (#1E88E5), bordes redondeados 8px, font 16px bold ──
        btnIniciarSesion = new JButton("Iniciar Sesión") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar fondo redondeado (8px de arco) con color azul primario
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(AZUL_OSCURO);  // Hover: #1565C0
                } else {
                    g2.setColor(AZUL_PRIMARIO);  // Normal: #1E88E5
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Dibujar texto centrado en blanco
                g2.setColor(BLANCO);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnIniciarSesion.setFont(ThemeManager.BOTON);  // 16px Bold
        btnIniciarSesion.setForeground(BLANCO);
        btnIniciarSesion.setContentAreaFilled(false);
        btnIniciarSesion.setBorderPainted(false);
        btnIniciarSesion.setFocusPainted(false);
        btnIniciarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIniciarSesion.setPreferredSize(new Dimension(0, 50));  // Height 50px
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 0, 5, 0);
        panelCard.add(btnIniciarSesion, gbc);

        // ── 7. Botón "Salir" discreto ────────────────────────────────────
        btnSalir = new JButton("Salir");
        btnSalir.setFont(ThemeManager.TEXTO_PEQUENO);
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
     * Usa la paleta POS: texto #1E293B, bordes #CBD5E1, fondo blanco.
     */
    public void reaplicarTemaLogin() {
        Color fgColor = Main.isDarkMode ? BLANCO : GRIS_OSCURO;
        Color textGris = Main.isDarkMode ? new Color(170, 170, 170) : GRIS_MEDIO;
        Color bgField = Main.isDarkMode ? new Color(0x3D, 0x3D, 0x3D) : BLANCO;
        Color borderField = Main.isDarkMode ? new Color(0x55, 0x55, 0x55) : GRIS_CLARO;

        // Estilos para el campo Usuario (border radius 8px, border #CBD5E1)
        txtUsuario.setBackground(bgField);
        txtUsuario.setForeground(fgColor);
        txtUsuario.setCaretColor(fgColor);
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderField, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Estilos para el campo Password (border radius 8px, border #CBD5E1)
        txtPassword.setBackground(bgField);
        txtPassword.setForeground(fgColor);
        txtPassword.setCaretColor(fgColor);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderField, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Título: azul primario si carga logo, azul primario si no carga logo
        ImageIcon logo = obtenerLogo();
        if (logo != null) {
            lblTitulo.setForeground(Main.isDarkMode ? BLANCO : AZUL_PRIMARIO);
        } else {
            // "🐱 NekoMart" en azul primario, fuente bold 32px según requisitos
            lblTitulo.setForeground(Main.isDarkMode ? BLANCO : AZUL_PRIMARIO);
        }
        lblSubtitulo.setForeground(textGris);
        btnSalir.setForeground(textGris);
        btnTema.setForeground(BLANCO); // Siempre blanco sobre fondo azul

        // Repintar fondo y tarjeta
        panelFondo.repaint();
        panelCard.repaint();
    }

    /**
     * Dibuja un gatito estilizado con Graphics2D.
     * Usa formas geométricas simples en tonos azul POS.
     *
     * @param g2   Contexto gráfico 2D
     * @param cx   Centro X del dibujo
     * @param cy   Centro Y del dibujo
     * @param size Tamaño base del gatito
     */
    private void dibujarGatito(Graphics2D g2, int cx, int cy, int size) {
        // Cara del gato (círculo principal) — azul primario
        g2.setColor(AZUL_PRIMARIO);
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

        // Interior de orejas (azul muy claro)
        g2.setColor(AZUL_MUY_CLARO);
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
        g2.setColor(BLANCO);
        g2.fillOval(cx - 16, cy - 8, 14, 14);
        g2.fillOval(cx + 2, cy - 8, 14, 14);
        g2.setColor(GRIS_OSCURO);
        g2.fillOval(cx - 12, cy - 4, 7, 7);
        g2.fillOval(cx + 6, cy - 4, 7, 7);
        // Brillos en los ojos
        g2.setColor(BLANCO);
        g2.fillOval(cx - 11, cy - 3, 3, 3);
        g2.fillOval(cx + 7, cy - 3, 3, 3);

        // Nariz (triángulo verde éxito)
        g2.setColor(ThemeManager.EXITO);
        Path2D nariz = new Path2D.Double();
        nariz.moveTo(cx, cy + 4);
        nariz.lineTo(cx - 4, cy + 10);
        nariz.lineTo(cx + 4, cy + 10);
        nariz.closePath();
        g2.fill(nariz);

        // Boca (dos líneas curvas)
        g2.setColor(GRIS_OSCURO);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(cx - 8, cy + 6, 8, 8, 0, -180);
        g2.drawArc(cx, cy + 6, 8, 8, 0, -180);

        // Bigotes (3 a cada lado)
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(GRIS_MEDIO);
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
     * Panel personalizado que dibuja una tarjeta con bordes redondeados (16px)
     * y sombra suave difuminada. Soporta animación de desplazamiento vertical y
     * color de fondo adaptable al tema.
     */
    private class ShadowPanel extends JPanel {
        // Desplazamiento vertical para la animación de entrada
        private int animYOffset = 50;

        public ShadowPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(340, 520));
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
            // Sombra: 0 8px 32px rgba(0,0,0,0.12)
            int maxSombra = Main.isDarkMode ? 4 : 12;
            for (int i = 0; i < 8; i++) {
                int alpha = Math.max(0, maxSombra - i);
                g2.setColor(new Color(0, 0, 0, alpha));
                g2.fillRoundRect(i, i + 2, width - (i * 2), height - (i * 2), 24, 24);
            }

            // Dibujar fondo de la tarjeta: blanco (claro) o gris oscuro (oscuro)
            // Border radius: 16px
            g2.setColor(Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : BLANCO);
            g2.fillRoundRect(6, 6, width - 12, height - 12, 16, 16);

            g2.dispose();
        }
    }
}