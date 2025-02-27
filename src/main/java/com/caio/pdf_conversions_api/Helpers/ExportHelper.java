package com.caio.pdf_conversions_api.Helpers;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;


public class ExportHelper {
    // Initialize with your data

    public static void exportData(List<Object[]> resultados, List<Object[]> verificacao, String savePath, String saveName) {
        try (SXSSFWorkbook documento = new SXSSFWorkbook(100)) { // Keeping 100 rows in memory
            criaPlanilhaEAdicionaDados(documento, resultados, verificacao);
            try (FileOutputStream out = new FileOutputStream(savePath + saveName + ".xlsx")) {
                documento.write(out);
            }
            documento.dispose(); // Clear temporary files
            System.out.println("Conversão concluída com êxito. Nome do arquivo salvo: " + saveName + ".xlsx");
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error while exporting data", e);
        }
    }

    public static void exportToCSV(List<Object[]> data, String filePath) throws IOException, ParseException {
        // Create a BufferedWriter to write to the CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            // Iterate over the data list
            for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
                Object[] rowData = data.get(rowIndex);

                // Iterate over each cell in the row
                for (int colIndex = 0; colIndex < rowData.length; colIndex++) {
                    Object cellData = rowData[colIndex];

                    if (!(cellData instanceof String)) cellData = String.valueOf(cellData);

                    // Write the cell data to the CSV, ensuring proper escaping for special characters
                    writer.write(escapeCSV((String) cellData));

                    // Add a comma between values in the same row (except for the last column)
                    if (colIndex < rowData.length - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();  // New line after each row
            }
        }
    }

    // Utility method to escape CSV special characters (e.g., commas, quotes)
    private static String escapeCSV(String data) {
        if (data.isEmpty())
            return "''";

        // If the data contains commas, quotes, or newlines, escape it
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            data = "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }

    public static void criaPlanilhaEAdicionaDados(SXSSFWorkbook documento, List<Object[]> resultados, List<Object[]> verificacao) throws ParseException {
        // Reuse styles to save memory
        CellStyle defaultStyle = createDefaultStyle(documento);
        CellStyle headerStyle = createHeaderStyle(documento);
        CellStyle dateStyle = createDateStyle(documento);
        CellStyle numberStyle = createNumberStyle(documento);

        // Create sheets
        Sheet planilhaPdfs = documento.createSheet(WorkbookUtil.createSafeSheetName("PDFs"));
        Sheet planilhaVerificacao = documento.createSheet(WorkbookUtil.createSafeSheetName("Verificação"));

        // Populate the sheets
        populateSheet(planilhaPdfs, resultados, defaultStyle, headerStyle, dateStyle, numberStyle);

        if (verificacao != null)
            populateSheet(planilhaVerificacao, verificacao, defaultStyle, headerStyle, dateStyle, numberStyle);

        // Optionally set column widths manually (avoid autoSizeColumn for performance)
        for (int i = 0; i < 10; i++) { // Adjust `10` based on expected columns
            planilhaPdfs.setColumnWidth(i, 4000); // Set fixed width
            planilhaVerificacao.setColumnWidth(i, 4000);
        }
    }

    private static CellStyle createDefaultStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createDateStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createNumberStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static void populateSheet(Sheet sheet, List<Object[]> data, CellStyle defaultStyle, CellStyle headerStyle, CellStyle dateStyle, CellStyle numberStyle) throws ParseException {
        int rowIndex = 0;

        // Create a light gray style for striping
        CellStyle stripedStyle = sheet.getWorkbook().createCellStyle();
        stripedStyle.cloneStyleFrom(defaultStyle);
        stripedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        stripedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (Object[] rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(15);


            for (int colIndex = 0; colIndex < rowData.length; colIndex++) {
                Cell cell = row.createCell(colIndex);
                Object cellData = rowData[colIndex];

                if (rowIndex == 1) { // Header row
                    cell.setCellStyle(headerStyle);
                    cell.setCellValue((String) cellData);
                } else if (cellData instanceof String date && isDate(date)) { // Date
                    cell.setCellStyle(dateStyle);
                    cell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").parse(date));
                } else {
                    if (cellData instanceof String value){
                        cell.setCellStyle(defaultStyle);
                        cell.setCellValue(value);
                    } else if (cellData instanceof Double value){
                        cell.setCellStyle(numberStyle);
                        cell.setCellValue(value);
                    } else if (cellData instanceof Integer value){
                        cell.setCellStyle(numberStyle);
                        cell.setCellValue(value);
                    }
                }
            }
        }
    }

    private static boolean isDate(String value) {
        return value.matches("\\d{2}/\\d{2}/\\d{4}");
    }

    private static boolean isNumber(String value) {
        return value.matches("-?[0-9]+(\\.[0-9]*)?");
    }
}
