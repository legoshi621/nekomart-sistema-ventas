package com.nekomart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO encargado de recuperar estadísticas de ventas de la base de datos SQLite.
 * Todo el código está en español y documentado con comentarios.
 */
public class EstadisticasDAO {

    /**
     * Obtiene el monto total de ventas realizadas el día de hoy.
     * Query SQL: SUM(total) WHERE DATE(fecha) = DATE('now')
     *
     * @return Suma de ventas de hoy, o 0.0 si no hay registros.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public double obtenerVentasHoy() throws SQLException {
        String sql = "SELECT SUM(total) FROM ventas WHERE DATE(fecha) = DATE('now')";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    /**
     * Obtiene el monto total de ventas realizadas en el mes actual.
     * Query SQL: SUM(total) WHERE strftime('%Y-%m', fecha) = strftime('%Y-%m', 'now')
     *
     * @return Suma de ventas del mes, o 0.0 si no hay registros.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public double obtenerVentasMes() throws SQLException {
        String sql = "SELECT SUM(total) FROM ventas WHERE strftime('%Y-%m', fecha) = strftime('%Y-%m', 'now')";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    /**
     * Obtiene la cantidad de productos vendidos hoy.
     * Query SQL: SUM(cantidad) de detalle_venta filtrando ventas del día.
     *
     * @return Total de unidades vendidas hoy.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public int obtenerProductosVendidosHoy() throws SQLException {
        String sql = "SELECT SUM(dv.cantidad) FROM detalle_venta dv " +
                     "JOIN ventas v ON dv.venta_id = v.id " +
                     "WHERE DATE(v.fecha) = DATE('now')";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Obtiene la suma total de ingresos históricos del sistema.
     * Query SQL: SUM(total) de todas las ventas
     *
     * @return Total de ingresos históricos.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public double obtenerIngresosTotales() throws SQLException {
        String sql = "SELECT SUM(total) FROM ventas";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    /**
     * Obtiene el Top 5 de productos más vendidos en el sistema.
     *
     * @return Lista de mapas conteniendo "producto" (nombre), "cantidad_vendida", e "ingresos".
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public List<Map<String, Object>> obtenerTopProductos() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT p.nombre AS producto, SUM(dv.cantidad) AS cantidad_vendida, " +
                     "SUM(dv.cantidad * dv.precio_unitario) AS ingresos " +
                     "FROM detalle_venta dv " +
                     "JOIN productos p ON dv.producto_id = p.id " +
                     "GROUP BY p.id " +
                     "ORDER BY cantidad_vendida DESC " +
                     "LIMIT 5";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("producto", rs.getString("producto"));
                map.put("cantidad_vendida", rs.getInt("cantidad_vendida"));
                map.put("ingresos", rs.getDouble("ingresos"));
                lista.add(map);
            }
        }
        return lista;
    }

    /**
     * Obtiene las ventas agrupadas por día para los últimos 7 días.
     * Se usa un LinkedHashMap ordenado de forma cronológica.
     *
     * @return Mapa ordenado cronológicamente con la fecha (YYYY-MM-DD) y el monto total de ventas.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public Map<String, Double> obtenerVentasUltimos7Dias() throws SQLException {
        // Inicializamos el mapa con los últimos 7 días en 0.0 para asegurar la completitud
        Map<String, Double> ventasPorDia = new LinkedHashMap<>();
        java.time.LocalDate hoy = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String fechaStr = hoy.minusDays(i).toString(); // Formato YYYY-MM-DD
            ventasPorDia.put(fechaStr, 0.0);
        }

        // Consulta SQL para agrupar ventas en el rango de los últimos 7 días
        String sql = "SELECT DATE(fecha) AS dia, SUM(total) AS total_dia " +
                     "FROM ventas " +
                     "WHERE DATE(fecha) >= DATE('now', '-6 days') " +
                     "GROUP BY DATE(fecha) " +
                     "ORDER BY DATE(fecha) ASC";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String dia = rs.getString("dia");
                double total = rs.getDouble("total_dia");
                // Solo si la fecha está dentro de nuestro mapa (evitando discrepancias menores de zona horaria)
                if (ventasPorDia.containsKey(dia)) {
                    ventasPorDia.put(dia, total);
                }
            }
        }
        return ventasPorDia;
    }
}