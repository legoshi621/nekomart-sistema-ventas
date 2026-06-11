package com.nekomart.dao;

import com.nekomart.models.DetalleVenta;
import com.nekomart.models.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para las entidades 'Venta' y 'DetalleVenta'.
 * Implementa el control transaccional explícito (AutoCommit = false) para garantizar
 * que una venta y sus detalles se guarden juntos o ninguno lo haga en caso de error.
 * Todo el código está en español.
 */
public class VentaDAO {

    private final ConexionDB conexionDB = ConexionDB.getInstancia();

    /**
     * Guarda una transacción de venta en la base de datos de manera atómica.
     * Inserta la cabecera, cada detalle y reduce el stock del producto del inventario.
     * En caso de cualquier error o stock insuficiente, la transacción se revierte (Rollback).
     *
     * @param venta Objeto Venta conteniendo la información de la cabecera y su lista de detalles.
     * @return true si la venta y todo su flujo asociado se guardó correctamente, false de lo contrario.
     */
    public boolean guardarVenta(Venta venta) {
        String queryVenta = "INSERT INTO ventas (folio, total, metodo_pago, monto_recibido, cambio, empleado_id) VALUES (?, ?, ?, ?, ?, ?)";
        String queryDetalle = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        String queryStock = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";

        Connection conn = null;
        PreparedStatement psVenta = null;
        PreparedStatement psDetalle = null;
        PreparedStatement psStock = null;

        try {
            conn = conexionDB.getConexion();
            
            // 1. Iniciar transacción desactivando el auto-commit
            conn.setAutoCommit(false);

            // 2. Guardar la cabecera de la venta
            psVenta = conn.prepareStatement(queryVenta, Statement.RETURN_GENERATED_KEYS);
            psVenta.setString(1, venta.getFolio());
            psVenta.setDouble(2, venta.getTotal());
            psVenta.setString(3, venta.getMetodoPago());
            psVenta.setDouble(4, venta.getMontoRecibido());
            psVenta.setDouble(5, venta.getCambio());
            psVenta.setInt(6, venta.getEmpleadoId());

            int filasVenta = psVenta.executeUpdate();
            if (filasVenta == 0) {
                throw new SQLException("Fallo al insertar la cabecera de la venta.");
            }

            // Obtener el ID auto-generado para la venta
            int idVentaGenerado = -1;
            try (ResultSet rsKeys = psVenta.getGeneratedKeys()) {
                if (rsKeys.next()) {
                    idVentaGenerado = rsKeys.getInt(1);
                    venta.setId(idVentaGenerado);
                } else {
                    throw new SQLException("Fallo al obtener el ID de venta generado.");
                }
            }

            // 3. Preparar statements de detalles y actualización de inventario
            psDetalle = conn.prepareStatement(queryDetalle, Statement.RETURN_GENERATED_KEYS);
            psStock = conn.prepareStatement(queryStock);

            // 4. Guardar cada detalle de venta y decrementar stock de producto
            for (DetalleVenta detalle : venta.getDetalles()) {
                // Insertar detalle venta
                psDetalle.setInt(1, idVentaGenerado);
                psDetalle.setInt(2, detalle.getProductoId());
                psDetalle.setInt(3, detalle.getCantidad());
                psDetalle.setDouble(4, detalle.getPrecioUnitario());

                int filasDetalle = psDetalle.executeUpdate();
                if (filasDetalle == 0) {
                    throw new SQLException("Fallo al insertar el detalle de venta para el producto ID: " + detalle.getProductoId());
                }

                // Guardar la clave generada del detalle
                try (ResultSet rsDetKeys = psDetalle.getGeneratedKeys()) {
                    if (rsDetKeys.next()) {
                        detalle.setId(rsDetKeys.getInt(1));
                    }
                }
                detalle.setVentaId(idVentaGenerado);

                // Decrementar stock correspondiente y asegurar que no quede en negativo (control en AND stock >= cantidad)
                psStock.setInt(1, detalle.getCantidad());
                psStock.setInt(2, detalle.getProductoId());
                psStock.setInt(3, detalle.getCantidad());

                int filasStock = psStock.executeUpdate();
                if (filasStock == 0) {
                    // Si filas stock es 0, significa que no se cumplió la condición stock >= cantidad (insuficiente)
                    throw new SQLException("Stock insuficiente o producto descontinuado para el ID: " + detalle.getProductoId());
                }
            }

            // 5. Si todo fue correcto, realizar commit
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al procesar la venta. Ejecutando Rollback... Razón: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Fallo al ejecutar Rollback: " + rollbackEx.getMessage());
                }
            }
        } finally {
            // 6. Restaurar el estado de auto-commit del pool/conexión y cerrar recursos
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException autoCommitEx) {
                    System.err.println("Error al restablecer AutoCommit: " + autoCommitEx.getMessage());
                }
            }
            try { if (psVenta != null) psVenta.close(); } catch (SQLException ignored) {}
            try { if (psDetalle != null) psDetalle.close(); } catch (SQLException ignored) {}
            try { if (psStock != null) psStock.close(); } catch (SQLException ignored) {}
        }
        return false;
    }

    /**
     * Genera de manera consecutiva el siguiente folio disponible para registrar una venta.
     * Formato generado: V-000001
     *
     * @return String con el folio consecutivo.
     */
    public String generarSiguienteFolio() {
        String query = "SELECT ISNULL(MAX(id), 0) + 1 AS siguiente FROM ventas";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                int siguiente = rs.getInt("siguiente");
                return String.format("V-%06d", siguiente);
            }
        } catch (SQLException e) {
            System.err.println("Error al generar el consecutivo del folio: " + e.getMessage());
        }
        return "V-000001"; // Fallback por defecto
    }

    /**
     * Busca y recupera la información de una venta por su ID, cargando todos sus detalles asociados.
     *
     * @param id Identificador único de la venta.
     * @return Objeto Venta con detalles incluidos, o null si no se encuentra.
     */
    public Venta buscarPorId(int id) {
        String queryVenta = "SELECT id, folio, fecha, total, metodo_pago, monto_recibido, cambio, empleado_id FROM ventas WHERE id = ?";
        String queryDetalles = "SELECT id, venta_id, producto_id, cantidad, precio_unitario FROM detalle_venta WHERE venta_id = ?";
        
        Venta venta = null;
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement psV = conn.prepareStatement(queryVenta)) {
            
            psV.setInt(1, id);
            
            try (ResultSet rsV = psV.executeQuery()) {
                if (rsV.next()) {
                    venta = new Venta(
                        rsV.getInt("id"),
                        rsV.getString("folio"),
                        rsV.getTimestamp("fecha"),
                        rsV.getDouble("total"),
                        rsV.getString("metodo_pago"),
                        rsV.getDouble("monto_recibido"),
                        rsV.getDouble("cambio"),
                        rsV.getInt("empleado_id")
                    );
                    
                    // Recuperar y asociar la lista de productos de la venta
                    try (PreparedStatement psD = conn.prepareStatement(queryDetalles)) {
                        psD.setInt(1, id);
                        
                        try (ResultSet rsD = psD.executeQuery()) {
                            while (rsD.next()) {
                                DetalleVenta det = new DetalleVenta(
                                    rsD.getInt("id"),
                                    rsD.getInt("venta_id"),
                                    rsD.getInt("producto_id"),
                                    rsD.getInt("cantidad"),
                                    rsD.getDouble("precio_unitario")
                                );
                                venta.agregarDetalle(det);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar la venta por ID: " + e.getMessage());
        }
        return venta;
    }

    /**
     * Lista todas las cabeceras de ventas ordenadas por fecha en forma descendente.
     * No carga los detalles (útil para listados optimizados).
     *
     * @return Lista conteniendo los registros de ventas.
     */
    public List<Venta> listarTodas() {
        List<Venta> lista = new ArrayList<>();
        String query = "SELECT id, folio, fecha, total, metodo_pago, monto_recibido, cambio, empleado_id FROM ventas ORDER BY fecha DESC";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new Venta(
                    rs.getInt("id"),
                    rs.getString("folio"),
                    rs.getTimestamp("fecha"),
                    rs.getDouble("total"),
                    rs.getString("metodo_pago"),
                    rs.getDouble("monto_recibido"),
                    rs.getDouble("cambio"),
                    rs.getInt("empleado_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar las ventas registradas: " + e.getMessage());
        }
        return lista;
    }
}
