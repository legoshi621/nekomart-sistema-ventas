package com.nekomart.dao;

import com.nekomart.models.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar operaciones CRUD de productos en la base de datos SQLite.
 * Todo el código está comentado en español.
 */
public class ProductoDAO {

    /**
     * Lista todos los productos de la base de datos.
     */
    public List<Producto> listarTodos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria FROM productos";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Busca un producto por su ID.
     */
    public Producto buscarPorId(int id) {
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria FROM productos WHERE id = ?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca un producto por su código.
     */
    public Producto buscarPorCodigo(String codigo) {
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria FROM productos WHERE codigo = ?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearProducto(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto por código: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crea un nuevo producto en la base de datos.
     */
    public boolean crear(Producto producto) {
        String sql = "INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setDouble(3, producto.getPrecio());
            stmt.setInt(4, producto.getStock());
            stmt.setInt(5, producto.getStockMinimo());
            stmt.setString(6, producto.getCategoria());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza un producto existente.
     */
    public boolean actualizar(Producto producto) {
        String sql = "UPDATE productos SET codigo=?, nombre=?, precio=?, stock=?, stock_minimo=?, categoria=? WHERE id=?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setDouble(3, producto.getPrecio());
            stmt.setInt(4, producto.getStock());
            stmt.setInt(5, producto.getStockMinimo());
            stmt.setString(6, producto.getCategoria());
            stmt.setInt(7, producto.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto por su ID.
     */
    public boolean eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca productos por nombre (búsqueda parcial).
     */
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria FROM productos WHERE nombre LIKE ?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nombre + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por nombre: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Busca productos por categoría.
     */
    public List<Producto> buscarPorCategoria(String categoria) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria FROM productos WHERE categoria LIKE ?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + categoria + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por categoría: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Actualiza el stock de un producto (resta la cantidad vendida).
     */
    public boolean actualizarStock(int productoId, int cantidad) {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cantidad);
            stmt.setInt(2, productoId);
            stmt.setInt(3, cantidad);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene productos con stock menor o igual al stock mínimo.
     */
    public List<Producto> obtenerProductosBajoStock() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria FROM productos WHERE stock <= stock_minimo";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos bajo stock: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Mapea un ResultSet a un objeto Producto.
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecio(rs.getDouble("precio"));
        p.setStock(rs.getInt("stock"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setCategoria(rs.getString("categoria"));
        return p;
    }
}