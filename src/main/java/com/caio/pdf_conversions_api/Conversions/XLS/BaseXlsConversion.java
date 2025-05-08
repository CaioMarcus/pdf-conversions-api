package com.caio.pdf_conversions_api.Conversions.XLS;

import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.LineData;
import com.caio.pdf_conversions_api.Conversions.ConversionThread;
import com.caio.pdf_conversions_api.Exceptions.ConversionException;
import com.caio.pdf_conversions_api.Helpers.Helper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseXlsConversion extends ConversionThread {

    protected String configFileName;
    protected Object[] indexLine;
    protected List<String> unwantedLines;
    protected Map<String, List<Map<String, String>>> documentTypes;
    protected List<Map<String, String>> documentTypeList;
    protected final String docTypeCell = "A4";
    protected double currentSum;
    protected double acceptableDifferencePercentage = 5;
    protected String fileName;

    protected BaseXlsConversion(String pdfPath, String[] conversionToConvert) {
        super(pdfPath, conversionToConvert);
        this.setConfigFileName();
        this.readJsonAndSetFields();
        this.setUnwantedLines();
        this.setIndexLine();
        this.resultados.add(indexLine);
    }

    @Override
    public void setDatePatterns() {

    }

    @Override
    public void run() {
        try {
            this.convertFiles();
        } catch (Exception e) {
            this.conversionProgress = -1f;
            if (e instanceof ConversionException)
                this.error = e.getMessage();
            throw new RuntimeException(e);
        }
    }

    protected void convertFiles(){
        for (String arquivo : this.arquivosNaPasta) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            this.fileName = arquivo;
            String filePath = Path.of(this.pdfPath, arquivo).toString();
            System.out.println("Converting File: " + arquivo);
            this.convertFile(filePath, arquivo);
        }
    }

    protected void convertFile(String filePath, String fileName) {
        try (BufferedInputStream bufferedFile = new BufferedInputStream(new FileInputStream(filePath));
            Workbook workbook = WorkbookFactory.create(bufferedFile)) {
                processWorkbook(workbook, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void processWorkbook(Workbook workbook, String fileName) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        FieldValue documentType = this.getDocumentTypeName(sheet);

        this.documentTypeList = this.documentTypes.get(documentType.getValue());

        for (Map<String, String> currentDocumentType : this.documentTypeList) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            try {
                resetDocumentVariables();
                processSheet(sheet, currentDocumentType, documentType);
                return;
            } catch (Exception e) {
                System.out.println("File Failed: " + fileName);
                e.printStackTrace();
            }
        }
    }

    protected void processSheet(Sheet sheet, Map<String, String> currentDocumentType, FieldValue documentType) throws Exception {
        String date = getDate(sheet, currentDocumentType);
        for (int lineIdx = Integer.parseInt(currentDocumentType.get(BaseField.START_LINE.getValue())) - 1; lineIdx <= sheet.getLastRowNum(); lineIdx++) {
            Row row = sheet.getRow(lineIdx);
            if (isLineUnwanted(row)) continue;
            processSheetRow(row, currentDocumentType, documentType, date);
        }
    }

    protected String getDate(Sheet sheet, Map<String, String> currentDocumentType) throws Exception {
        return convertDate(getTextValueFromCell(BaseField.DATE, sheet, currentDocumentType));
    }

    protected FieldValue getDocumentTypeName(Sheet sheet) throws Exception {
        String docType = getTextValueFromCell(sheet, this.docTypeCell);
        return this.processDocumentType(docType);
    }

    private String getTextValueFromCell(FieldValue baseFieldEnum, Sheet sheet, Map<String, String> currentDocumentType) throws Exception {
        String cellPosition = currentDocumentType.get(baseFieldEnum.getValue());
        Cell dateCell = getCellFromPosition(sheet, cellPosition);
        if (dateCell.getCellType() != CellType.STRING) throw new Exception("Invalid Cell Type");
        return dateCell.getStringCellValue();
    }

    private String getTextValueFromCell(Sheet sheet, String cellPosition) throws Exception {
        Cell dateCell = getCellFromPosition(sheet, cellPosition);
        if (dateCell.getCellType() != CellType.STRING) throw new Exception("Invalid Cell Type");
        return dateCell.getStringCellValue();
    }


    protected void readJsonAndSetFields() {
        ObjectMapper objectMapper = new ObjectMapper();
        documentTypes = new HashMap<>();
        try {
            InputStream inputStream = getClass().getResourceAsStream(this.configFileName);
            this.documentTypes = objectMapper.readValue(inputStream, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void addResult(Object[] result, Double value){
        this.resultados.add(result);
        currentSum += value;
    }

    protected Cell getCellFromPosition(Sheet sheet, String position){
        int rowIndex = Integer.parseInt(position.substring(1, 2)) - 1;
        int colIndex = CellReference.convertColStringToIndex(position.substring(0, 1));
        return sheet.getRow(rowIndex).getCell(colIndex);
    }

    protected void resetDocumentVariables(){
        this.currentSum = 0D;
    }

    protected void doVerification(String documentGivenValue,String date){
        double documentGivenValueDouble = Helper.ajustaNumero(documentGivenValue);
        String verificationResult = "VALORES BATERAM";

        if (!Helper.isClose(documentGivenValueDouble, this.currentSum, this.acceptableDifferencePercentage / 100)) {
            verificationResult = "VALORES NÃO BATERAM";
        }

        this.verificacao.add(new String[]{
                verificationResult,
                "INFORMADO:",
                String.valueOf(documentGivenValueDouble),
                "CALCULADO:",
                String.valueOf(this.currentSum),
                "DATA:", date,
        });
    }

    protected abstract void setConfigFileName();
    protected abstract FieldValue processDocumentType(String docType);
    protected abstract void processSheetRow(Row row, Map<String, String> currentDocumentType, FieldValue documentType, String date) throws Exception;
    protected abstract boolean isLineUnwanted(Row row);
    protected abstract void setUnwantedLines();
    protected abstract void setIndexLine();
    protected abstract boolean isVerificationLine(Row row, Map<String, String> currentDocumentType);
}
