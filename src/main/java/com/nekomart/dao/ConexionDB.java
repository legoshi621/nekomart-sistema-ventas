package com.nekomart.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase que implementa el patrón Singleton para la gestión de la conexión a la base de datos SQL Server.
 * Todo el código está documentado y comentado en español.
 */
public class ConexionDB {

    // Única instancia de la clase (Singleton)
    private static ConexionDB instancia;
    
    // Objeto Connection de java.sql
    private Connection conexion;

    // Configuración por defecto para SQL Server
    private String host = "localhost";
    private String puerto = "1433";
    private String dbNombre = "NekoMartDB";
    private String usuario = "sa";
    private String password = "YourStrongPassword123"; // Reemplazar con la contraseña de su servidor

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
     *
     * @return Objeto Connection activo.
     * @throws SQLException En caso de error en la conexión o credenciales incorrectas.
     */
    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                // Registrar explícitamente el driver JDBC de Microsoft SQL Server
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                
                // Url de conexión con soporte para encriptación de datos obligatoria y confianza en el certificado
                // (Requerido para evitar problemas SSL comunes en entornos de desarrollo local de SQL Server)
                String url = String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true;",
                    host, puerto, dbNombre
                );
                
                conexion = DriverManager.getConnection(url, usuario, password);
            } catch (ClassNotFoundException e) {
                throw new SQLException("No se encontró el driver JDBC de SQL Server (com.microsoft.sqlserver.jdbc.SQLServerDriver).", e);
            }
        }
        return conexion;
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

    /**
     * Permite reconfigurar las credenciales de conexión en tiempo de ejecución.
     * Al cambiar de credenciales se cierra la conexión actual para que la siguiente
     * petición obtenga una con los nuevos datos.
     *
     * @param host Nombre o IP del servidor SQL.
     * @param puerto Puerto del servidor SQL (comúnmente 1433).
     * @param dbNombre Nombre de la base de datos.
     * @param usuario Usuario de SQL Server (p. ej. 'sa').
     * @param password Contraseña de SQL Server.
     */
    public void configurarConexion(String host, String puerto, String dbNombre, String usuario, String password) {
        this.host = host;
        this.puerto = puerto;
        this.dbNombre = dbNombre;
        this.usuario = usuario;
        this.password = password;
        cerrarConexion(); // Cierra para forzar nueva conexión con los nuevos parámetros
    }
}
