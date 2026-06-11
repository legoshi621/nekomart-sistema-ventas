package com.nekomart.ui;

import com.nekomart.models.Usuario;
import com.nekomart.services.AuthService;
import com.nekomart.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Pantalla de inicio de sesión (Login) diseñada con FlatLaf.
 * Cuenta con un diseño moderno, logotipo, sombras suaves y animación de carga.
 * Todo el código está comentado en español.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIniciarSesion;
    private JButton btnSalir;

    // Instancia del servicio de autenticación
    private AuthService authService;

    // Panel de la tarjeta central con animación
    private ShadowPanel panelCard;

    public LoginFrame() {
        authService = new AuthService();
        
        // Configuración básica del JFrame
        setTitle("NekoMart - Iniciar Sesión");
        setSize(460, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        setResizable(false);
        
        // Inicializar la interfaz de usuario
        initComponents();
        
        // Iniciar animación de deslizamiento vertical al cargar la pantalla
        iniciarAnimacionCarga();
    }

    /**
     * Construye y organiza todos los componentes visuales.
     */
    private void initComponents() {
        // Panel de fondo con gradiente elegante
        JPanel panelFondo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Gradiente suave de azul suave a gris claro
                GradientPaint gp = new GradientPaint(0, 0, new Color(224, 231, 255), 0, getHeight(), new Color(243, 244, 246));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panelFondo.setLayout(new GridBagLayout());
        
        // Instanciar panel tipo tarjeta con sombra suave
        panelCard = new ShadowPanel();
        panelCard.setLayout(new GridBagLayout());
        panelCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // 1. Logotipo de NekoMart
        JLabel lblLogo = new JLabel("", SwingConstants.CENTER);
        java.net.URL logoURL = getClass().getResource("/nekomart_logo.png");
        if (logoURL != null) {
            ImageIcon logoIcon = new ImageIcon(logoURL);
            Image scaledImg = logoIcon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(scaledImg));
        } else {
            lblLogo.setText("🐱"); // Carácter alternativo si no se encuentra la imagen
            lblLogo.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelCard.add(lblLogo, gbc);

        // 2. Título "NekoMart"
        JLabel lblTitulo = new JLabel("NekoMart", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(new Color(30, 58, 138)); // Azul corporativo
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 2, 5);
        panelCard.add(lblTitulo, gbc);

        // 3. Subtítulo "Sistema de Ventas"
        JLabel lblSubtitulo = new JLabel("Sistema de Ventas", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(Color.GRAY);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 5, 20, 5); // Más margen abajo antes de los campos
        panelCard.add(lblSubtitulo, gbc);

        // Resetear insets y ancho de cuadrícula para los campos
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.gridwidth = 1;

        // 4. Etiqueta y Campo de Usuario
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelCard.add(lblUsuario, gbc);

        gbc.gridx = 1;
        txtUsuario = new JTextField(15);
        txtUsuario.putClientProperty("JTextField.placeholderText", "Ingresa tu usuario");
        txtUsuario.putClientProperty("JTextField.showClearButton", true);
        panelCard.add(txtUsuario, gbc);

        // 5. Etiqueta y Campo de Contraseña
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelCard.add(lblPassword, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.putClientProperty("JTextField.placeholderText", "Ingresa tu contraseña");
        txtPassword.putClientProperty("JTextField.showRevealButton", true);
        panelCard.add(txtPassword, gbc);

        // Atajo: Presionar Enter para iniciar sesión
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnIniciarSesion.doClick();
                }
            }
        });

        // 6. Botones de Acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setOpaque(false);
        
        btnIniciarSesion = new JButton("Iniciar Sesión");
        btnIniciarSesion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        // Botón azul corporativo
        btnIniciarSesion.setBackground(new Color(30, 58, 138));
        btnIniciarSesion.setForeground(Color.WHITE);
        btnIniciarSesion.putClientProperty("JButton.buttonType", "default");
        btnIniciarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSalir = new JButton("Salir");
        btnSalir.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelBotones.add(btnIniciarSesion);
        panelBotones.add(btnSalir);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 5, 5, 5);
        panelCard.add(panelBotones, gbc);

        // Asignar los ActionListeners (Eventos de clic)
        btnIniciarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarLogin();
            }
        });

        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Añadir el panel tipo tarjeta centrado en el fondo
        GridBagConstraints gbcFondo = new GridBagConstraints();
        gbcFondo.gridx = 0;
        gbcFondo.gridy = 0;
        gbcFondo.insets = new Insets(10, 10, 10, 10);
        panelFondo.add(panelCard, gbcFondo);

        // Añadir panel principal al JFrame
        add(panelFondo);
    }

    /**
     * Lógica principal que se ejecuta al presionar "Iniciar Sesión".
     */
    private void realizarLogin() {
        String username = txtUsuario.getText();
        String password = new String(txtPassword.getPassword());

        Usuario usuarioValidado = authService.login(username, password);

        if (usuarioValidado != null) {
            SessionManager.getInstancia().setUsuarioActual(usuarioValidado);
            
            // Abrir la ventana principal
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos",
                    "Error de acceso",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inicia un temporizador para realizar una animación de deslizamiento vertical (slide-in) suave.
     */
    private void iniciarAnimacionCarga() {
        Timer timer = new Timer(15, new ActionListener() {
            private double currentY = 50.0; // Desplazamiento inicial en pixeles
            @Override
            public void actionPerformed(ActionEvent e) {
                currentY = currentY * 0.85; // Disminución de velocidad (Ease-out)
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
     * Panel personalizado que dibuja una tarjeta blanca con bordes redondeados y sombra suave.
     */
    private class ShadowPanel extends JPanel {
        private int animYOffset = 50;

        public ShadowPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(380, 420));
        }

        public void setAnimYOffset(int offset) {
            this.animYOffset = offset;
        }

        @Override
        public void paint(Graphics g) {
            // Aplicar la animación de traslación
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

            // Dibujar sombra suave de afuera hacia adentro
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(0, 0, 0, 8 - i));
                g2.fillRoundRect(i, i, width - (i * 2), height - (i * 2), 24, 24);
            }

            // Dibujar fondo de la tarjeta blanca
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(5, 5, width - 10, height - 10, 20, 20);
            
            g2.dispose();
        }
    }
}
