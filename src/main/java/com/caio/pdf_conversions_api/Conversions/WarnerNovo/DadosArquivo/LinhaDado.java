package com.caio.pdf_conversions_api.Conversions.WarnerNovo.DadosArquivo;

public class LinhaDado {
    private String pais, tipo, porcentagemVendasBase, data;
    private Integer vendas;
    private Double precoBase, capa, direitos, porcentagemVendas;

    public LinhaDado(String pais, String tipo, Integer vendas ,Double precoBase, String porcentagemVendasBase,
                     Double capa, Double direitos, String data) {
        this.pais = pais;
        this.tipo = tipo;
        this.precoBase = precoBase;
        this.vendas = vendas;
        this.porcentagemVendasBase = porcentagemVendasBase;
        this.capa = capa;
        this.direitos = direitos;
        this.data = data;
        analisaPorcentagemVendas();
    }

    private void analisaPorcentagemVendas(){
        String[] porcentagemVendasBaseSep = this.porcentagemVendasBase.split(" ");
        if (porcentagemVendasBaseSep.length == 1){
            this.porcentagemVendas = Double.parseDouble(this.porcentagemVendasBase) / 100;
            return;
        }

        Double numerador = null, denominador = Double.parseDouble(porcentagemVendasBaseSep[2].replace("%", ""));
        if (this.porcentagemVendasBase.toUpperCase().contains("*RECEIPTS") || this.porcentagemVendasBase.toUpperCase().contains("*RECEITAS"))
            numerador = precoBase;
        else
            numerador = Double.parseDouble(porcentagemVendasBaseSep[0]);
        this.porcentagemVendas = numerador / denominador;
    }

    public String getPais() {
        return pais;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getVendas() {
        return vendas;
    }

    public Double getPrecoBase() {
        return precoBase;
    }

    public Double getCapa() {
        return capa;
    }

    public Double getDireitos() {
        return direitos;
    }

    public Double getPorcentagemVendas() {
        return porcentagemVendas;
    }

    public String getData() {
        return data;
    }
}
