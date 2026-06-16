package com.nekomart.services;

import com.nekomart.dao.DevolucionDAO;
import com.nekomart.dao.VentaDAO;
import com.nekomart.models.Devolucion;
import com.nekomart.models.DetalleVenta;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar la lógica de negocio de las devoluciones en el sistema NekoMart.
 * Actúa como intermediario entre la interfaz de usuario (UI) y el DAO.
 * Todo el código está documentado y comentado en español.
 */
public class DevolucionService {

    private final DevolucionDAO devolucionDAO;
    private final VentaDAO ventaDAO;

    /**
     * Constructor por defecto que inicializa las instancias de los DAOs correspondientes.
     */
    public DevolucionService() {
        this.devolucionDAO = new DevolucionDAO();
        this.ventaDAO = new VentaDAO();
    }

    /**
     * Procesa una devolución validando que la cantidad solicitada no exceda la cantidad
     * originalmente vendida del producto seleccionado en esa venta.
     *
     * @param idVenta        Identificador de la venta asociada.
     * @param idDetalleVenta Identificador del detalle de venta específico.
     * @param cantidad       Cantidad de unidades a devolver.
     * @param motivo         El motivo de la devolución.
     * @param tipoReembolso  El método de reembolso (EFECTIVO o CREDITO).
     * @param idAdmin        Identificador del administrador que autoriza la devolución.
     * @return true si la devolución fue registrada con éxito en la base de datos, false de lo contrario.
     */
    public boolean procesarDevolucion(int idVenta, int idDetalleVenta, int cantidad, String motivo, String tipoReembolso, int idAdmin) {
        try {
            // 1. Obtener la lista de detalles de la venta para poder validar las cantidades vendidas
            List<DetalleVenta> detalles = ventaDAO.obtenerDetalles(idVenta);
            DetalleVenta detalleEncontrado = null;

            for (DetalleVenta d : detalles) {
                if (d.getId() == idDetalleVenta) {
                    detalleEncontrado = d;
                    break;
                }
            }

            if (detalleEncontrado == null) {
                System.err.println("Error: No se encontró el detalle de venta con ID: " + idDetalleVenta + " para la venta con ID: " + idVenta);
                return false;
            }

            // 2. Validar que la cantidad a devolver no sea mayor a la cantidad vendida originalmente
            if (cantidad <= 0) {
                System.err.println("Error: La cantidad a devolver debe ser mayor a 0.");
                return false;
            }

            if (cantidad > detalleEncontrado.getCantidad()) {
                System.err.println("Error: La cantidad a devolver (" + cantidad + ") no puede ser mayor a la cantidad vendida (" + detalleEncontrado.getCantidad() + ").");
                return false;
            }

            // 3. Crear el objeto Devolucion para pasar al DAO
            Devolucion dev = new Devolucion();
            dev.setIdVenta(idVenta);
            dev.setIdDetalleVenta(idDetalleVenta);
            dev.setCantidad(cantidad);
            dev.setMotivo(motivo);
            dev.setTipoReembolso(tipoReembolso);
            dev.setIdAdmin(idAdmin);

            // 4. Invocar al DAO para registrar en base de datos e iniciar transacción de stock e inventario
            return devolucionDAO.registrarDevolucion(dev);

        } catch (Exception e) {
            System.err.println("Excepción al procesar devolución en DevolucionService: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene una lista de devoluciones registradas asociadas a una venta específica.
     *
     * @param idVenta Identificador de la venta.
     * @return Lista de devoluciones de la venta.
     */
    public List<Devolucion> obtenerDevolucionesPorVenta(int idVenta) {
        try {
            return devolucionDAO.obtenerDevolucionesPorVenta(idVenta);
        } catch (Exception e) {
            System.err.println("Excepción al obtener devoluciones por venta en DevolucionService: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene la lista de todas las devoluciones registradas en el sistema.
     *
     * @return Lista de todas las devoluciones.
     */
    public List<Devolucion> obtenerTodasDevoluciones() {
        try {
            return devolucionDAO.obtenerTodasDevoluciones();
        } catch (Exception e) {
            System.err.println("Excepción al obtener todas las devoluciones en DevolucionService: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
