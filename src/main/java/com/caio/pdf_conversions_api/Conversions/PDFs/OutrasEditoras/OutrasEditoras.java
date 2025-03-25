package com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras;

import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.LineData;
import com.caio.pdf_conversions_api.Conversions.PDFs.BasePdfConversion;
import com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras.Models.OutrasEditorasColumn;
import com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras.Models.OutrasEditorasDocumento;
import com.caio.pdf_conversions_api.Conversions.PDFs.Position;
import com.caio.pdf_conversions_api.Export.ResultData;
import com.caio.pdf_conversions_api.Helpers.Helper;
import com.google.rpc.Help;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;


public class OutrasEditoras extends BasePdfConversion {

    protected OutrasEditorasDocumento currentDocument;
    protected double testSum;
    public OutrasEditoras(String pdfPath) {
        super(pdfPath);
    }

    @Override
    protected void setIndexLine() {

    }

    @Override
    protected void setUnwantedPageLines() {

    }

    @Override
    protected void setUnwantedLines() {
        this.unwantedLines = new String[]{
                "REPASSE DE DIREITOS AUTORAIS",
                "Nome:",
                "Endereço:",
                "Cidade:",
                "Banco:",
                "Obra",
                "Valor líquido:",
                "Adtos do período:",
                "Sub total do produto:"
        };
    }

    @Override
    protected void setDateLine() {
        this.dateLine = "Período:";
    }

    @Override
    protected void setVerificationLine() {
        this.verificationLine = "Total Geral do período:";
    }

    @Override
    protected void processLine(LineData line) {
        if (!this.isDataLine(line)){
//            System.out.println(line.getFullLine());
            return;
        }
        /*if (line.getFullLine().contains("Sub total do produto:")){
            String[] lineSep = line.getLineSeparated();
            testSum += Helper.ajustaNumero(lineSep[lineSep.length - 1]);
            System.out.println(line.getFullLine());
            System.out.println(this.documentTotalSum  + "  " +  this.testSum);
            return;
        }*/
//        line.getFullLine();

        Object repasse = getValueFromLine(line, OutrasEditorasColumn.REPASSE, true);
        Object percentage = getValueFromLine(line, OutrasEditorasColumn.PERCENTAGE, true);
//        Object vlrUnit = getValueFromLine(line, OutrasEditorasColumn.VLR_UNIT, true);
        Object vendas = getValueFromLine(line, OutrasEditorasColumn.VENDAS, true);

        Object repassante = treatRepassante(getValueFromLine(line, OutrasEditorasColumn.REPASSANTE, false));

        Object obra = getValueFromLine(line, OutrasEditorasColumn.OBRA, false);

        ResultData resultData = new ResultData();

        resultData.setUnits(vendas);
        resultData.setTrack_name(obra);
        resultData.setDistributor(repassante);
        resultData.setGross_revenue(repasse);
        resultData.setPercent_owned(percentage);
        resultData.setStatement_date(this.currentDate);
        resultData.setPath(this.currentFile);

        this.addResult(resultData, (Double) repasse);
    }

    @Override
    protected boolean isDataLine(LineData line) {
        String[] lineSep = line.getLineSeparated();
        String lastWord = lineSep[lineSep.length - 1];
        return Helper.verificaRateioObra(lastWord);
    }

    @Override
    protected void executeBeforeReadingPage(PDDocument document) {
        this.currentDocument = OutrasEditorasPreProcessor.getOutrasEditorasDocumentoAjustado(1, document);

        System.out.println("Colunas extraidas do documento: " + this.currentFile);
    }

    @Override
    protected String extractVerificationLine(LineData line){
        String[] lineSep = line.getLineSeparated();
        if (lineSep.length > 2 && Helper.verificaRateioObra(lineSep[lineSep.length - 3]))
            return lineSep[lineSep.length - 2];
        return lineSep[lineSep.length - 1];
    }

    @Override
    public void setDatePatterns() {
        this.datePatterns = new LinkedHashMap<>(){
            {
                put(Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4}) e (\\d{2})/(\\d{2})/(\\d{4})"), Pair.of(2,3));
                put(Pattern.compile("Periodo: (\\d{2})/(\\d{2})/(\\d{4}) e (\\d{2})/(\\d{2})/(\\d{4})"), Pair.of(2,3));
                put(Pattern.compile("(\\d{2})/(\\d{2})/(\\d{2}) E (\\d{2})/(\\d{2})/(\\d{2})"), Pair.of(2,3));
                put(Pattern.compile("(\\dº \\w+)/(\\d{4})"), Pair.of(1,2));
                put(Pattern.compile("(\\w+)/(\\d{4})"), Pair.of(1,2));
                put(Pattern.compile("(\\d{2})/(\\d{2})/(\\d{2}) e (\\d{2})/(\\d{2})/(\\d{2})"), Pair.of(2,3));
            }
        };
    }

    private Object getValueFromLine(LineData line, OutrasEditorasColumn column, boolean parseDouble){
        Position vlrUnitPosition = currentDocument.getColumnPosition(column);
        String value = line.getStringFromPosition(vlrUnitPosition.getX(), vlrUnitPosition.getW()).trim();

        if (!value.isEmpty() && parseDouble) {
            return Helper.ajustaNumero(value);
        }

        return value;
    }

    private Object treatRepassante(Object repassante){
        String repassanteStr = ((String) repassante);
        int slashIndex = repassanteStr.indexOf('/');
        if (slashIndex == -1) return repassante;
        return repassanteStr.substring(0, slashIndex);
    }
}
