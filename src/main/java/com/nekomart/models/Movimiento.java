package com.nekomart.models;

/**
 * Modelo que representa un movimiento de inventario (entrada, salida o ajuste).
 * Se utiliza para el Kardex y el registro de entradas de mercancía.
 * Todo el código está comentado en español.
 */
public class Movimiento {

    // Identificador único del movimiento
    private int id;

    // ID del producto asociado al movimiento
    private int productoId;

    // Tipo de movimiento: ENTRADA, SALIDA o AJUSTE
    private String tipo;

    // Cantidad de unidades del movimiento
    private int cantidad;

    // Fecha del movimiento en formato texto (YYYY-MM-DD HH:MM:SS)
    private String fecha;

    // ID del usuario que realizó el movimiento
    private int usuarioId;

    // ── Constructores ────────────────────────────────────────────────────

    /**
     * Constructor vacío para creación dinámica.
     */
    public Movimiento() {
    }

    /**
     * Constructor completo para crear un movimiento con todos los datos.
     *
     * @param id         Identificador del movimiento
     * @param productoId ID del producto
     * @param tipo       Tipo de movimiento (ENTRADA/SALIDA/AJUSTE)
     * @param cantidad   Cantidad de unidades
     * @param fecha      Fecha del movimiento
     * @param usuarioId  ID del usuario que registró
     */
    public Movimiento(int id, int productoId, String tipo, int cantidad, String fecha, int usuarioId) {
        this.id = id;
        this.productoId = productoId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.usuarioId = usuarioId;
    }

    // ── Getters y Setters ────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return "Movimiento{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", tipo='" + tipo + '\'' +
                ", cantidad=" + cantidad +
                ", fecha='" + fecha + '\'' +
                ", usuarioId=" + usuarioId +
                '}';
    }
}
