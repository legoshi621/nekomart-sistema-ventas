package com.nekomart.ui;

import com.nekomart.models.Usuario;
import com.nekomart.utils.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicación NekoMart.
 * Muestra el contenido dinámicamente dependiendo del rol del usuario actual.
 * Todo el código está comentado en español.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        // Configuración principal de la ventana
        setTitle("NekoMart - Sistema de Ventas");
        setSize(1000, 700);
        setLocationRelativeTo(null); // Centrar en la pantalla
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Inicializa y agrega los componentes a la vista
        initComponents();
    }

    /**
     * Construye la barra superior y el contenido principal.
     */
    private void initComponents() {
        // Obtener el usuario actual desde el SessionManager
        Usuario usuarioActual = SessionManager.getInstancia().getUsuarioActual();
        
        // Medida de seguridad: Si no hay usuario, regresar al login inmediatamente
        if (usuarioActual == null) {
            ejecutarCierreSesion();
            return;
        }

        /* -----------------------------------------------------
         * BARRA SUPERIOR
         * ----------------------------------------------------- */
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Panel izquierdo para la info del usuario
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        
        JLabel lblBienvenido = new JLabel("Bienvenido, " + usuarioActual.getNombreCompleto());
        lblBienvenido.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JLabel lblRol = new JLabel("Rol: " + usuarioActual.getRol());
        lblRol.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRol.setForeground(Color.DARK_GRAY);

        panelInfo.add(lblBienvenido);
        panelInfo.add(lblRol);

        // Botón derecho para cerrar sesión
        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> ejecutarCierreSesion());

        panelSuperior.add(panelInfo, BorderLayout.WEST);
        panelSuperior.add(btnCerrarSesion, BorderLayout.EAST);

        // Agregamos la barra superior en la parte norte de la ventana
        add(panelSuperior, BorderLayout.NORTH);

        /* -----------------------------------------------------
         * CONTENIDO PRINCIPAL
         * ----------------------------------------------------- */
        String rol = usuarioActual.getRol();

        if ("ADMIN".equalsIgnoreCase(rol)) {
            // Si es ADMIN, construimos un JTabbedPane
            JTabbedPane tabbedPane = new JTabbedPane();

            // Pestaña de Inventario
            JPanel panelInventario = crearPanelModulo("Módulo de Inventario (próximamente)");
            tabbedPane.addTab("Inventario", panelInventario);

            // Pestaña de Ventas
            JPanel panelVentas = crearPanelModulo("Módulo de Ventas (próximamente)");
            tabbedPane.addTab("Ventas", panelVentas);

            add(tabbedPane, BorderLayout.CENTER);

        } else {
            // Si es EMPLEADO (u otro), mostramos solo un panel simple de Ventas
            JPanel panelVentas = crearPanelModulo("Módulo de Ventas (próximamente)");
            add(panelVentas, BorderLayout.CENTER);
        }
    }

    /**
     * Helper para crear rápidamente un JPanel con un JLabel centrado.
     * 
     * @param texto Mensaje que aparecerá en el centro del panel.
     * @return JPanel configurado.
     */
    private JPanel crearPanelModulo(String texto) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.ITALIC, 16));
        label.setForeground(Color.GRAY);
        panel.add(label);
        return panel;
    }

    /**
     * Realiza el proceso de limpieza de sesión, destruye la ventana actual
     * y redirige al usuario de vuelta a la pantalla de Login.
     */
    private void ejecutarCierreSesion() {
        // Limpiar la referencia al usuario actual en memoria
        SessionManager.getInstancia().cerrarSesion();
        
        // Cerrar este MainFrame liberando los recursos de la ventana
        this.dispose();
        
        // Instanciar y mostrar nuevamente la ventana de Login
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }
}
