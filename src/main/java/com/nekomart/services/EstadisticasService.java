package com.nekomart.services;

import com.nekomart.dao.EstadisticasDAO;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que actúa como intermediario para obtener estadísticas de ventas.
 */
public class EstadisticasService {

    private final EstadisticasDAO estadisticasDAO;

    public EstadisticasService() {
        this.estadisticasDAO = new EstadisticasDAO();
    }

    // ══════════════════════════════════════════════════════════════════
    // MÉTODOS GENERALES (ADMIN)
    // ══════════════════════════════════════════════════════════════════

    public double getVentasHoy() {
        try {
            return estadisticasDAO.obtenerVentasHoy();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getVentasHoy: " + e.getMessage());
            return 0.0;
        }
    }

    public double getVentasMes() {
        try {
            return estadisticasDAO.obtenerVentasMes();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getVentasMes: " + e.getMessage());
            return 0.0;
        }
    }

    public int getProductosVendidosHoy() {
        try {
            return estadisticasDAO.obtenerProductosVendidosHoy();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getProductosVendidosHoy: " + e.getMessage());
            return 0;
        }
    }

    public double getIngresosTotales() {
        try {
            return estadisticasDAO.obtenerIngresosTotales();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getIngresosTotales: " + e.getMessage());
            return 0.0;
        }
    }

    public List<Map<String, Object>> getTopProductos() {
        try {
            return estadisticasDAO.obtenerTopProductos();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getTopProductos: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Map<String, Double> getVentasUltimos7Dias() {
        try {
            return estadisticasDAO.obtenerVentasUltimos7Dias();
        } catch (Exception e) {
            System.err.println("Error en EstadisticasService.getVentasUltimos7Dias: " + e.getMessage());
            Map<String, Double> vacio = new LinkedHashMap<>();
            LocalDate hoy = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                vacio.put(hoy.minusDays(i).toString(), 0.0);
            }
            return vacio;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // MÉTODOS FILTRADOS POR EMPLEADO
    // ══════════════════════════════════════════════════════════════════

    public double getVentasHoyPorEmpleado(int empleadoId) {
        try {
            return estadisticasDAO.obtenerVentasHoyPorEmpleado(empleadoId);
        } catch (Exception e) {
            System.err.println("Error en getVentasHoyPorEmpleado: " + e.getMessage());
            return 0.0;
        }
    }

    public double getVentasMesPorEmpleado(int empleadoId) {
        try {
            return estadisticasDAO.obtenerVentasMesPorEmpleado(empleadoId);
        } catch (Exception e) {
            System.err.println("Error en getVentasMesPorEmpleado: " + e.getMessage());
            return 0.0;
        }
    }

    public int getProductosVendidosHoyPorEmpleado(int empleadoId) {
        try {
            return estadisticasDAO.obtenerProductosVendidosHoyPorEmpleado(empleadoId);
        } catch (Exception e) {
            System.err.println("Error en getProductosVendidosHoyPorEmpleado: " + e.getMessage());
            return 0;
        }
    }

    public double getIngresosTotalesPorEmpleado(int empleadoId) {
        try {
            return estadisticasDAO.obtenerIngresosTotalesPorEmpleado(empleadoId);
        } catch (Exception e) {
            System.err.println("Error en getIngresosTotalesPorEmpleado: " + e.getMessage());
            return 0.0;
        }
    }

    public List<Map<String, Object>> getTopProductosPorEmpleado(int empleadoId) {
        try {
            return estadisticasDAO.obtenerTopProductosPorEmpleado(empleadoId);
        } catch (Exception e) {
            System.err.println("Error en getTopProductosPorEmpleado: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Map<String, Double> getVentasUltimos7DiasPorEmpleado(int empleadoId) {
        try {
            return estadisticasDAO.obtenerVentasUltimos7DiasPorEmpleado(empleadoId);
        } catch (Exception e) {
            System.err.println("Error en getVentasUltimos7DiasPorEmpleado: " + e.getMessage());
            Map<String, Double> vacio = new LinkedHashMap<>();
            LocalDate hoy = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                vacio.put(hoy.minusDays(i).toString(), 0.0);
            }
            return vacio;
        }
    }
}
