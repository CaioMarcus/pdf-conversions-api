package com.caio.pdf_conversions_api.Conversions.PDFs.RelatorioAnalitico;

import com.caio.pdf_conversions_api.Export.RelatorioAnalitico.RelatorioAnaliticoResultData;
import com.caio.pdf_conversions_api.Export.ResultData;

public class RelatorioAnaliticoOldExportFormat extends RelatorioAnalitico{
    public RelatorioAnaliticoOldExportFormat(String pdfPath, String xlsName, String[] filesToConvert) {
        super(pdfPath, xlsName, filesToConvert);
    }

    @Override
    protected RelatorioAnaliticoResultData getResultData(Object percentOwned,
                                       Object catalogId,
                                       Object owner,
                                       Object ownerPseudonym,
                                       Object trackName,
                                       Object source,
                                       Object iswc,
                                       Object isrc,
                                       Object cae,
                                       Object statementDate,
                                       Object ownerId,
                                       Object type,
                                       Object performanceEvent,
                                       Object path) {
        return OldRelatorioAnaliticoExportData.fromRelatorioAnaliticoResultData(super.getResultData(percentOwned,
                catalogId,
                owner,
                ownerPseudonym,
                trackName,
                source,
                iswc,
                isrc,
                cae,
                statementDate,
                ownerId,
                type,
                performanceEvent,
                path));
    }
}
