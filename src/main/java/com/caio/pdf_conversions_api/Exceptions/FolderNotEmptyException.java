package com.caio.pdf_conversions_api.Exceptions;

public class FolderNotEmptyException extends Exception{
    public FolderNotEmptyException(){
        super("The PDFs folder is not empty.");
    }
}
