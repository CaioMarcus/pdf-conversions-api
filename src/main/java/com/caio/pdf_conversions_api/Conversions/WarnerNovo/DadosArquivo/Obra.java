package com.caio.pdf_conversions_api.Conversions.WarnerNovo.DadosArquivo;

import java.util.ArrayList;
import java.util.List;

public class Obra {
    private String nome, configuracao;
    List<LinhaDado> linhasDados;

    public Obra(String nome, String configuracao) {
        this.nome = nome;
        this.configuracao = configuracao;
        this.linhasDados = new ArrayList<>();
    }

    public void adicionaLinhaDado(LinhaDado linha){
        this.linhasDados.add(linha);
    }

    public String getNome() {
        return nome;
    }

    public String getConfiguracao() {
        return configuracao;
    }

    public List<LinhaDado> getLinhasDados() {
        return linhasDados;
    }
}
