package com.caio.pdf_conversions_api.Exceptions;

public class InvalidFileException extends Exception
{
    private final String file;

    public InvalidFileException(String file)
    {
        super("Invalid file found");
        this.file = file;
    }
    public String getFileName()
    {
        return this.file;
    }
}
