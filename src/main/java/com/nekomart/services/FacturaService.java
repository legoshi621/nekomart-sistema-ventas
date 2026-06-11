package com.nekomart.services;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.nekomart.models.DetalleVenta;
import com.nekomart.models.Venta;
import com.nekomart.dao.ProductoDAO;
import com.nekomart.models.Producto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Servicio para generación de facturas en formato PDF usando iText 7.
 * Crea un PDF profesional con diseño corporativo NekoMart (#1e3a8a).
 * Todo el código está comentado en español.
 */
public class FacturaService {

    // ── Colores corporativos NekoMart ─────────────────────────────────────
    /** Azul corporativo oscuro: #1e3a8a */
    private static final DeviceRgb AZUL_CORP    = new DeviceRgb(30, 58, 138);
    /** Azul corporativo claro para encabezado de tabla */
    private static final DeviceRgb AZUL_TABLA   = new DeviceRgb(59, 130, 246);
    /** Fondo blanco para filas pares */
    private static final DeviceRgb BLANCO       = new DeviceRgb(255, 255, 255);
    /** Fondo gris suave para filas alternas */
    private static final DeviceRgb GRIS_SUAVE   = new DeviceRgb(241, 245, 249);
    /** Verde para el total final */
    private static final DeviceRgb VERDE_TOTAL  = new DeviceRgb(22, 163, 74);
    /** Blanco puro para texto sobre fondo oscuro */
    private static final DeviceRgb BLANCO_TEXTO = new DeviceRgb(255, 255, 255);

    private final ProductoDAO productoDAO;

    public FacturaService() {
        this.productoDAO = new ProductoDAO();
    }

    /**
     * Genera un PDF de factura profesional para la venta dada.
     *
     * @param venta    Objeto Venta con folio, fecha, totales y método de pago.
     * @param detalles Lista de DetalleVenta con productos, cantidades y precios.
     * @return Ruta absoluta del archivo PDF generado, o null si ocurrió un error.
     */
    public String generarPDF(Venta venta, List<DetalleVenta> detalles) {
        try {
            // ── Crear directorio "facturas/" si no existe ─────────────────
            Files.createDirectories(Paths.get("facturas"));
            String rutaArchivo = "facturas" + File.separator + "factura_" + venta.getFolio() + ".pdf";

            // ── Inicializar documento PDF ──────────────────────────────────
            PdfWriter   writer   = new PdfWriter(rutaArchivo);
            PdfDocument pdfDoc   = new PdfDocument(writer);
            Document    document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(36, 50, 36, 50);

            // ── Fuentes ───────────────────────────────────────────────────
            PdfFont fuenteNormal  = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fuenteNegrita = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // ════════════════════════════════════════════════════════════════
            // SECCIÓN 1: ENCABEZADO (Logo textual + datos de empresa)
            // ════════════════════════════════════════════════════════════════
            // Nombre de empresa estilizado (sin emoji: Helvetica no soporta Unicode extendido)
            Paragraph encabezado = new Paragraph("NekoMart")
                    .setFont(fuenteNegrita)
                    .setFontSize(28)
                    .setFontColor(AZUL_CORP)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2);
            document.add(encabezado);

            document.add(new Paragraph("Sistema de Ventas Profesional")
                    .setFont(fuenteNormal)
                    .setFontSize(11)
                    .setFontColor(new DeviceRgb(100, 116, 139))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));

            // Línea separadora azul gruesa bajo el encabezado
            SolidLine lineaAzul = new SolidLine(2f);
            lineaAzul.setColor(AZUL_CORP);
            document.add(new LineSeparator(lineaAzul).setMarginBottom(12));

            // ════════════════════════════════════════════════════════════════
            // SECCIÓN 2: DATOS DE LA FACTURA (folio, fecha, método de pago)
            // ════════════════════════════════════════════════════════════════
            Table tablaInfo = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(16);

            // Columna izquierda: FACTURA + folio
            Cell celdaTitulo = new Cell()
                    .add(new Paragraph("FACTURA")
                            .setFont(fuenteNegrita).setFontSize(18).setFontColor(AZUL_CORP))
                    .add(new Paragraph("Folio: " + venta.getFolio())
                            .setFont(fuenteNormal).setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

            // Columna derecha: fecha y método de pago (alineado a la derecha)
            Cell celdaFecha = new Cell()
                    .add(new Paragraph("Fecha:")
                            .setFont(fuenteNegrita).setFontSize(10).setFontColor(AZUL_CORP))
                    .add(new Paragraph(venta.getFecha())
                            .setFont(fuenteNormal).setFontSize(10))
                    .add(new Paragraph("Método de pago: " + venta.getMetodoPago())
                            .setFont(fuenteNormal).setFontSize(10).setMarginTop(4))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT);

            tablaInfo.addCell(celdaTitulo);
            tablaInfo.addCell(celdaFecha);
            document.add(tablaInfo);

            // ════════════════════════════════════════════════════════════════
            // SECCIÓN 3: TABLA DE PRODUCTOS
            // ════════════════════════════════════════════════════════════════
            // Encabezado de la tabla con fondo azul
            float[] anchoColumnas = {3f, 1.2f, 1.5f, 1.5f};
            Table tablaProductos = new Table(UnitValue.createPercentArray(anchoColumnas))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            String[] encabezados = {"Producto", "Cantidad", "Precio Unit.", "Subtotal"};
            for (String enc : encabezados) {
                tablaProductos.addHeaderCell(
                    new Cell()
                        .add(new Paragraph(enc)
                                .setFont(fuenteNegrita)
                                .setFontSize(10)
                                .setFontColor(BLANCO_TEXTO))
                        .setBackgroundColor(AZUL_TABLA)
                        .setPadding(6)
                        .setTextAlignment(TextAlignment.CENTER)
                );
            }

            // Filas de productos con colores alternos
            double subtotalGlobal = 0;
            for (int i = 0; i < detalles.size(); i++) {
                DetalleVenta d = detalles.get(i);
                // Usar DeviceRgb explícito para evitar incompatibilidad de tipos con Color
                DeviceRgb fondoFila = (i % 2 == 0) ? BLANCO : GRIS_SUAVE;

                // Obtener nombre del producto
                String nombreProducto = "Producto #" + d.getProductoId();
                try {
                    Producto p = productoDAO.buscarPorId(d.getProductoId());
                    if (p != null) nombreProducto = p.getNombre();
                } catch (Exception ex) {
                    // Si falla la consulta, se usa el ID como fallback
                }

                double subtotal = d.getCantidad() * d.getPrecioUnitario();
                subtotalGlobal += subtotal;

                // Celda: nombre del producto
                tablaProductos.addCell(new Cell()
                        .add(new Paragraph(nombreProducto).setFont(fuenteNormal).setFontSize(10))
                        .setBackgroundColor(fondoFila).setPadding(5)
                        .setBorderLeft(new SolidBorder(AZUL_CORP, 0.5f)));

                // Celda: cantidad
                tablaProductos.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(d.getCantidad())).setFont(fuenteNormal).setFontSize(10))
                        .setBackgroundColor(fondoFila).setPadding(5)
                        .setTextAlignment(TextAlignment.CENTER));

                // Celda: precio unitario
                tablaProductos.addCell(new Cell()
                        .add(new Paragraph(String.format("$%.2f", d.getPrecioUnitario())).setFont(fuenteNormal).setFontSize(10))
                        .setBackgroundColor(fondoFila).setPadding(5)
                        .setTextAlignment(TextAlignment.RIGHT));

                // Celda: subtotal
                tablaProductos.addCell(new Cell()
                        .add(new Paragraph(String.format("$%.2f", subtotal)).setFont(fuenteNegrita).setFontSize(10))
                        .setBackgroundColor(fondoFila).setPadding(5)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorderRight(new SolidBorder(AZUL_CORP, 0.5f)));
            }
            document.add(tablaProductos);

            // ════════════════════════════════════════════════════════════════
            // SECCIÓN 4: RESUMEN DE TOTALES
            // ════════════════════════════════════════════════════════════════
            Table tablaTotal = new Table(UnitValue.createPercentArray(new float[]{3f, 1.5f}))
                    .setWidth(UnitValue.createPercentValue(60))
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setMarginBottom(24);

            // Subtotal
            tablaTotal.addCell(crearCeldaResumen("Subtotal:", fuenteNormal, false));
            tablaTotal.addCell(crearCeldaResumen(String.format("$%.2f", subtotalGlobal), fuenteNormal, false));

            // Método de pago
            if (venta.getMetodoPago().equalsIgnoreCase("Efectivo")) {
                tablaTotal.addCell(crearCeldaResumen("Monto recibido:", fuenteNormal, false));
                tablaTotal.addCell(crearCeldaResumen(String.format("$%.2f", venta.getMontoRecibido()), fuenteNormal, false));
                tablaTotal.addCell(crearCeldaResumen("Cambio:", fuenteNormal, false));
                tablaTotal.addCell(crearCeldaResumen(String.format("$%.2f", venta.getCambio()), fuenteNormal, false));
            }

            // TOTAL (resaltado)
            Cell celdaLblTotal = new Cell()
                    .add(new Paragraph("TOTAL:")
                            .setFont(fuenteNegrita).setFontSize(14).setFontColor(BLANCO_TEXTO))
                    .setBackgroundColor(VERDE_TOTAL).setPadding(8)
                    .setBorder(Border.NO_BORDER);
            Cell celdaValTotal = new Cell()
                    .add(new Paragraph(String.format("$%.2f", venta.getTotal()))
                            .setFont(fuenteNegrita).setFontSize(14).setFontColor(BLANCO_TEXTO))
                    .setBackgroundColor(VERDE_TOTAL).setPadding(8)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER);
            tablaTotal.addCell(celdaLblTotal);
            tablaTotal.addCell(celdaValTotal);
            document.add(tablaTotal);

            // ════════════════════════════════════════════════════════════════
            // SECCIÓN 5: PIE DE PÁGINA
            // ════════════════════════════════════════════════════════════════
            SolidLine lineaPie = new SolidLine(0.5f);
            lineaPie.setColor(new DeviceRgb(203, 213, 225));
            document.add(new LineSeparator(lineaPie).setMarginBottom(8));

            document.add(new Paragraph("Gracias por su compra en NekoMart")
                    .setFont(fuenteNormal).setFontSize(10)
                    .setFontColor(new DeviceRgb(100, 116, 139))
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Este documento es una representación impresa de su comprobante de compra.")
                    .setFont(fuenteNormal).setFontSize(8)
                    .setFontColor(new DeviceRgb(148, 163, 184))
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();

            System.out.println("PDF generado: " + rutaArchivo);
            return new File(rutaArchivo).getAbsolutePath();

        } catch (IOException e) {
            System.err.println("Error al generar PDF: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea una celda de la tabla de resumen de totales con estilo uniforme.
     */
    private Cell crearCeldaResumen(String texto, PdfFont fuente, boolean esTotal) {
        Cell celda = new Cell()
                .add(new Paragraph(texto).setFont(fuente).setFontSize(11))
                .setPadding(4)
                .setBorder(new SolidBorder(new DeviceRgb(203, 213, 225), 0.5f))
                .setTextAlignment(TextAlignment.RIGHT);
        return celda;
    }
}
