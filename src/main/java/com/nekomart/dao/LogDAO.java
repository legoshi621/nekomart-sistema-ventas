package com.nekomart.dao;

import com.nekomart.models.LogSistema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar los logs de auditoría del sistema.
 * Inserta y consulta registros en la tabla logs_sistema.
 * Usa PreparedStatement para prevenir inyección SQL.
 * No cierra conexiones ya que usa el patrón Singleton de ConexionDB.
 * Todo el código está comentado en español.
 */
public class LogDAO {

    /**
     * Registra un nuevo log de auditoría en la base de datos.
     *
     * @param idUsuario   ID del usuario que realizó la acción (0 si no aplica).
     * @param accion      Tipo de acción realizada (LOGIN, VENTA, ELIMINAR_PRODUCTO, etc.).
     * @param descripcion Descripción detallada de la acción.
     * @return true si el log se registró exitosamente, false en caso de error.
     */
    public boolean registrarLog(int idUsuario, String accion, String descripcion) {
        // SQL para insertar un nuevo registro de log
        String sql = "INSERT INTO logs_sistema (id_usuario, accion, descripcion) VALUES (?, ?, ?)";

        try {
            // Obtener la conexión singleton
            Connection conn = ConexionDB.getInstancia().getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);

            // Asignar parámetros al PreparedStatement
            ps.setInt(1, idUsuario);
            ps.setString(2, accion);
            ps.setString(3, descripcion);

            // Ejecutar la inserción
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (Exception e) {
            // Imprimir error en consola sin interrumpir el flujo
            System.err.println("Error al registrar log de auditoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene todos los logs de auditoría registrados, ordenados del más reciente al más antiguo.
     *
     * @return Lista de objetos LogSistema con todos los registros.
     */
    public List<LogSistema> obtenerTodos() {
        List<LogSistema> logs = new ArrayList<>();
        // SQL para obtener todos los logs ordenados por fecha descendente
        String sql = "SELECT * FROM logs_sistema ORDER BY fecha_hora DESC";

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            // Recorrer los resultados y mapear cada fila a un objeto LogSistema
            while (rs.next()) {
                LogSistema log = new LogSistema();
                log.setId(rs.getInt("id"));
                log.setFechaHora(rs.getString("fecha_hora"));
                log.setIdUsuario(rs.getInt("id_usuario"));
                log.setAccion(rs.getString("accion"));
                log.setDescripcion(rs.getString("descripcion"));
                log.setIpMaquina(rs.getString("ip_maquina"));
                logs.add(log);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener logs de auditoría: " + e.getMessage());
        }

        return logs;
    }

    /**
     * Obtiene los logs de auditoría filtrados por un usuario específico.
     *
     * @param idUsuario ID del usuario cuyas acciones se quieren consultar.
     * @return Lista de objetos LogSistema del usuario especificado.
     */
    public List<LogSistema> obtenerPorUsuario(int idUsuario) {
        List<LogSistema> logs = new ArrayList<>();
        // SQL para filtrar logs por usuario específico
        String sql = "SELECT * FROM logs_sistema WHERE id_usuario = ? ORDER BY fecha_hora DESC";

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            // Mapear resultados a objetos LogSistema
            while (rs.next()) {
                LogSistema log = new LogSistema();
                log.setId(rs.getInt("id"));
                log.setFechaHora(rs.getString("fecha_hora"));
                log.setIdUsuario(rs.getInt("id_usuario"));
                log.setAccion(rs.getString("accion"));
                log.setDescripcion(rs.getString("descripcion"));
                log.setIpMaquina(rs.getString("ip_maquina"));
                logs.add(log);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener logs por usuario: " + e.getMessage());
        }

        return logs;
    }
}
