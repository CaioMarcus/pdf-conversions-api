package com.caio.pdf_conversions_api.Conversions.Universal.UniversalXlsReader;

public enum Field {
    START_LINE("Start Line"),
    REF("REF"),
    TYPE("Type"),
    FORMAT("Format"),
    ALBUM("Album"),
    SONG_NAME("Song Name"),
    QUANTITY("Quantity"),
    ROYALTY_PERCENTAGE("Royalty Percentage"),
    PRICE("Price"),
    DUE_AMOUNT("Due Amount"),
    ENDING_LINE("Ending Line"),
    BENEFICIARIO("Beneficiario"),
    TOTAL("Total");

    private String value;

    Field(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
