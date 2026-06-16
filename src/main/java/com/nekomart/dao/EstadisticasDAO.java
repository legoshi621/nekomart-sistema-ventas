package com.nekomart.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class EstadisticasDAO {

    // ══════════════════════════════════════════════════════════════════
    // MÉTODOS GENERALES (ADMIN)
    // ══════════════════════════════════════════════════════════════════

    public double obtenerVentasHoy() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha) = DATE('now')";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error obtenerVentasHoy: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return 0.0;
    }

    public double obtenerVentasMes() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE strftime('%Y-%m', fecha) = strftime('%Y-%m', 'now')";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error obtenerVentasMes: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return 0.0;
    }

    public int obtenerProductosVendidosHoy() {
        String sql = "SELECT COALESCE(SUM(dv.cantidad), 0) FROM detalle_venta dv " +
                     "INNER JOIN ventas v ON dv.venta_id = v.id WHERE DATE(v.fecha) = DATE('now')";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error obtenerProductosVendidosHoy: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return 0;
    }

    public double obtenerIngresosTotales() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error obtenerIngresosTotales: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return 0.0;
    }

    public List<Map<String, Object>> obtenerTopProductos() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT p.nombre AS producto, SUM(dv.cantidad) AS cantidad_vendida, " +
                     "SUM(dv.cantidad * dv.precio_unitario) AS ingresos " +
                     "FROM detalle_venta dv " +
                     "INNER JOIN productos p ON dv.producto_id = p.id " +
                     "GROUP BY dv.producto_id " +
                     "ORDER BY cantidad_vendida DESC LIMIT 5";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("producto", rs.getString("producto"));
                map.put("cantidad_vendida", rs.getInt("cantidad_vendida"));
                map.put("ingresos", rs.getDouble("ingresos"));
                lista.add(map);
            }
        } catch (SQLException e) {
            System.err.println("Error obtenerTopProductos: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return lista;
    }

    public Map<String, Double> obtenerVentasUltimos7Dias() {
        Map<String, Double> mapa = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            mapa.put(hoy.minusDays(i).toString(), 0.0);
        }

        String sql = "SELECT DATE(fecha) AS fecha_venta, SUM(total) AS total " +
                     "FROM ventas WHERE DATE(fecha) >= DATE('now', '-6 days') " +
                     "GROUP BY DATE(fecha) ORDER BY DATE(fecha)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String fecha = rs.getString("fecha_venta");
                double total = rs.getDouble("total");
                if (mapa.containsKey(fecha)) {
                    mapa.put(fecha, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error obtenerVentasUltimos7Dias: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return mapa;
    }

    // ══════════════════════════════════════════════════════════════════
    // MÉTODOS FILTRADOS POR EMPLEADO
    // ══════════════════════════════════════════════════════════════════

    public double obtenerVentasHoyPorEmpleado(int empleadoId) {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas " +
                     "WHERE DATE(fecha) = DATE('now') AND empleado_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, empleadoId);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error obtenerVentasHoyPorEmpleado: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return 0.0;
    }

    public double obtenerVentasMesPorEmpleado(int empleadoId) {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas " +
                     "WHERE strftime('%Y-%m', fecha) = strftime('%Y-%m', 'now') AND empleado_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, empleadoId);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error obtenerVentasMesPorEmpleado: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return 0.0;
    }

    public int obtenerProductosVendidosHoyPorEmpleado(int empleadoId) {
        String sql = "SELECT COALESCE(SUM(dv.cantidad), 0) FROM detalle_venta dv " +
                     "INNER JOIN ventas v ON dv.venta_id = v.id " +
                     "WHERE DATE(v.fecha) = DATE('now') AND v.empleado_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, empleadoId);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error obtenerProductosVendidosHoyPorEmpleado: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return 0;
    }

    public double obtenerIngresosTotalesPorEmpleado(int empleadoId) {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE empleado_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, empleadoId);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error obtenerIngresosTotalesPorEmpleado: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return 0.0;
    }

    public List<Map<String, Object>> obtenerTopProductosPorEmpleado(int empleadoId) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT p.nombre AS producto, SUM(dv.cantidad) AS cantidad_vendida, " +
                     "SUM(dv.cantidad * dv.precio_unitario) AS ingresos " +
                     "FROM detalle_venta dv " +
                     "INNER JOIN ventas v ON dv.venta_id = v.id " +
                     "INNER JOIN productos p ON dv.producto_id = p.id " +
                     "WHERE v.empleado_id = ? " +
                     "GROUP BY dv.producto_id " +
                     "ORDER BY cantidad_vendida DESC LIMIT 5";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, empleadoId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("producto", rs.getString("producto"));
                map.put("cantidad_vendida", rs.getInt("cantidad_vendida"));
                map.put("ingresos", rs.getDouble("ingresos"));
                lista.add(map);
            }
        } catch (SQLException e) {
            System.err.println("Error obtenerTopProductosPorEmpleado: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return lista;
    }

    public Map<String, Double> obtenerVentasUltimos7DiasPorEmpleado(int empleadoId) {
        Map<String, Double> mapa = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            mapa.put(hoy.minusDays(i).toString(), 0.0);
        }

        String sql = "SELECT DATE(fecha) AS fecha_venta, SUM(total) AS total " +
                     "FROM ventas WHERE DATE(fecha) >= DATE('now', '-6 days') AND empleado_id = ? " +
                     "GROUP BY DATE(fecha) ORDER BY DATE(fecha)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, empleadoId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String fecha = rs.getString("fecha_venta");
                double total = rs.getDouble("total");
                if (mapa.containsKey(fecha)) {
                    mapa.put(fecha, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error obtenerVentasUltimos7DiasPorEmpleado: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        }
        return mapa;
    }
}