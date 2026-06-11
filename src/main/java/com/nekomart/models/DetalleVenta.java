package com.nekomart.models;

/**
 * Clase Modelo (POJO) que representa el Detalle de una Venta en el sistema NekoMart.
 */
public class DetalleVenta {
    private int id;
    private int ventaId;
    private int productoId;
    private int cantidad;
    private double precioUnitario;

    // Constructor vacío
    public DetalleVenta() {
    }

    // Constructor sin ID ni ventaId (útil para cuando estamos armando la venta en memoria antes de registrar)
    public DetalleVenta(int productoId, int cantidad, double precioUnitario) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Constructor con ventaId pero sin ID de detalle
    public DetalleVenta(int ventaId, int productoId, int cantidad, double precioUnitario) {
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Constructor completo
    public DetalleVenta(int id, int ventaId, int productoId, int cantidad, double precioUnitario) {
        this.id = id;
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Getters y Setters con documentación

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVentaId() {
        return ventaId;
    }

    public void setVentaId(int ventaId) {
        this.ventaId = ventaId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    // Método de conveniencia para calcular el subtotal del detalle
    public double getSubtotal() {
        return this.cantidad * this.precioUnitario;
    }

    @Override
    public String toString() {
        return "DetalleVenta{" +
                "id=" + id +
                ", ventaId=" + ventaId +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
