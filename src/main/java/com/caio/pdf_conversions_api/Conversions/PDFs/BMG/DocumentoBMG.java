package com.caio.pdf_conversions_api.Conversions.PDFs.BMG;

import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.LineData;
import com.caio.pdf_conversions_api.Conversions.PDFs.BMG.Enum.BMGDocument;
import com.caio.pdf_conversions_api.Conversions.PDFs.BMG.Enum.BMGPosition;
import com.caio.pdf_conversions_api.Conversions.PDFs.BasePdfConversion;
import com.caio.pdf_conversions_api.Export.ResultData;
import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

public class DocumentoBMG extends BasePdfConversion {

    private BMGDocument documentType;

    public DocumentoBMG(String pdfPath, String xlsName, String[] filesToConvert) {
        super(pdfPath, xlsName, filesToConvert);
    }

    private final String[] switchSongTerms = new String[]{
            "Share Due", "Song Total",
            "porcentagem devidos", "Total por música",
    };

    private final String[] songTotalTerms = new String[]{
            "Song Total",
            "Total por música",
    };

    private final String[] ignoreSongLineTerms = new String[]{
            "Statement Total", "Source Total",
            "Total do relatório", "Total por origem"
    };

    private String currentSongName;
    private boolean songInNextLine = false;

    @Override
    protected List<LineData> extractPageData(PDDocument document, int page) throws IOException {
        BmgPdfStripper stripper;

        if (this.stripperBounds == null)
            stripper = new BmgPdfStripper();
        else
            stripper = new BmgPdfStripper(this.stripperBounds);

        stripper.setSortByPosition(this.stripperSetSortByPosition);
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        return stripper.getLines(document);
    }

    @Override
    protected String extractValueFromVerificationLine(LineData line) {
        String[] lineSep = line.getLineSeparated();
        return lineSep[lineSep.length - 1];
    }

    @Override
    protected void setIndexLine() {

    }

    @Override
    protected void setUnwantedPageLines() {
        this.unwantedPageLines = new String[]{
                "Payment Details", "Royalty Balance", "Summary Statement", "Summary Statement", // Termos em Ingles
                "Work Show Produções Artísticas Ltda", "Saldo de royalties", "Total do relatório",//Termos em Portugues
        };
    }

    @Override
    protected void setUnwantedLines() {
        this.unwantedLines = new String[]{
                "Source Total", "Source Name", "Client Code", "Share Amount", "Amount Due", // Termos em Ingles
                "Total por origem" //Termos em Portugues
        };
    }

    @Override
    protected void setDateLine() {
    }

    @Override
    protected boolean isDateLine(LineData line) {
        return line.getFullLine()
                .toUpperCase().matches("ROYALTY STATEMENT \\w+ \\d{4} TO \\w+ \\d{4}");
    }

    @Override
    protected void setVerificationLine() {
        this.verificationLine = "Statement Total";
    }

    @Override
    protected void executeOnFirstPage() {
        this.setCurrentDocumentType();
    }

    @Override
    protected void processLine(LineData line) {
        switch (this.documentType) {
            case BASE_BMG -> this.processBaseBmg(line);
            case ROYALTY_BMG -> this.processRoyaltyBMG(line);
            case null, default -> {
            }
        }
    }

    protected void processRoyaltyBMG(LineData line){
        String linha = line.getFullLine();
        String[] lineSep = line.getLineSeparated();

        if (!isDataLine(line)){
            this.switchSong(line);
            return;
        }

        if (linha.toUpperCase().contains(String.format("%s TOTAL", this.currentSongName))) return;

        Double netRevenue = Helper.ajustaNumero(lineSep[lineSep.length - 1]);
        Double grossRevenue = Helper.ajustaNumero(lineSep[lineSep.length - 2]);
        Double percentOwned = Helper.ajustaNumero(lineSep[lineSep.length - 3]);
        Integer units = Helper.convertToInteger(lineSep[lineSep.length - 4]);

        int offset = 0;
        String catalogNumber = lineSep[lineSep.length - 5];
        if (catalogNumber.matches("\\d{4}-\\d{4}")) {
            catalogNumber = "";
            offset = 1;
        }

        String salesDate = lineSep[lineSep.length - 6 + offset];
        String country = lineSep[lineSep.length - 7 + offset];
        String type = lineSep[lineSep.length - 8 + offset];
        String source = lineSep[lineSep.length - 9 + offset];

        ResultData resultData = addResultData(netRevenue,
                grossRevenue,
                percentOwned,
                units,
                catalogNumber,
                salesDate,
                source,
                country,
                type
                );

        this.addResult(resultData);
    }

    protected void processBaseBmg(LineData line) {
        String linha = line.getFullLine();

        if (Arrays.stream(this.switchSongTerms).anyMatch(linha::contains)){
            songInNextLine = true;
            /*if (Arrays.stream(this.songTotalTerms).anyMatch(linha::contains)) {
                this.doVerification(line, currentFile);
                return;
                *//*if (Math.round(somatorio) != Math.round(Double.parseDouble(valor.replace(",", "")))) {
                    verificacao.put(verificacao.size(), new String[]{"NÃO BATEU", obra, "Valor encontrado:",
                            valor, "Valor Somado:", String.valueOf(somatorio), data});
                } else {
                    verificacao.put(verificacao.size(), new String[]{"BATEU", obra, "Valor encontrado:",
                            valor, "Valor Somado:", String.valueOf(somatorio), data});
                }
                somatorio = 0.0;*//*
            }*/
            return;
        }
        if (this.songInNextLine){
            switchSong(line);
            this.songInNextLine = false;
            return;
        }

        if (!isDataLine(line)) return;

        String[] lineSeparated = line.getLineSeparated();
        String amountDueStr = lineSeparated[lineSeparated.length - 1];

        Double amountDue = Helper.ajustaNumero(amountDueStr);
        Double share = Helper.ajustaNumero(lineSeparated[lineSeparated.length - 2]);
        Double amountReceived = Helper.ajustaNumero(lineSeparated[lineSeparated.length - 3]);


        Integer units = getUnitsFromLine(line);

        String catalogNumber = line.getStringFromPosition(BMGPosition.CatalogNumber.getX(), BMGPosition.CatalogNumber.getW())
                .trim();

        String period = line.getStringFromPosition(BMGPosition.Period.getX(), BMGPosition.Period.getW())
                .trim();

        String sourceName = line.getStringFromPosition(BMGPosition.SourceName.getX(), BMGPosition.SourceName.getW())
                .trim();

        String country = line.getStringFromPosition(BMGPosition.Country.getX(), BMGPosition.Country.getW())
                .trim();

        String incomeType = line.getStringFromPosition(BMGPosition.IncomeType.getX(), BMGPosition.IncomeType.getW())
                .trim();


        ResultData resultData = addResultData(amountDue,
                amountReceived,
                share,
                units,
                catalogNumber,
                period,
                sourceName,
                country,
                incomeType);

        this.addResult(resultData);
    }

    private ResultData addResultData(Double netRevenue,
                                     Double grossRevenue,
                                     Double percentOwned,
                                     Integer units,
                                     String catalogNumber,
                                     String salesDate,
                                     String sourceName,
                                     String country,
                                     String incomeType) {
        ResultData resultData = new ResultData();

        resultData.setTrack_name(this.currentSongName);
        resultData.setNet_revenue(netRevenue);
        resultData.setGross_revenue(grossRevenue);
        resultData.setPercent_owned(percentOwned);
        resultData.setUnits(units);
        resultData.setCatalog_id(catalogNumber);
        resultData.setSales_date(salesDate);
        resultData.setSource(sourceName);
        resultData.setCountry(country);
        resultData.setType(incomeType);
        resultData.setPath(this.currentFile);
        resultData.setStatement_date(this.currentDate);

        return resultData;
    }

    private Integer getUnitsFromLine(LineData line) {
        String unitsStr = Helper.corrigeSeparadorInt(
                line.getStringFromPosition(BMGPosition.Units.getX(), BMGPosition.Units.getW())
                        
                        .replace(",", "")
                        .trim()
                );
        return unitsStr.isEmpty() ? 0 : Integer.parseInt(unitsStr);
    }

    @Override
    protected boolean isDataLine(LineData line) {
        String[] lineSep = line.getLineSeparated();

        return lineSep.length > 1 &&
                Helper.verificaRateioObra(lineSep[lineSep.length - 2]) &&
                Arrays.stream(this.ignoreSongLineTerms).noneMatch(line.getFullLine()::contains);
    }

    private void setCurrentDocumentType(){
        this.documentType = this.currentPageLines.getFirst().getFullLine().toUpperCase().contains("SUMMARY STATEMENT") ?
                BMGDocument.BASE_BMG :
                BMGDocument.ROYALTY_BMG;
    }

    private void switchSong(LineData line) {
        this.currentSongName = line
                .getStringFromPosition(BMGPosition.SongName.getX(), 99) //TODO: Change to full line
                .trim();
    }

    @Override
    protected void executeBeforeReadingPage(PDDocument document) {

    }

    @Override
    public void setDatePatterns() {
        this.datePatterns = new LinkedHashMap<>() {
            {
                put(Pattern.compile("Royalty Statement (\\w+) (\\d{4}) to \\w+ \\d{4}"), Pair.of(1,2));
                put(Pattern.compile("ROYALTY STATEMENT (\\w+) (\\d{4}) TO \\w+ \\d{4}"), Pair.of(1,2));
                put(Pattern.compile("royalty statement (\\w+) (\\d{4}) to \\w+ \\d{4}"), Pair.of(1,2));
            }
        };
    }
}
