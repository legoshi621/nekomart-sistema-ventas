package com.nekomart.dao;

import com.nekomart.models.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar operaciones CRUD de productos en la base de datos SQLite.
 * Admite fechas de caducidad, lotes y borrado lógico (Soft Delete).
 * Todo el código está comentado en español.
 */
public class ProductoDAO {

    /**
     * Lista todos los productos activos de la base de datos (activo = 1).
     */
    public List<Producto> listarTodos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria, imagen_ruta, fecha_caducidad, lote, activo FROM productos WHERE activo = 1";

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
     * Busca un producto por su ID (sin importar su estado activo/inactivo).
     */
    public Producto buscarPorId(int id) {
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria, imagen_ruta, fecha_caducidad, lote, activo FROM productos WHERE id = ?";

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
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria, imagen_ruta, fecha_caducidad, lote, activo FROM productos WHERE codigo = ?";

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
        String sql = "INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, imagen_ruta, fecha_caducidad, lote, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setDouble(3, producto.getPrecio());
            stmt.setInt(4, producto.getStock());
            stmt.setInt(5, producto.getStockMinimo());
            stmt.setString(6, producto.getCategoria());
            stmt.setString(7, producto.getImagenRuta());
            stmt.setString(8, producto.getFechaCaducidad());
            stmt.setString(9, producto.getLote());
            stmt.setInt(10, producto.isActivo() ? 1 : 0);

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
        String sql = "UPDATE productos SET codigo=?, nombre=?, precio=?, stock=?, stock_minimo=?, categoria=?, imagen_ruta=?, fecha_caducidad=?, lote=?, activo=? WHERE id=?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setDouble(3, producto.getPrecio());
            stmt.setInt(4, producto.getStock());
            stmt.setInt(5, producto.getStockMinimo());
            stmt.setString(6, producto.getCategoria());
            stmt.setString(7, producto.getImagenRuta());
            stmt.setString(8, producto.getFechaCaducidad());
            stmt.setString(9, producto.getLote());
            stmt.setInt(10, producto.isActivo() ? 1 : 0);
            stmt.setInt(11, producto.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Realiza un borrado lógico (Soft Delete) de un producto, cambiando 'activo' a 0.
     */
    public boolean eliminar(int id) {
        String sql = "UPDATE productos SET activo = 0 WHERE id = ?";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar (Soft Delete) producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca productos activos por nombre (búsqueda parcial).
     */
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria, imagen_ruta, fecha_caducidad, lote, activo FROM productos WHERE nombre LIKE ? AND activo = 1";

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
     * Busca productos activos por categoría.
     */
    public List<Producto> buscarPorCategoria(String categoria) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria, imagen_ruta, fecha_caducidad, lote, activo FROM productos WHERE categoria LIKE ? AND activo = 1";

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
     * Actualiza el stock de un producto activo (resta la cantidad vendida).
     */
    public boolean actualizarStock(int productoId, int cantidad) {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ? AND activo = 1";

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
     * Suma stock a un producto activo (para entradas de mercancía).
     *
     * @param productoId ID del producto al que se le sumará stock
     * @param cantidad   Cantidad de unidades a agregar
     * @return true si la operación fue exitosa, false en caso de error
     */
    public boolean sumarStock(int productoId, int cantidad) {
        String sql = "UPDATE productos SET stock = stock + ? WHERE id = ? AND activo = 1";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cantidad);
            stmt.setInt(2, productoId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al sumar stock: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene productos activos con stock menor o igual al stock mínimo.
     */
    public List<Producto> obtenerProductosBajoStock() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria, imagen_ruta, fecha_caducidad, lote, activo FROM productos WHERE stock <= stock_minimo AND activo = 1";

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
     * Obtiene la lista de todos los productos eliminados lógicamente (inactivos).
     *
     * @return Lista de objetos Producto inactivos (activo = 0).
     */
    public List<Producto> obtenerInactivos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, stock_minimo, categoria, imagen_ruta, fecha_caducidad, lote, activo FROM productos WHERE activo = 0";

        try (Connection conn = ConexionDB.getInstancia().getConexion();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos inactivos: " + e.getMessage());
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
        p.setImagenRuta(rs.getString("imagen_ruta"));
        p.setFechaCaducidad(rs.getString("fecha_caducidad"));
        p.setLote(rs.getString("lote"));
        p.setActivo(rs.getInt("activo") == 1);
        return p;
    }
}