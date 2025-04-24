package com.caio.pdf_conversions_api.Export;

import lombok.Setter;

@Setter
public class VerificationData implements CsvExportable {
    private static final String indexLine = "status, " +
            "informed_total, " +
            "summed_total, " +
            "difference, " +
            "document_date, " +
            "document";

    private Object status = "\"\"";
    private Object informed_total = "\"\"";
    private Object summed_total = "\"\"";
    private Object difference = "\"\"";
    private Object document_date = "\"\"";
    private Object document = "\"\"";

    @Override
    public String getIndexLine() {
        return indexLine;
    }

    public String getCsvLine() {
        return String.join(",",
                String.format("\"%s\"", status),
                String.format("\"%s\"", informed_total),
                String.format("\"%s\"", summed_total),
                String.format("\"%s\"", difference),
                String.format("\"%s\"", document_date),
                String.format("\"%s\"", document)
        );
    }
}
