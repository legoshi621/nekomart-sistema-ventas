package com.nekomart.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase Modelo (POJO) que representa una Venta en el sistema NekoMart.
 * La fecha se almacena como String en formato ISO8601 (compatible con SQLite).
 */
public class Venta {
    private int id;
    private String folio;
    private String fecha;         // Formato: "yyyy-MM-dd HH:mm:ss" (SQLite usa TEXT)
    private double total;
    private String metodoPago;
    private double montoRecibido;
    private double cambio;
    private int empleadoId;

    // Lista de detalles asociados a esta venta (relación de composición)
    private List<DetalleVenta> detalles;

    // Constructor vacío
    public Venta() {
        this.detalles = new ArrayList<>();
    }

    // Constructor sin ID ni fecha (útil para registrar nuevas ventas)
    public Venta(String folio, double total, String metodoPago, double montoRecibido, double cambio, int empleadoId) {
        this.folio = folio;
        this.total = total;
        this.metodoPago = metodoPago;
        this.montoRecibido = montoRecibido;
        this.cambio = cambio;
        this.empleadoId = empleadoId;
        this.detalles = new ArrayList<>();
    }

    // Constructor completo
    public Venta(int id, String folio, String fecha, double total, String metodoPago,
                 double montoRecibido, double cambio, int empleadoId) {
        this.id = id;
        this.folio = folio;
        this.fecha = fecha;
        this.total = total;
        this.metodoPago = metodoPago;
        this.montoRecibido = montoRecibido;
        this.cambio = cambio;
        this.empleadoId = empleadoId;
        this.detalles = new ArrayList<>();
    }

    // Getters y Setters con documentación

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public double getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(double montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    public double getCambio() {
        return cambio;
    }

    public void setCambio(double cambio) {
        this.cambio = cambio;
    }

    public int getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(int empleadoId) {
        this.empleadoId = empleadoId;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    // Método de conveniencia para agregar un detalle a la venta
    public void agregarDetalle(DetalleVenta detalle) {
        if (this.detalles == null) {
            this.detalles = new ArrayList<>();
        }
        this.detalles.add(detalle);
    }

    @Override
    public String toString() {
        return "Venta{" +
                "id=" + id +
                ", folio='" + folio + '\'' +
                ", fecha='" + fecha + '\'' +
                ", total=" + total +
                ", metodoPago='" + metodoPago + '\'' +
                ", montoRecibido=" + montoRecibido +
                ", cambio=" + cambio +
                ", empleadoId=" + empleadoId +
                ", numeroDetalles=" + (detalles != null ? detalles.size() : 0) +
                '}';
    }
}
