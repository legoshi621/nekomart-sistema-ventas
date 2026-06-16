package com.nekomart.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.nekomart.models.Usuario;
import com.nekomart.services.AuthService;
import com.nekomart.utils.SessionManager;

public class LoginFrame extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIniciarSesion;
    private JButton btnMostrarPassword;
    private JPanel panelFondo;
    
    private AuthService authService;
    private boolean passwordVisible = false;

    public LoginFrame() {
        authService = new AuthService();
        setTitle("NekoMart - Iniciar Sesión");
        setSize(460, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        // FONDO CON GRADIENTE AZUL Y DECORACIONES
        panelFondo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradiente diagonal
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(96, 165, 250),
                    getWidth(), getHeight(), new Color(30, 136, 229)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Círculos decorativos
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillOval(-50, -50, 200, 200);
                g2.fillOval(getWidth() - 100, getHeight() - 150, 250, 250);
                g2.fillOval(getWidth() / 2 - 100, -80, 150, 150);
            }
        };
        panelFondo.setLayout(new GridBagLayout());

        // TARJETA BLANCA
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setPreferredSize(new Dimension(400, 500));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(40, 35, 30, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // LOGO - CARRITO + TEXTO
        JPanel panelLogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                dibujarCarrito(g2, 0, 10, 35);
                
                g2.setColor(new Color(30, 58, 138));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                g2.drawString("NekoMart", 45, 35);
            }
        };
        panelLogo.setOpaque(false);
        panelLogo.setPreferredSize(new Dimension(180, 50));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        tarjeta.add(panelLogo, gbc);

        // SLOGAN
        JLabel lblSlogan = new JLabel("Sistema de Punto de Venta", JLabel.CENTER);
        lblSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSlogan.setForeground(new Color(100, 116, 139));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 25, 0);
        tarjeta.add(lblSlogan, gbc);

        // LÍNEA DECORATIVA
        JPanel lineaDecorativa = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(203, 213, 225),
                    getWidth(), 0, new Color(30, 136, 229)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 2);
            }
        };
        lineaDecorativa.setPreferredSize(new Dimension(0, 2));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 25, 0);
        tarjeta.add(lineaDecorativa, gbc);

        // CAMPO USUARIO
        JPanel panelUsuario = new JPanel(new BorderLayout(0, 0));
        panelUsuario.setBackground(Color.WHITE);
        panelUsuario.setPreferredSize(new Dimension(0, 50));
        panelUsuario.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true));
        
        JLabel lblIconoUsuario = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 58, 138));
                g2.fillOval(8, 16, 16, 16);
                g2.fillArc(2, 32, 28, 16, 0, 180);
            }
        };
        lblIconoUsuario.setPreferredSize(new Dimension(40, 50));
        panelUsuario.add(lblIconoUsuario, BorderLayout.WEST);
        
        txtUsuario = new JTextField();
        txtUsuario.setBorder(null);
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.putClientProperty("FlatLaf.placeholderText", "Nombre de usuario");
        panelUsuario.add(txtUsuario, BorderLayout.CENTER);
        
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tarjeta.add(panelUsuario, gbc);

        // CAMPO PASSWORD
        JPanel panelPassword = new JPanel(new BorderLayout(0, 0));
        panelPassword.setBackground(Color.WHITE);
        panelPassword.setPreferredSize(new Dimension(0, 50));
        panelPassword.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true));
        
        JLabel lblIconoCandado = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 58, 138));
                g2.fillRoundRect(10, 18, 18, 14, 3, 3);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawArc(10, 10, 18, 12, 0, 180);
            }
        };
        lblIconoCandado.setPreferredSize(new Dimension(40, 50));
        panelPassword.add(lblIconoCandado, BorderLayout.WEST);
        
        txtPassword = new JPasswordField();
        txtPassword.setBorder(null);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setEchoChar('•');
        panelPassword.add(txtPassword, BorderLayout.CENTER);
        
        btnMostrarPassword = new JButton("");
        btnMostrarPassword.setPreferredSize(new Dimension(45, 50));
        btnMostrarPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnMostrarPassword.setForeground(new Color(30, 58, 138));
        btnMostrarPassword.setContentAreaFilled(false);
        btnMostrarPassword.setBorderPainted(false);
        btnMostrarPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMostrarPassword.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            txtPassword.setEchoChar(passwordVisible ? (char)0 : '•');
        });
        panelPassword.add(btnMostrarPassword, BorderLayout.EAST);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 25, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tarjeta.add(panelPassword, gbc);

        // BOTÓN INICIAR SESIÓN
        btnIniciarSesion = new JButton("INICIAR SESIÓN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color btnColor = getModel().isPressed() ? new Color(29, 78, 216) : 
                                getModel().isRollover() ? new Color(37, 99, 235) : 
                                new Color(30, 136, 229);
                
                g2.setColor(btnColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btnIniciarSesion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIniciarSesion.setForeground(Color.WHITE);
        btnIniciarSesion.setPreferredSize(new Dimension(0, 50));
        btnIniciarSesion.setFocusPainted(false);
        btnIniciarSesion.setBorderPainted(false);
        btnIniciarSesion.setContentAreaFilled(false);
        btnIniciarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIniciarSesion.addActionListener(e -> realizarLogin());
        
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tarjeta.add(btnIniciarSesion, gbc);

        // TEXTO INFERIOR
        JLabel lblFooter = new JLabel("© 2026 NekoMart - Todos los derechos reservados", JLabel.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFooter.setForeground(new Color(148, 163, 184));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        tarjeta.add(lblFooter, gbc);

        panelFondo.add(tarjeta);
        add(panelFondo);
    }

    private void dibujarCarrito(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(30, 58, 138));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        g2.drawLine(x, y + 8, x + 10, y + 8);
        g2.drawLine(x + 10, y + 8, x + 14, y + 28);
        g2.drawLine(x + 14, y + 28, x + size - 10, y + 28);
        g2.drawLine(x + size - 10, y + 28, x + size, y + 10);
        g2.drawLine(x + size, y + 10, x + 10, y + 10);
        g2.fillOval(x + 16, y + 30, 8, 8);
        g2.fillOval(x + size - 22, y + 30, 8, 8);
    }

    private void realizarLogin() {
        String username = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor complete todos los campos",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Usuario usuario = authService.login(username, password);

        if (usuario != null) {
            SessionManager.getInstancia().setUsuarioActual(usuario);
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
}