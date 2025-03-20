package com.caio.pdf_conversions_api.Export;

import lombok.Setter;

@Setter
public class VerificationData implements CsvExportable{

    private Object status;
    private Object informed_total;
    private Object summed_total;
    private Object difference;
    private Object document_date;
    private Object document;

    public String getCsvLine() {
        return String.join(",",
                String.valueOf(status),
                String.valueOf(informed_total),
                String.valueOf(summed_total),
                String.valueOf(difference),
                String.valueOf(document_date),
                String.valueOf(document)
        );
    }
}
