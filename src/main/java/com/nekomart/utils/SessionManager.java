package com.nekomart.utils;

import com.nekomart.models.Usuario;

/**
 * Gestor de sesión que mantiene el usuario autenticado durante la ejecución.
 * Implementa el patrón Singleton para garantizar una única instancia.
 * Todo el código está comentado en español.
 */
public class SessionManager {

    // Única instancia de la clase (Singleton)
    private static SessionManager instancia;

    // Usuario actualmente autenticado en el sistema
    private Usuario usuarioActual;

    // Constructor privado para evitar instanciación externa
    private SessionManager() {
        this.usuarioActual = null;
    }

    /**
     * Obtiene la instancia única de SessionManager.
     * Implementa verificación doble para ser thread-safe.
     *
     * @return Instancia única de SessionManager.
     */
    public static synchronized SessionManager getInstancia() {
        if (instancia == null) {
            instancia = new SessionManager();
        }
        return instancia;
    }

    /**
     * Retorna el usuario actualmente autenticado.
     *
     * @return Objeto Usuario o null si no hay sesión activa.
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Establece el usuario autenticado en la sesión.
     *
     * @param usuario Objeto Usuario a guardar en la sesión.
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    /**
     * Cierra la sesión actual estableciendo el usuario en null.
     */
    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    /**
     * Verifica si hay una sesión activa con un usuario autenticado.
     *
     * @return true si hay usuario en sesión, false en caso contrario.
     */
    public boolean haySesionActiva() {
        return usuarioActual != null;
    }
}