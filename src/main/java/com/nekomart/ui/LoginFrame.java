package com.nekomart.ui;

import com.nekomart.models.Usuario;
import com.nekomart.services.AuthService;
import com.nekomart.utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Pantalla de inicio de sesión (Login) diseñada con FlatLaf.
 * Todo el código está comentado en español.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIniciarSesion;
    private JButton btnSalir;

    // Instancia del servicio de autenticación
    private AuthService authService;

    public LoginFrame() {
        authService = new AuthService();
        
        // Configuración básica del JFrame
        setTitle("NekoMart - Iniciar Sesión");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        setResizable(false);
        
        // Inicializar la interfaz de usuario
        initComponents();
    }

    /**
     * Construye y organiza todos los componentes visuales.
     */
    private void initComponents() {
        // Panel principal usando GridBagLayout para alinear todo al centro de forma limpia
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        // 1. Título "NekoMart"
        JLabel lblTitulo = new JLabel("NekoMart", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Ocupar ambas columnas
        panelPrincipal.add(lblTitulo, gbc);

        // 2. Subtítulo "Sistema de Ventas"
        JLabel lblSubtitulo = new JLabel("Sistema de Ventas", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 20, 10); // Más margen abajo del subtítulo
        panelPrincipal.add(lblSubtitulo, gbc);

        // 3. Etiqueta y Campo de Usuario
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel lblUsuario = new JLabel("Usuario:");
        panelPrincipal.add(lblUsuario, gbc);

        gbc.gridx = 1;
        txtUsuario = new JTextField(15);
        // Uso de propiedad de FlatLaf para mostrar un texto de ayuda (placeholder)
        txtUsuario.putClientProperty("JTextField.placeholderText", "Usuario");
        panelPrincipal.add(txtUsuario, gbc);

        // 4. Etiqueta y Campo de Contraseña
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblPassword = new JLabel("Contraseña:");
        panelPrincipal.add(lblPassword, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        txtPassword.putClientProperty("JTextField.placeholderText", "Contraseña");
        panelPrincipal.add(txtPassword, gbc);

        // Configuración para que al presionar 'Enter' en la contraseña se intente iniciar sesión
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnIniciarSesion.doClick(); // Simular clic en el botón
                }
            }
        });

        // 5. Botones de Acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        btnIniciarSesion = new JButton("Iniciar Sesión");
        // Indicar a FlatLaf que este es el botón primario (color azul/destacado por defecto)
        btnIniciarSesion.putClientProperty("JButton.buttonType", "default");
        
        btnSalir = new JButton("Salir");

        panelBotones.add(btnIniciarSesion);
        panelBotones.add(btnSalir);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 5, 10);
        panelPrincipal.add(panelBotones, gbc);

        // 6. Asignar los ActionListeners (Eventos de clic)
        btnIniciarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarLogin();
            }
        });

        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Cierra completamente la aplicación
            }
        });

        // Añadir panel principal al JFrame
        add(panelPrincipal);
    }

    /**
     * Lógica principal que se ejecuta al presionar "Iniciar Sesión".
     */
    private void realizarLogin() {
        String username = txtUsuario.getText();
        String password = new String(txtPassword.getPassword());

        // Llamamos al servicio para validar las credenciales
        Usuario usuarioValidado = authService.login(username, password);

        if (usuarioValidado != null) {
            // Guardar usuario en el gestor de sesión (Singleton)
            SessionManager.getInstancia().setUsuarioActual(usuarioValidado);
            
            // Abrir la ventana principal (asumiendo que MainFrame ya está o será creado)
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            
            // Cerrar la ventana actual de Login
            this.dispose();
        } else {
            // Mostrar alerta al fallar la autenticación usando JOptionPane
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos",
                    "Error de acceso",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
