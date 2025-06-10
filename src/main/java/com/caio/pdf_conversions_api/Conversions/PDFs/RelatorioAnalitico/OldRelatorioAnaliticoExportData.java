package com.caio.pdf_conversions_api.Conversions.PDFs.RelatorioAnalitico;

import com.caio.pdf_conversions_api.Export.RelatorioAnalitico.RelatorioAnaliticoResultData;

public class OldRelatorioAnaliticoExportData extends RelatorioAnaliticoResultData {

    public static OldRelatorioAnaliticoExportData fromRelatorioAnaliticoResultData(RelatorioAnaliticoResultData resultData) {
        OldRelatorioAnaliticoExportData oldData = new OldRelatorioAnaliticoExportData();

        oldData.setCatalog_id(resultData.getCatalog_id());
        oldData.setIswc(resultData.getIswc());
        oldData.setTrack_name(resultData.getTrack_name());
        oldData.setOwner(resultData.getOwner());
        oldData.setOwner_pseudonym(resultData.getOwner_pseudonym());
        oldData.setOwner_id(resultData.getOwner_id());
        oldData.setCae(resultData.getCae());
        oldData.setSource(resultData.getSource());
        oldData.setType(resultData.getType());
        oldData.setPercent_owned(resultData.getPercent_owned());
        oldData.setStatement_date(resultData.getStatement_date());

        return oldData;
    }

    @Override
    public String getIndexLine() {
        return "COD.OBRA, " +
                "ISWC, " +
                "TÍTULO PRINCIPAL DA OBRA, " +
                "COD.TITULAR, " +
                "TITULAR, " +
                "PSEUDONIMO, " +
                "COD.CAE, " +
                "ASSOCIAÇÃO, " +
                "CAT, " +
                "%, " +
                "DATA";
    }

    @Override
    public String getCsvLine() {
        return String.join(",",
                String.format(this.commaFormat, this.catalog_id),
                String.format(this.commaFormat, this.iswc),
                String.format(this.commaFormat, this.track_name),
                String.format(this.commaFormat, this.owner_id),
                String.format(this.commaFormat, this.owner),
                String.format(this.commaFormat, this.owner_pseudonym),
                String.format(this.commaFormat, this.cae),
                String.format(this.commaFormat, this.source),
                String.format(this.commaFormat, this.type),
                String.format(this.commaFormat, this.percent_owned),
                String.format(this.commaFormat, this.statement_date)
        );
    }
}
