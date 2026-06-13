package com.nekomart.services;

import com.nekomart.models.Producto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;

/**
 * Servicio para exportar datos del sistema a archivos Excel (.xlsx).
 * Utiliza Apache POI para la generación de hojas de cálculo.
 */
public class ExcelService {

    /**
     * Exporta una lista de productos al formato Excel (.xlsx).
     * Genera un archivo con encabezados estilizados y datos del inventario.
     *
     * @param productos   Lista de productos a exportar
     * @param rutaArchivo Ruta completa donde se guardará el archivo Excel
     * @return true si la exportación fue exitosa, false en caso de error
     */
    public boolean exportarInventario(List<Producto> productos, String rutaArchivo) {
        try {
            // Crear libro de trabajo Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Inventario NekoMart");

            // Estilo para encabezado (fondo lavanda, texto blanco, negrita)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);

            // Crear fila de encabezado con los nombres de las columnas
            String[] headers = {"Código", "Nombre", "Categoría", "Precio", "Stock", "Lote", "Fecha Caducidad"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agregar datos de cada producto como una fila
            int rowNum = 1;
            for (Producto p : productos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getCodigo());
                row.createCell(1).setCellValue(p.getNombre());
                row.createCell(2).setCellValue(p.getCategoria());
                row.createCell(3).setCellValue(p.getPrecio());
                row.createCell(4).setCellValue(p.getStock());
                row.createCell(5).setCellValue(p.getLote() != null ? p.getLote() : "");
                row.createCell(6).setCellValue(p.getFechaCaducidad() != null ? p.getFechaCaducidad() : "");
            }

            // Ajustar automáticamente el ancho de cada columna al contenido
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar el archivo Excel en la ruta especificada
            FileOutputStream fileOut = new FileOutputStream(rutaArchivo);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            return true;
        } catch (Exception e) {
            System.err.println("Error al exportar Excel: " + e.getMessage());
            return false;
        }
    }
}
