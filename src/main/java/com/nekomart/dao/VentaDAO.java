package com.nekomart.dao;

import com.nekomart.models.Venta;
import com.nekomart.models.DetalleVenta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar operaciones de ventas en la base de datos SQLite.
 * Todo el código está comentado en español.
 */
public class VentaDAO {

    /**
     * Registra una nueva venta con sus detalles en una transacción.
     */
    public boolean registrarVenta(Venta venta, List<DetalleVenta> detalles) {
        String sqlVenta = "INSERT INTO ventas (folio, fecha, total, metodo_pago, monto_recibido, cambio, empleado_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getInstancia().getConexion()) {
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar la venta
            PreparedStatement stmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            stmtVenta.setString(1, venta.getFolio());
            stmtVenta.setString(2, venta.getFecha());
            stmtVenta.setDouble(3, venta.getTotal());
            stmtVenta.setString(4, venta.getMetodoPago());
            stmtVenta.setDouble(5, venta.getMontoRecibido());
            stmtVenta.setDouble(6, venta.getCambio());
            stmtVenta.setInt(7, venta.getEmpleadoId());
            stmtVenta.executeUpdate();

            // Obtener el ID generado de la venta
            ResultSet generatedKeys = stmtVenta.getGeneratedKeys();
            int ventaId = 0;
            if (generatedKeys.next()) {
                ventaId = generatedKeys.getInt(1);
            }

            // 2. Insertar los detalles de la venta
            PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle);
            for (DetalleVenta detalle : detalles) {
                stmtDetalle.setInt(1, ventaId);
                stmtDetalle.setInt(2, detalle.getProductoId());
                stmtDetalle.setInt(3, detalle.getCantidad());
                stmtDetalle.setDouble(4, detalle.getPrecioUnitario());
                stmtDetalle.addBatch();
            }
            stmtDetalle.executeBatch();

            conn.commit(); // Confirmar transacción
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar venta: " + e.getMessage());
            try {
                ConexionDB.getInstancia().getConexion().rollback();
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * Lista todas las ventas registradas.
     */
    public List<Venta> listarTodas() {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT id, folio, fecha, total, metodo_pago, monto_recibido, cambio, empleado_id FROM ventas ORDER BY fecha DESC";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Venta v = new Venta();
                v.setId(rs.getInt("id"));
                v.setFolio(rs.getString("folio"));
                v.setFecha(rs.getString("fecha"));
                v.setTotal(rs.getDouble("total"));
                v.setMetodoPago(rs.getString("metodo_pago"));
                v.setMontoRecibido(rs.getDouble("monto_recibido"));
                v.setCambio(rs.getDouble("cambio"));
                v.setEmpleadoId(rs.getInt("empleado_id"));
                ventas.add(v);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ventas: " + e.getMessage());
        }
        return ventas;
    }

    /**
     * Busca una venta por su folio.
     */
    public Venta buscarPorFolio(String folio) {
        String sql = "SELECT id, folio, fecha, total, metodo_pago, monto_recibido, cambio, empleado_id FROM ventas WHERE folio = ?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, folio);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Venta v = new Venta();
                v.setId(rs.getInt("id"));
                v.setFolio(rs.getString("folio"));
                v.setFecha(rs.getString("fecha"));
                v.setTotal(rs.getDouble("total"));
                v.setMetodoPago(rs.getString("metodo_pago"));
                v.setMontoRecibido(rs.getDouble("monto_recibido"));
                v.setCambio(rs.getDouble("cambio"));
                v.setEmpleadoId(rs.getInt("empleado_id"));
                return v;
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar venta por folio: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene los detalles de una venta específica.
     */
    public List<DetalleVenta> obtenerDetalles(int ventaId) {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT id, venta_id, producto_id, cantidad, precio_unitario FROM detalle_venta WHERE venta_id = ?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ventaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DetalleVenta d = new DetalleVenta();
                d.setId(rs.getInt("id"));
                d.setVentaId(rs.getInt("venta_id"));
                d.setProductoId(rs.getInt("producto_id"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecioUnitario(rs.getDouble("precio_unitario"));
                detalles.add(d);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles de venta: " + e.getMessage());
        }
        return detalles;
    }

    /**
     * Genera un folio único para la venta
     */
    public String generarFolio() {
        String sql = "SELECT COUNT(*) as total FROM ventas";
        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int total = rs.getInt("total") + 1;
                return String.format("VENTA-%03d", total);
            }
        } catch (SQLException e) {
            System.err.println("Error al generar folio: " + e.getMessage());
        }
        return "VENTA-001";
    }
}