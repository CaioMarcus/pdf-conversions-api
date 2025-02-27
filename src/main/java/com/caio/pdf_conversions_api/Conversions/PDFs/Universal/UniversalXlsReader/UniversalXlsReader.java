/*
package com.caio.pdf_conversions_api.Conversions.Universal.UniversalXlsReader;

import com.caio.pdf_conversions_api.Conversions.Universal.UniversalXlsReader.Lines.Line;
import com.caio.pdf_conversions_api.Helpers.Helper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;


public class UniversalXlsReader {
    private final String xlsPath;
    private final String outputPath;
    private final String outputFileName;
    private List<UniversalResult> result;

    //Document Type Control
    private Map<String, List<Map<String, String>>> documentTypes;
    private List<Map<String, String>> documentTypeList;
    private TreeMap<Integer, String> summaryPositions;

    public UniversalXlsReader(String outputFileName, String xlsPath, String outputPath) {
        this.readJsonAndSetFields();
        this.documentTypeList = null;
        this.outputFileName = outputFileName;
        this.xlsPath = xlsPath;
        this.outputPath = outputPath;
    }

    public void convertFiles() {
        File folder = new File(this.xlsPath);
        String[] files = folder.list();
        if (files == null) return;

        String failedFilesPath = "D:\\Conversoes\\Nao Rodaram";
        File failedFolder = new File(failedFilesPath);
        if (!failedFolder.exists()) {
            failedFolder.mkdirs();
        }

        this.result = new ArrayList<>();
        tryConversion(files, 1, failedFilesPath);

        try {
            this.exportData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void tryConversion(String[] files, int jsonField, String failedFilesPath) {
        for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
            String fileName = files[fileIndex];
            try {

                UniversalResult documentResult = readAndWriteToJson(Paths.get(this.xlsPath, fileName).toString());
                if (documentResult == null)
                    throw new RuntimeException();
                this.result.add(documentResult);
            } catch (Exception e) {
//                try {
//                    Files.move(Paths.get(this.xlsPath, fileName), Paths.get(failedFilesPath, fileName), StandardCopyOption.REPLACE_EXISTING);
//                    System.out.println("Failed File: " + fileName);
//                } catch (IOException ioException) {
//                    ioException.printStackTrace();
//                }
            }
        }
    }

    private void moveFilesBackToOriginalFolder(String failedFilesPath) {
        File failedFolder = new File(failedFilesPath);
        String[] failedFiles = failedFolder.list();
        if (failedFiles != null) {
            for (String fileName : failedFiles) {
                try {
                    Files.move(Paths.get(failedFilesPath, fileName), Paths.get(this.xlsPath, fileName), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getBeneficiario(Sheet sheet){
        for (Row row : sheet){
            for (Cell cell : row){
                if (cell.getCellType() == CellType.STRING && (
                        cell.getStringCellValue().contains("Beneficiário: ") ||
                        cell.getStringCellValue().contains("Beneficiario: "))){

                    return cell.getStringCellValue()
                            .replace("Beneficiario: ", "")
                            .replace("Beneficiário: ", "")
                            .trim();
                }
            }
        }
        System.out.println("Failed to get Beneficiario");
        throw new RuntimeException("Failed to get Beneficiario");
    }

    private String getDate(Sheet sheet){
        for (Row row : sheet){
            for (Cell cell : row){
                if (cell.getCellType() == CellType.STRING && (
                        cell.getStringCellValue().toUpperCase().contains("PERÍODO : ") ||
                        cell.getStringCellValue().toUpperCase().contains("PERIODO : ") ||
                        cell.getStringCellValue().toUpperCase().contains("PERÍODO: ") ||
                        cell.getStringCellValue().toUpperCase().contains("PERIODO: "))){

                    return cell.getStringCellValue()
                            .replace("PERÍODO: ", "")
                            .replace("PERIODO: ", "")
                            .replace("PERÍODO : ", "")
                            .replace("PERIODO : ", "")
                            .trim();
                }
            }
        }
        System.out.println("Failed to get Date");
        throw new RuntimeException("Failed to get Date");
    }


    private boolean verificaArquivoDuplicado(UniversalResult document){
        List<UniversalResult> arquivosDuplicados = this.result.stream().filter(doc ->
                doc.getBeneficiario().equals(document.getBeneficiario()) &&
                doc.getTipo().equals(document.getTipo()) &&
                doc.getDate().equals(document.getDate()) &&
                doc.getSumValue().equals(document.getSumValue())
        ).collect(Collectors.toList());

        boolean arquivoDuplicado = arquivosDuplicados.size() > 0;

        return arquivoDuplicado;
    }

    private UniversalResult readAndWriteToJson(String filePath) {
        System.out.println("Reading File: " + filePath);
        try (FileInputStream file = new FileInputStream(filePath); Workbook workbook = WorkbookFactory.create(file)) {
            // Assuming there is only one sheet, change if there are multiple sheets
            Sheet sheet = workbook.getSheetAt(0);

            // Getting and Setting document type name
            String documentTypeName = this.getDocumentTypeName(sheet);
            String documentType = documentTypeName.replace(" ", "").toUpperCase();
            this.documentTypeList = this.documentTypes.get(documentType);

            // Getting Beneficiario
            String beneficiario = getBeneficiario(sheet);
            // Getting Date
            String date = getDate(sheet);

            int tries = 0;
            while (tries < this.documentTypeList.size()) {
                try {
                    Map<String, String> currentDocumentType = this.documentTypeList.get(tries);

                    //Criando documento de resultado e setando informações básicas
                    UniversalResult documentResult = new UniversalResult();
                    documentResult.setBeneficiario(beneficiario);
                    documentResult.setTipo(documentTypeName.replace("Relatório de Pagamentos :", "").trim());
                    documentResult.setDate(date);

                    String totalField = currentDocumentType.get(Field.TOTAL.getValue());
                    boolean totalFixed = totalField.matches("[A-Z]\\d");

                    if (totalFixed) {
                        Cell totalCell = sheet.getRow(Integer.parseInt(totalField.substring(1, 2)) - 1).getCell(CellReference.convertColStringToIndex(totalField.substring(0, 1)));
                        if (totalCell.getCellType() == CellType.STRING)
                            documentResult.setTotalValue(Double.parseDouble(totalCell.getStringCellValue().replace(",", ".")));
                        else
                            documentResult.setTotalValue(totalCell.getNumericCellValue());
                    } else if (currentDocumentType.get(Field.TOTAL.getValue()).equals("null")) {
                        documentResult.setTotalValue(0D);
                        documentResult.setValueStatus("TOTAL NÃO INFORMADO");
                    }



                    for (int i = Integer.parseInt(currentDocumentType.get(Field.START_LINE.getValue())) - 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);

                        if (!this.isValidRow(row)) continue;

                        if (!totalFixed && (
                                row.getCell(row.getFirstCellNum()).getStringCellValue().replace(" ", "").equalsIgnoreCase("TOTALDIREITOS:") ||
                                row.getCell(row.getFirstCellNum()).getStringCellValue().replace(" ", "").contains("TOTAL")
                        )) {
                            Row nextRow = sheet.getRow(++i);
                            double documentValue = nextRow.getCell(nextRow.getFirstCellNum()).getNumericCellValue();

                            documentResult.setTotalValue(documentValue);
                            if (withinErrorMargin(documentValue, documentResult.getSumValue(), 5))
                                documentResult.setValueStatus("VALORES BATERAM");
                            else
                                documentResult.setValueStatus("VALORES NÃO BATERAM");

                            break;
                        }

                        Line line = new Line();
                        if (row.getLastCellNum() < 5)
                            continue;
                        // Extracting values from specific columns
                        String ref = "";
                        try {
                            ref = row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.REF.getValue()))).getStringCellValue();
                        } catch (Exception e) {
                            System.out.println("Test");
                        }

                        // Rarely, the type can come as a number
                        String type = "";
                        try {
                            Cell typeCell = row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.TYPE.getValue())));
                            if (typeCell.getCellType() == CellType.NUMERIC)
                                type = String.valueOf(typeCell.getNumericCellValue());
                            else
                                type = row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.TYPE.getValue()))).getStringCellValue();
                        } catch (Exception e) {

                        }
                        String format = "";
                        try {
                            format = row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.FORMAT.getValue()))).getStringCellValue();
                        } catch (Exception e) {

                        }
                        String album;
                        try {
                            album = row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.ALBUM.getValue()))).getStringCellValue();
                        } catch (NullPointerException e) {
                            album = "";
                        }
                        String songName = row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.SONG_NAME.getValue()))).getStringCellValue();
                        double price = row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.PRICE.getValue()))).getNumericCellValue();
                        int amount = 0;
                        try {
                            amount = (int) row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.QUANTITY.getValue()))).getNumericCellValue();
                        } catch (Exception e) {
                            try {
                                amount = (int) Helper.ajustaNumero(row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.QUANTITY.getValue()))).getStringCellValue().trim());
                            } catch (Exception ex) {
//                                ex.printStackTrace();
                            }
                        }
                        double royaltyPercentage = row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.ROYALTY_PERCENTAGE.getValue()))).getNumericCellValue();
                        double dueAmount = row.getCell(CellReference.convertColStringToIndex(currentDocumentType.get(Field.DUE_AMOUNT.getValue()))).getNumericCellValue();

                        // Create a Line object and set the values using setters


                        line.setRef(ref);
                        line.setType(type);
                        line.setFormat(format);
                        line.setAlbum(album);
                        line.setSongName(songName);
                        line.setPrice(price);
                        line.setAmount(amount);
                        line.setRoyaltyPercentage(royaltyPercentage);
                        line.setDueAmount(dueAmount);

                        // Adding entry to the list
                        documentResult.addLine(line);
                    }

                    if (totalFixed) {
                        if (withinErrorMargin(documentResult.getTotalValue(), documentResult.getSumValue(), 5))
                            documentResult.setValueStatus("VALORES BATERAM");
                        else
                            documentResult.setValueStatus("VALORES NÃO BATERAM");
                    }

                    //Verificando duplicatas. Precisa estar após os valores serem lidos.
                    if (verificaArquivoDuplicado(documentResult)){
                        System.out.println("Arquivo Duplicado Encontrado. Ignorando");
                        return null;
                    } else {
                        System.out.println("Success");
                        return documentResult;
                    }
                } catch (Exception e) {
                    tries++;
//                    e.printStackTrace();
                }
            }
            System.out.println("FILE FAILED");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean withinErrorMargin(double originalValue, double givenValue, double errorMarginPercentage) {
        // Calculate the acceptable range
        double errorMargin = originalValue * (errorMarginPercentage / 100.0);
        double lowerBound = originalValue - errorMargin;
        double upperBound = originalValue + errorMargin;

        // Check if the given value falls within the acceptable range
        return (givenValue >= lowerBound) && (givenValue <= upperBound);
    }

    private void exportData() throws IOException {
        System.out.println("Exporting Data");
        // Writing entries to JSON file
        try (Writer writer = new FileWriter(Path.of(this.outputPath, this.outputFileName + ".json").toString())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this.result, writer);
        }
    }


    private boolean isValidRow(Row row) {
        for (Cell cell : row) {
            CellType cellType = cell.getCellType();
            if (cellType == CellType.NUMERIC || (cellType == CellType.STRING && !cell.getStringCellValue().isEmpty()))
                return true;
        }
        return false;
    }

    private List<Cell> getValidCells(Row row) {
        List<Cell> validCells = new ArrayList<>();
        for (Cell cell : row) {
            if (cell != null)
                validCells.add(cell);
        }
        return validCells;
    }

    private void setSummaryPositions(Row summaryRow) {
        for (Cell cell : summaryRow) {
            if (cell == null) continue;
            this.summaryPositions.put(cell.getColumnIndex(), cell.getStringCellValue().replace(System.lineSeparator(), " "));
        }
    }

    private void readJsonAndSetFields() {
        ObjectMapper objectMapper = new ObjectMapper();
        documentTypes = new HashMap<>();
        try {
            InputStream inputStream = getClass().getResourceAsStream("/SonyMusicPublishingConfig.json");
            this.documentTypes = objectMapper.readValue(inputStream, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDocumentTypeName(Sheet documentSheet) {
        for (Row row : documentSheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    if (cellValue.toUpperCase().replace(" ", "").contains("RELATÓRIODEPAGAMENTOS")) {
                        String documentTypeName = cellValue;
                        return documentTypeName;
                    }
                }
            }
        }
        return null;
    }

}
*/
