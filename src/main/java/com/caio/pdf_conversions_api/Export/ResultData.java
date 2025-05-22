package com.caio.pdf_conversions_api.Export;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResultData implements CsvExportable {
    private final String commaFormat = "\"%s\"";

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
                "track_name, " +
                "type, " +
                "units, " +
                "currency, " +
                "statement_date, " +
                "path";

    private Object catalog_id = "";
    private Object owner_id = "";
    private Object category = "";
    private Object characteristic = "";
    private Object configuration = "";
    private Object country = "";
    private Object distributor = "";
    private Object gross_revenue = "";
    private Object net_revenue = "";
    private Object isrc = "";
    private Object iswc = "";
    private Object cae = "";
    private Object owner = "";
    private Object owner_pseudonym = "";
    private Object percent_owned = "";
    private Object performance_event = "";
    private Object sales_date = "";
    private Object source = "";
    private Object track_artist = "";
    private Object track_name = "";
    private Object type = "";
    private Object units = "";
    private Object currency = "";
    private Object statement_date = "";
    private Object path = "";


    @Override
    public String getIndexLine() {
        return indexLine;
    }

    public String getCsvLine() {
        return String.join(",",
                String.format(this.commaFormat, catalog_id),
                String.format(this.commaFormat, owner_id),
                String.format(this.commaFormat, category),
                String.format(this.commaFormat, characteristic),
                String.format(this.commaFormat, configuration),
                String.format(this.commaFormat, country),
                String.format(this.commaFormat, distributor),
                gross_revenue.equals("") ? String.format(this.commaFormat, gross_revenue) : String.valueOf(gross_revenue),
                net_revenue.equals("") ? String.format(this.commaFormat, net_revenue) : String.valueOf(net_revenue),
                String.format(this.commaFormat, isrc),
                String.format(this.commaFormat, iswc),
                String.format(this.commaFormat, cae),
                String.format(this.commaFormat, owner),
                String.format(this.commaFormat, owner_pseudonym),
                percent_owned.equals("") ? String.format(this.commaFormat, percent_owned) : String.valueOf(percent_owned),
                String.format(this.commaFormat, performance_event),
                String.format(this.commaFormat, sales_date),
                String.format(this.commaFormat, source),
                String.format(this.commaFormat, track_artist),
                String.format(this.commaFormat, track_name),
                String.format(this.commaFormat, type),
                units.equals("") ? String.format(this.commaFormat, units) : String.valueOf(units),
                currency.equals("") ? String.format(this.commaFormat, currency) : String.valueOf(currency),
                String.format(this.commaFormat, statement_date),
                String.format(this.commaFormat, path)
        );
    }

}
