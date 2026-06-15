package com.nekomart.dao;

import com.nekomart.models.Devolucion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar las devoluciones en la base de datos SQLite.
 * Realiza registros en transacciones seguras y consultas filtradas.
 * Todo el código está comentado en español.
 */
public class DevolucionDAO {

    /**
     * Registra una devolución en la base de datos dentro de una transacción.
     * Inserta la devolución, reintegra el stock del producto y registra el movimiento de inventario.
     *
     * @param dev Objeto Devolucion con los datos a registrar.
     * @return true si la transacción fue exitosa, false en caso contrario.
     */
    public boolean registrarDevolucion(Devolucion dev) {
        Connection conn = null;
        PreparedStatement stmtDev = null;
        PreparedStatement stmtGetProd = null;
        PreparedStatement stmtStock = null;
        PreparedStatement stmtMov = null;
        ResultSet rsKeys = null;
        ResultSet rsGetProd = null;
        boolean originalAutoCommit = true;

        try {
            // Obtener la conexión Singleton sin cerrarla en esta clase
            conn = ConexionDB.getInstancia().getConexion();
            originalAutoCommit = conn.getAutoCommit();
            
            // Iniciar transacción (BEGIN TRANSACTION)
            conn.setAutoCommit(false);

            // Asegurar que la devolución tenga fecha asignada
            String fechaRegistro = dev.getFecha();
            if (fechaRegistro == null || fechaRegistro.trim().isEmpty()) {
                fechaRegistro = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                dev.setFecha(fechaRegistro);
            }

            // 1. Insertar registro en la tabla de devoluciones
            String sqlDev = "INSERT INTO devoluciones (id_venta, id_detalle_venta, cantidad, motivo, tipo_reembolso, fecha, id_admin) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmtDev = conn.prepareStatement(sqlDev, Statement.RETURN_GENERATED_KEYS);
            stmtDev.setInt(1, dev.getIdVenta());
            stmtDev.setInt(2, dev.getIdDetalleVenta());
            stmtDev.setInt(3, dev.getCantidad());
            stmtDev.setString(4, dev.getMotivo());
            stmtDev.setString(5, dev.getTipoReembolso());
            stmtDev.setString(6, dev.getFecha());
            stmtDev.setInt(7, dev.getIdAdmin());

            int rowsDev = stmtDev.executeUpdate();
            if (rowsDev == 0) {
                throw new SQLException("No se pudo insertar el registro de devolución.");
            }

            // Obtener el ID autogenerado
            rsKeys = stmtDev.getGeneratedKeys();
            if (rsKeys.next()) {
                dev.setId(rsKeys.getInt(1));
            }

            // 2. Obtener el producto_id desde detalle_venta
            String sqlGetProd = "SELECT producto_id FROM detalle_venta WHERE id = ?";
            stmtGetProd = conn.prepareStatement(sqlGetProd);
            stmtGetProd.setInt(1, dev.getIdDetalleVenta());
            rsGetProd = stmtGetProd.executeQuery();

            int productoId = -1;
            if (rsGetProd.next()) {
                productoId = rsGetProd.getInt("producto_id");
            } else {
                throw new SQLException("No se encontró el detalle de venta con ID: " + dev.getIdDetalleVenta());
            }

            // 3. Actualizar stock en productos (SUMAR la cantidad devuelta)
            String sqlUpdateStock = "UPDATE productos SET stock = stock + ? WHERE id = ?";
            stmtStock = conn.prepareStatement(sqlUpdateStock);
            stmtStock.setInt(1, dev.getCantidad());
            stmtStock.setInt(2, productoId);

            int rowsStock = stmtStock.executeUpdate();
            if (rowsStock == 0) {
                throw new SQLException("No se pudo actualizar el stock del producto con ID: " + productoId);
            }

            // 4. Registrar movimiento de inventario (Kardex) tipo 'ENTRADA' con motivo 'DEVOLUCION'
            String sqlMov = "INSERT INTO movimientos_inventario (producto_id, tipo, cantidad, fecha, usuario_id, motivo) "
                    + "VALUES (?, 'ENTRADA', ?, ?, ?, 'DEVOLUCION')";
            stmtMov = conn.prepareStatement(sqlMov);
            stmtMov.setInt(1, productoId);
            stmtMov.setInt(2, dev.getCantidad());
            stmtMov.setString(3, dev.getFecha());
            stmtMov.setInt(4, dev.getIdAdmin());

            int rowsMov = stmtMov.executeUpdate();
            if (rowsMov == 0) {
                throw new SQLException("No se pudo registrar el movimiento de inventario para la devolución.");
            }

            // Confirmar transacción si todo es correcto
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar devolución (Rollback aplicado): " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            // Restaurar el estado original de autoCommit de la conexión
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ex) {
                    System.err.println("Error al restaurar autoCommit: " + ex.getMessage());
                }
            }
            // Cerrar manualmente solo recursos locales (Statement y ResultSet)
            if (rsKeys != null) {
                try { rsKeys.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (rsGetProd != null) {
                try { rsGetProd.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (stmtDev != null) {
                try { stmtDev.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (stmtGetProd != null) {
                try { stmtGetProd.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (stmtStock != null) {
                try { stmtStock.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (stmtMov != null) {
                try { stmtMov.close(); } catch (SQLException e) { /* ignorar */ }
            }
        }
    }

    /**
     * Obtiene una lista de devoluciones asociadas a una venta específica.
     *
     * @param idVenta ID de la venta a consultar.
     * @return Lista de devoluciones pertenecientes a la venta.
     */
    public List<Devolucion> obtenerDevolucionesPorVenta(int idVenta) {
        List<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT id, id_venta, id_detalle_venta, cantidad, motivo, tipo_reembolso, fecha, id_admin "
                + "FROM devoluciones WHERE id_venta = ? ORDER BY fecha DESC";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idVenta);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Devolucion dev = new Devolucion();
                dev.setId(rs.getInt("id"));
                dev.setIdVenta(rs.getInt("id_venta"));
                dev.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
                dev.setCantidad(rs.getInt("cantidad"));
                dev.setMotivo(rs.getString("motivo"));
                dev.setTipoReembolso(rs.getString("tipo_reembolso"));
                dev.setFecha(rs.getString("fecha"));
                dev.setIdAdmin(rs.getInt("id_admin"));
                lista.add(dev);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener devoluciones por venta: " + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignorar */ }
            }
        }
        return lista;
    }

    /**
     * Obtiene la lista completa de todas las devoluciones del sistema.
     *
     * @return Lista de todas las devoluciones.
     */
    public List<Devolucion> obtenerTodasDevoluciones() {
        List<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT id, id_venta, id_detalle_venta, cantidad, motivo, tipo_reembolso, fecha, id_admin "
                + "FROM devoluciones ORDER BY fecha DESC";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Devolucion dev = new Devolucion();
                dev.setId(rs.getInt("id"));
                dev.setIdVenta(rs.getInt("id_venta"));
                dev.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
                dev.setCantidad(rs.getInt("cantidad"));
                dev.setMotivo(rs.getString("motivo"));
                dev.setTipoReembolso(rs.getString("tipo_reembolso"));
                dev.setFecha(rs.getString("fecha"));
                dev.setIdAdmin(rs.getInt("id_admin"));
                lista.add(dev);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las devoluciones: " + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignorar */ }
            }
        }
        return lista;
    }

    /**
     * Obtiene devoluciones registradas dentro de un rango de fechas.
     *
     * @param fechaInicio Rango de fecha de inicio (YYYY-MM-DD).
     * @param fechaFin    Rango de fecha de fin (YYYY-MM-DD).
     * @return Lista de devoluciones que cumplen con el filtro.
     */
    public List<Devolucion> obtenerDevolucionesPorFecha(String fechaInicio, String fechaFin) {
        List<Devolucion> lista = new ArrayList<>();
        String sql = "SELECT id, id_venta, id_detalle_venta, cantidad, motivo, tipo_reembolso, fecha, id_admin "
                + "FROM devoluciones WHERE DATE(fecha) >= DATE(?) AND DATE(fecha) <= DATE(?) ORDER BY fecha DESC";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fechaInicio);
            stmt.setString(2, fechaFin);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Devolucion dev = new Devolucion();
                dev.setId(rs.getInt("id"));
                dev.setIdVenta(rs.getInt("id_venta"));
                dev.setIdDetalleVenta(rs.getInt("id_detalle_venta"));
                dev.setCantidad(rs.getInt("cantidad"));
                dev.setMotivo(rs.getString("motivo"));
                dev.setTipoReembolso(rs.getString("tipo_reembolso"));
                dev.setFecha(rs.getString("fecha"));
                dev.setIdAdmin(rs.getInt("id_admin"));
                lista.add(dev);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener devoluciones por rango de fechas: " + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignorar */ }
            }
        }
        return lista;
    }
}
