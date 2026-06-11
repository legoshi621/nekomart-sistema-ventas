package com.nekomart.utils;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * Clase de configuración para el envío de correos SMTP.
 * Almacena las credenciales de forma persistente usando java.util.prefs.Preferences
 * (registro de Windows / preferencias de usuario).
 * Todo el código está comentado en español.
 */
public class ConfiguracionCorreo {

    // Nodo de preferencias en el registro del sistema
    private static final Preferences PREFS = Preferences.userNodeForPackage(ConfiguracionCorreo.class);

    // Claves de las preferencias
    private static final String KEY_HOST     = "smtp_host";
    private static final String KEY_PORT     = "smtp_port";
    private static final String KEY_USUARIO  = "email_usuario";
    private static final String KEY_PASSWORD = "email_password";

    // Valores por defecto (Gmail)
    public static final String DEFAULT_HOST = "smtp.gmail.com";
    public static final String DEFAULT_PORT = "587";

    // ── Getters dinámicos (leen desde Preferences) ────────────────────────

    /** Servidor SMTP (ej. smtp.gmail.com) */
    public static String getSmtpHost() {
        return PREFS.get(KEY_HOST, DEFAULT_HOST);
    }

    /** Puerto SMTP (ej. 587 para TLS, 465 para SSL) */
    public static String getSmtpPort() {
        return PREFS.get(KEY_PORT, DEFAULT_PORT);
    }

    /** Correo electrónico del remitente */
    public static String getEmailUsuario() {
        return PREFS.get(KEY_USUARIO, "");
    }

    /** Contraseña de aplicación del remitente */
    public static String getEmailPassword() {
        return PREFS.get(KEY_PASSWORD, "");
    }

    // ── Setters ───────────────────────────────────────────────────────────

    /** Guarda la configuración SMTP de forma persistente */
    public static void guardarConfiguracion(String host, String port, String usuario, String password) {
        PREFS.put(KEY_HOST,     host);
        PREFS.put(KEY_PORT,     port);
        PREFS.put(KEY_USUARIO,  usuario);
        PREFS.put(KEY_PASSWORD, password);
    }

    /**
     * Verifica si las credenciales de correo están configuradas.
     *
     * @return true si hay usuario y contraseña guardados.
     */
    public static boolean estaConfigurado() {
        return !getEmailUsuario().isEmpty() && !getEmailPassword().isEmpty();
    }

    /**
     * Muestra un diálogo modal para que el administrador configure
     * los parámetros SMTP del sistema.
     *
     * @param parent Componente padre para centrar el diálogo.
     */
    public static void mostrarDialogoConfiguracion(Component parent) {
        // ── Crear diálogo ─────────────────────────────────────────────────
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "⚙ Configuración de Correo SMTP", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 320);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        // ── Panel de campos ───────────────────────────────────────────────
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtHost     = new JTextField(getSmtpHost(), 22);
        JTextField txtPort     = new JTextField(getSmtpPort(), 6);
        JTextField txtUsuario  = new JTextField(getEmailUsuario(), 22);
        JPasswordField txtPass = new JPasswordField(getEmailPassword(), 22);

        // Fila 0: Servidor SMTP
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Servidor SMTP:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(txtHost, gbc);

        // Fila 1: Puerto
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Puerto:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(txtPort, gbc);

        // Fila 2: Email remitente
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Correo remitente:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(txtUsuario, gbc);

        // Fila 3: Contraseña de aplicación
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Contraseña de app:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(txtPass, gbc);

        // Nota informativa sobre Gmail
        JLabel lblNota = new JLabel("<html><i>💡 Para Gmail: activa 'Contraseña de aplicación'<br>"
                + "en tu cuenta Google (Seguridad → Verificación en 2 pasos).</i></html>");
        lblNota.setForeground(new Color(100, 100, 120));
        lblNota.setFont(lblNota.getFont().deriveFont(Font.ITALIC, 11f));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 6, 4, 6);
        panelCampos.add(lblNota, gbc);

        // ── Botones ───────────────────────────────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnGuardar  = new JButton("✔ Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.putClientProperty("JButton.buttonType", "default");
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        // ── Ensamblar diálogo ─────────────────────────────────────────────
        dialog.setLayout(new BorderLayout());
        dialog.add(panelCampos, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);

        // ── Eventos ───────────────────────────────────────────────────────
        btnCancelar.addActionListener(e -> dialog.dispose());

        btnGuardar.addActionListener(e -> {
            String host     = txtHost.getText().trim();
            String port     = txtPort.getText().trim();
            String usuario  = txtUsuario.getText().trim();
            String password = new String(txtPass.getPassword());

            // Validación básica
            if (host.isEmpty() || port.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Todos los campos son obligatorios.",
                        "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Guardar en preferencias del sistema
            guardarConfiguracion(host, port, usuario, password);
            JOptionPane.showMessageDialog(dialog,
                    "Configuración guardada correctamente.\n"
                    + "Se utilizará para el envío de facturas por correo.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }
}
