package com.nekomart.services;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.io.*;

/**
 * Servicio para la impresión de archivos PDF en NekoMart.
 * Usa PrinterJob de Java con diálogo de selección de impresora.
 * Como alternativa robusta, también ofrece apertura del PDF con
 * el visor del sistema (que permite imprimir desde allí).
 * Todo el código está comentado en español.
 */
public class ImpresionService {

    /**
     * Imprime el archivo PDF usando el mecanismo nativo de Java.
     * Muestra el diálogo de impresora y envía el trabajo al spool.
     *
     * @param rutaPDF Ruta absoluta o relativa del archivo PDF a imprimir.
     * @return true si el trabajo de impresión se envió correctamente.
     */
    public boolean imprimirPDF(String rutaPDF) {
        File archivoPDF = new File(rutaPDF);

        // Verificar que el archivo existe
        if (!archivoPDF.exists()) {
            mostrarError("No se encontró el archivo PDF:\n" + archivoPDF.getAbsolutePath());
            return false;
        }

        try {
            // ── Intentar impresión directa con javax.print ─────────────────
            FileInputStream fis = new FileInputStream(archivoPDF);
            DocFlavor flavor    = DocFlavor.INPUT_STREAM.PDF;

            // Buscar impresoras que soporten PDF directamente
            PrintService[] servicios = PrintServiceLookup.lookupPrintServices(flavor, null);

            if (servicios.length > 0) {
                // Mostrar diálogo de selección de impresora
                PrintRequestAttributeSet atribs = new HashPrintRequestAttributeSet();
                atribs.add(new Copies(1));
                atribs.add(new JobName("Factura NekoMart - " + archivoPDF.getName(), null));

                PrintService seleccionado = ServiceUI.printDialog(
                        null, 200, 200, servicios, servicios[0], flavor, atribs);

                if (seleccionado != null) {
                    DocPrintJob trabajo = seleccionado.createPrintJob();
                    Doc documento = new SimpleDoc(fis, flavor, null);
                    trabajo.print(documento, atribs);
                    fis.close();
                    return true;
                } else {
                    // El usuario canceló el diálogo
                    fis.close();
                    return false;
                }
            } else {
                // No hay impresoras con soporte PDF directo — abrir con visor del sistema
                fis.close();
                return abrirConVisorSistema(archivoPDF);
            }

        } catch (PrintException | IOException e) {
            System.err.println("Error de impresión directa: " + e.getMessage());
            // Fallback: abrir con el visor del sistema operativo
            return abrirConVisorSistema(archivoPDF);
        }
    }

    /**
     * Abre el PDF con el visor predeterminado del sistema operativo
     * (Acrobat Reader, Edge, SumatraPDF, etc.), desde donde el usuario
     * puede imprimir usando Ctrl+P.
     *
     * @param archivoPDF Archivo PDF a abrir.
     * @return true si el visor se pudo abrir correctamente.
     */
    public boolean abrirConVisorSistema(File archivoPDF) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivoPDF);

                // Mostrar aviso informativo
                JOptionPane.showMessageDialog(null,
                        "El PDF se abrió en el visor de PDF de tu sistema.\n"
                        + "Usa Ctrl+P dentro del visor para imprimir.",
                        "PDF Abierto",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                mostrarError("La apertura automática de archivos no está disponible en este sistema.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error al abrir PDF con visor: " + e.getMessage());
            mostrarError("No se pudo abrir el PDF automáticamente.\n"
                    + "Ruta del archivo:\n" + archivoPDF.getAbsolutePath());
            return false;
        }
    }

    /**
     * Muestra un mensaje de error con diseño NekoMart.
     *
     * @param mensaje Texto del mensaje de error.
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null,
                mensaje,
                "Error de Impresión",
                JOptionPane.ERROR_MESSAGE);
    }
}
