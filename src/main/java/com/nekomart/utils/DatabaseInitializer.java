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
}
