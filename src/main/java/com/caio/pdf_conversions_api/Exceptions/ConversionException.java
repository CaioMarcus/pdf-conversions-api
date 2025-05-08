package com.caio.pdf_conversions_api.Exceptions;

public class ConversionException extends RuntimeException {
    public ConversionException() {
    }

    public ConversionException(String message) {
        super(message);
    }
}
