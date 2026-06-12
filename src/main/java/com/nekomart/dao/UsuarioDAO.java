package com.nekomart.dao;

import com.nekomart.models.Usuario;
import com.nekomart.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar operaciones de usuarios en la base de datos SQLite.
 * Todo el código está comentado en español.
 */
public class UsuarioDAO {

    /**
     * Valida las credenciales de un usuario al iniciar sesión.
     */
    public Usuario login(String username, String passwordPlano) {
        String sql = "SELECT id, username, password_hash, rol, nombre_completo, foto_ruta FROM usuarios WHERE username = ?";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashDB = rs.getString("password_hash");
                String hashInput = PasswordUtils.hashPassword(passwordPlano);
                if (hashDB.equals(hashInput)) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setRol(rs.getString("rol"));
                    u.setNombreCompleto(rs.getString("nombre_completo"));
                    u.setFotoRuta(rs.getString("foto_ruta"));
                    return u;
                }
            }
        } catch (SQLException e) { 
            System.err.println("Error login: " + e.getMessage()); 
        }
        return null;
    }

    /**
     * Lista todos los usuarios de la base de datos.
     */
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, username, rol, nombre_completo, foto_ruta FROM usuarios";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setRol(rs.getString("rol"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setFotoRuta(rs.getString("foto_ruta"));
                lista.add(u);
            }
        } catch (SQLException e) { 
            System.err.println("Error listar: " + e.getMessage()); 
        }
        return lista;
    }

    /**
     * Crea un nuevo usuario.
     */
    public boolean crear(Usuario u, String passwordPlano) {
        String sql = "INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getUsername());
            stmt.setString(2, PasswordUtils.hashPassword(passwordPlano));
            stmt.setString(3, u.getRol());
            stmt.setString(4, u.getNombreCompleto());
            stmt.setString(5, u.getFotoRuta());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("Error crear: " + e.getMessage()); 
            return false; 
        }
    }

    /**
     * Inserta un usuario con contraseña ya hasheada.
     */
    public boolean insertar(Usuario u) {
        String sql = "INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getUsername());
            stmt.setString(2, PasswordUtils.hashPassword(u.getPasswordHash()));
            stmt.setString(3, u.getRol());
            stmt.setString(4, u.getNombreCompleto());
            stmt.setString(5, u.getFotoRuta());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("Error insertar: " + e.getMessage()); 
            return false; 
        }
    }

    /**
     * Actualiza un usuario existente, con opción de cambiar o no su contraseña.
     */
    public boolean actualizar(Usuario u, boolean cambiarPassword) {
        String sql;
        if (cambiarPassword) {
            sql = "UPDATE usuarios SET username = ?, password_hash = ?, rol = ?, nombre_completo = ?, foto_ruta = ? WHERE id = ?";
        } else {
            sql = "UPDATE usuarios SET username = ?, rol = ?, nombre_completo = ?, foto_ruta = ? WHERE id = ?";
        }
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, u.getUsername());
            if (cambiarPassword) {
                stmt.setString(2, PasswordUtils.hashPassword(u.getPasswordHash()));
                stmt.setString(3, u.getRol());
                stmt.setString(4, u.getNombreCompleto());
                stmt.setString(5, u.getFotoRuta());
                stmt.setInt(6, u.getId());
            } else {
                stmt.setString(2, u.getRol());
                stmt.setString(3, u.getNombreCompleto());
                stmt.setString(4, u.getFotoRuta());
                stmt.setInt(5, u.getId());
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un usuario por su ID.
     */
    public Usuario buscarPorId(int id) {
        String sql = "SELECT id, username, password_hash, rol, nombre_completo, foto_ruta FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setRol(rs.getString("rol"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setFotoRuta(rs.getString("foto_ruta"));
                return u;
            }
        } catch (SQLException e) { 
            System.err.println("Error buscarPorId: " + e.getMessage()); 
        }
        return null;
    }

    /**
     * Elimina un usuario por su ID.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("Error eliminar: " + e.getMessage()); 
            return false; 
        }
    }

    /**
     * Busca un usuario por su nombre de usuario (username).
     */
    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT id, username, password_hash, rol, nombre_completo, foto_ruta FROM usuarios WHERE username = ?";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setRol(rs.getString("rol"));
                u.setNombreCompleto(rs.getString("nombre_completo"));
                u.setFotoRuta(rs.getString("foto_ruta"));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Error buscarPorUsername: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crea un usuario recibiendo el objeto Usuario completo.
     * La contraseña debe venir ya hasheada.
     */
    public boolean crear(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getPasswordHash());
            stmt.setString(3, usuario.getRol());
            stmt.setString(4, usuario.getNombreCompleto());
            stmt.setString(5, usuario.getFotoRuta());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error crear(Usuario): " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza un usuario recibiendo el objeto Usuario completo.
     * Si la contraseña (passwordHash) es nula o vacía, no se actualiza.
     */
    public boolean actualizar(Usuario usuario) {
        boolean cambiarPassword = usuario.getPasswordHash() != null && !usuario.getPasswordHash().trim().isEmpty();
        String sql;
        if (cambiarPassword) {
            sql = "UPDATE usuarios SET username = ?, password_hash = ?, rol = ?, nombre_completo = ?, foto_ruta = ? WHERE id = ?";
        } else {
            sql = "UPDATE usuarios SET username = ?, rol = ?, nombre_completo = ?, foto_ruta = ? WHERE id = ?";
        }
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsername());
            if (cambiarPassword) {
                stmt.setString(2, usuario.getPasswordHash());
                stmt.setString(3, usuario.getRol());
                stmt.setString(4, usuario.getNombreCompleto());
                stmt.setString(5, usuario.getFotoRuta());
                stmt.setInt(6, usuario.getId());
            } else {
                stmt.setString(2, usuario.getRol());
                stmt.setString(3, usuario.getNombreCompleto());
                stmt.setString(4, usuario.getFotoRuta());
                stmt.setInt(5, usuario.getId());
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar(Usuario): " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza la contraseña de un usuario específico.
     *
     * @param usuarioId         ID del usuario.
     * @param nuevoPasswordHash Nuevo hash de la contraseña encriptada.
     * @return true si se actualizó con éxito, false en caso contrario.
     */
    public boolean actualizarPassword(int usuarioId, String nuevoPasswordHash) {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoPasswordHash);
            stmt.setInt(2, usuarioId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizarPassword: " + e.getMessage());
            return false;
        }
    }
}