package com.caio.pdf_conversions_api.Conversions.XLS.Sony;

import com.caio.pdf_conversions_api.Conversions.XLS.FieldValue;

public enum SonyTypes implements FieldValue {
    EXTRATO_EP("EXTRATO_EP"),
    EXTRATO_EX("EXTRATO"),
    EXTRATO_EPX("EXTRATO_EPX");

    private String value;

    SonyTypes(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
