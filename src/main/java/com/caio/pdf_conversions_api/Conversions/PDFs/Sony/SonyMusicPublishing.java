package com.caio.pdf_conversions_api.Conversions.PDFs.Sony;

import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.LineData;

import com.caio.pdf_conversions_api.Conversions.PDFs.BasePdfConversion;
import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SonyMusicPublishing extends BasePdfConversion {

    private boolean startReading;
    protected String tipoExecucao;

    public SonyMusicPublishing(String pdfPath, String xlsName, String[] filesToConvert) {
        super(pdfPath, xlsName, filesToConvert);
    }

    @Override
    protected String extractVerificationLine(LineData line) {
        return line.getLineSeparated()[1];
    }

    @Override
    protected void setIndexLine() {
        this.indexLine = new String[]{
                "Fonte Pagadora",
                "Tipo Execução",
                "Artista",
                "Titulo",
                "Unidades",
                "Taxa de Performance",
                "Porcentagem Royalty",
                "Valor Líquido",
                "Data",
                "Arquivo"
        };
    }

    @Override
    protected void setUnwantedPageLines() {

    }

    @Override
    protected void setUnwantedLines() {
        this.unwantedLines = new String[]{
                "Extrato", "PERÍODO", "Data", "PERIODO DE"
        };
    }

    @Override
    protected void setDateLine() {

    }

    @Override
    protected void resetDocumentAtributes() {
        super.resetDocumentAtributes();
        this.startReading = false;
    }

    @Override
    protected void setVerificationLine() {
        this.verificationLine = "Total Rendimento Total Líquido";
    }

    @Override
    protected boolean isDateLine(LineData line) {
        Pattern pattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{4} a \\d{2}/\\d{2}/\\d{4})");
        Matcher matcher = pattern.matcher(line.getFullLine());
        return matcher.find();
    }
    // Descobrir qual é
    /*@Override
    protected void processLine(LineData line) {
        if (this.canStartReading(line)){
            this.startReading = true;
            return;
        }

        if (!this.startReading) return;

        if (!isDataLine(line)) return;



        String[] lineSep = line.getLineSeparated();

        double liquidValue = Helper.ajustaNumero(lineSep[lineSep.length - 1]);
        double royaltyRate = Helper.ajustaNumero(lineSep[lineSep.length - 2].replace("%", ""));
//        double performanceRate = Helper.ajustaNumero(this.correctNumber(lineSep[lineSep.length - 3]));
//        int unitsSold = Integer.parseInt(this.correctNumber(line.getStringFromPosition(Helper.mmParaPx(178.00), Helper.mmParaPx(19.00)).replaceAll("\\s+", "")));

        String rubric = line.getStringFromPosition(Helper.mmParaPx(30.00), Helper.mmParaPx(31.00)).trim();
        String title = line.getStringFromPosition(Helper.mmParaPx(63.00), Helper.mmParaPx(40.00)).trim();
        String period = line.getStringFromPosition(Helper.mmParaPx(3.00), Helper.mmParaPx(26.00)).trim();
//        String interpreter = line.getStringFromPosition(Helper.mmParaPx(195.40), Helper.mmParaPx(29.00)).trim();

        Object[] result = new Object[]{
                rubric,
                title,
//                interpreter,
                period,
//                unitsSold,
//                performanceRate,
                royaltyRate,
                liquidValue,
                this.currentDate
        };

        this.addResult(result, liquidValue);
    }*/

    @Override
    protected void processLine(LineData line) {
        if (isTipoExecucaoLine(line)){
            setTipoExecucao(line);
            return;
        }

        if (this.canStartReading(line)){
            this.startReading = true;
            return;
        }

        if (!this.startReading) return;


        if (!isDataLine(line)) return;


        String[] lineSep = line.getLineSeparated();

        double liquidValue = Helper.ajustaNumero(lineSep[lineSep.length - 1]);
        double royaltyRate = Helper.ajustaNumero(lineSep[lineSep.length - 2].replace("%", ""));

        Object performanceRate;
        try {
            performanceRate = Helper.ajustaNumero(this.correctNumber(lineSep[lineSep.length - 3]));
        } catch (NumberFormatException e) {
            performanceRate = "Não possível ler";
        }

        Object unitsSold = null;
        try {
            unitsSold = this.correctNumber(line.getStringFromPosition(Helper.mmParaPx(208.00), Helper.mmParaPx(23.00)).replaceAll("\\s+", ""));
            if (!((String) unitsSold).isEmpty())
                unitsSold = Integer.parseInt((String) unitsSold);
        } catch (NumberFormatException e) {
            unitsSold = Double.NaN;
        }

        String fontePagadora = line.getStringFromPosition(Helper.mmParaPx(3.00), Helper.mmParaPx(35.00)).trim();
        String artista = line.getStringFromPosition(Helper.mmParaPx(152.00), Helper.mmParaPx(28.00)).trim();
        String titulo = line.getStringFromPosition(Helper.mmParaPx(180.00), Helper.mmParaPx(43.00)).trim();

        this.addResult(new Object[] {
                fontePagadora,
                tipoExecucao,
                artista,
                titulo,
                unitsSold,
                performanceRate,
                royaltyRate,
                liquidValue,
                this.currentDate,
                this.currentFile
        }, liquidValue);

    }

    @Override
    public void setDatePatterns() {
        this.datePatterns = new LinkedHashMap<>() {
            {
                put(Pattern.compile("(\\d{2}/\\d{2}/\\d{4})"), Pair.of(1,1));
            }
        };
    }

    @Override
    protected boolean isDataLine(LineData line) {
        String[] lineSep = line.getLineSeparated();
        return Helper.isNumeroComVirgula(lineSep[lineSep.length - 1]);
    }

    @Override
    protected void executeBeforeReadingPage(PDDocument document) {

    }

    protected boolean isTipoExecucaoLine(LineData line) {
        String[] lineSep = line.getLineSeparated();
        return lineSep[0].matches("\\d{2}/\\d{2}/\\d{4}");
    }

    private String correctNumber(String number){
        return number.replaceAll("[A-Za-z/&]+", "");
    }

    @Override
    protected void doVerification(LineData line, String currentDocumentName) {
        LineData lineToDoVerification = this.currentPageLines.get(this.currentPageLines.indexOf(line) + 1);
        super.doVerification(lineToDoVerification, currentDocumentName);
    }


    protected boolean canStartReading(LineData line) {
        return line.getFullLine().toUpperCase().contains("FONTE PAGADORA") || line.getFullLine().contains("COMPETÊNCIA");
    }

    private void setTipoExecucao(LineData line){
        String fullLine = line.getFullLine();
        String[] lineSep = line.getLineSeparated();
        this.tipoExecucao = fullLine
                            .replace(lineSep[0], "")
                            .replace(lineSep[lineSep.length - 1], "")
                            .replace(lineSep[lineSep.length - 2], "")
                            .trim();
    }
}
