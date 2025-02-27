package com.caio.pdf_conversions_api.Conversions.XLS;

public enum BaseField implements FieldValue{
    DATE("Date"),
    START_LINE("StartLine"),
    DOC_TYPE("DocType");


    private String value;

    BaseField(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
