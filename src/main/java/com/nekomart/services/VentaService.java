package com.nekomart.services;

import com.nekomart.dao.VentaDAO;
import com.nekomart.dao.ProductoDAO;
import com.nekomart.models.Venta;
import com.nekomart.models.DetalleVenta;
import com.nekomart.models.Producto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que gestiona la lógica de negocio de las ventas.
 * Todo el código está comentado en español.
 */
public class VentaService {

    private final VentaDAO ventaDAO;
    private final ProductoDAO productoDAO;

    public VentaService() {
        this.ventaDAO = new VentaDAO();
        this.productoDAO = new ProductoDAO();
    }

    /**
     * Registra una venta completa: guarda la venta, sus detalles y actualiza el
     * stock.
     */
    public boolean procesarVenta(List<DetalleVenta> detalles, String metodoPago,
            double montoRecibido, int empleadoId) {
        try {
            // Calcular el total
            double total = 0;
            for (DetalleVenta d : detalles) {
                total += d.getCantidad() * d.getPrecioUnitario();
            }

            // Calcular cambio
            double cambio = 0;
            if (metodoPago.equals("Efectivo")) {
                cambio = montoRecibido - total;
                if (cambio < 0) {
                    System.err.println("Monto recibido insuficiente");
                    return false;
                }
            }

            // Crear objeto Venta
            Venta venta = new Venta();
            venta.setFolio(ventaDAO.generarFolio());
            venta.setFecha(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            venta.setTotal(total);
            venta.setMetodoPago(metodoPago);
            venta.setMontoRecibido(metodoPago.equals("Efectivo") ? montoRecibido : total);
            venta.setCambio(cambio);
            venta.setEmpleadoId(empleadoId);

            // Registrar venta en la BD
            if (ventaDAO.registrarVenta(venta, detalles)) {
                // Actualizar stock de cada producto
                for (DetalleVenta d : detalles) {
                    productoDAO.actualizarStock(d.getProductoId(), d.getCantidad());
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error al procesar venta: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista todas las ventas registradas.
     */
    public List<Venta> obtenerTodasLasVentas() {
        try {
            return ventaDAO.listarTodas();
        } catch (Exception e) {
            System.err.println("Error al obtener ventas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene los detalles de una venta específica.
     */
    public List<DetalleVenta> obtenerDetalles(int ventaId) {
        try {
            return ventaDAO.obtenerDetalles(ventaId);
        } catch (Exception e) {
            System.err.println("Error al obtener detalles: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene las ventas filtradas por un rango de fechas.
     */
    public List<Venta> obtenerVentasPorFecha(String fechaInicio, String fechaFin) {
        try {
            return ventaDAO.listarPorRangoFechas(fechaInicio, fechaFin);
        } catch (Exception e) {
            System.err.println("Error al obtener ventas por fecha: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}