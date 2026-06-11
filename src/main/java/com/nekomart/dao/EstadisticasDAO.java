package com.nekomart.dao;
import java.sql.*;

public class EstadisticasDAO {
    public double ventasHoy() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha) = DATE('now', 'localtime')";
        try (Connection c = ConexionDB.getInstancia().getConexion(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            if (r.next()) return r.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int totalVentasHoy() {
        String sql = "SELECT COUNT(*) FROM ventas WHERE DATE(fecha) = DATE('now', 'localtime')";
        try (Connection c = ConexionDB.getInstancia().getConexion(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            if (r.next()) return r.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double ingresosTotales() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas";
        try (Connection c = ConexionDB.getInstancia().getConexion(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            if (r.next()) return r.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}