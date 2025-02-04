package com.caio.pdf_conversions_api.Conversions.RelatorioAnalitico;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TitularPessoaFisica {
    private String nome;
    private String codigoCae;
    private String codigoEscritor;
    private String categoria;
    private String link;
    private Double valor;
    private boolean editora;
    private boolean editadoPorAlguem;


    public TitularPessoaFisica(LinhaRelatorioAnalitico linhaRelatorio) {
        String[] linhaSep = linhaRelatorio.getLinhaSeparada();
        String linha = linhaRelatorio.getLinha();

        this.codigoEscritor = linhaSep[0];
        this.nome = LeitorInformacoes.getTitularLinha(linha);
        this.link = linhaSep[linhaSep.length - 1];
        if (linhaRelatorio.isComDuasDatas()){
            this.valor = trataValor(linhaSep[linhaSep.length - 4]);
            this.categoria = linhaSep[linhaSep.length - 5];
        } else if (linhaRelatorio.isComUmaData()){
            this.valor = trataValor(linhaSep[linhaSep.length - 3]);
            this.categoria = linhaSep[linhaSep.length - 4];
        } else{
            this.valor = trataValor(linhaSep[linhaSep.length - 2]);
            this.categoria = linhaSep[linhaSep.length - 3];
        }
        this.editora = this.categoria.equals("E");
        this.editadoPorAlguem = false;
        this.codigoCae = Arrays.stream(linhaSep).filter(x -> x.matches("\\d{5}.\\d{2}.\\d{2}.\\d{2}")).collect(Collectors.joining());
    }
    private Double trataValor(String valor){
        int quantiaVirgulas = valor.length() - valor.replaceAll(",", "").length();
        for (; quantiaVirgulas > 1; quantiaVirgulas--)
            valor = valor.replaceFirst(",", "");

        return Double.parseDouble(valor.replace(",", "."));
    }
    public String getNome() {
        return nome;
    }
    public String getCodigoCae() {
        return codigoCae;
    }

    public String getCodigoEscritor() {
        return codigoEscritor;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getLink() {
        return link;
    }

    public Double getValor() {
        return valor;
    }

    public boolean isEditora() {
        return editora;
    }

    public boolean isEditadoPorAlguem() {
        return editadoPorAlguem;
    }

    public void setEditadoPorAlguem(boolean editadoPorAlguem) {
        this.editadoPorAlguem = editadoPorAlguem;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
