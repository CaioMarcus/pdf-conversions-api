package com.caio.pdf_conversions_api.Conversions.PDFs.Abramus;

import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.LineData;
import com.caio.pdf_conversions_api.Conversions.PDFs.BasePdfConversion;
import com.caio.pdf_conversions_api.Export.ResultData;
import com.caio.pdf_conversions_api.Export.VerificationData;
import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


public class AbramusDigital extends BasePdfConversion {

    private String currentSong;
    private String currentAuthor;
    private boolean primeiroValorPassou;
    private Double totalObraAtualCalculado = null;
    private Double totalObraAtualFornecido = null;
    private AbramusDocType currentAbramusDigitalDocument;

    private final Map<AbramusDocType, Map<String, Double[]>> columnsCoordinates = new HashMap<>();

    public AbramusDigital(String pdfPath) {
        super(pdfPath);

        Map<String, Double[]> abramusDigitalLargePageCoordinates = new HashMap<>();
        abramusDigitalLargePageCoordinates.put("territorio", new Double[]{Helper.mmParaPx(4.96), Helper.mmParaPx(23.07)});
        abramusDigitalLargePageCoordinates.put("fonte", new Double[]{Helper.mmParaPx(28.03), Helper.mmParaPx(39.55)});
        abramusDigitalLargePageCoordinates.put("formato", new Double[]{Helper.mmParaPx(67.57), Helper.mmParaPx(34.46)});
        abramusDigitalLargePageCoordinates.put("periodo", new Double[]{Helper.mmParaPx(102.03), Helper.mmParaPx(29.55)});
        this.columnsCoordinates.put(AbramusDocType.ABRAMUS_DIGITAL_LARGE_PAGES, abramusDigitalLargePageCoordinates);

        Map<String, Double[]> abramusDigitalCoordinates = new HashMap<>();
        abramusDigitalCoordinates.put("territorio", new Double[]{Helper.mmParaPx(12.35), Helper.mmParaPx(24.69)});
        abramusDigitalCoordinates.put("fonte", new Double[]{Helper.mmParaPx(37.04), Helper.mmParaPx(42.33)});
        abramusDigitalCoordinates.put("formato", new Double[]{Helper.mmParaPx(79.37), Helper.mmParaPx(37.04)});
        abramusDigitalCoordinates.put("periodo", new Double[]{Helper.mmParaPx(116.42), Helper.mmParaPx(31.75)});
        this.columnsCoordinates.put(AbramusDocType.ABRAMUS_DIGITAL, abramusDigitalCoordinates);

        this.verificacao.add(new String[]{
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        });
    }

    @Override
    protected String extractVerificationLine(LineData line) {
        return "";
    }

    @Override
    protected void setIndexLine() {
        this.indexLine = new String[]{"OBRA", "TIPO", "TERRITÓRIO", "FONTE", "FORMATO", "PERÍODO", "EXEC.", "TOTAL", "DIVISÃO", "DATA", "ARQUIVO"};
    }

    @Override
    protected void setUnwantedPageLines() {

    }

    @Override
    protected void setUnwantedLines() {
        this.unwantedLines = new String[] {
                "Demonstrativo do Autor"
        };
    }

    @Override
    protected void setDateLine() {
    }

    @Override
    protected void setVerificationLine() {

    }

    @Override
    protected void processLine(LineData line) {
        if (this.currentDate == null) return;
        String fullLine = line.getFullLine();
        String[] lineSeparated = line.getLineSeparated();
        String currentValue = lineSeparated[lineSeparated.length - 1];

        if (!Helper.verificaRateioObra(currentValue)) {
            return;
        }
        double currentValueDouble = Helper.ajustaNumero(currentValue);
        String currentTotal = lineSeparated[lineSeparated.length - 2];

        if (!Helper.verificaRateioObra(currentTotal)){
            this.setNewObra(line, currentValueDouble);
            return;
        }
        double currentTotalDouble = Helper.ajustaNumero(currentTotal);

        Map<String, Double[]> currentCoordinates = columnsCoordinates.get(currentAbramusDigitalDocument);

        String tipoAtual = lineSeparated[0];
        String territorioAtual = line.getStringFromPosition(currentCoordinates.get("territorio")[0], currentCoordinates.get("territorio")[1]).trim();
        String fonteAtual = line.getStringFromPosition(currentCoordinates.get("fonte")[0], currentCoordinates.get("fonte")[1]).trim();
        String formatoAtual = line.getStringFromPosition(currentCoordinates.get("formato")[0], currentCoordinates.get("formato")[1]).trim();
        String periodoAtual = line.getStringFromPosition(currentCoordinates.get("periodo")[0], currentCoordinates.get("periodo")[1]).trim();
        String execAtual = lineSeparated[lineSeparated.length - 3];

        /*Object[] results = new Object[] {
                currentSong,
                tipoAtual,
                territorioAtual,
                fonteAtual,
                formatoAtual,
                periodoAtual,
                execAtual,
                currentTotalDouble,
                currentValueDouble,
                currentDate,
                this.currentFile
        };*/

        ResultData resultData = new ResultData();
        resultData.setNet_revenue(currentValueDouble);
        resultData.setGross_revenue(currentTotalDouble);
        resultData.setUnits(execAtual);
        resultData.setSales_date(periodoAtual);
        resultData.setType(formatoAtual);
        resultData.setSource(fonteAtual);
        resultData.setCountry(territorioAtual);
        resultData.setTrack_name(this.currentSong);
        resultData.setTrack_artist(this.currentAuthor);
        resultData.setCharacteristic(tipoAtual);
        resultData.setStatement_date(this.currentDate);
        resultData.setPath(this.currentFile);

        this.addResult(resultData, currentValueDouble);
        totalObraAtualCalculado += currentValueDouble;

//        System.out.println(fullLine);
    }

    @Override
    protected boolean isDateLine(LineData line) {
        String lineUpper = line.getFullLine().toUpperCase();

        return lineUpper.matches("\\d{4} - [\\wÇ]+") ||
                lineUpper.matches("[\\wÇ]+ - \\d{4}") ||
                lineUpper.matches("\\dº TRIMESTRE - \\d{4}") ||
                lineUpper.matches("\\d{4} - \\dº TRIMESTRE");
    }

    @Override
    protected boolean isDataLine(LineData line) {
        return false;
    }

    @Override
    protected void executeBeforeReadingPage(PDDocument document) {

    }

    @Override
    public void setDatePatterns() {
        this.datePatterns = new LinkedHashMap<>() {
            {
                put(Pattern.compile("(\\dº TRIMESTRE) - (\\d{4})"), Pair.of(1,2));
                put(Pattern.compile("(\\d{4}) - (\\dº TRIMESTRE)"), Pair.of(2,1));
                put(Pattern.compile("(\\d{4}) - (\\w+)"), Pair.of(2,1));
                put(Pattern.compile("(\\w+) - (\\d{4})"), Pair.of(1,2));
            }
        };
    }

    private void setNewObra(LineData line, double currentValue){
        String fullLine = line.getFullLine();

        String possibleNewSong;
        String possibleNewAuthor;
        try {
            int parenthesisIndex = fullLine.indexOf("(");
            possibleNewSong = fullLine.substring(0, parenthesisIndex).trim();
            possibleNewAuthor = fullLine.substring(parenthesisIndex + 1)
                    .replace(line.getLineSeparated()[line.getLineSeparated().length - 1], "")
                    .trim();
        }catch (StringIndexOutOfBoundsException e){
            possibleNewSong = "";
            possibleNewAuthor = "";
        }

        if (possibleNewSong.equals(currentSong))
            return;

        if (this.totalObraAtualCalculado != null) {
            doSongVerification(this.totalObraAtualCalculado, this.totalObraAtualFornecido, this.currentSong, this.currentFile);
        }
        this.currentSong = possibleNewSong;
        this.currentAuthor = possibleNewAuthor;
        this.totalObraAtualFornecido = currentValue;
        this.totalObraAtualCalculado = 0D;
    }

    private void doSongVerification(Double totalObraAtualCalculado, Double totalObraAtualFornecido, String currentSong, String currentFile) {
        String verificationResult = "OBRA BATEU";

        if (!Helper.isClose(totalObraAtualFornecido, this.totalObraAtualCalculado, this.acceptableDifferencePercentage)) {
            verificationResult = "OBRA NÃO BATEU";
        }

        VerificationData verificationData = new VerificationData();
        verificationData.setStatus(verificationResult + ": " + currentSong);
        verificationData.setInformed_total(totalObraAtualFornecido);
        verificationData.setSummed_total(totalObraAtualCalculado);
        verificationData.setDifference(totalObraAtualFornecido - totalObraAtualCalculado);
        verificationData.setDocument(this.currentFile);
        verificationData.setDocument_date(this.currentDate);

        this.verificacaoResultData.add(verificationData);

        /*this.verificacao.add(new Object[]{
                verificationResult,
                "VALOR INFORMADO: ",
                totalObraAtualFornecido,
                "VALOR CALCULADO: ",
                totalObraAtualCalculado,
                "NOME OBRA:",
                currentSong,
                "ARQUIVO:",
                currentFile});*/
    }

    @Override
    protected List<LineData> extractPageData(PDDocument document, int page) throws IOException {
        checkDocumentType(document);
        return super.extractPageData(document, page);
    }

    private void checkDocumentType(PDDocument document){
        if (this.currentAbramusDigitalDocument != null) return;
        if (document.getPage(0).getMediaBox().getHeight() > 842.00)
            this.currentAbramusDigitalDocument = AbramusDocType.ABRAMUS_DIGITAL_LARGE_PAGES;
        else
            this.currentAbramusDigitalDocument = AbramusDocType.ABRAMUS_DIGITAL;
    }
}
