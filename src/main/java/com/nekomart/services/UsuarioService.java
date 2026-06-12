package com.nekomart.services;

import com.nekomart.dao.UsuarioDAO;
import com.nekomart.models.Usuario;
import com.nekomart.utils.PasswordUtils;

import java.util.List;

/**
 * Servicio que implementa la lógica de negocio para la gestión de usuarios.
 * Se encarga de las validaciones de duplicados y encriptación de contraseñas.
 * Todo el código está comentado en español.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Obtiene todos los usuarios registrados en el sistema.
     *
     * @return Lista de objetos Usuario.
     */
    public List<Usuario> obtenerTodos() {
        try {
            return usuarioDAO.listarTodos();
        } catch (Exception e) {
            System.err.println("Error en UsuarioService.obtenerTodos: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Busca un usuario específico por su ID.
     *
     * @param id Identificador único del usuario.
     * @return El objeto Usuario correspondiente, o null si no se encuentra.
     */
    public Usuario obtenerPorId(int id) {
        try {
            return usuarioDAO.buscarPorId(id);
        } catch (Exception e) {
            System.err.println("Error en UsuarioService.obtenerPorId: " + e.getMessage());
            return null;
        }
    }

    /**
     * Guarda (crea o actualiza) un usuario, realizando las validaciones pertinentes.
     *
     * @param usuario       Objeto con los datos del usuario.
     * @param passwordPlano Contraseña en texto plano (obligatoria si es nuevo, opcional si es edición).
     * @return true si la operación fue exitosa, false si hubo un error o duplicación del username.
     */
    public boolean guardarUsuario(Usuario usuario, String passwordPlano) {
        try {
            // 1. Validar que el username no esté duplicado
            Usuario existente = usuarioDAO.buscarPorUsername(usuario.getUsername());
            if (existente != null) {
                // Si es un nuevo registro, o si es edición pero pertenece a otro usuario diferente
                if (usuario.getId() == 0 || existente.getId() != usuario.getId()) {
                    System.err.println("Error: El nombre de usuario '" + usuario.getUsername() + "' ya está en uso.");
                    return false;
                }
            }

            // 2. Procesar contraseña y guardar
            if (usuario.getId() == 0) {
                // Nuevo usuario: la contraseña es estrictamente obligatoria
                if (passwordPlano == null || passwordPlano.trim().isEmpty()) {
                    System.err.println("Error: La contraseña es obligatoria para nuevos usuarios.");
                    return false;
                }
                // Hashear la contraseña
                usuario.setPasswordHash(PasswordUtils.hashPassword(passwordPlano));
                return usuarioDAO.crear(usuario);
            } else {
                // Usuario existente (Edición):
                if (passwordPlano != null && !passwordPlano.trim().isEmpty()) {
                    // Si se proporcionó una nueva contraseña, la hasheamos
                    usuario.setPasswordHash(PasswordUtils.hashPassword(passwordPlano));
                } else {
                    // Si no se proporcionó, dejamos el hash vacío/nulo para que no se actualice
                    usuario.setPasswordHash(null);
                }
                return usuarioDAO.actualizar(usuario);
            }
        } catch (Exception e) {
            System.err.println("Error en UsuarioService.guardarUsuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id Identificador del usuario a eliminar.
     * @return true si se eliminó correctamente, false en caso contrario.
     */
    public boolean eliminarUsuario(int id) {
        try {
            return usuarioDAO.eliminar(id);
        } catch (Exception e) {
            System.err.println("Error en UsuarioService.eliminarUsuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cambia la contraseña de un usuario validando sus credenciales actuales.
     *
     * @param usuarioId  ID del usuario que cambia su contraseña.
     * @param passActual Contraseña actual en texto plano.
     * @param passNueva  Nueva contraseña en texto plano.
     * @return true si el cambio fue exitoso, false en caso contrario (datos inválidos o error en BD).
     */
    public boolean cambiarPassword(int usuarioId, String passActual, String passNueva) {
        try {
            // 1. Validar nueva contraseña
            if (passNueva == null || passNueva.trim().isEmpty() || passNueva.length() < 4) {
                System.err.println("Error: La nueva contraseña debe tener al menos 4 caracteres.");
                return false;
            }

            // 2. Obtener el usuario de la BD
            Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
            if (usuario == null) {
                System.err.println("Error: Usuario no encontrado.");
                return false;
            }

            // 3. Validar que la contraseña actual coincida con la almacenada
            if (!PasswordUtils.verifyPassword(passActual, usuario.getPasswordHash())) {
                System.err.println("Error: La contraseña actual es incorrecta.");
                return false;
            }

            // 4. Hashear la nueva contraseña y actualizar en la BD
            String nuevoHash = PasswordUtils.hashPassword(passNueva);
            return usuarioDAO.actualizarPassword(usuarioId, nuevoHash);
        } catch (Exception e) {
            System.err.println("Error en UsuarioService.cambiarPassword: " + e.getMessage());
            return false;
        }
    }
}