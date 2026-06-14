package com.nekomart;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.nekomart.ui.LoginFrame;
import com.nekomart.utils.DisenoSystem;

import javax.swing.*;
import java.awt.*;

/**
 * Clase principal (punto de entrada) de la aplicación NekoMart.
 * Encargada de inicializar el tema visual con soporte para modo claro/oscuro
 * y lanzar la primera pantalla (Login).
 * Paleta de colores POS profesional aplicada globalmente.
 * Todo el código está comentado en español.
 */
public class Main {

    // Variable global para controlar si el modo oscuro está activo
    public static boolean isDarkMode = false;

    /**
     * Método principal que se ejecuta al lanzar el sistema.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        // Inicializar el tema claro por defecto al arrancar
        aplicarTemaClaro();

        /*
         * Usar SwingUtilities.invokeLater para asegurar que la creación y
         * manipulación de la interfaz gráfica (GUI) ocurra dentro del
         * Event Dispatch Thread (EDT), previniendo así errores de concurrencia.
         */
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Crear y hacer visible la pantalla de inicio de sesión
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }

    /**
     * Configura y aplica el Tema Oscuro (FlatDarkLaf) con colores personalizados.
     */
    public static void aplicarTemaOscuro() {
        try {
            FlatDarkLaf.setup();

            // Configuración de colores personalizados de la paleta oscura
            UIManager.put("Panel.background", new Color(0x1E, 0x1E, 0x1E)); // Fondo #1E1E1E
            UIManager.put("TableHeader.background", new Color(0x2D, 0x2D, 0x2D)); // Cabecera oscura
            UIManager.put("TableHeader.foreground", Color.WHITE); // Texto blanco
            UIManager.put("Table.background", new Color(0x2D, 0x2D, 0x2D)); // Fondo tabla oscuro
            UIManager.put("Table.foreground", Color.WHITE); // Texto blanco
            UIManager.put("Label.foreground", Color.WHITE); // Texto etiquetas blanco
            
            // Botones azul primario de la nueva paleta con texto blanco
            UIManager.put("Button.background", DisenoSystem.AZUL_PRIMARIO);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.arc", 8);
            
            UIManager.put("Component.arc", 8);
            UIManager.put("TextField.arc", 8);
            UIManager.put("Component.focusWidth", 2);
            UIManager.put("Component.focusColor", DisenoSystem.AZUL_CLARO); // Focus azul POS
        } catch (Exception ex) {
            System.err.println("No se pudo establecer FlatDarkLaf: " + ex.getMessage());
        }
    }

    /**
     * Configura y aplica el Tema Claro (FlatLightLaf) con colores personalizados POS.
     */
    public static void aplicarTemaClaro() {
        try {
            FlatLightLaf.setup();

            // Configuración de colores personalizados de la paleta POS
            UIManager.put("Panel.background", DisenoSystem.FONDO_PRINCIPAL); // Fondo general
            UIManager.put("TableHeader.background", DisenoSystem.AZUL_PRIMARIO); // Cabecera azul primario
            UIManager.put("TableHeader.foreground", Color.WHITE); // Texto blanco
            UIManager.put("Table.background", DisenoSystem.BLANCO); // Fondo tabla blanco
            UIManager.put("Table.foreground", DisenoSystem.GRIS_OSCURO); // Texto principal
            UIManager.put("Label.foreground", DisenoSystem.GRIS_OSCURO); // Texto etiquetas
            
            // Botones azul primario de la nueva paleta con texto blanco
            UIManager.put("Button.background", DisenoSystem.AZUL_PRIMARIO);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.arc", 8);
            
            UIManager.put("Component.arc", 8);
            UIManager.put("TextField.arc", 8);
            UIManager.put("Component.focusWidth", 2);
            UIManager.put("Component.focusColor", DisenoSystem.AZUL_CLARO); // Focus azul POS
        } catch (Exception ex) {
            System.err.println("No se pudo establecer FlatLightLaf: " + ex.getMessage());
        }
    }

    /**
     * Alterna entre el tema claro y el oscuro y refresca todas las ventanas abiertas.
     */
    public static void cambiarTema() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            aplicarTemaOscuro();
        } else {
            aplicarTemaClaro();
        }

        // Actualizar la apariencia de todos los componentes en todas las ventanas abiertas
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
