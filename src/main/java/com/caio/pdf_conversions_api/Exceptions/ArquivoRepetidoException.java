package com.caio.pdf_conversions_api.Exceptions;

import java.util.Map;

public class ArquivoRepetidoException extends ConversionException{

    private final String mensagem;

    public ArquivoRepetidoException(String nomeArquivo, String data) {
        this.mensagem = String.format("Já existe um arquivo com a mesma data (%s) na conversão: %s", data, nomeArquivo);
    }

    public ArquivoRepetidoException(Map<String, String> repetidos){
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("Arquivos repetidos encontrados:\n\n");

        for (String nome : repetidos.keySet()){
            mensagem.append(String.format("Nome: %s, data: %s \n", nome, repetidos.get(nome)));
        }

        this.mensagem = String.valueOf(mensagem);
    }

    @Override
    public String getMessage() {
        return mensagem;
    }
}
