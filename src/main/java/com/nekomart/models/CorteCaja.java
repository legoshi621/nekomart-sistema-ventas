package com.nekomart.models;

/**
 * Clase Modelo (POJO) que representa un Corte de Caja en el sistema NekoMart.
 * Todo el código está comentado en español.
 */
public class CorteCaja {
    private int id;
    private String fechaApertura;
    private String fechaCierre;
    private int idAdmin;
    private double montoInicial;
    private double montoEsperado;
    private double montoReal;
    private double diferencia;
    private String estado; // 'ABIERTO' o 'CERRADO'

    /**
     * Constructor vacío.
     */
    public CorteCaja() {
    }

    /**
     * Constructor con todos los parámetros.
     */
    public CorteCaja(int id, String fechaApertura, String fechaCierre, int idAdmin, 
                     double montoInicial, double montoEsperado, double montoReal, 
                     double diferencia, String estado) {
        this.id = id;
        this.fechaApertura = fechaApertura;
        this.fechaCierre = fechaCierre;
        this.idAdmin = idAdmin;
        this.montoInicial = montoInicial;
        this.montoEsperado = montoEsperado;
        this.montoReal = montoReal;
        this.diferencia = diferencia;
        this.estado = estado;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(String fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public String getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(String fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public double getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(double montoInicial) {
        this.montoInicial = montoInicial;
    }

    public double getMontoEsperado() {
        return montoEsperado;
    }

    public void setMontoEsperado(double montoEsperado) {
        this.montoEsperado = montoEsperado;
    }

    public double getMontoReal() {
        return montoReal;
    }

    public void setMontoReal(double montoReal) {
        this.montoReal = montoReal;
    }

    public double getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(double diferencia) {
        this.diferencia = diferencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // ── Método toString() ──────────────────────────────────────────────────

    @Override
    public String toString() {
        return "CorteCaja{" +
                "id=" + id +
                ", fechaApertura='" + fechaApertura + '\'' +
                ", fechaCierre='" + fechaCierre + '\'' +
                ", idAdmin=" + idAdmin +
                ", montoInicial=" + montoInicial +
                ", montoEsperado=" + montoEsperado +
                ", montoReal=" + montoReal +
                ", diferencia=" + diferencia +
                ", estado='" + estado + '\'' +
                '}';
    }
}
