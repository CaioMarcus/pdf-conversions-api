package com.caio.pdf_conversions_api.Conversions.RelatorioAnalitico;

public class LinhaRelatorioAnalitico {
    private String linha;
    private String[] linhaSeparada;
    private TitularPessoaFisica titular;
    private boolean comUmaData;
    private boolean comDuasDatas;

    public LinhaRelatorioAnalitico(String linha, String[] linhaSeparada) {
        this.comUmaData = false;
        this.comDuasDatas = false;
        this.linha = linha;
        this.linhaSeparada = linhaSeparada;
        this.analisaDatasLinha();
        this.titular = new TitularPessoaFisica(this);
    }
    public String getLinha() {
        return linha;
    }
    public String[] getLinhaSeparada() {
        return linhaSeparada;
    }
    public TitularPessoaFisica getTitular() {
        return titular;
    }
    public
    boolean isComUmaData() {
        return comUmaData;
    }
    public boolean isComDuasDatas() {
        return comDuasDatas;
    }
    private void analisaDatasLinha(){
        int i = 2;
        String dado = this.linhaSeparada[linhaSeparada.length - i];
        if (this.linhaSeparada[0].equals("1969388"))
            System.out.println('d');
        while (!dado.matches("\\d+,\\d{2}") && i <= 6){
            if (dado.matches("\\d{2}/\\d{2}/\\d{2}")) {
                if (this.comUmaData)
                    this.comDuasDatas = true;
                this.comUmaData = true;
            }
            i++;
            dado = this.linhaSeparada[linhaSeparada.length - i];
        }
    }
}
