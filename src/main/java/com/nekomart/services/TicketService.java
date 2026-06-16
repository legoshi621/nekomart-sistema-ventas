package com.nekomart.services;

import com.nekomart.models.Venta;
import com.nekomart.models.DetalleVenta;
import com.nekomart.models.Producto;

import java.awt.*;
import java.awt.print.*;
import java.util.List;

/**
 * Servicio para generar e imprimir tickets de venta.
 * Utiliza la API nativa de Java (java.awt.print), no requiere librerías externas.
 */
public class TicketService implements Printable {

    private Venta venta;
    private List<DetalleVenta> detalles;
    private String nombreEmpleado;
    private ProductoService productoService;

    public TicketService(Venta venta, List<DetalleVenta> detalles, String nombreEmpleado) {
        this.venta = venta;
        this.detalles = detalles;
        this.nombreEmpleado = nombreEmpleado;
        this.productoService = new ProductoService();
    }

    /**
     * Método principal para iniciar el proceso de impresión.
     */
    public void imprimirTicket() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);

        // Mostrar diálogo de selección de impresora
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                System.err.println("Error al imprimir el ticket: " + e.getMessage());
            }
        }
    }

    /**
     * Dibuja el contenido del ticket en la página.
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE; // Solo imprimimos 1 página
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // Configuración de fuentes
        Font fuenteNormal = new Font("Monospaced", Font.PLAIN, 10);
        Font fuenteNegrita = new Font("Monospaced", Font.BOLD, 12);
        Font fuenteTitulo = new Font("Monospaced", Font.BOLD, 14);

        g2d.setFont(fuenteTitulo);
        g2d.drawString("NekoMart", 40, 20);
        
        g2d.setFont(fuenteNormal);
        g2d.drawString("Sistema de Ventas POS", 30, 35);
        g2d.drawString("--------------------------------", 10, 50);

        g2d.drawString("Folio: " + venta.getFolio(), 10, 65);
        g2d.drawString("Fecha: " + venta.getFecha(), 10, 80);
        g2d.drawString("Cajero: " + nombreEmpleado, 10, 95);
        g2d.drawString("--------------------------------", 10, 110);

        // Encabezados de productos
        g2d.drawString("Producto", 10, 125);
        g2d.drawString("Cant", 130, 125);
        g2d.drawString("Precio", 160, 125);
        g2d.drawString("Subtotal", 200, 125);
        g2d.drawString("------------------------------------------------", 10, 135);

        int yPos = 150;
        double total = 0;

        // Listar productos
        for (DetalleVenta d : detalles) {
            Producto p = productoService.obtenerPorId(d.getProductoId());
            String nombre = p != null ? p.getNombre() : "Producto #" + d.getProductoId();
            
            // Truncar nombre si es muy largo
            if (nombre.length() > 15) {
                nombre = nombre.substring(0, 12) + "...";
            }

            double subtotal = d.getCantidad() * d.getPrecioUnitario();
            total += subtotal;

            g2d.drawString(nombre, 10, yPos);
            g2d.drawString(String.valueOf(d.getCantidad()), 135, yPos);
            g2d.drawString(String.format("$%.2f", d.getPrecioUnitario()), 160, yPos);
            g2d.drawString(String.format("$%.2f", subtotal), 200, yPos);
            
            yPos += 15;
        }

        g2d.drawString("------------------------------------------------", 10, yPos + 5);
        yPos += 20;

        // Total
        g2d.setFont(fuenteNegrita);
        g2d.drawString("TOTAL: " + String.format("$%.2f", venta.getTotal()), 10, yPos);
        g2d.setFont(fuenteNormal);
        yPos += 20;

        g2d.drawString("Método Pago: " + venta.getMetodoPago(), 10, yPos);
        yPos += 20;
        g2d.drawString("--------------------------------", 10, yPos);
        yPos += 20;

        g2d.drawString("¡Gracias por su compra!", 30, yPos);
        yPos += 15;
        g2d.drawString("www.nekomart.com", 45, yPos);

        return PAGE_EXISTS;
    }
}