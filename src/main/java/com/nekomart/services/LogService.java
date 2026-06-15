package com.nekomart.services;

import com.nekomart.dao.LogDAO;
import com.nekomart.models.LogSistema;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio estático para el registro de logs de auditoría del sistema.
 * Proporciona un método estático para registrar acciones desde cualquier
 * parte del sistema sin necesidad de instanciar la clase.
 * Los errores se manejan silenciosamente para no interrumpir el flujo principal.
 * Todo el código está comentado en español.
 */
public class LogService {

    // Instancia estática del DAO para acceder a la base de datos
    private static final LogDAO logDAO = new LogDAO();

    /**
     * Registra una acción en el sistema de logs de auditoría.
     * Este método es estático para poder llamarse desde cualquier parte del código.
     * Los errores se capturan silenciosamente para no romper el flujo principal.
     *
     * @param idUsuario   ID del usuario que realizó la acción (0 si no aplica).
     * @param accion      Tipo de acción (LOGIN, VENTA, ELIMINAR_PRODUCTO, etc.).
     * @param descripcion Descripción detallada de la acción realizada.
     */
    public static void registrar(int idUsuario, String accion, String descripcion) {
        try {
            // Delegar al DAO la inserción del registro
            logDAO.registrarLog(idUsuario, accion, descripcion);
        } catch (Exception e) {
            // Manejo silencioso: solo imprime en consola, no lanza excepción
            System.err.println("Error al registrar log (silencioso): " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los logs de auditoría del sistema.
     *
     * @return Lista de todos los registros de log, o lista vacía si hay error.
     */
    public static List<LogSistema> obtenerTodos() {
        try {
            return logDAO.obtenerTodos();
        } catch (Exception e) {
            System.err.println("Error al obtener logs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene los logs de auditoría de un usuario específico.
     *
     * @param idUsuario ID del usuario a consultar.
     * @return Lista de registros de log del usuario, o lista vacía si hay error.
     */
    public static List<LogSistema> obtenerPorUsuario(int idUsuario) {
        try {
            return logDAO.obtenerPorUsuario(idUsuario);
        } catch (Exception e) {
            System.err.println("Error al obtener logs por usuario: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
