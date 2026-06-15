package com.nekomart.ui;

import com.nekomart.services.UsuarioService;
import com.nekomart.models.Usuario;
import com.nekomart.utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Panel de gestión de usuarios con diseño pastel moderno.
 * Tabla con fotos de perfil circulares, badges de rol con colores
 * (ADMIN=lavanda, EMPLEADO=menta), y estilo visual consistente.
 * Todo el código está comentado en español.
 */
public class UsuariosFrame extends JPanel {

    // ── Colores de la paleta centralizada usando ThemeManager ────────────────
    private static final Color LAVANDA = ThemeManager.AZUL_PRIMARIO;
    private static final Color LAVANDA_CLARO = ThemeManager.AZUL_MUY_CLARO;
    private static final Color MENTA = ThemeManager.EXITO;
    private static final Color CORAL = ThemeManager.AZUL_PRIMARIO;
    private static final Color FONDO = ThemeManager.FONDO_PRINCIPAL;
    private static final Color TEXTO_OSCURO = ThemeManager.GRIS_OSCURO;
    private static final Color TEXTO_GRIS = ThemeManager.GRIS_MEDIO;
    private static final Color BORDE = ThemeManager.GRIS_CLARO;
    private static final Color FILA_ALTERNA = ThemeManager.FONDO_PRINCIPAL;
    private static final Color HOVER_LAVANDA = ThemeManager.AZUL_MUY_CLARO;
    private static final Color VERDE_OSCURO = ThemeManager.EXITO;

    // Badges de Rol
    private static final Color BADGE_ADMIN_BG = new Color(155, 89, 182); // #9B59B6
    private static final Color BADGE_EMPLEADO_BG = new Color(26, 188, 156); // #1ABC9C

    // ── DAO y componentes de la tabla ─────────────────────────────────────
    private UsuarioService usuarioService;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    // Columnas de la tabla (Foto, Username, Nombre Completo, Rol, ID-oculto, FotoRuta-oculto)
    private final String[] COLUMNAS = {"Foto", "Username", "Nombre Completo", "Rol", "ID", "FotoRuta"};

    public UsuariosFrame() {
        usuarioService = new UsuarioService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(FONDO);

        initComponents();
        cargarUsuarios();
    }

    /**
     * Inicializa los componentes visuales del panel de usuarios.
     */
    private void initComponents() {
        // ══════════════════════════════════════════════════════════════════
        // PANEL SUPERIOR — Botones de acción
        // ══════════════════════════════════════════════════════════════════
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelSuperior.setOpaque(false);

        JButton btnNuevo = crearBotonPastel("👤 Nuevo Usuario", ThemeManager.AZUL_PRIMARIO, Color.WHITE);
        btnNuevo.setToolTipText("Crear un nuevo usuario");

        JButton btnEditar = crearBotonPastel("✏️ Editar", ThemeManager.AZUL_PRIMARIO, Color.WHITE);
        btnEditar.setToolTipText("Editar el usuario seleccionado");

        JButton btnEliminar = crearBotonPastel("🗑️ Eliminar", ThemeManager.PELIGRO, Color.WHITE);
        btnEliminar.setToolTipText("Eliminar el usuario seleccionado");

        JButton btnRefrescar = crearBotonPastel("🔄 Refrescar", ThemeManager.AZUL_MUY_CLARO, ThemeManager.GRIS_OSCURO);
        btnRefrescar.setToolTipText("Recargar la lista de usuarios");

        panelSuperior.add(btnNuevo);
        panelSuperior.add(btnEditar);
        panelSuperior.add(btnEliminar);
        panelSuperior.add(btnRefrescar);

        // ══════════════════════════════════════════════════════════════════
        // TABLA DE USUARIOS — Con foto circular y badge de rol
        // ══════════════════════════════════════════════════════════════════
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return ImageIcon.class; // Columna foto
                return String.class;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setAutoCreateRowSorter(true);
        tablaUsuarios.setRowHeight(48); // Altura para acomodar fotos circulares
        tablaUsuarios.setShowGrid(false);
        tablaUsuarios.setIntercellSpacing(new Dimension(0, 0));
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaUsuarios.setForeground(TEXTO_OSCURO);
        tablaUsuarios.setSelectionBackground(HOVER_LAVANDA);
        tablaUsuarios.setSelectionForeground(TEXTO_OSCURO);

        // Estilo del header (fondo #F1F5F9, texto #1E293B 14px bold)
        JTableHeader header = tablaUsuarios.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(ThemeManager.GRIS_MUY_CLARO);
        header.setForeground(ThemeManager.GRIS_OSCURO);
        header.setPreferredSize(new Dimension(0, 48));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // ── Configurar columnas ──────────────────────────────────────────

        // Columna 0: Foto (50px de ancho)
        tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(70);
        tablaUsuarios.getColumnModel().getColumn(0).setCellRenderer(new FotoCellRenderer());

        // Columnas 1 y 2: Username y Nombre (renderer con filas alternadas)
        DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(HOVER_LAVANDA);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
                }
                c.setForeground(TEXTO_OSCURO);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        tablaUsuarios.getColumnModel().getColumn(1).setCellRenderer(textRenderer);
        tablaUsuarios.getColumnModel().getColumn(2).setCellRenderer(textRenderer);

        // Columna 3: Rol (con badge de color)
        tablaUsuarios.getColumnModel().getColumn(3).setCellRenderer(new RolBadgeRenderer());

        // Columnas 4 y 5: ID y FotoRuta (ocultas, uso interno)
        tablaUsuarios.getColumnModel().getColumn(4).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(4).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(4).setWidth(0);
        tablaUsuarios.getColumnModel().getColumn(5).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(5).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(5).setWidth(0);

        // Panel contenedor con bordes redondeados
        JPanel panelTabla = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panelTabla.setOpaque(false);
        panelTabla.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        panelTabla.add(scrollPane, BorderLayout.CENTER);

        // ══════════════════════════════════════════════════════════════════
        // EVENTOS
        // ══════════════════════════════════════════════════════════════════
        btnNuevo.addActionListener(e -> abrirDialogoUsuario(null));
        btnEditar.addActionListener(e -> editarUsuarioSeleccionado());
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
        add(panelTabla, BorderLayout.CENTER);
    }

    /**
     * Crea un botón estilizado con colores pastel.
     */
    private JButton crearBotonPastel(String texto, Color bgColor, Color fgColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    /**
     * Carga todos los usuarios en la tabla.
     */
    private void cargarUsuarios() {
        modeloTabla.setRowCount(0);
        List<Usuario> usuarios = usuarioService.obtenerTodos();

        for (Usuario u : usuarios) {
            modeloTabla.addRow(new Object[]{
                    u.getFotoRuta(),         // Columna 0: ruta de foto (se renderiza como imagen circular)
                    u.getUsername(),          // Columna 1
                    u.getNombreCompleto(),   // Columna 2
                    u.getRol(),              // Columna 3
                    u.getId(),               // Columna 4 (oculta)
                    u.getFotoRuta()          // Columna 5 (oculta, respaldo)
            });
        }
    }

    /**
     * Abre un diálogo estilizado para crear o editar un usuario.
     * Columna izquierda: datos del usuario. Columna derecha: foto de perfil.
     *
     * @param usuario Usuario a editar, o null para crear uno nuevo
     */
    private void abrirDialogoUsuario(Usuario usuario) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                usuario != null ? "Editar Usuario" : "Nuevo Usuario",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // ── Panel principal con dos columnas ──────────────────────────────
        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2, 15, 0));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        panelPrincipal.setBackground(FONDO);

        // ── COLUMNA IZQUIERDA: campos de datos ───────────────────────────
        JPanel panelDatos = new JPanel(new GridBagLayout());
        panelDatos.setOpaque(false);
        panelDatos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtUsername = new JTextField(usuario != null ? usuario.getUsername() : "", 18);
        JTextField txtNombre = new JTextField(usuario != null ? usuario.getNombreCompleto() : "", 18);
        JPasswordField txtPassword = new JPasswordField(18);
        JComboBox<String> cmbRol = new JComboBox<>(new String[]{"EMPLEADO", "ADMIN"});

        if (usuario != null) {
            cmbRol.setSelectedItem(usuario.getRol());
        }

        String lblPasswordTxt = usuario != null ? "Contraseña (vacío = sin cambio):" : "Contraseña:";

        // Agregar campos al formulario
        addField(panelDatos, gbc, 0, "Username:", txtUsername);
        addField(panelDatos, gbc, 1, "Nombre Completo:", txtNombre);
        addField(panelDatos, gbc, 2, lblPasswordTxt, txtPassword);
        addField(panelDatos, gbc, 3, "Rol:", cmbRol);

        // ── COLUMNA DERECHA: foto de perfil ──────────────────────────────
        JPanel panelFoto = new JPanel(new BorderLayout(0, 8));
        panelFoto.setOpaque(false);
        panelFoto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Contenedor de imagen circular (80x80px con borde de 2px #E2E8F0)
        JLabel lblFoto = new JLabel("Sin foto", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 80;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.setColor(com.nekomart.Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE);
                g2.fillOval(x, y, size, size);
                Shape oldClip = g2.getClip();
                Shape clip = new Ellipse2D.Double(x, y, size, size);
                g2.setClip(clip);
                super.paintComponent(g2);
                g2.setClip(oldClip);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x, y, size, size);
                g2.dispose();
            }
        };
        lblFoto.setPreferredSize(new Dimension(80, 80));
        lblFoto.setMinimumSize(new Dimension(80, 80));
        lblFoto.setOpaque(false);
        lblFoto.setForeground(TEXTO_GRIS);

        // Ruta de foto actual
        final String[] fotoRuta = {usuario != null ? usuario.getFotoRuta() : null};

        // Mostrar foto actual si existe
        if (fotoRuta[0] != null && !fotoRuta[0].isEmpty()) {
            cargarMiniatura(lblFoto, fotoRuta[0]);
        }

        JButton btnSeleccionarFoto = crearBotonPastel("📁 Seleccionar Foto", LAVANDA, Color.WHITE);
        JButton btnQuitarFoto = crearBotonPastel("✖ Quitar Foto", FILA_ALTERNA, TEXTO_OSCURO);

        JPanel panelBotonesFoto = new JPanel(new GridLayout(1, 2, 5, 0));
        panelBotonesFoto.setOpaque(false);
        panelBotonesFoto.add(btnSeleccionarFoto);
        panelBotonesFoto.add(btnQuitarFoto);

        JPanel wrapperFoto = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
        wrapperFoto.setOpaque(false);
        wrapperFoto.add(lblFoto);
        panelFoto.add(wrapperFoto, BorderLayout.CENTER);
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

        // ── Botones Guardar / Cancelar ────────────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelBotones.setBackground(FONDO);

        JButton btnCancelar = crearBotonPastel("Cancelar", FILA_ALTERNA, TEXTO_OSCURO);
        JButton btnGuardar = crearBotonPastel("✔ Guardar", CORAL, Color.WHITE);

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        // Layout del diálogo
        dialog.setLayout(new BorderLayout());
        dialog.add(panelPrincipal, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);
        dialog.getContentPane().setBackground(FONDO);

        // Evento Cancelar
        btnCancelar.addActionListener(e -> dialog.dispose());

        // Evento Guardar
        btnGuardar.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String nombre = txtNombre.getText().trim();
            String password = new String(txtPassword.getPassword());
            String rol = (String) cmbRol.getSelectedItem();

            // Validación de campos obligatorios
            if (username.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Username y Nombre Completo son obligatorios.",
                        "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (usuario == null) {
                // Crear nuevo usuario
                if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "La contraseña es obligatoria para nuevos usuarios.",
                            "Error de validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Usuario nuevo = new Usuario(username, password, rol, nombre);
                nuevo.setFotoRuta(fotoRuta[0]);
                if (usuarioService.guardarUsuario(nuevo, password)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Usuario creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarUsuarios();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Error al crear el usuario. ¿El username ya existe o los datos son inválidos?",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Editar usuario existente
                usuario.setUsername(username);
                usuario.setNombreCompleto(nombre);
                usuario.setRol(rol);
                usuario.setFotoRuta(fotoRuta[0]);
                if (usuarioService.guardarUsuario(usuario, password)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Usuario actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarUsuarios();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Error al actualizar el usuario. ¿El username ya está en uso?",
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
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXTO_OSCURO);
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    /**
     * Copia la foto seleccionada al directorio imagenes/empleados/.
     *
     * @param archivoOrigen Archivo de imagen seleccionado
     * @return Ruta relativa guardada, o null si falla
     */
    private String copiarFotoEmpleado(File archivoOrigen) {
        try {
            Path dirDestino = Paths.get("imagenes", "empleados");
            Files.createDirectories(dirDestino);
            String nombreArchivo = System.currentTimeMillis() + "_" + archivoOrigen.getName();
            Path rutaDestino = dirDestino.resolve(nombreArchivo);
            Files.copy(archivoOrigen.toPath(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);
            return "imagenes/empleados/" + nombreArchivo;
        } catch (IOException ex) {
            System.err.println("Error al copiar foto de empleado: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Carga y escala la imagen en el JLabel de vista previa.
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
                    80,
                    80,
                    Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
            label.setText("");
        } else {
            label.setIcon(null);
            label.setText("Foto no encontrada");
        }
    }

    /**
     * Abre el diálogo de edición con el usuario seleccionado.
     */
    private void editarUsuarioSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila >= 0) {
            int modelRow = tablaUsuarios.convertRowIndexToModel(fila);
            int id = (int) modeloTabla.getValueAt(modelRow, 4);
            Usuario usuario = usuarioService.obtenerPorId(id);
            if (usuario != null) {
                abrirDialogoUsuario(usuario);
            }
        }
    }

    /**
     * Elimina el usuario seleccionado.
     */
    private void eliminarUsuarioSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un usuario para eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tablaUsuarios.convertRowIndexToModel(fila);
        String nombre = (String) modeloTabla.getValueAt(modelRow, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar al usuario '" + nombre + "'?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabla.getValueAt(modelRow, 4);
            if (usuarioService.eliminarUsuario(id)) {
                JOptionPane.showMessageDialog(this, "Usuario eliminado.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el usuario.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // RENDERERS PERSONALIZADOS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Renderer para la columna de foto de perfil.
     * Muestra la foto recortada en forma circular (50x50px).
     * Si no hay foto, muestra un círculo lavanda con las iniciales del usuario.
     */
    private class FotoCellRenderer extends JPanel implements TableCellRenderer {
        private Image foto;
        private String iniciales = "";
        private boolean tieneFoto = false;
        private boolean isSelected = false;
        private int row = 0;

        public FotoCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            this.isSelected = isSelected;
            this.row = row;
            this.tieneFoto = false;
            this.foto = null;
            this.iniciales = "";

            // Intentar cargar la foto desde la ruta
            String ruta = value != null ? value.toString() : "";
            if (!ruta.isEmpty()) {
                File f = new File(ruta);
                if (f.exists()) {
                    ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                    foto = icon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                    tieneFoto = true;
                }
            }

            // Si no hay foto, obtener iniciales del nombre
            if (!tieneFoto) {
                int modelRow = table.convertRowIndexToModel(row);
                Object nombreObj = table.getModel().getValueAt(modelRow, 2);
                if (nombreObj != null) {
                    String nombre = nombreObj.toString().trim();
                    if (!nombre.isEmpty()) {
                        String[] partes = nombre.split("\\s+");
                        iniciales = "";
                        for (int i = 0; i < Math.min(partes.length, 2); i++) {
                            iniciales += partes[i].substring(0, 1).toUpperCase();
                        }
                    }
                }
            }

            // Color de fondo según selección y fila
            if (isSelected) {
                setBackground(HOVER_LAVANDA);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
            }

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = 36;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            if (tieneFoto && foto != null) {
                // Dibujar foto recortada en forma circular
                Shape clip = new Ellipse2D.Double(x, y, size, size);
                g2.setClip(clip);
                g2.drawImage(foto, x, y, size, size, null);
                g2.setClip(null);

                // Borde del círculo
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, size, size);
            } else {
                // Círculo lavanda con iniciales en blanco
                g2.setColor(LAVANDA);
                g2.fillOval(x, y, size, size);

                // Iniciales centradas
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (size - fm.stringWidth(iniciales)) / 2;
                int ty = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(iniciales, tx, ty);
            }

            g2.dispose();
        }
    }

    /**
     * Renderer para la columna de rol que muestra un badge con color.
     * - ADMIN: fondo lavanda (#9B59B6), texto blanco
     * - EMPLEADO: fondo menta (#1ABC9C), texto blanco
     */
    private class RolBadgeRenderer extends JPanel implements TableCellRenderer {
        private String rol = "";
        private boolean isSelected = false;
        private int row = 0;

        public RolBadgeRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            this.rol = value != null ? value.toString() : "";
            this.isSelected = isSelected;
            this.row = row;

            if (isSelected) {
                setBackground(HOVER_LAVANDA);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
            }

            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Determinar colores del badge según el rol
            Color badgeBg;
            Color badgeFg = Color.WHITE;
            if (rol.equalsIgnoreCase("ADMIN")) {
                badgeBg = BADGE_ADMIN_BG;
            } else {
                badgeBg = BADGE_EMPLEADO_BG;
            }

            // Dibujar badge pill (redondeado)
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(rol);
            int badgeW = textW + 24; // padding 12px each side => +24
            int badgeH = 20; // height 20px
            int bx = 10;
            int by = (getHeight() - badgeH) / 2;

            g2.setColor(badgeBg);
            g2.fillRoundRect(bx, by, badgeW, badgeH, 4, 4); // border radius 4px

            g2.setColor(badgeFg);
            int tx = bx + (badgeW - textW) / 2;
            int ty = by + (badgeH + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(rol, tx, ty);

            g2.dispose();
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        reaplicarTemaUsuarios();
    }

    public void reaplicarTemaUsuarios() {
        Color fondo = com.nekomart.Main.isDarkMode ? new Color(0x1E, 0x1E, 0x1E) : FONDO;
        Color card = com.nekomart.Main.isDarkMode ? new Color(0x2D, 0x2D, 0x2D) : Color.WHITE;
        Color textClaro = com.nekomart.Main.isDarkMode ? Color.WHITE : TEXTO_OSCURO;
        Color selectionBg = com.nekomart.Main.isDarkMode ? new Color(70, 60, 100) : HOVER_LAVANDA;
        Color border = com.nekomart.Main.isDarkMode ? new Color(60, 60, 60) : BORDE;

        setBackground(fondo);
        if (tablaUsuarios != null) {
            tablaUsuarios.setForeground(textClaro);
            tablaUsuarios.setSelectionBackground(selectionBg);
            tablaUsuarios.setSelectionForeground(com.nekomart.Main.isDarkMode ? Color.WHITE : ThemeManager.GRIS_OSCURO);
            
            JTableHeader header = tablaUsuarios.getTableHeader();
            if (header != null) {
                header.setBackground(com.nekomart.Main.isDarkMode ? card : ThemeManager.GRIS_MUY_CLARO);
                header.setForeground(com.nekomart.Main.isDarkMode ? Color.WHITE : ThemeManager.GRIS_OSCURO);
            }
        }
        
        actualizarComponentesHijos(this, fondo, card, textClaro, border);
    }

    private void actualizarComponentesHijos(Component comp, Color fondo, Color card, Color textClaro, Color border) {
        if (comp instanceof JScrollPane) {
            ((JScrollPane) comp).getViewport().setBackground(card);
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                actualizarComponentesHijos(child, fondo, card, textClaro, border);
            }
        }
    }
}
