package com.nekomart;

import com.formdev.flatlaf.FlatLightLaf;
import com.nekomart.ui.LoginFrame;

import javax.swing.*;

/**
 * Clase principal (punto de entrada) de la aplicación NekoMart.
 * Encargada de inicializar el tema visual y lanzar la primera pantalla.
 * Todo el código está comentado en español.
 */
public class Main {

    /**
     * Método principal que se ejecuta al lanzar el sistema.
     * 
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {

        /*
         * Configurar FlatLaf como Look & Feel antes de instanciar cualquier ventana.
         * Se utiliza FlatLightLaf para un diseño claro, moderno y consistente.
         */
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("No se pudo establecer el Look and Feel de FlatLaf:");
            ex.printStackTrace();
        }

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
}
