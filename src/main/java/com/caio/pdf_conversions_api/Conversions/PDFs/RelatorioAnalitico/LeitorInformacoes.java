package com.caio.pdf_conversions_api.Conversions.PDFs.RelatorioAnalitico;

import java.util.List;

public class LeitorInformacoes {
    private static List<String> linhasObras;
    private static List<String> linhasTitular;
    private static int indiceLinhasObras;
    private static int indiceLinhasTitular;

    public static void setDados(List<String> linhasTitular, List<String> linhasObras){
        LeitorInformacoes.linhasObras = linhasObras;
        LeitorInformacoes.linhasTitular = linhasTitular;
        LeitorInformacoes.indiceLinhasObras = 0;
        LeitorInformacoes.indiceLinhasTitular = 0;
    }

    public static String getTitularLinha(String linha){
        for (; LeitorInformacoes.indiceLinhasTitular < LeitorInformacoes.linhasTitular.size(); LeitorInformacoes.indiceLinhasTitular++){
            String nomeTitular = LeitorInformacoes.linhasTitular.get(LeitorInformacoes.indiceLinhasTitular);
            if (linha.contains(nomeTitular))
                return nomeTitular;
        }
        return null;
    }

    public static String getObraLinha(String linha){
        for (; LeitorInformacoes.indiceLinhasObras < LeitorInformacoes.linhasObras.size(); LeitorInformacoes.indiceLinhasObras++){
            String nomeObra = LeitorInformacoes.linhasObras.get(LeitorInformacoes.indiceLinhasObras);
            if (linha.contains(nomeObra))
                return nomeObra;
        }
        return null;
    }

}
