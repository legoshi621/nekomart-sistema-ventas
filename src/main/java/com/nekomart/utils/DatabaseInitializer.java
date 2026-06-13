package com.nekomart.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
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
}
