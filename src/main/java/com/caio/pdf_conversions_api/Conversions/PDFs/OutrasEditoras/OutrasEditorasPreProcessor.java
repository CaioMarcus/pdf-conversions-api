package com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras;

import com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras.Models.OutrasEditorasColumn;
import com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras.Models.OutrasEditorasDocumento;
import com.caio.pdf_conversions_api.Conversions.PDFs.Position;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutrasEditorasPreProcessor {

    private static final double BASE_WIDTH = 1000;
    private static List<String> indexWords;
    private static String indexLineCommomWord = "Obra";

    private static final Map<String, OutrasEditorasColumn> columnsConverter;

    static {
        readJsonAndSetFields();
        columnsConverter = new HashMap<>();
        columnsConverter.put("Produto", OutrasEditorasColumn.PRODUTO);
        columnsConverter.put("Obra", OutrasEditorasColumn.OBRA);
        columnsConverter.put("Repassante", OutrasEditorasColumn.REPASSANTE);
        columnsConverter.put("Vendas", OutrasEditorasColumn.VENDAS);
        columnsConverter.put("Vendas/Views", OutrasEditorasColumn.VENDAS);
        columnsConverter.put("QtdeRef.", OutrasEditorasColumn.VENDAS);
        columnsConverter.put("Vlr.Unit", OutrasEditorasColumn.VLR_UNIT);
        columnsConverter.put("Vlr.Arrec", OutrasEditorasColumn.VLR_UNIT);
        columnsConverter.put("Valor", OutrasEditorasColumn.VLR_UNIT);
        columnsConverter.put("BaseCálc.", OutrasEditorasColumn.VLR_UNIT);
        columnsConverter.put("%", OutrasEditorasColumn.PERCENTAGE);
        columnsConverter.put("%Aut.", OutrasEditorasColumn.PERCENTAGE);
        columnsConverter.put("%Fin", OutrasEditorasColumn.PERCENTAGE);
        columnsConverter.put("Repasse", OutrasEditorasColumn.REPASSE);
        columnsConverter.put("Autor", OutrasEditorasColumn.REPASSE);
        columnsConverter.put("Editora", OutrasEditorasColumn.IGNORE);

        columnsConverter.put("TitulodaObra", OutrasEditorasColumn.OBRA);
        columnsConverter.put("ValorTitular", OutrasEditorasColumn.REPASSE);
        columnsConverter.put("Qtde", OutrasEditorasColumn.VENDAS);
        columnsConverter.put("%Perc.", OutrasEditorasColumn.PERCENTAGE);
        columnsConverter.put("Preço", OutrasEditorasColumn.IGNORE);
        columnsConverter.put("%Taxa", OutrasEditorasColumn.IGNORE);
        columnsConverter.put("%Controle", OutrasEditorasColumn.IGNORE);
        columnsConverter.put("ValorReceb.", OutrasEditorasColumn.IGNORE);

        columnsConverter.put("Valorbruto", OutrasEditorasColumn.IGNORE);
        columnsConverter.put("VlrAutor", OutrasEditorasColumn.REPASSE);
        columnsConverter.put("%Editora", OutrasEditorasColumn.IGNORE);
        columnsConverter.put("VlrEditora", OutrasEditorasColumn.IGNORE);
    }

    public static OutrasEditorasDocumento getOutrasEditorasDocumentoAjustado(int precision, PDDocument document){
        try {

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            PDPage pageToCalibrate = document.getPage(0);
            double increaseAmount = (double) 1 / precision;

            // Get the index line (Produto, Obra, Repassante, etc), mostly because of the Y position.
            Rectangle2D.Double rectTudo = getIndexLineRectangle(pageToCalibrate, increaseAmount);

            Map<OutrasEditorasColumn, Position> columns = getColumnsFromIndexLine(pageToCalibrate, increaseAmount, stripper, rectTudo);
//            removeEmptyColumns(pageToCalibrate, rectTudo.y, rectTudo.height, increaseAmount, rectTudo, columns);

//            double documentHeight = getRectHeightWithAllLines(pageToCalibrate, increaseAmount, stripper, rectTudo);
//
//            rectTudo.x = columns.values().stream().min(Comparator.comparingDouble(ColunaOutrasEditoras::getX)).get().getX();
//            rectTudo.y = rectTudo.getY() + rectTudo.getHeight();
//            rectTudo.width = BASE_WIDTH;
//            rectTudo.height = documentHeight - rectTudo.getHeight();
//
//            // Correcting Columns
//            for (Map.Entry<String, ColunaOutrasEditoras> stringColunaOutrasEditorasEntry : columns.entrySet()) {
//                stringColunaOutrasEditorasEntry.getValue().setY(rectTudo.y);
//                stringColunaOutrasEditorasEntry.getValue().setHeight(rectTudo.height);
//            }

//            String[] columnsIndex = getColumnsIndexLine(columns);
            return new OutrasEditorasDocumento(columns);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*private String[] getColumnsIndexLine(Map<String, ColunaOutrasEditoras> columns){
        List<String> columnsName = new ArrayList<>();
        for (Map.Entry<String, ColunaOutrasEditoras> stringColunaOutrasEditorasEntry : columns.entrySet()) {
            columnsName.add(columnsData.get(stringColunaOutrasEditorasEntry.getKey().toUpperCase()));
        }

        columnsName.add(columnsData.get("REPASSE"));
        columnsName.add(columnsData.get("FONTE"));
        columnsName.add(columnsData.get("DATA"));

        columnsName.add("DOCUMENTO");
        return columnsName.toArray(String[]::new);
    }*/

    private static Rectangle2D.Double getIndexLineRectangle(PDPage page, double increaseAmount){
        Rectangle2D.Double currentRec = new Rectangle2D.Double(0,0,BASE_WIDTH,0);
        try {
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            // Calibrating rectangle on index line;
            calibrateRectWithIndexLine(page, increaseAmount, stripper, currentRec);
            return currentRec;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void calibrateRectWithIndexLine(PDPage page, double increaseAmount, PDFTextStripperByArea stripper, Rectangle2D.Double currentRec) throws IOException {
        calibrateRectHeightWithIndexAsLastLine(page, increaseAmount, stripper, currentRec);

        String indexLine = calibrateRectWithIndexAsOnlyLine(page, increaseAmount, stripper, currentRec);
        assert indexLine != null;
        String[] indexLineSep = indexLine.split(" ");

        calibrateRectXWithIndexLine(page, increaseAmount, stripper, currentRec, indexLineSep[0]);
    }

    private static String calibrateRectWithIndexAsOnlyLine(PDPage page, double increaseAmount, PDFTextStripperByArea stripper, Rectangle2D.Double currentRec) throws IOException {
        currentRec.y = 0;
        currentRec.height = 30;
        String lastLine = "";
        String[] lines = new String[0];
        // Decreasing line until arrived ad index line;
        while(!lastLine.contains(indexLineCommomWord) || lines.length != 1) {
            lines = extractLinesFromRect(page, stripper, currentRec);

            if (lines.length >= 1) {
                lastLine = lines[lines.length - 1];
                if (lastLine.contains(indexLineCommomWord)) {
//                    if (lines.length == 1){
//                        stripper.removeRegion("IndexLine");
//                        return lastLine;
//                    }
                    currentRec.y += increaseAmount;
                    currentRec.height -= increaseAmount;
                    continue;
                }
            }
            currentRec.height += increaseAmount;
        }
        stripper.removeRegion("IndexLine");
        return lastLine;
    }

    private static void calibrateRectHeightWithIndexAsLastLine(PDPage page, double increaseAmount, PDFTextStripperByArea stripper, Rectangle2D.Double currentRec) throws IOException {
        // Increasing until reaches index line
        for (int h = 0; h < page.getMediaBox().getHeight(); h++){
            String[] lines = extractLinesFromRect(page, stripper, currentRec);
            if(lines.length > 0) {
                String lastLine = lines[lines.length - 1];
                if (lastLine.contains(indexLineCommomWord))
                    break;
            }
            currentRec.height += increaseAmount;
        }
    }

    private static double getRectHeightWithAllLines(PDPage page, double increaseAmount, PDFTextStripperByArea stripper, Rectangle2D.Double rect) throws IOException {
        Rectangle2D.Double currentRec = (Rectangle2D.Double) rect.clone();
        for (int h = 0; h < page.getMediaBox().getHeight(); h++) {
            String[] lines = extractLinesFromRect(page, stripper, currentRec);
            String lastLine = lines[lines.length - 1];
            if (lineIsLastDocumentLine(lastLine)){
                return currentRec.height - increaseAmount;
            }
            currentRec.height += increaseAmount;
        }
        return 0D;
    }

    private static void calibrateRectXWithIndexLine(PDPage page, double increaseAmount, PDFTextStripperByArea stripper, Rectangle2D.Double currentRec, String indexFirstWord) throws IOException {
        for (int x = 0; x < BASE_WIDTH; x++){
            String[] lines = extractLinesFromRect(page, stripper, currentRec);
            String firstWord = lines[0].split(" ")[0];

            if (!firstWord.equals(indexFirstWord)){
                currentRec.x -= increaseAmount;
                currentRec.width += increaseAmount;
                break;
            }

            currentRec.x += increaseAmount;
            currentRec.width -= increaseAmount;
        }
    }

    private static Map<OutrasEditorasColumn, Position> getColumnsFromIndexLine(PDPage page, double increaseAmount, PDFTextStripperByArea stripper, Rectangle2D.Double rect) throws IOException {
        Rectangle2D.Double currentRec = (Rectangle2D.Double) rect.clone();
        currentRec.height += 5;

        String indexLine = extractLinesFromRect(page, stripper, currentRec)[0]
                /*.replace("Vlr. ", "Vlr.")
                .replace("% Aut.", "%Aut.")
                .replace("Qtde Ref.", "QtdeRef.")
                .replace("Base Cálc.", "BaseCálc.")
                .replace("% Fin", "%Fin")*/
                .trim();

        currentRec.width = 0;
        String previousFirstLine = "";
        Map<OutrasEditorasColumn, Position> colunas = new HashMap<>();

        for (int h = 0; h < BASE_WIDTH; h++){
            String[] lines = extractLinesFromRect(page, stripper, currentRec);
            if (lines.length > 0) {
                String firstLine = lines[0]
                        .replace("Vlr. ", "Vlr.")
                        .replace("Vlr A", "VlrA")
                        .replace("Vlr E", "VlrE")
                        .replace("% A", "%A")
                        .replace("% T", "%T")
                        .replace("% C", "%C")
                        .replace("% F", "%F")
                        .replace("% E", "%E")
                        .replace("% P", "%P")
                        .replace("Qtde R", "QtdeR")
                        .replace("Base C", "BaseC")
                        .replace("Titulo d", "Titulod")
                        .replace("Tituloda O", "TitulodaO")
                        .replace("Valor R", "ValorR")
                        .replace("Valor T", "ValorT")
                        .replace("Valor b", "Valorb")
                        .trim();
                String[] firstLineSep = firstLine.split(" ");
                if (firstLineSep.length > 1){
                    double columnWidth = currentRec.width - increaseAmount;
                    if (indexWords.contains(firstLineSep[0].toUpperCase())) {
                        // Creating Column
                        OutrasEditorasColumn currentColumn = columnsConverter.get(previousFirstLine);
                        if (currentColumn == null) {
                            throw new NullPointerException("Outras Editoras Column is null");
                        }
                        int widthOffset = -3;
                        if (previousFirstLine.equals("Valor") || previousFirstLine.equals("Vlr.Arrec")){
                            widthOffset = -10;
                        } else if (previousFirstLine.equals("Repassante") || previousFirstLine.equals("Obra")){
                            widthOffset = -20;
                        }

                        int xOffset = -3;
                        if (previousFirstLine.equals("Autor")){
                            xOffset = -7;
                        }

//                        if ((indexLine.contains("%Aut.") || indexLine.contains("%Fin")) && firstLineSep[0].equals("%")){
//                            currentRec.width += increaseAmount;
//                            continue;
//                        }
                        colunas.put(currentColumn, new Position(currentRec.x + xOffset, columnWidth + widthOffset));
                        indexLine = indexLine.replaceFirst(lines[0].substring(0, lines[0].length() - 1), "").trim();
                    }
                    // Setting rect width to start getting next column
                    currentRec.x += columnWidth;
                    currentRec.width = 0;
                } else if (indexLine.replace(" ", "").equals(firstLineSep[0].trim())){
                    double columnWidth = currentRec.width - increaseAmount;

                    OutrasEditorasColumn currentColumn = columnsConverter.get(firstLineSep[0].trim());
                    colunas.put(currentColumn, new Position(currentRec.x - 10, columnWidth + 20));
                    indexLine = indexLine.replace(previousFirstLine, "").trim();
                }
                previousFirstLine = firstLine.trim();
            }
            currentRec.width += increaseAmount;
        }
        return colunas;
    }

    private static String[] extractLinesFromRect(PDPage page, PDFTextStripperByArea stripper, Rectangle2D.Double currentRec) throws IOException {
        stripper.addRegion("region", currentRec);
        stripper.extractRegions(page);
        stripper.getTextForRegion("region");

        String[] lines = stripper.getTextForRegion("region").split(System.lineSeparator());

        stripper.removeRegion("region");
        return lines;
    }

    private static String extractLineFromRect(PDPage page, PDFTextStripperByArea stripper, Rectangle2D.Double currentRec) throws IOException {
        stripper.addRegion("region", currentRec);
        stripper.extractRegions(page);
        stripper.getTextForRegion("region");

        String line = stripper.getTextForRegion("region");

        stripper.removeRegion("region");
        return line;
    }

    private static boolean lineIsLastDocumentLine(String line) {
        String regex = "PÁGINA \\d+ DE \\d+";
        Pattern pattern = Pattern.compile(regex);
        String fixedLine = line
                .toUpperCase()
                .replaceAll("\\s{2,}", " ")
                .trim();
        Matcher matcher = pattern.matcher(fixedLine);
        return matcher.find();
    }

    protected static void readJsonAndSetFields() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map jsonData = getMapFromJson(objectMapper, "/OutrasEditorasConfig.json");
            indexWords = (ArrayList<String>) jsonData.get("indexWords");
            indexWords.replaceAll(String::toUpperCase);

//            Map readedColumnsData = getMapFromJson(objectMapper, "/NomesColunas.json");
//            columnsData = readedColumnsData;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static Map getMapFromJson(ObjectMapper objectMapper, String file) throws IOException {
        InputStream inputStream = OutrasEditorasPreProcessor.class.getResourceAsStream(file);
        Map jsonData = objectMapper.readValue(inputStream, Map.class);
        return jsonData;
    }

    protected static String treatColumnName(String columnName){
        if (columnName.equals("%"))
            return "PERCENTAGE";
        return columnName.toUpperCase().replace(".", "_");
    }
}
