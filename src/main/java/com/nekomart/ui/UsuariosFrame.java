package com.nekomart.ui;

import com.nekomart.services.UsuarioService;
import com.nekomart.models.Usuario;
import com.nekomart.utils.ThemeManager;
import com.nekomart.utils.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class UsuariosFrame extends JPanel {

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

    private static final Color BADGE_ADMIN_BG = new Color(155, 89, 182);
    private static final Color BADGE_EMPLEADO_BG = new Color(26, 188, 156);
    private static final Color BADGE_ACTIVO_BG = new Color(16, 185, 129);
    private static final Color BADGE_BLOQUEADO_BG = new Color(239, 68, 68);

    private UsuarioService usuarioService;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JButton btnBloquear;

    private final String[] COLUMNAS = {"Foto", "Username", "Nombre Completo", "Rol", "Estado", "ID", "Activo", "FotoRuta"};

    public UsuariosFrame() {
        usuarioService = new UsuarioService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(FONDO);

        initComponents();
        cargarUsuarios();
    }

    private void initComponents() {
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelSuperior.setOpaque(false);

        JButton btnNuevo = crearBotonPastel("👤 Nuevo Usuario", ThemeManager.AZUL_PRIMARIO, Color.WHITE);
        JButton btnEditar = crearBotonPastel("✏️ Editar", ThemeManager.AZUL_PRIMARIO, Color.WHITE);
        JButton btnEliminar = crearBotonPastel("🗑️ Eliminar", ThemeManager.PELIGRO, Color.WHITE);
        btnBloquear = crearBotonPastel("🔒 Bloquear", new Color(245, 158, 11), Color.WHITE);
        JButton btnRefrescar = crearBotonPastel("🔄 Refrescar", ThemeManager.AZUL_MUY_CLARO, ThemeManager.GRIS_OSCURO);

        panelSuperior.add(btnNuevo);
        panelSuperior.add(btnEditar);
        panelSuperior.add(btnEliminar);
        panelSuperior.add(btnBloquear);
        panelSuperior.add(btnRefrescar);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return ImageIcon.class;
                if (column == 6) return Boolean.class;
                return String.class;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setAutoCreateRowSorter(true);
        tablaUsuarios.setRowHeight(48);
        tablaUsuarios.setShowGrid(false);
        tablaUsuarios.setIntercellSpacing(new Dimension(0, 0));
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaUsuarios.setForeground(TEXTO_OSCURO);
        tablaUsuarios.setSelectionBackground(HOVER_LAVANDA);
        tablaUsuarios.setSelectionForeground(TEXTO_OSCURO);

        JTableHeader header = tablaUsuarios.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(ThemeManager.GRIS_MUY_CLARO);
        header.setForeground(TEXTO_OSCURO);
        header.setPreferredSize(new Dimension(0, 48));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(70);
        tablaUsuarios.getColumnModel().getColumn(0).setCellRenderer(new FotoCellRenderer());

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
        tablaUsuarios.getColumnModel().getColumn(3).setCellRenderer(new RolBadgeRenderer());
        tablaUsuarios.getColumnModel().getColumn(4).setCellRenderer(new EstadoBadgeRenderer());

        ocultarColumna(5);
        ocultarColumna(6);
        ocultarColumna(7);

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

        btnNuevo.addActionListener(e -> abrirDialogoUsuario(null));
        btnEditar.addActionListener(e -> editarUsuarioSeleccionado());
        btnEliminar.addActionListener(e -> eliminarUsuarioSeleccionado());
        btnBloquear.addActionListener(e -> bloquearDesbloquearUsuario());
        btnRefrescar.addActionListener(e -> cargarUsuarios());

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

    private void ocultarColumna(int indice) {
        tablaUsuarios.getColumnModel().getColumn(indice).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(indice).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(indice).setWidth(0);
    }

    private JButton crearBotonPastel(String texto, Color bgColor, Color fgColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void cargarUsuarios() {
        modeloTabla.setRowCount(0);
        List<Usuario> usuarios = usuarioService.obtenerTodos();

        for (Usuario u : usuarios) {
            boolean activo = verificarEstadoActivo(u.getId());
            modeloTabla.addRow(new Object[]{
                    u.getFotoRuta(),
                    u.getUsername(),
                    u.getNombreCompleto(),
                    u.getRol(),
                    activo ? "Activo" : "Bloqueado",
                    u.getId(),
                    activo,
                    u.getFotoRuta()
            });
        }
    }

    private boolean verificarEstadoActivo(int id) {
        try {
            java.sql.Connection conn = com.nekomart.dao.ConexionDB.getInstancia().getConexion();
            java.sql.PreparedStatement stmt = conn.prepareStatement(
                "SELECT activo FROM usuarios WHERE id = ?");
            stmt.setInt(1, id);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int valor = rs.getInt("activo");
                return valor == 1;
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error al verificar estado: " + e.getMessage());
        }
        return true;
    }

    private void abrirDialogoUsuario(Usuario usuario) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                usuario != null ? "Editar Usuario" : "Nuevo Usuario",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2, 15, 0));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        panelPrincipal.setBackground(FONDO);

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

        addField(panelDatos, gbc, 0, "Username:", txtUsername);
        addField(panelDatos, gbc, 1, "Nombre Completo:", txtNombre);
        addField(panelDatos, gbc, 2, lblPasswordTxt, txtPassword);
        addField(panelDatos, gbc, 3, "Rol:", cmbRol);

        JPanel panelFoto = new JPanel(new BorderLayout(0, 8));
        panelFoto.setOpaque(false);
        panelFoto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblFoto = new JLabel("Sin foto", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 80;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.setColor(Color.WHITE);
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

        final String[] fotoRuta = {usuario != null ? usuario.getFotoRuta() : null};

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
                }
            }
        });

        btnQuitarFoto.addActionListener(e -> {
            fotoRuta[0] = null;
            lblFoto.setIcon(null);
            lblFoto.setText("Sin foto");
        });

        panelPrincipal.add(panelDatos);
        panelPrincipal.add(panelFoto);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panelBotones.setBackground(FONDO);

        JButton btnCancelar = crearBotonPastel("Cancelar", FILA_ALTERNA, TEXTO_OSCURO);
        JButton btnGuardar = crearBotonPastel("✔ Guardar", CORAL, Color.WHITE);

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        dialog.setLayout(new BorderLayout());
        dialog.add(panelPrincipal, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);
        dialog.getContentPane().setBackground(FONDO);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnGuardar.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String nombre = txtNombre.getText().trim();
            String password = new String(txtPassword.getPassword());
            String rol = (String) cmbRol.getSelectedItem();

            if (username.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Username y Nombre Completo son obligatorios.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (usuario == null) {
                if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "La contraseña es obligatoria.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Usuario nuevo = new Usuario(username, password, rol, nombre);
                nuevo.setFotoRuta(fotoRuta[0]);
                if (usuarioService.guardarUsuario(nuevo, password)) {
                    JOptionPane.showMessageDialog(dialog, "Usuario creado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarUsuarios();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error al crear usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                usuario.setUsername(username);
                usuario.setNombreCompleto(nombre);
                usuario.setRol(rol);
                usuario.setFotoRuta(fotoRuta[0]);
                if (usuarioService.guardarUsuario(usuario, password)) {
                    JOptionPane.showMessageDialog(dialog, "Usuario actualizado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarUsuarios();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error al actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setVisible(true);
    }

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

    private String copiarFotoEmpleado(File archivoOrigen) {
        try {
            Path dirDestino = Paths.get("imagenes", "empleados");
            Files.createDirectories(dirDestino);
            String nombreArchivo = System.currentTimeMillis() + "_" + archivoOrigen.getName();
            Path rutaDestino = dirDestino.resolve(nombreArchivo);
            Files.copy(archivoOrigen.toPath(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);
            return "imagenes/empleados/" + nombreArchivo;
        } catch (IOException ex) {
            System.err.println("Error al copiar foto: " + ex.getMessage());
            return null;
        }
    }

    private void cargarMiniatura(JLabel label, String rutaFoto) {
        if (rutaFoto == null || rutaFoto.isEmpty()) {
            label.setIcon(null);
            label.setText("Sin foto");
            return;
        }
        File f = new File(rutaFoto);
        if (f.exists()) {
            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
            label.setText("");
        }
    }

    private void editarUsuarioSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila >= 0) {
            int modelRow = tablaUsuarios.convertRowIndexToModel(fila);
            int id = (int) modeloTabla.getValueAt(modelRow, 5);
            Usuario usuario = usuarioService.obtenerPorId(id);
            if (usuario != null) {
                abrirDialogoUsuario(usuario);
            }
        }
    }

    private void eliminarUsuarioSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tablaUsuarios.convertRowIndexToModel(fila);
        int id = (int) modeloTabla.getValueAt(modelRow, 5);
        String nombre = (String) modeloTabla.getValueAt(modelRow, 2);

        Usuario usuarioActual = SessionManager.getInstancia().getUsuarioActual();
        if (usuarioActual != null && usuarioActual.getId() == id) {
            JOptionPane.showMessageDialog(this, "No puedes eliminar tu propio usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al usuario '" + nombre + "'?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (usuarioService.eliminarUsuario(id)) {
                JOptionPane.showMessageDialog(this, "Usuario eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void bloquearDesbloquearUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tablaUsuarios.convertRowIndexToModel(fila);
        int id = (int) modeloTabla.getValueAt(modelRow, 5);
        String nombre = (String) modeloTabla.getValueAt(modelRow, 2);
        String rol = (String) modeloTabla.getValueAt(modelRow, 3);
        boolean estaActivo = (boolean) modeloTabla.getValueAt(modelRow, 6);

        Usuario usuarioActual = SessionManager.getInstancia().getUsuarioActual();
        if (usuarioActual != null && usuarioActual.getId() == id) {
            JOptionPane.showMessageDialog(this, "No puedes bloquear tu propio usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (rol.equals("ADMIN") && estaActivo) {
            long adminsActivos = 0;
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                if ("ADMIN".equals(modeloTabla.getValueAt(i, 3)) && (boolean) modeloTabla.getValueAt(i, 6)) {
                    adminsActivos++;
                }
            }
            if (adminsActivos <= 1) {
                JOptionPane.showMessageDialog(this,
                    "No se puede bloquear. Debe haber al menos un administrador activo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String accion = estaActivo ? "BLOQUEAR" : "DESBLOQUEAR";
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de " + accion + " al usuario '" + nombre + "'?",
                "Confirmar " + accion, JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean exito = usuarioService.cambiarEstadoActivo(id, !estaActivo);
            if (exito) {
                JOptionPane.showMessageDialog(this,
                    "Usuario " + (estaActivo ? "bloqueado" : "desbloqueado") + " exitosamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "Error al cambiar el estado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

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

            String ruta = value != null ? value.toString() : "";
            if (!ruta.isEmpty()) {
                File f = new File(ruta);
                if (f.exists()) {
                    ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                    foto = icon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                    tieneFoto = true;
                }
            }

            if (!tieneFoto) {
                int modelRow = table.convertRowIndexToModel(row);
                Object nombreObj = table.getModel().getValueAt(modelRow, 2);
                if (nombreObj != null) {
                    String nombre = nombreObj.toString().trim();
                    if (!nombre.isEmpty()) {
                        String[] partes = nombre.split("\\s+");
                        for (int i = 0; i < Math.min(partes.length, 2); i++) {
                            iniciales += partes[i].substring(0, 1).toUpperCase();
                        }
                    }
                }
            }

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
                Shape clip = new Ellipse2D.Double(x, y, size, size);
                g2.setClip(clip);
                g2.drawImage(foto, x, y, size, size, null);
                g2.setClip(null);
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, size, size);
            } else {
                g2.setColor(LAVANDA);
                g2.fillOval(x, y, size, size);
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

            Color badgeBg = rol.equalsIgnoreCase("ADMIN") ? BADGE_ADMIN_BG : BADGE_EMPLEADO_BG;

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(rol);
            int badgeW = textW + 24;
            int badgeH = 20;
            int bx = 10;
            int by = (getHeight() - badgeH) / 2;

            g2.setColor(badgeBg);
            g2.fillRoundRect(bx, by, badgeW, badgeH, 4, 4);

            g2.setColor(Color.WHITE);
            int tx = bx + (badgeW - textW) / 2;
            int ty = by + (badgeH + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(rol, tx, ty);

            g2.dispose();
        }
    }

    private class EstadoBadgeRenderer extends JPanel implements TableCellRenderer {
        private String estado = "";
        private boolean isSelected = false;
        private int row = 0;

        public EstadoBadgeRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            this.estado = value != null ? value.toString() : "";
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

            Color badgeBg = estado.equals("Activo") ? BADGE_ACTIVO_BG : BADGE_BLOQUEADO_BG;

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(estado);
            int badgeW = textW + 24;
            int badgeH = 20;
            int bx = 10;
            int by = (getHeight() - badgeH) / 2;

            g2.setColor(badgeBg);
            g2.fillRoundRect(bx, by, badgeW, badgeH, 4, 4);

            g2.setColor(Color.WHITE);
            int tx = bx + (badgeW - textW) / 2;
            int ty = by + (badgeH + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(estado, tx, ty);

            g2.dispose();
        }
    }
}








