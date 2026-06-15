package com.nekomart.dao;

import com.nekomart.models.CorteCaja;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para gestionar los cortes de caja en la base de datos SQLite.
 * Realiza registros seguros y consultas organizadas.
 * Todo el código está comentado en español.
 */
public class CorteCajaDAO {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Abre un nuevo corte de caja insertando un registro en estado 'ABIERTO'.
     *
     * @param idAdmin      ID del administrador que realiza la apertura.
     * @param montoInicial El dinero con el que se inicia la caja.
     * @return true si la inserción fue exitosa, false en caso contrario.
     */
    public boolean abrirCorte(int idAdmin, double montoInicial) {
        String sql = "INSERT INTO cortes_caja (fecha_apertura, id_admin, monto_inicial, estado) VALUES (?, ?, ?, 'ABIERTO')";
        PreparedStatement stmt = null;

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            String fechaApertura = LocalDateTime.now().format(formatter);

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fechaApertura);
            stmt.setInt(2, idAdmin);
            stmt.setDouble(3, montoInicial);

            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al abrir corte de caja: " + e.getMessage());
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignorar */
                }
            }
        }
    }

    /**
     * Obtiene el monto total vendido en un periodo de tiempo.
     *
     * @param fechaInicio Fecha de inicio en formato yyyy-MM-dd HH:mm:ss.
     * @param fechaFin    Fecha de fin en formato yyyy-MM-dd HH:mm:ss.
     * @return El total acumulado de las ventas.
     */
    public double obtenerVentasPeriodo(String fechaInicio, String fechaFin) {
        String sql = "SELECT COALESCE(SUM(total), 0) AS total_ventas FROM ventas WHERE fecha BETWEEN ? AND ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fechaInicio);
            stmt.setString(2, fechaFin);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_ventas");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas del periodo: " + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) {}
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        return 0.0;
    }

    /**
     * Cierra un corte de caja activo calculando las ventas y diferencias de forma transaccional.
     *
     * @param idCorte   ID del corte de caja a cerrar.
     * @param montoReal El monto físicamente contado en la caja.
     * @return true si la transacción fue exitosa, false en caso contrario.
     */
    public boolean cerrarCorte(int idCorte, double montoReal) {
        Connection conn = null;
        PreparedStatement stmtGetCorte = null;
        PreparedStatement stmtUpdate = null;
        ResultSet rsCorte = null;
        boolean originalAutoCommit = true;

        try {
            conn = ConexionDB.getInstancia().getConexion();
            originalAutoCommit = conn.getAutoCommit();
            
            // ── Iniciar Transacción SQL ──
            conn.setAutoCommit(false);

            // 1. Obtener la fecha de apertura del corte
            String sqlGetCorte = "SELECT fecha_apertura FROM cortes_caja WHERE id = ?";
            stmtGetCorte = conn.prepareStatement(sqlGetCorte);
            stmtGetCorte.setInt(1, idCorte);
            rsCorte = stmtGetCorte.executeQuery();

            if (!rsCorte.next()) {
                throw new SQLException("No se encontró el corte de caja con ID: " + idCorte);
            }
            String fechaApertura = rsCorte.getString("fecha_apertura");

            // Fecha de cierre (momento actual)
            String fechaCierre = LocalDateTime.now().format(formatter);

            // 2. Calcular monto esperado: Suma de ventas en el período usando el método auxiliar
            double montoEsperado = obtenerVentasPeriodo(fechaApertura, fechaCierre);

            // 3. Calcular la diferencia
            double diferencia = montoReal - montoEsperado;

            // 4. Actualizar todos los campos en la base de datos
            String sqlUpdate = "UPDATE cortes_caja "
                    + "SET fecha_cierre = ?, monto_esperado = ?, monto_real = ?, diferencia = ?, estado = 'CERRADO' "
                    + "WHERE id = ?";
            stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setString(1, fechaCierre);
            stmtUpdate.setDouble(2, montoEsperado);
            stmtUpdate.setDouble(3, montoReal);
            stmtUpdate.setDouble(4, diferencia);
            stmtUpdate.setInt(5, idCorte);

            int filasActualizadas = stmtUpdate.executeUpdate();
            if (filasActualizadas == 0) {
                throw new SQLException("No se pudo actualizar el registro del corte.");
            }

            // ── Commit si todo OK ──
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al cerrar corte de caja (Aplicando rollback): " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            // Restaurar autoCommit original
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ex) {
                    System.err.println("Error al restaurar autoCommit: " + ex.getMessage());
                }
            }
            // Cerrar recursos manuales
            if (rsCorte != null) {
                try { rsCorte.close(); } catch (SQLException e) {}
            }
            if (stmtGetCorte != null) {
                try { stmtGetCorte.close(); } catch (SQLException e) {}
            }
            if (stmtUpdate != null) {
                try { stmtUpdate.close(); } catch (SQLException e) {}
            }
        }
    }

    /**
     * Obtiene el corte de caja actualmente abierto.
     *
     * @return El objeto CorteCaja abierto, o null si no hay ninguno.
     */
    public CorteCaja obtenerCorteAbierto() {
        String sql = "SELECT id, fecha_apertura, fecha_cierre, id_admin, monto_inicial, monto_esperado, monto_real, diferencia, estado "
                + "FROM cortes_caja WHERE estado = 'ABIERTO' LIMIT 1";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearCorte(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener corte abierto: " + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) {}
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        return null;
    }

    /**
     * Lista todos los cortes de caja de la base de datos ordenados por fecha de apertura descendente.
     *
     * @return Lista de objetos CorteCaja.
     */
    public List<CorteCaja> obtenerHistorialCortes() {
        List<CorteCaja> lista = new ArrayList<>();
        String sql = "SELECT id, fecha_apertura, fecha_cierre, id_admin, monto_inicial, monto_esperado, monto_real, diferencia, estado "
                + "FROM cortes_caja ORDER BY fecha_apertura DESC";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(mapearCorte(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener historial de cortes: " + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) {}
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        return lista;
    }

    /**
     * Obtiene un corte de caja específico por su ID.
     *
     * @param id ID del corte de caja a buscar.
     * @return Objeto CorteCaja correspondiente o null si no existe.
     */
    public CorteCaja obtenerCortePorId(int id) {
        String sql = "SELECT id, fecha_apertura, fecha_cierre, id_admin, monto_inicial, monto_esperado, monto_real, diferencia, estado "
                + "FROM cortes_caja WHERE id = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ConexionDB.getInstancia().getConexion();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearCorte(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener corte por ID: " + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) {}
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
        return null;
    }

    /**
     * Auxiliar para mapear un ResultSet a un objeto CorteCaja.
     */
    private CorteCaja mapearCorte(ResultSet rs) throws SQLException {
        CorteCaja corte = new CorteCaja();
        corte.setId(rs.getInt("id"));
        corte.setFechaApertura(rs.getString("fecha_apertura"));
        corte.setFechaCierre(rs.getString("fecha_cierre"));
        corte.setIdAdmin(rs.getInt("id_admin"));
        corte.setMontoInicial(rs.getDouble("monto_inicial"));
        corte.setMontoEsperado(rs.getDouble("monto_esperado"));
        corte.setMontoReal(rs.getDouble("monto_real"));
        corte.setDiferencia(rs.getDouble("diferencia"));
        corte.setEstado(rs.getString("estado"));
        return corte;
    }
}
