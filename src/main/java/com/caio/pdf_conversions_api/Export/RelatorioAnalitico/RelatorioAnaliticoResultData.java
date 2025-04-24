package com.caio.pdf_conversions_api.Export.RelatorioAnalitico;

import com.caio.pdf_conversions_api.Export.ResultData;
import lombok.Getter;

public class RelatorioAnaliticoResultData extends ResultData {

    private static final String indexLine =
            "catalog_id, " +
                    "owner_id, " +
                    "category, " +
                    "characteristic, " +
                    "configuration, " +
                    "country, " +
                    "distributor, " +
                    "gross_revenue, " +
                    "net_revenue, " +
                    "isrc, " +
                    "iswc, " +
                    "cae, " +
                    "owner, " +
                    "owner_pseudonym, " +
                    "percent_owned, " +
                    "performance_event, " +
                    "sales_date, " +
                    "source, " +
                    "track_artist, " +
                    "track_name_registered, " +
                    "type, " +
                    "units, " +
                    "currency, " +
                    "registered_date, " +
                    "path";

    @Override
    public String getIndexLine() {
        return indexLine;
    }
}
