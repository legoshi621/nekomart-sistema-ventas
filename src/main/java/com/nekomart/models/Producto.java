package com.nekomart.models;

/**
 * Clase Modelo (POJO) que representa a un Producto en el sistema NekoMart.
 */
public class Producto {
    private int id;
    private String codigo;
    private String nombre;
    private double precio;
    private int stock;
    private int stockMinimo;
    private String categoria;
    private String imagenRuta; // Ruta de la imagen del producto

    // Constructor vacío
    public Producto() {
    }

    // Constructor sin ID (útil para inserciones)
    public Producto(String codigo, String nombre, double precio, int stock, int stockMinimo, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.categoria = categoria;
    }

    // Constructor completo
    public Producto(int id, String codigo, String nombre, double precio, int stock, int stockMinimo, String categoria) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.categoria = categoria;
    }

    // Getters y Setters con documentación

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagenRuta() {
        return imagenRuta;
    }

    public void setImagenRuta(String imagenRuta) {
        this.imagenRuta = imagenRuta;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                ", stockMinimo=" + stockMinimo +
                ", categoria='" + categoria + '\'' +
                ", imagenRuta='" + imagenRuta + '\'' +
                '}';
    }
}
