package com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras.Models;

public enum OutrasEditorasColumn {
    PRODUTO("PRODUTO"),
    OBRA("OBRA"),
    REPASSANTE("REPASSANTE"),
    VENDAS("VENDAS"),
    VLR_UNIT("VLR.UNIT"),
    PERCENTAGE("PERCENTAGE"),
    IGNORE("IGNORE"),
    REPASSE("REPASSE");

    OutrasEditorasColumn(String columnName) {
        this.columnName = columnName;
    }

    private String columnName;

    public String getColumnName() {
        return columnName;
    }

}



