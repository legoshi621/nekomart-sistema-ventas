package com.nekomart.dao;

import com.nekomart.models.Usuario;
import com.nekomart.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario login(String username, String passwordPlano) {
        String sql = "SELECT id, username, password_hash, rol, nombre_completo, foto_ruta, email, telefono, activo FROM usuarios WHERE username = ?";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Verificar si está activo
                int activo = rs.getInt("activo");
                if (activo == 0) {
                    System.err.println("Usuario bloqueado");
                    return null;
                }
                
                String hashDB = rs.getString("password_hash");
                String hashInput = PasswordUtils.hashPassword(passwordPlano);
                if (hashDB.equals(hashInput)) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setRol(rs.getString("rol"));
                    u.setNombreCompleto(rs.getString("nombre_completo"));
                    u.setFotoRuta(rs.getString("foto_ruta"));
                    u.setEmail(rs.getString("email"));
                    u.setTelefono(rs.getString("telefono"));
                    return u;
                }
            }
        } catch (SQLException e) { 
            System.err.println("Error login: " + e.getMessage()); 
        }
        return null;
    }

   public List<Usuario> listarTodos() {
    List<Usuario> lista = new ArrayList<>();
    String sql = "SELECT id, username, rol, nombre_completo, foto_ruta, email, telefono, activo FROM usuarios";
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
            u.setEmail(rs.getString("email"));
            u.setTelefono(rs.getString("telefono"));
            // Agregar campo activo - necesitamos agregarlo al modelo Usuario
            lista.add(u);
        }
    } catch (SQLException e) { 
        System.err.println("Error listar: " + e.getMessage()); 
    }
    return lista;
}
    public boolean crear(Usuario u, String passwordPlano) {
        String sql = "INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta, email, telefono, activo) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getUsername());
            stmt.setString(2, PasswordUtils.hashPassword(passwordPlano));
            stmt.setString(3, u.getRol());
            stmt.setString(4, u.getNombreCompleto());
            stmt.setString(5, u.getFotoRuta());
            stmt.setString(6, u.getEmail());
            stmt.setString(7, u.getTelefono());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { 
            System.err.println("Error crear: " + e.getMessage()); 
            return false; 
        }
    }

    public boolean actualizar(Usuario usuario) {
        boolean cambiarPassword = usuario.getPasswordHash() != null && !usuario.getPasswordHash().trim().isEmpty();
        String sql;
        if (cambiarPassword) {
            sql = "UPDATE usuarios SET username = ?, password_hash = ?, rol = ?, nombre_completo = ?, foto_ruta = ?, email = ?, telefono = ? WHERE id = ?";
        } else {
            sql = "UPDATE usuarios SET username = ?, rol = ?, nombre_completo = ?, foto_ruta = ?, email = ?, telefono = ? WHERE id = ?";
        }
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsername());
            if (cambiarPassword) {
                stmt.setString(2, usuario.getPasswordHash());
                stmt.setString(3, usuario.getRol());
                stmt.setString(4, usuario.getNombreCompleto());
                stmt.setString(5, usuario.getFotoRuta());
                stmt.setString(6, usuario.getEmail());
                stmt.setString(7, usuario.getTelefono());
                stmt.setInt(8, usuario.getId());
            } else {
                stmt.setString(2, usuario.getRol());
                stmt.setString(3, usuario.getNombreCompleto());
                stmt.setString(4, usuario.getFotoRuta());
                stmt.setString(5, usuario.getEmail());
                stmt.setString(6, usuario.getTelefono());
                stmt.setInt(7, usuario.getId());
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar: " + e.getMessage());
            return false;
        }
    }

    public Usuario buscarPorId(int id) {
        String sql = "SELECT id, username, password_hash, rol, nombre_completo, foto_ruta, email, telefono, activo FROM usuarios WHERE id = ?";
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
                u.setEmail(rs.getString("email"));
                u.setTelefono(rs.getString("telefono"));
                return u;
            }
        } catch (SQLException e) { 
            System.err.println("Error buscarPorId: " + e.getMessage()); 
        }
        return null;
    }

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

    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT id, username, password_hash, rol, nombre_completo, foto_ruta, email, telefono, activo FROM usuarios WHERE username = ?";
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
                u.setEmail(rs.getString("email"));
                u.setTelefono(rs.getString("telefono"));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Error buscarPorUsername: " + e.getMessage());
        }
        return null;
    }

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

    // NUEVO: Bloquear/desbloquear usuario
    public boolean cambiarEstadoActivo(int usuarioId, boolean activo) {
        String sql = "UPDATE usuarios SET activo = ? WHERE id = ?";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, activo ? 1 : 0);
            stmt.setInt(2, usuarioId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error cambiarEstadoActivo: " + e.getMessage());
            return false;
        }
    }
}