package com.nekomart.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utilidad para inicializar la base de datos SQLite.
 * Lee el archivo SQL desde los recursos y ejecuta las sentencias para crear las tablas.
 */
public class DatabaseInitializer {

    private static final String SCRIPT_FILE = "database_sqlite.sql";

    /**
     * Inicializa la base de datos ejecutando el script SQL.
     *
     * @param conexion Conexión activa a la base de datos SQLite.
     */
    public static void inicializarBaseDeDatos(Connection conexion) {
        System.out.println("Inicializando tablas...");
        
        try (InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(SCRIPT_FILE)) {
            if (is == null) {
                System.err.println("No se encontró el script de base de datos en resources: " + SCRIPT_FILE);
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                 Statement stmt = conexion.createStatement()) {

                StringBuilder sb = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) {
                    // Ignorar comentarios
                    if (linea.trim().startsWith("--")) {
                        continue;
                    }
                    sb.append(linea);
                    // Si la línea termina en ';', ejecutamos el comando
                    if (linea.trim().endsWith(";")) {
                        stmt.execute(sb.toString());
                        sb.setLength(0); // Limpiar para el siguiente comando
                    }
                }
                System.out.println("✅ Base de datos inicializada");
            }
        } catch (Exception e) {
            System.err.println("Error al ejecutar el script de inicialización: " + e.getMessage());
        }
    }

    /**
     * Verifica si existen las columnas de imágenes y fotos, y las agrega si hacen falta.
     */
    public static void verificarYMigrarColumnas(Connection conexion) {
        // Migración de imagen_ruta en la tabla productos
        if (!columnaExiste(conexion, "productos", "imagen_ruta")) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("ALTER TABLE productos ADD COLUMN imagen_ruta TEXT;");
                System.out.println("✅ Columna 'imagen_ruta' agregada a 'productos' exitosamente.");
            } catch (Exception e) {
                System.err.println("Error al agregar columna 'imagen_ruta': " + e.getMessage());
            }
        }

        // Migración de foto_ruta en la tabla usuarios
        if (!columnaExiste(conexion, "usuarios", "foto_ruta")) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("ALTER TABLE usuarios ADD COLUMN foto_ruta TEXT;");
                System.out.println("✅ Columna 'foto_ruta' agregada a 'usuarios' exitosamente.");
            } catch (Exception e) {
                System.err.println("Error al agregar columna 'foto_ruta': " + e.getMessage());
            }
        }

        // Migración de fecha_caducidad en la tabla productos
        if (!columnaExiste(conexion, "productos", "fecha_caducidad")) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("ALTER TABLE productos ADD COLUMN fecha_caducidad TEXT;");
                System.out.println("✅ Columna 'fecha_caducidad' agregada a 'productos' exitosamente.");
            } catch (Exception e) {
                System.err.println("Error al agregar columna 'fecha_caducidad': " + e.getMessage());
            }
        }

        // Migración de lote en la tabla productos
        if (!columnaExiste(conexion, "productos", "lote")) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("ALTER TABLE productos ADD COLUMN lote TEXT;");
                System.out.println("✅ Columna 'lote' agregada a 'productos' exitosamente.");
            } catch (Exception e) {
                System.err.println("Error al agregar columna 'lote': " + e.getMessage());
            }
        }

        // Migración de activo en la tabla productos
        if (!columnaExiste(conexion, "productos", "activo")) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("ALTER TABLE productos ADD COLUMN activo INTEGER NOT NULL DEFAULT 1;");
                System.out.println("✅ Columna 'activo' agregada a 'productos' exitosamente.");
            } catch (Exception e) {
                System.err.println("Error al agregar columna 'activo': " + e.getMessage());
            }
        }

        // Migración de la columna 'motivo' en 'movimientos_inventario'
        if (!columnaExiste(conexion, "movimientos_inventario", "motivo")) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("ALTER TABLE movimientos_inventario ADD COLUMN motivo TEXT;");
                System.out.println("✅ Columna 'motivo' agregada a 'movimientos_inventario' exitosamente.");
            } catch (Exception e) {
                System.err.println("Error al agregar columna 'motivo' a 'movimientos_inventario': " + e.getMessage());
            }
        }

        // Creación de la tabla 'devoluciones' si no existe
        if (!tablaExiste(conexion, "devoluciones")) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS devoluciones ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "id_venta INTEGER NOT NULL, "
                        + "id_detalle_venta INTEGER NOT NULL, "
                        + "cantidad INTEGER NOT NULL CHECK (cantidad > 0), "
                        + "motivo TEXT NOT NULL, "
                        + "tipo_reembolso TEXT NOT NULL CHECK (tipo_reembolso IN ('EFECTIVO', 'CREDITO')), "
                        + "fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "id_admin INTEGER NOT NULL, "
                        + "FOREIGN KEY (id_venta) REFERENCES ventas(id), "
                        + "FOREIGN KEY (id_admin) REFERENCES usuarios(id)"
                        + ");");
                System.out.println("✅ Tabla 'devoluciones' creada exitosamente durante la migración.");
            } catch (Exception e) {
                System.err.println("Error al crear la tabla 'devoluciones' durante la migración: " + e.getMessage());
            }
        }

        // Creación de la tabla 'cortes_caja' si no existe
        if (!tablaExiste(conexion, "cortes_caja")) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS cortes_caja ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "fecha_apertura TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "fecha_cierre TEXT, "
                        + "id_admin INTEGER NOT NULL, "
                        + "monto_inicial REAL NOT NULL DEFAULT 0, "
                        + "monto_esperado REAL DEFAULT 0, "
                        + "monto_real REAL DEFAULT 0, "
                        + "diferencia REAL DEFAULT 0, "
                        + "estado TEXT NOT NULL DEFAULT 'ABIERTO' CHECK (estado IN ('ABIERTO', 'CERRADO')), "
                        + "FOREIGN KEY (id_admin) REFERENCES usuarios(id)"
                        + ");");
                System.out.println("✅ Tabla 'cortes_caja' creada exitosamente durante la migración.");
            } catch (Exception e) {
                System.err.println("Error al crear la tabla 'cortes_caja' durante la migración: " + e.getMessage());
            }
        }

        // Creación de la tabla 'logs_sistema' si no existe (Auditoría)
        if (!tablaExiste(conexion, "logs_sistema")) {
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS logs_sistema ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "fecha_hora TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "id_usuario INTEGER, "
                        + "accion TEXT NOT NULL, "
                        + "descripcion TEXT, "
                        + "ip_maquina TEXT DEFAULT 'LOCAL'"
                        + ");");
                System.out.println("✅ Tabla 'logs_sistema' creada exitosamente durante la migración.");
            } catch (Exception e) {
                System.err.println("Error al crear la tabla 'logs_sistema' durante la migración: " + e.getMessage());
            }
        }

    }

    /**
     * Comprueba si una columna existe en una tabla específica.
     */
    private static boolean columnaExiste(Connection conexion, String tabla, String columna) {
        String sql = "PRAGMA table_info(" + tabla + ")";
        try (Statement stmt = conexion.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (columna.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al consultar pragma info para " + tabla + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Comprueba si una tabla específica existe en la base de datos.
     */
    private static boolean tablaExiste(Connection conexion, String nombreTabla) {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setString(1, nombreTabla);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar si la tabla " + nombreTabla + " existe: " + e.getMessage());
            return false;
        }
    }
}
