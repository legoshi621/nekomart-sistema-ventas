package com.nekomart.dao;

import com.nekomart.models.Usuario;
import com.nekomart.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad 'Usuario'.
 * Implementa consultas preparadas (PreparedStatement) para evitar ataques de inyección SQL.
 * Todo el código está en español.
 */
public class UsuarioDAO {

    private final ConexionDB conexionDB = ConexionDB.getInstancia();

    /**
     * Valida el acceso de un usuario al sistema mediante su nombre de usuario y contraseña.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña en texto plano a verificar.
     * @return Objeto Usuario si las credenciales coinciden con el registro, de lo contrario null.
     */
    public Usuario login(String username, String password) {
        String query = "SELECT id, username, password_hash, rol, nombre_completo FROM usuarios WHERE username = ?";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String passwordHashBD = rs.getString("password_hash");
                    
                    // Validar usando la utilidad segura de contraseñas
                    if (PasswordUtils.verifyPassword(password, passwordHashBD)) {
                        return new Usuario(
                            rs.getInt("id"),
                            rs.getString("username"),
                            passwordHashBD,
                            rs.getString("rol"),
                            rs.getString("nombre_completo")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error durante el inicio de sesión del usuario: " + e.getMessage());
        }
        return null;
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     * Encripta automáticamente la contraseña provista antes de realizar el guardado.
     *
     * @param usuario Objeto usuario con los datos y la contraseña en texto plano en su atributo passwordHash.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public boolean insertar(Usuario usuario) {
        String query = "INSERT INTO usuarios (username, password_hash, rol, nombre_completo) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, usuario.getUsername());
            
            // Encriptar contraseña en SHA-256 antes de enviar a base de datos
            String passwordEncriptada = PasswordUtils.hashPassword(usuario.getPasswordHash());
            ps.setString(2, passwordEncriptada);
            
            ps.setString(3, usuario.getRol());
            ps.setString(4, usuario.getNombreCompleto());
            
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                // Recuperar la clave ID generada por SQL Server
                try (ResultSet rsGeneratedKeys = ps.getGeneratedKeys()) {
                    if (rsGeneratedKeys.next()) {
                        usuario.setId(rsGeneratedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar el usuario: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene la información de un usuario dado su identificador único.
     *
     * @param id Identificador único del usuario.
     * @return Objeto Usuario con la información recuperada o null si no existe.
     */
    public Usuario buscarPorId(int id) {
        String query = "SELECT id, username, password_hash, rol, nombre_completo FROM usuarios WHERE id = ?";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("rol"),
                        rs.getString("nombre_completo")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retorna una lista con todos los usuarios registrados en el sistema.
     *
     * @return Lista de usuarios.
     */
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String query = "SELECT id, username, password_hash, rol, nombre_completo FROM usuarios";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("rol"),
                    rs.getString("nombre_completo")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar los usuarios: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Actualiza la información de un usuario en el sistema.
     *
     * @param usuario Objeto usuario con los datos actualizados.
     * @param actualizarPassword true si se desea modificar la contraseña, false si se mantiene la anterior.
     * @return true si la operación afectó al menos un registro, de lo contrario false.
     */
    public boolean actualizar(Usuario usuario, boolean actualizarPassword) {
        String query;
        if (actualizarPassword) {
            query = "UPDATE usuarios SET username = ?, password_hash = ?, rol = ?, nombre_completo = ? WHERE id = ?";
        } else {
            query = "UPDATE usuarios SET username = ?, rol = ?, nombre_completo = ? WHERE id = ?";
        }
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, usuario.getUsername());
            
            if (actualizarPassword) {
                String passwordEncriptada = PasswordUtils.hashPassword(usuario.getPasswordHash());
                ps.setString(2, passwordEncriptada);
                ps.setString(3, usuario.getRol());
                ps.setString(4, usuario.getNombreCompleto());
                ps.setInt(5, usuario.getId());
            } else {
                ps.setString(2, usuario.getRol());
                ps.setString(3, usuario.getNombreCompleto());
                ps.setInt(4, usuario.getId());
            }
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar el usuario: " + e.getMessage());
        }
        return false;
    }

    /**
     * Elimina un usuario del sistema por su ID.
     *
     * @param id Identificador único del usuario a eliminar.
     * @return true si la eliminación se completó satisfactoriamente.
     */
    public boolean eliminar(int id) {
        String query = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar el usuario: " + e.getMessage());
        }
        return false;
    }
}
