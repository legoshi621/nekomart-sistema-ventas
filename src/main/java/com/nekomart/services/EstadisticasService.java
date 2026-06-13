package com.nekomart.services;

import com.nekomart.dao.EstadisticasDAO;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Servicio que actúa como intermediario para obtener estadísticas de ventas.
 * Atrapa cualquier excepción de base de datos y provee valores por defecto seguros.
 * Todo el código está en español y documentado con comentarios.
 */
public class EstadisticasService {

    private final EstadisticasDAO estadisticasDAO;

    public EstadisticasService() {
        this.estadisticasDAO = new EstadisticasDAO();
    }

    /**
     * Obtiene las ventas totales de hoy con manejo de excepciones.
     *
     * @return Monto de ventas de hoy, o 0.0 en caso de error.
     */
    public double getVentasHoy() {
        try {
            return estadisticasDAO.obtenerVentasHoy();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getVentasHoy: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Obtiene las ventas totales del mes con manejo de excepciones.
     *
     * @return Monto de ventas del mes, o 0.0 en caso de error.
     */
    public double getVentasMes() {
        try {
            return estadisticasDAO.obtenerVentasMes();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getVentasMes: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Obtiene la cantidad de productos vendidos hoy con manejo de excepciones.
     *
     * @return Unidades vendidas hoy, o 0 en caso de error.
     */
    public int getProductosVendidosHoy() {
        try {
            return estadisticasDAO.obtenerProductosVendidosHoy();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getProductosVendidosHoy: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Obtiene los ingresos totales del sistema con manejo de excepciones.
     *
     * @return Monto total de ingresos históricos, o 0.0 en caso de error.
     */
    public double getIngresosTotales() {
        try {
            return estadisticasDAO.obtenerIngresosTotales();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getIngresosTotales: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Obtiene el Top 5 de productos con manejo de excepciones.
     *
     * @return Lista de mapas con información de productos, o lista vacía en caso de error.
     */
    public List<Map<String, Object>> getTopProductos() {
        try {
            return estadisticasDAO.obtenerTopProductos();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getTopProductos: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene el mapeo de ventas de los últimos 7 días con manejo de excepciones.
     *
     * @return Mapa de fechas a montos, o mapa vacío en caso de error.
     */
    public Map<String, Double> getVentasUltimos7Dias() {
        try {
            return estadisticasDAO.obtenerVentasUltimos7Dias();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getVentasUltimos7Dias: " + e.getMessage());
            // Inicializar al menos con mapa vacío o un mapa con los días en 0.0 si es posible
            Map<String, Double> vacio = new java.util.LinkedHashMap<>();
            java.time.LocalDate hoy = java.time.LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                vacio.put(hoy.minusDays(i).toString(), 0.0);
            }
            return vacio;
        }
    }
}
