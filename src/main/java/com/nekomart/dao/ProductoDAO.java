package com.nekomart.dao;

import com.nekomart.models.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad 'Producto'.
 * Utiliza PreparedStatement para garantizar seguridad ante Inyección SQL.
 * Todo el código está en español.
 */
public class ProductoDAO {

    private final ConexionDB conexionDB = ConexionDB.getInstancia();

    /**
     * Registra un nuevo producto en la base de datos.
     *
     * @param producto Objeto Producto con los datos a registrar.
     * @return true si el producto se guardó correctamente, false de lo contrario.
     */
    public boolean insertar(Producto producto) {
        String query = "INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setInt(5, producto.getStockMinimo());
            ps.setString(6, producto.getCategoria());
            
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                // Recuperar la clave auto-generada
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        producto.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar producto: " + e.getMessage());
        }
        return false;
    }

    /**
     * Busca un producto usando su código de barras o código comercial único.
     *
     * @param codigo Código del producto a buscar.
     * @return Objeto Producto encontrado o null si no existe.
     */
    public Producto buscarPorCodigo(String codigo) {
        String query = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria FROM productos WHERE codigo = ?";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, codigo);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("stock"),
                        rs.getInt("stock_minimo"),
                        rs.getString("categoria")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto por código: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca un producto usando su identificador único ID.
     *
     * @param id Identificador interno del producto.
     * @return Objeto Producto o null si no se encuentra.
     */
    public Producto buscarPorId(int id) {
        String query = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria FROM productos WHERE id = ?";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("stock"),
                        rs.getInt("stock_minimo"),
                        rs.getString("categoria")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lista todos los productos registrados en la base de datos.
     *
     * @return Lista conteniendo los productos.
     */
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String query = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria FROM productos";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new Producto(
                    rs.getInt("id"),
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getDouble("precio"),
                    rs.getInt("stock"),
                    rs.getInt("stock_minimo"),
                    rs.getString("categoria")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar los productos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Actualiza la información de un producto registrado en la base de datos.
     *
     * @param producto Objeto producto con la información modificada.
     * @return true si la actualización afectó algún registro, de lo contrario false.
     */
    public boolean actualizar(Producto producto) {
        String query = "UPDATE productos SET codigo = ?, nombre = ?, precio = ?, stock = ?, stock_minimo = ?, categoria = ? WHERE id = ?";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setInt(5, producto.getStockMinimo());
            ps.setString(6, producto.getCategoria());
            ps.setInt(7, producto.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
        }
        return false;
    }

    /**
     * Actualiza directamente el inventario/stock de un producto específico (p. ej. tras una venta).
     *
     * @param id Identificador único del producto.
     * @param cantidadVendida Cantidad de unidades vendidas a decrementar del stock actual.
     * @return true si se actualizó correctamente.
     */
    public boolean actualizarStock(int id, int cantidadVendida) {
        String query = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, cantidadVendida);
            ps.setInt(2, id);
            ps.setInt(3, cantidadVendida); // Previene de forma lógica el stock negativo
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar el stock del producto ID " + id + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Elimina un producto de la base de datos.
     *
     * @param id Identificador único del producto a eliminar.
     * @return true si la eliminación fue exitosa.
     */
    public boolean eliminar(int id) {
        String query = "DELETE FROM productos WHERE id = ?";
        
        try (Connection conn = conexionDB.getConexion();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar el producto: " + e.getMessage());
        }
        return false;
    }
}
