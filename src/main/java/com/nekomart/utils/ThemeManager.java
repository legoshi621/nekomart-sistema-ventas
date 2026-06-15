package com.nekomart.utils;

import java.awt.Color;
import java.awt.Font;

public class ThemeManager {
    // Colores principales
    public static final Color AZUL_PRIMARIO = new Color(30, 136, 229);      // #1E88E5 - Botones, sidebar activo
    public static final Color AZUL_OSCURO = new Color(21, 101, 192);        // #1565C0 - Hover botones
    public static final Color AZUL_CLARO = new Color(135, 206, 250);        // #87CEFA - Acentos
    public static final Color AZUL_MUY_CLARO = new Color(227, 242, 253);    // #E3F2FD - Fondos suaves

    public static final Color GRIS_OSCURO = new Color(30, 41, 59);          // #1E293B - Sidebar, texto principal
    public static final Color GRIS_MEDIO = new Color(100, 116, 139);        // #64748B - Texto secundario
    public static final Color GRIS_CLARO = new Color(203, 213, 225);        // #CBD5E1 - Bordes
    public static final Color GRIS_MUY_CLARO = new Color(241, 245, 249);    // #F1F5F9 - Fondos secciones

    public static final Color FONDO_PRINCIPAL = new Color(248, 250, 252);   // #F8FAFC - Área contenido
    public static final Color BLANCO = new Color(255, 255, 255);            // #FFFFFF - Tarjetas

    public static final Color EXITO = new Color(16, 185, 129);              // #10B981 - Verde
    public static final Color ADVERTENCIA = new Color(245, 158, 11);        // #F59E0B - Ámbar
    public static final Color PELIGRO = new Color(239, 68, 68);             // #EF4444 - Rojo
    
    // Fuentes
    public static final Font TITULO_GRANDE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font TITULO_SECCION = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBTITULO = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font TEXTO_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font TEXTO_PEQUENO = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BOTON = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font KPI_VALOR = new Font("Segoe UI", Font.BOLD, 28);
}
