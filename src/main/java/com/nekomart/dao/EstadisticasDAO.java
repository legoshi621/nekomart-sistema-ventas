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
 * IMPORTANTE: NO se cierra la conexión porque es un Singleton.
 * Solo se cierran Statement y ResultSet manualmente.
 */
public class EstadisticasDAO {

    /**
     * Obtiene el monto total de ventas realizadas el día de hoy.
     */
    public double obtenerVentasHoy() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha) = DATE('now', 'localtime')";
        
        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            if (conn == null || conn.isClosed()) {
                System.err.println("Conexión cerrada en obtenerVentasHoy");
                return 0.0;
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            double resultado = 0.0;
            if (rs.next()) {
                resultado = rs.getDouble(1);
            }
            
            // Cerrar SOLO statement y resultset
            rs.close();
            stmt.close();
            
            return resultado;
            
        } catch (SQLException e) {
            System.err.println("Error en obtenerVentasHoy: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene el monto total de ventas realizadas en el mes actual.
     */
    public double obtenerVentasMes() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE strftime('%Y-%m', fecha) = strftime('%Y-%m', 'now', 'localtime')";
        
        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            if (conn == null || conn.isClosed()) {
                System.err.println("Conexión cerrada en obtenerVentasMes");
                return 0.0;
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            double resultado = 0.0;
            if (rs.next()) {
                resultado = rs.getDouble(1);
            }
            
            rs.close();
            stmt.close();
            
            return resultado;
            
        } catch (SQLException e) {
            System.err.println("Error en obtenerVentasMes: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene la cantidad de productos vendidos hoy.
     */
    public int obtenerProductosVendidosHoy() throws SQLException {
        String sql = "SELECT COALESCE(SUM(dv.cantidad), 0) FROM detalle_venta dv " +
                     "JOIN ventas v ON dv.venta_id = v.id " +
                     "WHERE DATE(v.fecha) = DATE('now', 'localtime')";
        
        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            if (conn == null || conn.isClosed()) {
                System.err.println("Conexión cerrada en obtenerProductosVendidosHoy");
                return 0;
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            int resultado = 0;
            if (rs.next()) {
                resultado = rs.getInt(1);
            }
            
            rs.close();
            stmt.close();
            
            return resultado;
            
        } catch (SQLException e) {
            System.err.println("Error en obtenerProductosVendidosHoy: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene la suma total de ingresos históricos del sistema.
     */
    public double obtenerIngresosTotales() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas";
        
        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            if (conn == null || conn.isClosed()) {
                System.err.println("Conexión cerrada en obtenerIngresosTotales");
                return 0.0;
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            double resultado = 0.0;
            if (rs.next()) {
                resultado = rs.getDouble(1);
            }
            
            rs.close();
            stmt.close();
            
            return resultado;
            
        } catch (SQLException e) {
            System.err.println("Error en obtenerIngresosTotales: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene el Top 5 de productos más vendidos en el sistema.
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

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            if (conn == null || conn.isClosed()) {
                System.err.println("Conexión cerrada en obtenerTopProductos");
                return lista;
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("producto", rs.getString("producto"));
                map.put("cantidad_vendida", rs.getInt("cantidad_vendida"));
                map.put("ingresos", rs.getDouble("ingresos"));
                lista.add(map);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error en obtenerTopProductos: " + e.getMessage());
            throw e;
        }
        
        return lista;
    }

    /**
     * Obtiene las ventas agrupadas por día para los últimos 7 días.
     */
    public Map<String, Double> obtenerVentasUltimos7Dias() throws SQLException {
        // Inicializar mapa con los últimos 7 días en 0.0
        Map<String, Double> ventasPorDia = new LinkedHashMap<>();
        java.time.LocalDate hoy = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String fechaStr = hoy.minusDays(i).toString();
            ventasPorDia.put(fechaStr, 0.0);
        }

        String sql = "SELECT DATE(fecha) AS dia, SUM(total) AS total_dia " +
                     "FROM ventas " +
                     "WHERE DATE(fecha) >= DATE('now', '-6 days', 'localtime') " +
                     "GROUP BY DATE(fecha) " +
                     "ORDER BY DATE(fecha) ASC";

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            if (conn == null || conn.isClosed()) {
                System.err.println("Conexión cerrada en obtenerVentasUltimos7Dias");
                return ventasPorDia;
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String dia = rs.getString("dia");
                double total = rs.getDouble("total_dia");
                if (ventasPorDia.containsKey(dia)) {
                    ventasPorDia.put(dia, total);
                }
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error en obtenerVentasUltimos7Dias: " + e.getMessage());
            throw e;
        }
        
        return ventasPorDia;
    }
}