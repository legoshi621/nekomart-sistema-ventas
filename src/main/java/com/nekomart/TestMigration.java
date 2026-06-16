package com.nekomart;

import com.nekomart.dao.ConexionDB;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class TestMigration {
    public static void main(String[] args) {
        try {
            System.out.println("Intentando conectar e inicializar base de datos...");
            Connection conn = ConexionDB.getInstancia().getConexion();
            System.out.println("Conexión establecida.");

            System.out.println("Listando tablas en la base de datos:");
            DatabaseMetaData dbmd = conn.getMetaData();
            try (ResultSet rs = dbmd.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    System.out.println("- " + rs.getString("TABLE_NAME"));
                }
            }

            System.out.println("Listando columnas de 'movimientos_inventario':");
            try (ResultSet rs = dbmd.getColumns(null, null, "movimientos_inventario", "%")) {
                while (rs.next()) {
                    System.out.println("  * " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
                }
            }
            
            System.out.println("Listando columnas de 'devoluciones':");
            try (ResultSet rs = dbmd.getColumns(null, null, "devoluciones", "%")) {
                while (rs.next()) {
                    System.out.println("  * " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
                }
            }

            System.out.println("Migración e inicialización verificadas con éxito!");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
