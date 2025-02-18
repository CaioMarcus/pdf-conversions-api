package com.caio.pdf_conversions_api.BaseDocumentReader;

import com.caio.pdf_conversions_api.Helpers.ConversionDateParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DocumentProcessor extends ConversionDateParser implements DocumentConverter {
    protected PDDocument currentDocument;
    protected String pdfPath;
    protected String savePath;
    protected String saveName;
    protected String[] filesPath;
    protected String[] lines;
    protected String[] linhasIndice;
    protected String[] linhasNaoPegar;
    protected boolean skipPage;
    protected boolean skipDocument;
    protected int conversionStartPage = 0;
    // File Specific
    protected String currentFileName;
    protected int currentPage;
    protected int lineIndex;
    protected Double documentTotal = 0D;
//    protected PDDocument currentPdfDocument;

    // Data
    protected List<StripperRegion> stripperRegions = new ArrayList<>();
    protected String mainRegion;
    protected Map<String, String[]> regionsMap = new HashMap<>();
    protected Map<String, String> columnsData = new HashMap<>();


    protected List<String[]> resultados = new ArrayList<>();
    protected List<String[]> verificacao = new ArrayList<>();

    public DocumentProcessor(String pdfPath, String savePath, String saveName) {
        this.pdfPath = pdfPath;
        this.savePath = savePath;
        this.saveName = saveName;

        this.stripperRegions = new ArrayList<>();

        this.readJsonAndSetFields();
    }

    @Override
    public void convertDocuments() {
        this.filesPath = new File(pdfPath).list();
        startProcessDocumentsThread();
    }

    protected void startProcessDocumentsThread() {
        for (String file : filesPath) {
            this.currentFileName = file;
            try (PDDocument document = Loader.loadPDF(new File(Path.of(pdfPath, file).toString()))) {
                this.currentDocument = document;
                this.beforeProcessDocument(document);
                this.processDocument(file, document);
                this.afterProcessDocument(document);

            } catch (Exception e) {
                System.out.println("Failed to process document: " + file);
                throw new RuntimeException(e);
            }
        }
        this.afterProcessingDocuments();
    }

    protected void afterProcessingDocuments(){
        this.exportData();
    }

    protected void beforeProcessDocument(PDDocument document) {

    }

    protected void processDocument(String file, PDDocument document) throws Exception {
        int totalPages = document.getNumberOfPages();
        for (int idxPage = conversionStartPage; idxPage < totalPages; idxPage++){
            this.currentPage = idxPage;
            System.out.printf("Reading page %d of %d\n", (idxPage + 1), totalPages);
            PDPage page = document.getPage(idxPage);

            beforeProcessPage(page);
            processPage(page);
            afterProcessPage(page);

            if (skipDocument){
                skipDocument = false;
                break;
            }
        }
    }

    protected void afterProcessDocument(PDDocument document) {

    }


    // Pages

    protected void beforeProcessPage(PDPage page) throws Exception {
        if (this.mainRegion == null){
            throw new NullPointerException("Main region is null");
        }
        if (this.stripperRegions == null || this.stripperRegions.isEmpty()){
            throw new Exception("Strippers are null or empty");
        }

        for (StripperRegion region : stripperRegions) {
            this.regionsMap.putAll(region.getRegionsContent(page));
        }
    }

    protected void processPage(PDPage page) {
        lines = this.regionsMap.get(this.mainRegion);
        for (lineIndex = 0; lineIndex < lines.length; lineIndex++) {

            String line = lines[lineIndex];
            String[] lineSeparated = line.split(" ");
            beforeProcessLine(lineIndex, line, lineSeparated);
            processLine(lineIndex, line, lineSeparated);
            afterProcessLine(lineIndex, line, lineSeparated);

            if (this.skipPage){
                this.skipPage = false;
                return;
            }
        }
    }

    protected void afterProcessPage(PDPage page) {

    }


    // Line

    protected void beforeProcessLine(int lineIndex, String line, String[] lineSeparated) {

    }

    protected void processLine(int lineIndex, String line, String[] lineSeparated) {

    }

    protected void afterProcessLine(int lineIndex, String line, String[] lineSeparated) {

    }

    private void readDocuments(){
        File filesFolder = new File(pdfPath);
        this.filesPath = filesFolder.list();
    }

    protected boolean checkValuesBetween(Double valueCalculated, Double valueToCheck, Double percentage){
        return Math.abs(valueToCheck - valueCalculated) < Math.abs(valueToCheck * percentage / 100);
    }

    protected void readJsonAndSetFields() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map columnsData = getMapFromJson(objectMapper, "/NomesColunas.json");
            this.columnsData = columnsData;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Map getMapFromJson(ObjectMapper objectMapper, String file) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(file);
        Map jsonData = objectMapper.readValue(inputStream, Map.class);
        return jsonData;
    }

    protected Double dist(Double numero) {
        return (numero * 72) / 25.4;
    }

    protected void skipPageOnNextLoop(){
        this.skipPage = true;
    }

    protected void skipDocument(){
        this.skipDocument = true;
    }

    protected String findSubstring(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public boolean containsPattern(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }


}
