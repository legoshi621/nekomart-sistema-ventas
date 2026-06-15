package com.nekomart.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.nekomart.models.Usuario;
import com.nekomart.services.UsuarioService;
import com.nekomart.utils.SessionManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ProfilePanel extends JPanel {
    private Usuario usuarioActual;
    private UsuarioService usuarioService;
    
    private JLabel lblFotoPerfil;
    private JTextField txtNombre, txtEmail, txtTelefono;
    private JPasswordField txtPasswordActual, txtPasswordNueva, txtPasswordConfirmar;
    private JButton btnTomarFoto, btnSubirFoto, btnEliminarFoto, btnGuardarTodo;
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private JDialog webcamDialog;
    private BufferedImage fotoTemporal = null;

    private final Color AZUL_PRIMARIO = new Color(59, 130, 246);
    private final Color GRIS_CLARO = new Color(241, 245, 249);
    private final Color GRIS_MEDIO = new Color(100, 116, 139);
    private final Color TEXTO_OSCURO = new Color(30, 41, 59);

    public ProfilePanel(Usuario usuario) {
        this.usuarioActual = usuario;
        this.usuarioService = new UsuarioService();
        
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblTitulo = new JLabel("Mi Perfil");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(TEXTO_OSCURO);
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setOpaque(false);

        // Panel izquierdo: Foto
        JPanel panelFoto = crearPanelFoto();
        panelFoto.setPreferredSize(new Dimension(300, 0));
        panelPrincipal.add(panelFoto, BorderLayout.WEST);

        // Panel derecho: Información + Contraseña
        JPanel panelInfo = crearPanelInfoYPassword();
        panelPrincipal.add(panelInfo, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);
        
        cargarDatosUsuario();
    }

    private JPanel crearPanelFoto() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(30, 20, 30, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        // Foto grande
        lblFotoPerfil = new JLabel();
        lblFotoPerfil.setIcon(crearIconoPlaceholder(200, 200));
        lblFotoPerfil.setHorizontalAlignment(SwingConstants.CENTER);
        lblFotoPerfil.setBorder(BorderFactory.createLineBorder(GRIS_CLARO, 2, true));
        gbc.gridy = 0;
        panel.add(lblFotoPerfil, gbc);

        // Botones de foto
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBotones.setOpaque(false);
        
        btnTomarFoto = new JButton("Tomar Foto");
        btnTomarFoto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTomarFoto.setBackground(AZUL_PRIMARIO);
        btnTomarFoto.setForeground(Color.WHITE);
        btnTomarFoto.setPreferredSize(new Dimension(110, 35));
        btnTomarFoto.setFocusPainted(false);
        btnTomarFoto.setBorderPainted(false);
        btnTomarFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTomarFoto.addActionListener(e -> abrirWebcam());
        panelBotones.add(btnTomarFoto);

        btnSubirFoto = new JButton("Subir Foto");
        btnSubirFoto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSubirFoto.setBackground(GRIS_MEDIO);
        btnSubirFoto.setForeground(Color.WHITE);
        btnSubirFoto.setPreferredSize(new Dimension(110, 35));
        btnSubirFoto.setFocusPainted(false);
        btnSubirFoto.setBorderPainted(false);
        btnSubirFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubirFoto.addActionListener(e -> subirFotoArchivo());
        panelBotones.add(btnSubirFoto);

        gbc.gridy = 1;
        panel.add(panelBotones, gbc);

        btnEliminarFoto = new JButton("Eliminar Foto");
        btnEliminarFoto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEliminarFoto.setBackground(new Color(239, 68, 68));
        btnEliminarFoto.setForeground(Color.WHITE);
        btnEliminarFoto.setPreferredSize(new Dimension(230, 35));
        btnEliminarFoto.setFocusPainted(false);
        btnEliminarFoto.setBorderPainted(false);
        btnEliminarFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminarFoto.addActionListener(e -> eliminarFoto());
        gbc.gridy = 2;
        panel.add(btnEliminarFoto, gbc);

        return panel;
    }

    private JPanel crearPanelInfoYPassword() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        // Título Información
        JLabel lblInfoTitulo = new JLabel("Información Personal");
        lblInfoTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInfoTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridy = 0;
        panel.add(lblInfoTitulo, gbc);

        // Nombre
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(crearLabel("Nombre completo *"), gbc);
        
        txtNombre = new JTextField();
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNombre.setPreferredSize(new Dimension(250, 38));
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        panel.add(txtNombre, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(crearLabel("Correo electrónico"), gbc);
        
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtEmail.setPreferredSize(new Dimension(250, 38));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);

        // Teléfono
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(crearLabel("Teléfono"), gbc);
        
        txtTelefono = new JTextField();
        txtTelefono.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTelefono.setPreferredSize(new Dimension(250, 38));
        txtTelefono.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        panel.add(txtTelefono, gbc);

        // Info solo lectura
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(crearLabel("Usuario"), gbc);
        
        JLabel lblUsername = new JLabel(usuarioActual != null ? usuarioActual.getUsername() : "");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUsername.setForeground(GRIS_MEDIO);
        gbc.gridx = 1;
        panel.add(lblUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(crearLabel("Rol"), gbc);
        
        JLabel lblRol = new JLabel(usuarioActual != null ? usuarioActual.getRol() : "");
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRol.setForeground(GRIS_MEDIO);
        gbc.gridx = 1;
        panel.add(lblRol, gbc);

        // Separador
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 20, 0);
        JPanel separador = new JPanel();
        separador.setBackground(new Color(226, 232, 240));
        separador.setPreferredSize(new Dimension(0, 1));
        panel.add(separador, gbc);

        // Título Contraseña
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 15, 0);
        JLabel lblPassTitulo = new JLabel("Cambiar Contraseña");
        lblPassTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPassTitulo.setForeground(TEXTO_OSCURO);
        panel.add(lblPassTitulo, gbc);

        // Contraseña actual
        gbc.gridwidth = 1;
        gbc.gridy = 8;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(crearLabel("Contraseña actual"), gbc);
        
        txtPasswordActual = new JPasswordField();
        txtPasswordActual.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPasswordActual.setPreferredSize(new Dimension(250, 38));
        txtPasswordActual.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        panel.add(txtPasswordActual, gbc);

        // Nueva contraseña
        gbc.gridx = 0; gbc.gridy = 9;
        panel.add(crearLabel("Nueva contraseña"), gbc);
        
        txtPasswordNueva = new JPasswordField();
        txtPasswordNueva.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPasswordNueva.setPreferredSize(new Dimension(250, 38));
        txtPasswordNueva.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        panel.add(txtPasswordNueva, gbc);

        // Confirmar contraseña
        gbc.gridx = 0; gbc.gridy = 10;
        panel.add(crearLabel("Confirmar contraseña"), gbc);
        
        txtPasswordConfirmar = new JPasswordField();
        txtPasswordConfirmar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPasswordConfirmar.setPreferredSize(new Dimension(250, 38));
        txtPasswordConfirmar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        panel.add(txtPasswordConfirmar, gbc);

        // Botón Guardar Todo
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 0, 0, 0);
        
        btnGuardarTodo = new JButton("GUARDAR CAMBIOS");
        btnGuardarTodo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardarTodo.setBackground(AZUL_PRIMARIO);
        btnGuardarTodo.setForeground(Color.WHITE);
        btnGuardarTodo.setPreferredSize(new Dimension(0, 45));
        btnGuardarTodo.setFocusPainted(false);
        btnGuardarTodo.setBorderPainted(false);
        btnGuardarTodo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardarTodo.addActionListener(e -> guardarTodo());
        panel.add(btnGuardarTodo, gbc);

        return panel;
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXTO_OSCURO);
        return label;
    }

    private void cargarDatosUsuario() {
        if (usuarioActual != null) {
            txtNombre.setText(usuarioActual.getNombreCompleto() != null ? usuarioActual.getNombreCompleto() : "");
            txtEmail.setText(usuarioActual.getEmail() != null ? usuarioActual.getEmail() : "");
            txtTelefono.setText(usuarioActual.getTelefono() != null ? usuarioActual.getTelefono() : "");
            
            if (usuarioActual.getFotoRuta() != null && !usuarioActual.getFotoRuta().isEmpty()) {
                cargarFotoDesdeRuta(usuarioActual.getFotoRuta());
            }
        }
    }

    private void cargarFotoDesdeRuta(String ruta) {
        try {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                BufferedImage imagen = ImageIO.read(archivo);
                BufferedImage imagenRedimensionada = redimensionarImagen(imagen, 200, 200);
                lblFotoPerfil.setIcon(new ImageIcon(imagenRedimensionada));
            }
        } catch (IOException e) {
            System.err.println("Error al cargar foto: " + e.getMessage());
        }
    }

    private void abrirWebcam() {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "No se detectó una cámara web", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (webcam.isOpen()) webcam.close();

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setImageSizeDisplayed(true);
        webcamPanel.setMirrored(false);

        webcamDialog = new JDialog();
        webcamDialog.setTitle("Tomar Foto");
        webcamDialog.add(webcamPanel, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnCapturar = new JButton("Capturar");
        btnCapturar.setBackground(AZUL_PRIMARIO);
        btnCapturar.setForeground(Color.WHITE);
        btnCapturar.addActionListener(e -> {
            BufferedImage imagen = webcam.getImage();
            if (imagen != null) {
                guardarFotoTemporal(imagen);
                webcamDialog.dispose();
                webcam.close();
            }
        });

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> {
            webcamDialog.dispose();
            webcam.close();
        });

        webcamDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (webcam != null && webcam.isOpen()) webcam.close();
            }
        });

        panelBotones.add(btnCapturar);
        panelBotones.add(btnCancelar);
        webcamDialog.add(panelBotones, BorderLayout.SOUTH);
        webcamDialog.pack();
        webcamDialog.setLocationRelativeTo(null);
        webcamDialog.setVisible(true);

        webcam.open();
    }

    private void guardarFotoTemporal(BufferedImage imagen) {
        try {
            BufferedImage imagenRedimensionada = redimensionarImagen(imagen, 200, 200);
            lblFotoPerfil.setIcon(new ImageIcon(imagenRedimensionada));
            fotoTemporal = imagenRedimensionada;
            JOptionPane.showMessageDialog(this, "Foto capturada. Recuerda guardar los cambios.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void subirFotoArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage imagen = ImageIO.read(fileChooser.getSelectedFile());
                if (imagen != null) {
                    BufferedImage imagenRedimensionada = redimensionarImagen(imagen, 200, 200);
                    lblFotoPerfil.setIcon(new ImageIcon(imagenRedimensionada));
                    fotoTemporal = imagenRedimensionada;
                    JOptionPane.showMessageDialog(this, "Foto seleccionada. Recuerda guardar los cambios.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarFoto() {
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar la foto?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            lblFotoPerfil.setIcon(crearIconoPlaceholder(200, 200));
            fotoTemporal = null;
            usuarioActual.setFotoRuta(null);
        }
    }

    private BufferedImage redimensionarImagen(BufferedImage original, int ancho, int alto) {
        BufferedImage redimensionada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = redimensionada.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int x = (original.getWidth() - Math.min(original.getWidth(), original.getHeight())) / 2;
        int y = (original.getHeight() - Math.min(original.getWidth(), original.getHeight())) / 2;
        int size = Math.min(original.getWidth(), original.getHeight());
        
        g2d.drawImage(original.getSubimage(x, y, size, size), 0, 0, ancho, alto, null);
        g2d.dispose();
        return redimensionada;
    }

    private ImageIcon crearIconoPlaceholder(int ancho, int alto) {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagen.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(GRIS_CLARO);
        g2d.fillOval(0, 0, ancho, alto);
        g2d.setColor(GRIS_MEDIO);
        g2d.fillOval(ancho/2 - 30, alto/2 - 50, 60, 60);
        g2d.fillOval(ancho/2 - 40, alto/2, 80, 60);
        g2d.dispose();
        return new ImageIcon(imagen);
    }

    private void guardarTodo() {
        // Validar información
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!txtEmail.getText().trim().isEmpty() && !txtEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Correo electrónico inválido", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar contraseña si se está cambiando
        String passActual = new String(txtPasswordActual.getPassword());
        String passNueva = new String(txtPasswordNueva.getPassword());
        String passConfirmar = new String(txtPasswordConfirmar.getPassword());

        boolean cambiarPassword = !passActual.isEmpty() || !passNueva.isEmpty() || !passConfirmar.isEmpty();

        if (cambiarPassword) {
            if (passActual.isEmpty() || passNueva.isEmpty() || passConfirmar.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos de contraseña", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (passNueva.length() < 4) {
                JOptionPane.showMessageDialog(this, "Mínimo 4 caracteres", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!passNueva.equals(passConfirmar)) {
                JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            // Guardar foto si hay una nueva
            String rutaFinal = usuarioActual.getFotoRuta();
            if (fotoTemporal != null) {
                rutaFinal = guardarFotoEnDisco(fotoTemporal);
            }
            
            // Actualizar datos del usuario
            usuarioActual.setNombreCompleto(txtNombre.getText().trim());
            usuarioActual.setEmail(txtEmail.getText().trim());
            usuarioActual.setTelefono(txtTelefono.getText().trim());
            usuarioActual.setFotoRuta(rutaFinal);
            
            // Guardar en BD
            boolean exito = usuarioService.guardarUsuario(usuarioActual, null);
            
            if (exito) {
                // Cambiar contraseña si es necesario
                if (cambiarPassword) {
                    boolean passCambiada = usuarioService.cambiarPassword(usuarioActual.getId(), passActual, passNueva);
                    if (!passCambiada) {
                        JOptionPane.showMessageDialog(this, "Error al cambiar contraseña", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                // Actualizar sesión
                SessionManager.getInstancia().setUsuarioActual(usuarioActual);
                JOptionPane.showMessageDialog(this, "Perfil actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                // Limpiar campos de contraseña
                txtPasswordActual.setText("");
                txtPasswordNueva.setText("");
                txtPasswordConfirmar.setText("");
                fotoTemporal = null;
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String guardarFotoEnDisco(BufferedImage imagen) {
        try {
            File carpetaFotos = new File("fotos");
            if (!carpetaFotos.exists()) carpetaFotos.mkdirs();
            
            String nombreArchivo = "usuario_" + usuarioActual.getId() + "_" + System.currentTimeMillis() + ".png";
            File archivo = new File(carpetaFotos, nombreArchivo);
            ImageIO.write(imagen, "png", archivo);
            return archivo.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Error al guardar foto: " + e.getMessage());
            return null;
        }
    }
}