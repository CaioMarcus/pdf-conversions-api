package com.caio.pdf_conversions_api.Exceptions;

public class FolderNotEmptyException extends ConversionException{
    public FolderNotEmptyException(){
        super("The PDFs folder is not empty.");
    }
}
