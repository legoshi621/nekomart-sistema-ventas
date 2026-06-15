package com.nekomart.models;

/**
 * Modelo que representa un registro de log del sistema para auditoría.
 * Cada acción importante del sistema se registra como un LogSistema.
 * Todo el código está comentado en español.
 */
public class LogSistema {

    // Identificador único del log
    private int id;
    // Fecha y hora en que se registró la acción
    private String fechaHora;
    // ID del usuario que realizó la acción (0 si no aplica)
    private int idUsuario;
    // Tipo de acción realizada (LOGIN, VENTA, ELIMINAR_PRODUCTO, etc.)
    private String accion;
    // Descripción detallada de la acción
    private String descripcion;
    // IP de la máquina donde se realizó la acción
    private String ipMaquina;

    /**
     * Constructor vacío requerido para instanciar el objeto sin datos iniciales.
     */
    public LogSistema() {
    }

    /**
     * Constructor completo con todos los atributos del log.
     *
     * @param id          Identificador único del log.
     * @param fechaHora   Fecha y hora del registro.
     * @param idUsuario   ID del usuario que realizó la acción.
     * @param accion      Tipo de acción realizada.
     * @param descripcion Descripción detallada.
     * @param ipMaquina   IP de la máquina.
     */
    public LogSistema(int id, String fechaHora, int idUsuario, String accion, String descripcion, String ipMaquina) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.idUsuario = idUsuario;
        this.accion = accion;
        this.descripcion = descripcion;
        this.ipMaquina = ipMaquina;
    }

    // ==================== GETTERS Y SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIpMaquina() {
        return ipMaquina;
    }

    public void setIpMaquina(String ipMaquina) {
        this.ipMaquina = ipMaquina;
    }

    @Override
    public String toString() {
        return "LogSistema{" +
                "id=" + id +
                ", fechaHora='" + fechaHora + '\'' +
                ", idUsuario=" + idUsuario +
                ", accion='" + accion + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", ipMaquina='" + ipMaquina + '\'' +
                '}';
    }
}
