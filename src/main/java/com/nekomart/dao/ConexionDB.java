package com.nekomart.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.nekomart.utils.DatabaseInitializer;

/**
 * Clase que implementa el patrón Singleton para la gestión de la conexión a la
 * base de datos SQLite.
 * Todo el código está documentado y comentado en español.
 */
public class ConexionDB {

    // Única instancia de la clase (Singleton)
    private static ConexionDB instancia;

    // Objeto Connection de java.sql
    private Connection conexion;

    // URL de conexión para SQLite local en la raíz del proyecto
    private final String dbUrl = "jdbc:sqlite:nekomart.db";

    // Constructor privado para evitar que se creen instancias fuera de esta clase
    private ConexionDB() {
    }

    /**
     * Obtiene la instancia única de ConexionDB.
     * Implementa sincronización para entornos multi-hilos (thread-safe).
     *
     * @return Instancia única de ConexionDB.
     */
    public static synchronized ConexionDB getInstancia() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    /**
     * Retorna una conexión activa hacia la base de datos.
     * Si no existe o fue cerrada, establece una nueva.
     * Verifica la existencia de tablas y las inicializa si es necesario.
     *
     * @return Objeto Connection activo.
     * @throws SQLException En caso de error en la conexión.
     */
    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                // Registrar explícitamente el driver JDBC de SQLite
                Class.forName("org.sqlite.JDBC");

                // Conectar a SQLite
                conexion = DriverManager.getConnection(dbUrl);

                // Verificar si la tabla 'usuarios' existe para inicializar DB si es necesario
                if (!tablaExiste("usuarios")) {
                    System.out.println("Base de datos no encontrada. Creando archivo e inicializando tablas...");
                    DatabaseInitializer.inicializarBaseDeDatos(conexion);
                }
                // Ejecutar migración automática de nuevas columnas (siempre)
                DatabaseInitializer.verificarYMigrarColumnas(conexion);

            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "No se encontró el driver JDBC de SQLite (org.sqlite.JDBC).",
                        e);
            }
        }
        return conexion;
    }

    /**
     * Verifica si una tabla específica existe en la base de datos SQLite.
     *
     * @param nombreTabla Nombre de la tabla a buscar.
     * @return true si la tabla existe, false en caso contrario.
     */
    private boolean tablaExiste(String nombreTabla) {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setString(1, nombreTabla);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar si la tabla existe: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cierra la conexión activa si esta se encuentra abierta.
     */
    public void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al intentar cerrar la conexión: " + e.getMessage());
            } finally {
                conexion = null;
            }
        }
    }
}