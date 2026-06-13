package com.nekomart.utils;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import java.awt.Image;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilidad para cargar imágenes desde URLs o archivos locales
 * Incluye caché, reintentos, carga asíncrona y placeholder.
 */
public class ImageLoader {
    
    // Caché en memoria para evitar descargar la misma imagen varias veces
    private static final ConcurrentHashMap<String, ImageIcon> cache = new ConcurrentHashMap<>();
    
    // Placeholder estático
    private static ImageIcon placeholderIcon = null;

    /**
     * Obtiene el placeholder por defecto
     */
    private static ImageIcon getPlaceholder(int ancho, int alto) {
        if (placeholderIcon == null) {
            try {
                // Intentar cargar el archivo desde resources
                URL placeholderUrl = ImageLoader.class.getResource("/imagenes/placeholder.png");
                if (placeholderUrl != null) {
                    Image img = ImageIO.read(placeholderUrl);
                    placeholderIcon = new ImageIcon(img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
                } else {
                    // Fallback a un archivo local temporal (por si el classpath falla durante el dev)
                    File f = new File("src/main/resources/imagenes/placeholder.png");
                    if(f.exists()){
                        Image img = ImageIO.read(f);
                        placeholderIcon = new ImageIcon(img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
                    }
                }
            } catch (Exception e) {
                System.err.println("No se pudo cargar el placeholder.");
            }
        }
        return placeholderIcon;
    }

    /**
     * Carga una imagen de forma síncrona (con reintentos y caché)
     */
    public static ImageIcon cargarImagen(String ruta, int ancho, int alto) {
        if (ruta == null || ruta.isEmpty()) {
            return getPlaceholder(ancho, alto);
        }

        String cacheKey = ruta + "_" + ancho + "x" + alto;
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        
        ImageIcon icon = null;
        if (ruta.startsWith("http")) {
            int maxIntentos = 3;
            for (int i = 0; i < maxIntentos; i++) {
                try {
                    URL url = new URL(ruta);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000); // 5 segundos de timeout
                    connection.setReadTimeout(5000);
                    connection.setRequestMethod("GET");
                    
                    int status = connection.getResponseCode();
                    if (status == HttpURLConnection.HTTP_OK) {
                        Image img = ImageIO.read(connection.getInputStream());
                        if (img != null) {
                            Image escalada = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                            icon = new ImageIcon(escalada);
                            cache.put(cacheKey, icon);
                            return icon;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Intento " + (i + 1) + " fallido para " + ruta + ": " + e.getMessage());
                    if (i == maxIntentos - 1) {
                        System.err.println("Se agotaron los reintentos para: " + ruta);
                    }
                }
            }
        } else {
            // Archivo local
            try {
                File file = new File(ruta);
                if (file.exists()) {
                    Image img = ImageIO.read(file);
                    if (img != null) {
                        Image escalada = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(escalada);
                        cache.put(cacheKey, icon);
                        return icon;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al cargar archivo local " + ruta + ": " + e.getMessage());
            }
        }
        
        // Si todo falla, devolver placeholder
        return getPlaceholder(ancho, alto);
    }
    
    /**
     * Carga imagen con tamaño por defecto (100x100)
     */
    public static ImageIcon cargarImagen(String ruta) {
        return cargarImagen(ruta, 100, 100);
    }

    /**
     * Carga una imagen de forma asíncrona y la asigna al JLabel cuando esté lista.
     * Muestra el placeholder mientras se carga.
     * 
     * @param ruta Ruta o URL de la imagen
     * @param ancho Ancho deseado
     * @param alto Alto deseado
     * @param label Componente JLabel donde se mostrará
     */
    public static void cargarImagenAsync(String ruta, int ancho, int alto, JLabel label) {
        if (ruta == null || ruta.isEmpty()) {
            label.setIcon(getPlaceholder(ancho, alto));
            return;
        }

        String cacheKey = ruta + "_" + ancho + "x" + alto;
        if (cache.containsKey(cacheKey)) {
            label.setIcon(cache.get(cacheKey));
            return;
        }

        // Poner placeholder mientras carga
        label.setIcon(getPlaceholder(ancho, alto));

        // Iniciar carga en background
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                // cargarImagen maneja los reintentos, el timeout y el caché internamente
                return cargarImagen(ruta, ancho, alto);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon result = get();
                    if (result != null) {
                        label.setIcon(result);
                    }
                } catch (Exception e) {
                    System.err.println("Error en SwingWorker al cargar la imagen: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
