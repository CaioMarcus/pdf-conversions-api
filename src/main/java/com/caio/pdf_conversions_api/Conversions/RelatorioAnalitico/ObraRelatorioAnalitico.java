package com.caio.pdf_conversions_api.Conversions.RelatorioAnalitico;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObraRelatorioAnalitico {
    private List<LinhaRelatorioAnalitico> linhas;
    private String codigoObra;
    private String ISWC;
    private String nomeObra;
    private Map<String, Double> somaLinks;
    private boolean inconsistente;
    private boolean autorEditadoPorAlguem;

    public ObraRelatorioAnalitico(String linha) {
        String[] linhaSep = linha.split(" ");
        this.codigoObra = linhaSep[0];
        this.ISWC = linhaSep[1];
        this.linhas = new ArrayList<>();
        this.somaLinks = new LinkedHashMap<>();
        this.nomeObra = LeitorInformacoes.getObraLinha(linha);
        this.inconsistente = true;
        this.autorEditadoPorAlguem = false;
    }

    public void adicionaLinha(LinhaRelatorioAnalitico linha){
        this.linhas.add(linha);
        Double valorGuardado = somaLinks.getOrDefault(linha.getTitular().getLink(), 0.0);
        somaLinks.put(linha.getTitular().getLink(), valorGuardado + linha.getTitular().getValor());
    }

    public List<LinhaRelatorioAnalitico> getLinhas() {
        return linhas;
    }

    public String getCodigoObra() {
        return codigoObra;
    }

    public String getISWC() {
        return ISWC;
    }

    public String getNomeObra() {
        return nomeObra;
    }
    public Double getValorLink(String link){
        return somaLinks.get(link);
    }

    public boolean isInconsistente() {
        return inconsistente;
    }

    public void setInconsistente(boolean inconsistente) {
        this.inconsistente = inconsistente;
    }

    public boolean isAutorEditadoPorAlguem() {
        return autorEditadoPorAlguem;
    }

    public void setAutorEditadoPorAlguem(boolean autorEditadoPorAlguem) {
        this.autorEditadoPorAlguem = autorEditadoPorAlguem;
    }
}
