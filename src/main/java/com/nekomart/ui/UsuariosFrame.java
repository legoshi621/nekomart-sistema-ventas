package com.nekomart.ui;

import com.nekomart.dao.UsuarioDAO;
import com.nekomart.models.Usuario;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Panel de gestión de usuarios (solo accesible para ADMIN).
 * Permite crear, editar y eliminar usuarios del sistema,
 * incluyendo subida y visualización de foto de perfil.
 * Todo el código está comentado en español.
 */
public class UsuariosFrame extends JPanel {

    private UsuarioDAO usuarioDAO;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    // Columnas de la tabla
    private final String[] COLUMNAS = { "ID", "Username", "Nombre Completo", "Rol", "Foto" };

    public UsuariosFrame() {
        usuarioDAO = new UsuarioDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        cargarUsuarios();
    }

    /**
     * Inicializa los componentes visuales del panel.
     */
    private void initComponents() {
        // Panel superior con botones de acción
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton btnNuevo = new JButton("👤 Nuevo Usuario");
        JButton btnEliminar = new JButton("🗑 Eliminar");
        JButton btnRefrescar = new JButton("🔄 Refrescar");

        btnNuevo.putClientProperty("JButton.buttonType", "default");
        btnNuevo.setToolTipText("Crear un nuevo usuario");
        btnEliminar.setToolTipText("Eliminar el usuario seleccionado");
        btnRefrescar.setToolTipText("Recargar la lista de usuarios");

        panelSuperior.add(btnNuevo);
        panelSuperior.add(btnEliminar);
        panelSuperior.add(btnRefrescar);

        // Tabla de usuarios — no editable directamente
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setAutoCreateRowSorter(true);
        tablaUsuarios.setRowHeight(28);

        // Ocultar columna ID y Foto (usadas internamente)
        tablaUsuarios.getColumnModel().getColumn(0).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setWidth(0);
        tablaUsuarios.getColumnModel().getColumn(4).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(4).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(4).setWidth(0);

        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);

        // Eventos de botones
        btnNuevo.addActionListener(e -> abrirDialogoUsuario(null));
        btnEliminar.addActionListener(e -> eliminarUsuarioSeleccionado());
        btnRefrescar.addActionListener(e -> cargarUsuarios());

        // Doble clic para editar
        tablaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarUsuarioSeleccionado();
                }
            }
        });

        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Carga todos los usuarios en la tabla.
     */
    private void cargarUsuarios() {
        modeloTabla.setRowCount(0);
        List<Usuario> usuarios = usuarioDAO.listarTodos();

        for (Usuario u : usuarios) {
            modeloTabla.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getNombreCompleto(),
                    u.getRol(),
                    u.getFotoRuta()
            });
        }
    }

    /**
     * Abre un diálogo dual-columna para crear o editar un usuario.
     * Columna izquierda: datos del usuario.
     * Columna derecha: vista previa de foto de perfil.
     *
     * @param usuario Usuario a editar, o null si se crea uno nuevo.
     */
    private void abrirDialogoUsuario(Usuario usuario) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                usuario != null ? "Editar Usuario" : "Nuevo Usuario",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // ── Panel principal con dos columnas ──────────────────────────
        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2, 15, 0));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        // ── COLUMNA IZQUIERDA: campos de datos ────────────────────────
        JPanel panelDatos = new JPanel(new GridBagLayout());
        panelDatos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Datos del Usuario",
                TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtUsername    = new JTextField(usuario != null ? usuario.getUsername() : "", 18);
        JTextField txtNombre      = new JTextField(usuario != null ? usuario.getNombreCompleto() : "", 18);
        JPasswordField txtPassword = new JPasswordField(18);
        JComboBox<String> cmbRol  = new JComboBox<>(new String[]{"EMPLEADO", "ADMIN"});

        if (usuario != null) {
            cmbRol.setSelectedItem(usuario.getRol());
        }

        String lblPassword = usuario != null ? "Contraseña (vacío = sin cambio):" : "Contraseña:";

        addField(panelDatos, gbc, 0, "Username:",     txtUsername);
        addField(panelDatos, gbc, 1, "Nombre Completo:", txtNombre);
        addField(panelDatos, gbc, 2, lblPassword,     txtPassword);
        addField(panelDatos, gbc, 3, "Rol:",          cmbRol);

        // ── COLUMNA DERECHA: foto de perfil ───────────────────────────
        JPanel panelFoto = new JPanel(new BorderLayout(0, 8));
        panelFoto.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Foto de Perfil",
                TitledBorder.LEFT, TitledBorder.TOP));

        // Contenedor de imagen con fondo gris claro
        JLabel lblFoto = new JLabel("Sin foto", SwingConstants.CENTER);
        lblFoto.setPreferredSize(new Dimension(220, 220));
        lblFoto.setMinimumSize(new Dimension(220, 220));
        lblFoto.setOpaque(true);
        lblFoto.setBackground(new Color(230, 232, 240));
        lblFoto.setForeground(new Color(130, 130, 150));
        lblFoto.setBorder(BorderFactory.createLineBorder(new Color(180, 185, 210)));

        // Ruta de foto actual
        final String[] fotoRuta = { usuario != null ? usuario.getFotoRuta() : null };

        // Mostrar foto actual si existe
        if (fotoRuta[0] != null && !fotoRuta[0].isEmpty()) {
            cargarMiniatura(lblFoto, fotoRuta[0]);
        }

        JButton btnSeleccionarFoto = new JButton("📁 Seleccionar Foto");
        btnSeleccionarFoto.setToolTipText("Selecciona una imagen JPG o PNG");

        JButton btnQuitarFoto = new JButton("✖ Quitar Foto");
        btnQuitarFoto.setToolTipText("Eliminar la foto de perfil asignada");

        JPanel panelBotonesFoto = new JPanel(new GridLayout(1, 2, 5, 0));
        panelBotonesFoto.add(btnSeleccionarFoto);
        panelBotonesFoto.add(btnQuitarFoto);

        panelFoto.add(lblFoto, BorderLayout.CENTER);
        panelFoto.add(panelBotonesFoto, BorderLayout.SOUTH);

        // Evento: seleccionar foto
        btnSeleccionarFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar foto de perfil");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Imágenes (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File archivoOrigen = chooser.getSelectedFile();
                String rutaRelativa = copiarFotoEmpleado(archivoOrigen);
                if (rutaRelativa != null) {
                    fotoRuta[0] = rutaRelativa;
                    cargarMiniatura(lblFoto, rutaRelativa);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Error al copiar la foto. Verifica los permisos.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Evento: quitar foto
        btnQuitarFoto.addActionListener(e -> {
            fotoRuta[0] = null;
            lblFoto.setIcon(null);
            lblFoto.setText("Sin foto");
        });

        panelPrincipal.add(panelDatos);
        panelPrincipal.add(panelFoto);

        // ── Botones Guardar / Cancelar ────────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnGuardar  = new JButton("✔ Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.putClientProperty("JButton.buttonType", "default");
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        // ── Layout del diálogo ────────────────────────────────────────
        dialog.setLayout(new BorderLayout());
        dialog.add(panelPrincipal, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);

        // Evento Cancelar
        btnCancelar.addActionListener(e -> dialog.dispose());

        // Evento Guardar
        btnGuardar.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String nombre   = txtNombre.getText().trim();
            String password = new String(txtPassword.getPassword());
            String rol      = (String) cmbRol.getSelectedItem();

            // Validación de campos obligatorios
            if (username.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Username y Nombre Completo son obligatorios.",
                        "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (usuario == null) {
                // ── Crear nuevo usuario ───────────────────────────────
                if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "La contraseña es obligatoria para nuevos usuarios.",
                            "Error de validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Usuario nuevo = new Usuario(username, password, rol, nombre);
                nuevo.setFotoRuta(fotoRuta[0]);
                if (usuarioDAO.insertar(nuevo)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Usuario creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarUsuarios();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Error al crear el usuario. ¿El username ya existe?",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // ── Editar usuario existente ──────────────────────────
                boolean cambiarPassword = !password.isEmpty();
                usuario.setUsername(username);
                usuario.setNombreCompleto(nombre);
                usuario.setRol(rol);
                usuario.setFotoRuta(fotoRuta[0]);
                if (cambiarPassword) {
                    usuario.setPasswordHash(password);
                }
                if (usuarioDAO.actualizar(usuario, cambiarPassword)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Usuario actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarUsuarios();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar el usuario.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Agrega un par (etiqueta, componente) al panel con GridBagLayout.
     */
    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    /**
     * Copia la foto seleccionada al directorio imagenes/empleados/ con nombre único,
     * y devuelve la ruta relativa para guardar en la base de datos.
     *
     * @param archivoOrigen Archivo de imagen seleccionado por el usuario.
     * @return Ruta relativa guardada (ej. "imagenes/empleados/1717200000000_foto.jpg"), o null si falla.
     */
    private String copiarFotoEmpleado(File archivoOrigen) {
        try {
            // Crear directorio si no existe
            Path dirDestino = Paths.get("imagenes", "empleados");
            Files.createDirectories(dirDestino);

            // Nombre único con timestamp
            String nombreArchivo = System.currentTimeMillis() + "_" + archivoOrigen.getName();
            Path rutaDestino = dirDestino.resolve(nombreArchivo);

            // Copiar archivo
            Files.copy(archivoOrigen.toPath(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            // Devolver ruta relativa con separadores estándar
            return "imagenes/empleados/" + nombreArchivo;
        } catch (IOException ex) {
            System.err.println("Error al copiar foto de empleado: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Carga y escala la imagen de la ruta dada en el JLabel de vista previa.
     *
     * @param label    JLabel donde mostrar la imagen.
     * @param rutaFoto Ruta relativa o absoluta de la foto.
     */
    private void cargarMiniatura(JLabel label, String rutaFoto) {
        if (rutaFoto == null || rutaFoto.isEmpty()) {
            label.setIcon(null);
            label.setText("Sin foto");
            return;
        }
        File f = new File(rutaFoto);
        if (f.exists()) {
            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(
                    label.getPreferredSize().width - 10,
                    label.getPreferredSize().height - 10,
                    Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
            label.setText("");
        } else {
            label.setIcon(null);
            label.setText("Foto no encontrada");
        }
    }

    /**
     * Abre el diálogo de edición con el usuario seleccionado en la tabla.
     */
    private void editarUsuarioSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila >= 0) {
            int id = (int) modeloTabla.getValueAt(fila, 0);
            Usuario usuario = usuarioDAO.buscarPorId(id);
            if (usuario != null) {
                abrirDialogoUsuario(usuario);
            }
        }
    }

    /**
     * Elimina el usuario seleccionado en la tabla.
     */
    private void eliminarUsuarioSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un usuario para eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = (String) modeloTabla.getValueAt(fila, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar al usuario '" + nombre + "'?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabla.getValueAt(fila, 0);
            if (usuarioDAO.eliminar(id)) {
                JOptionPane.showMessageDialog(this, "Usuario eliminado.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el usuario.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
