package com.caio.pdf_conversions_api.Conversions.XLS.Sony;

import com.caio.pdf_conversions_api.Conversions.XLS.BaseXlsConversion;
import com.caio.pdf_conversions_api.Conversions.XLS.FieldValue;
import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;

import java.util.*;
import java.util.regex.Pattern;

public class SonyMusicPublishing extends BaseXlsConversion {

    protected boolean isIndexWithArtist;
    protected boolean isIndexWithGravadora;
    protected boolean isIndexWithShow;
    protected boolean doVerificationNextLine;
    protected String tipoExecucao;
    protected Map<String, Set<SonyTypes>> foundedTypes = new HashMap<>();

    public SonyMusicPublishing(String pdfPath, String[] filesToConvert) {
        super(pdfPath, filesToConvert);
    }

    @Override
    protected void convertFiles() {
        super.convertFiles();
        this.printFoundReport();
    }

    @Override
    protected void setConfigFileName() {
        this.configFileName = "/SonyMusicPublishingConfig.json";
    }

    @Override
    protected FieldValue processDocumentType(String docType) {
        if (docType.equals("Extrato - Execução Pública Exterior")){
            return SonyTypes.EXTRATO_EPX;
        } else if (docType.equals("Extrato - Execução Pública")){
            return SonyTypes.EXTRATO_EP;
        }
        return SonyTypes.EXTRATO_EX;
    }

    @Override
    public void setDatePatterns() {
        this.datePatterns = new LinkedHashMap<>(){
            {
                put(Pattern.compile("(\\d{2}/\\d{2}/\\d{4}) a (\\d{2}/\\d{2}/\\d{4})"), Pair.of(1,1));
            }
        };
    }

    @Override
    protected void setIndexLine() {
        this.indexLine = new Object[]{
                "Pais",
                "Fonte Pagadora",
                "Tipo de Execução",
                "Referencia Autoral",
                "Interprete",
                "Nome Show",
                "Tipo",
                "Artista",
                "Titulo",
                "Qtd",
                "Vlr Rendimento",
                "Tx Royalty",
                "Vlr Liquido",
                "Data",
                "Arquivo"
        };
    }

    @Override
    protected void processSheetRow(Row row, Map<String, String> currentDocumentType, FieldValue documentType, String date) throws Exception {//        if (currentDocumentType.)
        if (documentType == SonyTypes.EXTRATO_EX){
            processExtrato(row, currentDocumentType, date);
        } else if (documentType == SonyTypes.EXTRATO_EP){
            processExtratoEP(row, currentDocumentType, date);
        } else if (documentType == SonyTypes.EXTRATO_EPX){
            processExtratoEPX(row, currentDocumentType, date);
        }
        addFoundedType(date, (SonyTypes) documentType);
    }

    @Override
    protected boolean isLineUnwanted(Row row) {
        return this.unwantedLines.stream().anyMatch(line -> row.getCell(0).getStringCellValue().contains(line));
    }

    @Override
    protected void setUnwantedLines() {
        this.unwantedLines = List.of(
//                "FONTE PAGADORA"
        );
    }

    private void processExtrato(Row row, Map<String, String> currentDocumentType, String date) throws Exception {
        if (isLineInvalidExtrato(row, currentDocumentType)) return;

        if (isLineWithTipoExecucao(row)){
            this.setTipoExecucao(row);
            return;
        }

        if (isLineIndexLine(row)){
            setIndexWithArtistOrGravadora(row, currentDocumentType);
            return;
        }

        String fontePagadora = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.FONTE_PAGADORA_LINE).trim();
        String tipo = this.getCellStringValueFromRow(row, currentDocumentType, this.isIndexWithGravadora ? SonyMusicPublishingFields.TIPO_WITH_GRAVADORA_LINE : SonyMusicPublishingFields.TIPO_LINE).trim();

        String titulo;
        if (this.isIndexWithArtist) {
            titulo = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.TITULO_WITH_ARTIST_LINE).trim();
        } else {
            titulo = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.TITULO_OBRA_LINE).trim();
        }
        Object qtd;
        try {
            qtd = Integer.parseInt(Helper.corrigeSeparadorInt(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.QTD_LINE)).trim());
        } catch (NumberFormatException e) {
            qtd = "";
        }
        double vlrRendimento = Helper.ajustaNumero(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.VLR_RENDIMENTO_LINE).trim());
        double txRoyalty = Helper.ajustaNumero(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.ROYALTY_LINE).trim().replace("%", ""));
        double vlrLiquido = Helper.ajustaNumero(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.VLR_LIQUIDO_LINE).trim());


        this.addResult(
                new Object[]{
                        "",
                        fontePagadora,
                        this.tipoExecucao,
                        "",
                        "",
                        "",
                        this.isIndexWithArtist ? "" : tipo,
                        this.isIndexWithArtist ? tipo : "",
                        titulo,
                        qtd,
                        vlrRendimento,
                        txRoyalty,
                        vlrLiquido,
                        date,
                        fileName
                }, vlrLiquido
        );
    }

    private void processExtratoEP(Row row, Map<String, String> currentDocumentType, String date) throws Exception {
        if (isLineInvalidExtratoEP(row)) return;

        if (doVerificationNextLine){
            this.doVerification(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.VLR_LIQUIDO_LINE).trim(), date);
            return;
        }

        if (isLineWithTipoExecucao(row)){
            this.setTipoExecucao(row);
            return;
        }

        if (isLineIndexLineEP(row)){
            setIndexWithShow(row, currentDocumentType);
            return;
        }

        if (isVerificationLine(row, currentDocumentType)){
            this.doVerificationNextLine = true;
            return;
        }

        String tipo = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.TIPO_LINE).trim();
        String titulo = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.TITULO_OBRA_LINE).trim();
        String referenciaAutoral = this.getCellStringValueFromRow(row, currentDocumentType, this.isIndexWithShow ? SonyMusicPublishingFields.REFERENCIA_AUTORAL_COM_PERIODO_SHOW : SonyMusicPublishingFields.REFERENCIA_AUTORAL_LINE).trim();
        String interprete = this.getCellStringValueFromRow(row, currentDocumentType, this.isIndexWithShow ? SonyMusicPublishingFields.INTERPRETE_LINE_COM_SHOW : SonyMusicPublishingFields.INTERPRETE_LINE).trim();
        String nomeShow = this.isIndexWithShow ? this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.NOME_SHOW_LINE).trim() : "";

        int qtd = Integer.parseInt(Helper.corrigeSeparadorInt(this.getCellStringValueFromRow(row, currentDocumentType, this.isIndexWithShow ? SonyMusicPublishingFields.QTD_LINE_COM_SHOW : SonyMusicPublishingFields.QTD_LINE)).trim());
        double vlrRendimento = Helper.ajustaNumero(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.VLR_RENDIMENTO_LINE).trim());
        double txRoyalty = Helper.ajustaNumero(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.ROYALTY_LINE).trim().replace("%", ""));
        double vlrLiquido = Helper.ajustaNumero(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.VLR_LIQUIDO_LINE).trim());

        this.addResult(
                new Object[]{
                        "",
                        "",
                        this.tipoExecucao,
                        referenciaAutoral,
                        interprete,
                        nomeShow,
                        tipo,
                        "",
                        titulo,
                        qtd,
                        vlrRendimento,
                        txRoyalty,
                        vlrLiquido,
                        date,
                        this.fileName
                }, vlrLiquido
        );
    }

    private void processExtratoEPX(Row row, Map<String, String> currentDocumentType, String date) throws Exception {
        if (isLineInvalidExtratoEP(row)) return;

        if (doVerificationNextLine){
            this.doVerification(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.VLR_LIQUIDO_LINE).trim(), date);
            return;
        }

        if (isLineWithTipoExecucao(row)){
            this.setTipoExecucao(row);
            return;
        }

        if (isLineIndexLineEP(row)){
//            setIndexWithShow(row, currentDocumentType);
            return;
        }

        if (isVerificationLine(row, currentDocumentType)){
            this.doVerificationNextLine = true;
            return;
        }

        String pais = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.PAIS_LINE).trim();
        String fontePagadora = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.FONTE_PAGADORA_LINE).trim();
        String tipo = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.TIPO_LINE).trim();

        String titulo = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.TITULO_OBRA_LINE).trim();
        String referenciaAutoral = this.getCellStringValueFromRow(row, currentDocumentType, this.isIndexWithShow ? SonyMusicPublishingFields.REFERENCIA_AUTORAL_COM_PERIODO_SHOW : SonyMusicPublishingFields.REFERENCIA_AUTORAL_LINE).trim();


        int qtd = Integer.parseInt(Helper.corrigeSeparadorInt(this.getCellStringValueFromRow(row, currentDocumentType, this.isIndexWithShow ? SonyMusicPublishingFields.QTD_LINE_COM_SHOW : SonyMusicPublishingFields.QTD_LINE)).trim());
        double vlrRendimento = Helper.ajustaNumero(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.VLR_RENDIMENTO_LINE).trim());
        double txRoyalty = Helper.ajustaNumero(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.ROYALTY_LINE).trim().replace("%", ""));
        double vlrLiquido = Helper.ajustaNumero(this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.VLR_LIQUIDO_LINE).trim());

        this.addResult(
                new Object[]{
                        pais,
                        fontePagadora,
                        this.tipoExecucao,
                        referenciaAutoral,
                        "",
                        "",
                        tipo,
                        "",
                        titulo,
                        qtd,
                        vlrRendimento,
                        txRoyalty,
                        vlrLiquido,
                        date,
                        this.fileName
                }, vlrLiquido
        );
    }

    protected boolean isVerificationLine(Row row, Map<String, String> currentDocumentType){
        try {
            String cellStringValue = this.getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.VLR_LIQUIDO_LINE).trim();
            return cellStringValue.equalsIgnoreCase("Total Líquido");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addFoundedType(String date, SonyTypes type) throws Exception {
        if(this.foundedTypes.containsKey(date)){
            Set<SonyTypes> existingTypes = this.foundedTypes.get(date);
            existingTypes.add(type);
        } else {
            Set<SonyTypes> types = new HashSet<>();
            types.add(type);
            this.foundedTypes.put(date, types);
        }
    }

    public void printFoundReport() {
        // Get all SonyTypes values
        Set<SonyTypes> allSonyTypes = EnumSet.allOf(SonyTypes.class);

        // Iterate over the map entries
        for (Map.Entry<String, Set<SonyTypes>> entry : foundedTypes.entrySet()) {
            String key = entry.getKey();
            Set<SonyTypes> typesSet = entry.getValue();

            // Find the missing SonyTypes by subtracting the set of current types from all types
            Set<SonyTypes> missingTypes = new HashSet<>(allSonyTypes);
            missingTypes.removeAll(typesSet);

            // If there are missing types, print the key and the missing types
            if (!missingTypes.isEmpty()) {
                System.out.println("Date: " + key + " is missing the following SonyTypes: " + missingTypes);
            }
        }
    }

    private void setTipoExecucao(Row row) throws Exception {
        this.tipoExecucao = getCellStringValueFromRow(row, 1).trim();
    }

    private void setIndexWithArtistOrGravadora(Row row, Map<String, String> currentDocumentType) throws Exception {
        this.isIndexWithArtist = getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.TIPO_LINE).equalsIgnoreCase("ARTISTA");
        this.isIndexWithGravadora = getCellStringValueFromRow(row, 0).equalsIgnoreCase("GRAVADORA");
    }

    private void setIndexWithShow(Row row, Map<String, String> currentDocumentType) throws Exception {
        this.isIndexWithShow = getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.QTD_LINE).equalsIgnoreCase("SHOW");
    }

    private boolean isLineWithTipoExecucao(Row row) throws Exception {
        String firstCell = getCellStringValueFromRow(row, 0);
        return firstCell.matches("\\d{2}/\\d{2}/\\d{4}");
    }

    private boolean isLineInvalidExtrato(Row row, Map<String, String> currentDocumentType) throws Exception {
        if (row.getLastCellNum() == 1) return true;
        String firstCell = getCellStringValueFromRow(row, 0);
        String rowWithDate = getCellStringValueFromRow(row, currentDocumentType, SonyMusicPublishingFields.TITULO_WITH_ARTIST_LINE);
        return firstCell.equalsIgnoreCase("EDITORA")
                || firstCell.equalsIgnoreCase("Data")
                || rowWithDate.matches("\\d{2}/\\d{2}/\\d{4} a \\d{2}/\\d{2}/\\d{4}");
    }

    private boolean isLineInvalidExtratoEP(Row row) throws Exception {
        if (row.getLastCellNum() == 1) return true;
        String firstCell = getCellStringValueFromRow(row, 0);
        return firstCell.contains("PERIODO DE");
    }

    private boolean isLineIndexLine(Row row) throws Exception {
        return getCellStringValueFromRow(row, 0).equalsIgnoreCase("FONTE PAGADORA") ||
                getCellStringValueFromRow(row, 0).equalsIgnoreCase("GRAVADORA");
    }

    private boolean isLineIndexLineEP(Row row) throws Exception {
        return getCellStringValueFromRow(row, 0).equalsIgnoreCase("COMPETÊNCIA");
    }

    private Object getCellValueFromRow(Row row, int index, CellType expectedType) throws Exception {
        Cell cell = row.getCell(index);
        CellType cellType = cell.getCellType();

        if (cell == null || cellType == CellType.BLANK) {
            return expectedType == CellType.STRING ? "" : 0D; // Retorna valor padrão para tipo esperado
        }

        if (cellType != expectedType) {
            throw new Exception("Expected cell type " + expectedType + " but found " + cellType);
        }

        return expectedType == CellType.STRING ? cell.getStringCellValue() : cell.getNumericCellValue();
    }

    private String getCellStringValueFromRow(Row row, Map<String, String> currentDocumentType, FieldValue fieldEnum) throws Exception {
        int index = CellReference.convertColStringToIndex(currentDocumentType.get(fieldEnum.getValue()));
        return (String) getCellValueFromRow(row, index, CellType.STRING);
    }

    private String getCellStringValueFromRow(Row row, int index) throws Exception {
        return (String) getCellValueFromRow(row, index, CellType.STRING);
    }

    private Double getCellNumericValueFromRow(Row row, Map<String, String> currentDocumentType, FieldValue fieldEnum) throws Exception {
        int index = CellReference.convertColStringToIndex(currentDocumentType.get(fieldEnum.getValue()));
        return (Double) getCellValueFromRow(row, index, CellType.NUMERIC);
    }

    @Override
    protected void resetDocumentVariables() {
        super.resetDocumentVariables();
        this.isIndexWithArtist = false;
        this.isIndexWithGravadora = false;
        this.isIndexWithShow = false;
        this.doVerificationNextLine = false;
        this.tipoExecucao = null;
    }
}
