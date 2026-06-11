package com.nekomart.ui;

import com.nekomart.models.Usuario;
import com.nekomart.utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Ventana principal del sistema que se muestra después del login.
 * Muestra diferentes opciones según el rol del usuario (ADMIN o EMPLEADO).
 * Todo el código está comentado en español.
 */
public class MainFrame extends JFrame {

    private JLabel lblBienvenido;
    private JLabel lblRol;
    private JButton btnCerrarSesion;
    private JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("NekoMart - Sistema de Ventas");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    /**
     * Construye y organiza todos los componentes visuales.
     */
    private void initComponents() {
        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();

        // Panel superior
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        lblBienvenido = new JLabel("Bienvenido, " + usuario.getNombreCompleto());
        lblBienvenido.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblRol = new JLabel("Rol: " + usuario.getRol());
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRol.setForeground(Color.GRAY);

        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.putClientProperty("JButton.buttonType", "default");

        panelSuperior.add(lblBienvenido);
        panelSuperior.add(Box.createHorizontalStrut(10));
        panelSuperior.add(lblRol);
        panelSuperior.add(Box.createHorizontalGlue());
        panelSuperior.add(btnCerrarSesion);

        // Panel de contenido según rol
        String rol = usuario.getRol().toUpperCase();

        if (rol.equals("ADMIN")) {
            tabbedPane = new JTabbedPane();

            // Pestaña Inventario con el módulo real
            InventarioFrame inventarioFrame = new InventarioFrame();
            tabbedPane.addTab("📦 Inventario", inventarioFrame);

            // Pestaña Ventas (próximamente)
            JPanel panelVentas = new JPanel(new GridBagLayout());
            JLabel lblVentas = new JLabel("🛒 Módulo de Ventas (próximamente)", SwingConstants.CENTER);
            lblVentas.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblVentas.setForeground(new Color(30, 58, 138));
            panelVentas.add(lblVentas);
            tabbedPane.addTab("Ventas", panelVentas);

            setLayout(new BorderLayout());
            add(panelSuperior, BorderLayout.NORTH);
            add(tabbedPane, BorderLayout.CENTER);

        } else {
            // Empleado solo ve Ventas
            JPanel panelContenido = new JPanel(new GridBagLayout());
            JLabel lblVentas = new JLabel("🛒 Módulo de Ventas (próximamente)", SwingConstants.CENTER);
            lblVentas.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblVentas.setForeground(new Color(30, 58, 138));
            panelContenido.add(lblVentas);

            setLayout(new BorderLayout());
            add(panelSuperior, BorderLayout.NORTH);
            add(panelContenido, BorderLayout.CENTER);
        }

        btnCerrarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarSesion();
            }
        });
    }

    /**
     * Cierra la sesión y regresa al login.
     */
    private void cerrarSesion() {
        SessionManager.getInstancia().cerrarSesion();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        this.dispose();
    }
}
