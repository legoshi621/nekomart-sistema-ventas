package com.nekomart.dao;

import com.nekomart.models.Movimiento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar los movimientos de inventario en la tabla 'movimientos_inventario'.
 * Permite registrar entradas, salidas y ajustes, y consultar el historial (Kardex).
 * Crea la tabla automáticamente si no existe.
 * Todo el código está comentado en español.
 */
public class MovimientoDAO {

    /**
     * Constructor que verifica y crea la tabla 'movimientos_inventario' si no existe.
     */
    public MovimientoDAO() {
        crearTablaSiNoExiste();
    }

    /**
     * Crea la tabla 'movimientos_inventario' en la base de datos si aún no existe.
     * Almacena el historial de entradas, salidas y ajustes del inventario.
     */
    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS movimientos_inventario ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "producto_id INTEGER NOT NULL, "
                + "tipo TEXT NOT NULL, "
                + "cantidad INTEGER NOT NULL, "
                + "fecha TEXT NOT NULL, "
                + "usuario_id INTEGER NOT NULL, "
                + "FOREIGN KEY (producto_id) REFERENCES productos(id), "
                + "FOREIGN KEY (usuario_id) REFERENCES usuarios(id)"
                + ")";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear tabla movimientos_inventario: " + e.getMessage());
        }
    }

    /**
     * Registra un nuevo movimiento de inventario en la base de datos.
     *
     * @param movimiento Objeto Movimiento con los datos del registro.
     * @return true si el registro fue exitoso, false en caso de error.
     */
    public boolean registrarMovimiento(Movimiento movimiento) {
        String sql = "INSERT INTO movimientos_inventario (producto_id, tipo, cantidad, fecha, usuario_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movimiento.getProductoId());
            stmt.setString(2, movimiento.getTipo());
            stmt.setInt(3, movimiento.getCantidad());
            stmt.setString(4, movimiento.getFecha());
            stmt.setInt(5, movimiento.getUsuarioId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar movimiento: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el historial de movimientos (Kardex) de un producto específico.
     * Los resultados se ordenan por fecha descendente (más recientes primero).
     *
     * @param productoId ID del producto a consultar.
     * @return Lista de objetos Movimiento con el historial del producto.
     */
    public List<Movimiento> obtenerHistorial(int productoId) {
        List<Movimiento> historial = new ArrayList<>();
        String sql = "SELECT m.id, m.producto_id, m.tipo, m.cantidad, m.fecha, m.usuario_id "
                + "FROM movimientos_inventario m "
                + "WHERE m.producto_id = ? "
                + "ORDER BY m.fecha DESC";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Movimiento mov = new Movimiento();
                mov.setId(rs.getInt("id"));
                mov.setProductoId(rs.getInt("producto_id"));
                mov.setTipo(rs.getString("tipo"));
                mov.setCantidad(rs.getInt("cantidad"));
                mov.setFecha(rs.getString("fecha"));
                mov.setUsuarioId(rs.getInt("usuario_id"));
                historial.add(mov);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener historial de movimientos: " + e.getMessage());
        }
        return historial;
    }

    /**
     * Obtiene el historial completo de todos los movimientos de inventario.
     * Útil para reportes generales. Ordenado por fecha descendente.
     *
     * @return Lista de todos los movimientos registrados.
     */
    public List<Movimiento> obtenerTodos() {
        List<Movimiento> movimientos = new ArrayList<>();
        String sql = "SELECT id, producto_id, tipo, cantidad, fecha, usuario_id "
                + "FROM movimientos_inventario ORDER BY fecha DESC";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Movimiento mov = new Movimiento();
                mov.setId(rs.getInt("id"));
                mov.setProductoId(rs.getInt("producto_id"));
                mov.setTipo(rs.getString("tipo"));
                mov.setCantidad(rs.getInt("cantidad"));
                mov.setFecha(rs.getString("fecha"));
                mov.setUsuarioId(rs.getInt("usuario_id"));
                movimientos.add(mov);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los movimientos: " + e.getMessage());
        }
        return movimientos;
    }
}
