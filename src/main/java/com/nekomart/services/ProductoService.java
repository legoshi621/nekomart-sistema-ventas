package com.nekomart.services;

import com.nekomart.dao.ProductoDAO;
import com.nekomart.models.Producto;
import com.nekomart.services.LogService;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que gestiona la lógica de negocio de los productos.
 * Actúa como intermediario entre la UI y el DAO.
 * Todo el código está comentado en español.
 */
public class ProductoService {

    private final ProductoDAO productoDAO;

    public ProductoService() {
        this.productoDAO = new ProductoDAO();
    }

    /**
     * Obtiene todos los productos.
     */
    public List<Producto> obtenerTodos() {
        try {
            return productoDAO.listarTodos();
        } catch (Exception e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Guarda un producto (crea o actualiza según si tiene ID).
     */
    public boolean guardarProducto(Producto producto) {
        try {
            if (producto.getId() == 0) {
                return productoDAO.crear(producto);
            } else {
                return productoDAO.actualizar(producto);
            }
        } catch (Exception e) {
            System.err.println("Error al guardar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto por su ID.
     */
    public boolean eliminarProducto(int id) {
        try {
            boolean eliminado = productoDAO.eliminar(id);
            if (eliminado) {
                // Registrar log de auditoría al eliminar producto
                // Se usa 0 como idUsuario porque el Service no tiene acceso al usuario actual
                LogService.registrar(0, "ELIMINAR_PRODUCTO", "Producto ID: " + id);
            }
            return eliminado;
        } catch (Exception e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca productos por texto (nombre o código).
     */
    public List<Producto> buscar(String texto) {
        try {
            List<Producto> porNombre = productoDAO.buscarPorNombre(texto);
            List<Producto> porCodigo = productoDAO.buscarPorCodigo(texto) != null
                    ? List.of(productoDAO.buscarPorCodigo(texto))
                    : new ArrayList<>();

            // Combinar resultados sin duplicados
            List<Producto> resultados = new ArrayList<>(porNombre);
            for (Producto p : porCodigo) {
                if (!resultados.contains(p)) {
                    resultados.add(p);
                }
            }
            return resultados;
        } catch (Exception e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene productos con stock bajo.
     */
    public List<Producto> obtenerBajoStock() {
        try {
            return productoDAO.obtenerProductosBajoStock();
        } catch (Exception e) {
            System.err.println("Error al obtener productos bajo stock: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}