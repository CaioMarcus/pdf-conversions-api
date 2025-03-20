package com.caio.pdf_conversions_api.Export;

import lombok.Setter;

@Setter
public class ResultData implements CsvExportable{
    private Object catalog_id;
    private Object owner_id;
    private Object category;
    private Object characteristic;
    private Object configuration;
    private Object country;
    private Object distributor;
    private Object gross_revenue;
    private Object net_revenue;
    private Object isrc;
    private Object owner;
    private Object owner_pseudonym;
    private Object percent_owned;
    private Object performance_event;
    private Object sales_date;
    private Object source;
    private Object track_artist;
    private Object track_name;
    private Object type;
    private Object units;
    private Object currency;
    private Object statement_date;
    private Object path;


    public String getCsvLine() {
        return String.join(",",
                String.valueOf(catalog_id),
                String.valueOf(owner_id),
                String.valueOf(category),
                String.valueOf(characteristic),
                String.valueOf(configuration),
                String.valueOf(country),
                String.valueOf(distributor),
                String.valueOf(gross_revenue),
                String.valueOf(net_revenue),
                String.valueOf(isrc),
                String.valueOf(owner),
                String.valueOf(owner_pseudonym),
                String.valueOf(percent_owned),
                String.valueOf(performance_event),
                String.valueOf(sales_date),
                String.valueOf(source),
                String.valueOf(track_artist),
                String.valueOf(track_name),
                String.valueOf(type),
                String.valueOf(units),
                String.valueOf(currency),
                String.valueOf(statement_date),
                String.valueOf(path)
        );
    }
}
