package com.caio.pdf_conversions_api.Models;

public enum ConversionStatus {
    IN_PROGRESS("Progress"),
    ERROR("Error"),
    COMPLETED("Completed"),
    RESULT("Result");

    private final String eventName;

    ConversionStatus(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
