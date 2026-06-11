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
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

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

        String rol = usuario.getRol().toUpperCase();

        if (rol.equals("ADMIN")) {
            tabbedPane = new JTabbedPane();

            // Pestaña Inventario
            InventarioFrame inventarioFrame = new InventarioFrame();
            tabbedPane.addTab("📦 Inventario", inventarioFrame);

            // Pestaña Ventas (POS)
            VentasFrame ventasFrame = new VentasFrame();
            tabbedPane.addTab("🛒 Ventas (POS)", ventasFrame);

            // Pestaña Historial de Ventas
            HistorialVentasFrame historialFrame = new HistorialVentasFrame();
            tabbedPane.addTab("📋 Historial", historialFrame);

            setLayout(new BorderLayout());
            add(panelSuperior, BorderLayout.NORTH);
            add(tabbedPane, BorderLayout.CENTER);

        } else {
            // Empleado solo ve Ventas e Historial
            tabbedPane = new JTabbedPane();

            VentasFrame ventasFrame = new VentasFrame();
            tabbedPane.addTab("🛒 Ventas (POS)", ventasFrame);

            HistorialVentasFrame historialFrame = new HistorialVentasFrame();
            tabbedPane.addTab("📋 Historial", historialFrame);

            setLayout(new BorderLayout());
            add(panelSuperior, BorderLayout.NORTH);
            add(tabbedPane, BorderLayout.CENTER);
        }

        btnCerrarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarSesion();
            }
        });
    }

    private void cerrarSesion() {
        SessionManager.getInstancia().cerrarSesion();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
        this.dispose();
    }
}