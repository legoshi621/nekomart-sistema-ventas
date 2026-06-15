package com.nekomart.models;

/**
 * Clase Modelo (POJO) que representa una Devolución en el sistema NekoMart.
 * Todo el código está comentado en español.
 */
public class Devolucion {
    private int id;
    private int idVenta;
    private int idDetalleVenta;
    private int cantidad;
    private String motivo;
    private String tipoReembolso; // Valores: 'EFECTIVO' o 'CREDITO'
    private String fecha; // Formato de fecha texto: YYYY-MM-DD HH:MM:SS
    private int idAdmin;

    /**
     * Constructor vacío.
     */
    public Devolucion() {
    }

    /**
     * Constructor con todos los parámetros.
     *
     * @param id             Identificador de la devolución
     * @param idVenta        ID de la venta asociada
     * @param idDetalleVenta ID del detalle de venta devuelto
     * @param cantidad       Cantidad de productos devueltos
     * @param motivo         Motivo de la devolución
     * @param tipoReembolso  Tipo de reembolso (EFECTIVO o CREDITO)
     * @param fecha          Fecha del registro
     * @param idAdmin        ID del administrador que autorizó la devolución
     */
    public Devolucion(int id, int idVenta, int idDetalleVenta, int cantidad, String motivo, String tipoReembolso, String fecha, int idAdmin) {
        this.id = id;
        this.idVenta = idVenta;
        this.idDetalleVenta = idDetalleVenta;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.tipoReembolso = tipoReembolso;
        this.fecha = fecha;
        this.idAdmin = idAdmin;
    }

    // ── Getters y Setters ────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdDetalleVenta() {
        return idDetalleVenta;
    }

    public void setIdDetalleVenta(int idDetalleVenta) {
        this.idDetalleVenta = idDetalleVenta;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getTipoReembolso() {
        return tipoReembolso;
    }

    public void setTipoReembolso(String tipoReembolso) {
        this.tipoReembolso = tipoReembolso;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    @Override
    public String toString() {
        return "Devolucion{" +
                "id=" + id +
                ", idVenta=" + idVenta +
                ", idDetalleVenta=" + idDetalleVenta +
                ", cantidad=" + cantidad +
                ", motivo='" + motivo + '\'' +
                ", tipoReembolso='" + tipoReembolso + '\'' +
                ", fecha='" + fecha + '\'' +
                ", idAdmin=" + idAdmin +
                '}';
    }
}
