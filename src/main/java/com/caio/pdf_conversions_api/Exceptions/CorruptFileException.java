package com.caio.pdf_conversions_api.Exceptions;

public class CorruptFileException extends ConversionException
{
    private final String file;

    public CorruptFileException(String fileName)
    {
        super("Corrupted file found " + fileName);
        this.file = fileName;
    }
    public String getErrorFile()
    {
        return this.file;
    }
}
