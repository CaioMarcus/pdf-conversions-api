package com.caio.pdf_conversions_api.Conversions.PDFs.Sony;

import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.LineData;

import com.caio.pdf_conversions_api.Conversions.PDFs.BasePdfConversion;
import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import static com.caio.pdf_conversions_api.Helpers.Helper.mmParaPx;

public class SonyMusic extends BasePdfConversion {

    public SonyMusic(String pdfPath, String xlsName) {
        super(pdfPath, xlsName);
    }

    @Override
    protected String extractVerificationLine(LineData line) {
        return "";
    }

    @Override
    protected void setIndexLine() {
        this.indexLine = new String[]
                {"Titulo", "Obra", "Pais", "Quantidade", "Royalties", "Distribuidos", "Valor", "Periodo", "Data"};
    }

    @Override
    protected void setUnwantedPageLines() {
        // No need this implementation, since this conversion will check valid lines;
    }

    @Override
    protected void setUnwantedLines() {
        // No need this implementation, since this conversion will check valid lines;
    }

    @Override
    protected void setDateLine() {
        this.dateLine = "Relatório:";
    }

    @Override
    protected void setVerificationLine() {

    }

    @Override
    public void setDatePatterns() {
        this.datePatterns = new LinkedHashMap<>() {
            {
                put(Pattern.compile("(Para o periodo que:) (\\d{2}.\\d{2}.\\d{4})"), Pair.of(2,2));
            }
        };
    }

    @Override
    protected void processLine(LineData line) {
        if (!isDataLine(line)) return;
        String[] lineSep = line.getLineSeparated();

        String productTitle = line.getStringFromPosition(mmParaPx(34.30), mmParaPx(29.40)).trim();
        String title = line.getStringFromPosition(mmParaPx(63.88), mmParaPx(27.20)).trim();
        String country = line.getStringFromPosition(mmParaPx(132.50), mmParaPx(17.10)).trim();
        String distibuted = line.getStringFromPosition(mmParaPx(151.78), mmParaPx(17.40)).trim();

        String amount = Helper.corrigeSeparadorInt(correctNumber(lineSep[lineSep.length - 8]));
        String royalties = Helper.corrigeSeparadorDouble(lineSep[lineSep.length - 3]);
        String value = Helper.corrigeSeparadorDouble(correctNumber(lineSep[lineSep.length - 1]));
        String period = lineSep[0];

        Object[] result = new String[]{
                productTitle, title, country, amount, royalties, distibuted, value, period, this.currentDate
        };

        this.addResult(result, Helper.ajustaNumero(value));
    }

    @Override
    protected boolean isDataLine(LineData line) {
        String[] lineSeparated = line.getLineSeparated();
        return lineSeparated[0].matches("^\\d{4}\\s*(Q?\\d{1,2})$");
    }

    protected String correctNumber(String number){
        String correctedNumber = number.trim();
        if (correctedNumber.endsWith("-"))
            return String.format("-%s", correctedNumber.substring(0, correctedNumber.length() - 1));
        return correctedNumber;
    }
}
