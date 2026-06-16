package com.nekomart.services;

import com.nekomart.dao.UsuarioDAO;
import com.nekomart.models.Usuario;
import com.nekomart.services.LogService;

/**
 * Servicio encargado de gestionar la lógica de autenticación de la aplicación.
 * Todo el código está documentado en español.
 */
public class AuthService {

    // Instancia del DAO para buscar en la base de datos
    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Valida las credenciales de un usuario.
     * Busca el usuario mediante UsuarioDAO y verifica que la contraseña ingresada,
     * tras ser encriptada con SHA-256 (mediante PasswordUtils), coincida con la
     * base de datos.
     *
     * @param username Nombre de usuario ingresado.
     * @param password Contraseña ingresada en texto plano.
     * @return El objeto Usuario si las credenciales son correctas, o null si son
     *         incorrectas o hay un error.
     */
    public Usuario login(String username, String password) {
        // Validación de datos vacíos
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return null; // Retorna null si las credenciales son incorrectas o inválidas
        }

        try {
            /*
             * Nota: La encriptación SHA-256 usando PasswordUtils y la comparación
             * se delegan internamente al método login() del UsuarioDAO por diseño,
             * el cual utiliza PasswordUtils.verifyPassword() para realizar el chequeo
             * seguro.
             */
            Usuario usuario = usuarioDAO.login(username.trim(), password);

            if (usuario != null) {
                // Registrar log de inicio de sesión exitoso
                LogService.registrar(usuario.getId(), "LOGIN", "Inicio de sesión exitoso");
            } else {
                // Registrar log de intento de inicio de sesión fallido
                LogService.registrar(0, "LOGIN_FALLIDO", "Intento fallido usuario: " + username.trim());
            }

            return usuario;

        } catch (Exception e) {
            // Manejo de excepciones con un bloque try-catch para no interrumpir el flujo de
            // UI
            System.err.println("Error interno durante la autenticación: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
