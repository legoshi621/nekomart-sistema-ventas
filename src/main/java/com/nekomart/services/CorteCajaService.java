package com.nekomart.services;

import com.nekomart.dao.CorteCajaDAO;
import com.nekomart.models.CorteCaja;
import java.util.List;

/**
 * Clase de Servicio que encapsula la lógica de negocio para los cortes de caja de NekoMart.
 * Sirve como puente entre los componentes visuales de la interfaz de usuario y el DAO.
 * Todo el código está comentado en español.
 */
public class CorteCajaService {

    private final CorteCajaDAO corteCajaDAO;

    /**
     * Constructor por defecto. Inicializa la instancia del DAO.
     */
    public CorteCajaService() {
        this.corteCajaDAO = new CorteCajaDAO();
    }

    /**
     * Llama al DAO para abrir un nuevo corte de caja.
     *
     * @param idAdmin      ID del administrador que abre el corte.
     * @param montoInicial Dinero inicial registrado en caja.
     * @return true si se abrió con éxito, false en caso contrario.
     */
    public boolean abrirCaja(int idAdmin, double montoInicial) {
        return corteCajaDAO.abrirCorte(idAdmin, montoInicial);
    }

    /**
     * Llama al DAO para cerrar un corte de caja activo.
     * Calcula la suma de ventas y las diferencias de forma segura y transaccional.
     *
     * @param idCorte   ID del corte de caja que se desea cerrar.
     * @param montoReal El monto físicamente contado en la caja.
     * @return true si se cerró correctamente, false en caso de error.
     */
    public boolean cerrarCaja(int idCorte, double montoReal) {
        return corteCajaDAO.cerrarCorte(idCorte, montoReal);
    }

    /**
     * Llama al DAO para obtener el corte de caja actualmente activo (abierto).
     *
     * @return Objeto CorteCaja si existe uno activo, o null si la caja está cerrada.
     */
    public CorteCaja obtenerCorteAbierto() {
        return corteCajaDAO.obtenerCorteAbierto();
    }

    /**
     * Obtiene las ventas acumuladas en un periodo de tiempo.
     */
    public double obtenerVentasPeriodo(String fechaInicio, String fechaFin) {
        return corteCajaDAO.obtenerVentasPeriodo(fechaInicio, fechaFin);
    }

    /**
     * Llama al DAO para obtener el historial completo de todos los cortes.
     *
     * @return Lista de todos los cortes de caja ordenados por fecha de apertura descendente.
     */
    public List<CorteCaja> obtenerHistorial() {
        return corteCajaDAO.obtenerHistorialCortes();
    }
}
