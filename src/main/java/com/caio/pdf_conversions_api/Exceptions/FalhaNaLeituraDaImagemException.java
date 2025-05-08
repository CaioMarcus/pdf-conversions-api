package com.caio.pdf_conversions_api.Exceptions;

public class FalhaNaLeituraDaImagemException extends ConversionException{

    public FalhaNaLeituraDaImagemException(){
        super("Houve uma Falha ao ler o conteúdo da imagem");
    }
}
