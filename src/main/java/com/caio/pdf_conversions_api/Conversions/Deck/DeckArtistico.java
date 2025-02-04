package com.caio.pdf_conversions_api.Conversions.Deck;

import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class DeckArtistico {
    private int obrasAdicionadas;
    public List<Map<String, String[]>> retornaResultados(String PDFpath, File pasta) throws IOException
    {
        String[] linhasTudo = new String[0];
        String[] linhasObras;
        String linhaData;
        double somaValores = 0;

        String[] arquivosNaPasta = pasta.list();
        Map<String, String[]> Verificacao = new LinkedHashMap<>();
        Map<String, String[]> Resultados = new LinkedHashMap<>();
        Resultados.put("0", new String[] {
                "Obra", "Vendas", "% Base Cessaão", "Total Faixas", "Preço Médio", "Valor Informado", "% Base Calculo",
                "% Part. Faixa", "% Desc. Embalagem", "% Coletivo", "% Royalty", "% Redução", "Valor Final", "Data"
        });
        List<Map<String, String[]>> cedulas = new ArrayList<>();

        assert arquivosNaPasta != null;
        for (String nomeDoArquivo : arquivosNaPasta)
        {
            PDDocument reader = Loader.loadPDF(Path.of(PDFpath, nomeDoArquivo).toFile());
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            PDFTextStripperByArea stripper2 = new PDFTextStripperByArea();

            Rectangle2D rect0;
            Rectangle2D rect1;
            Rectangle2D rectData;
            Rectangle2D rectTudo;

            rectTudo = new Rectangle2D.Double(convDis(6.35), convDis(50.80), convDis(284.16), convDis(148.17)); // Obra
            rect0 = new Rectangle2D.Double(convDis(18.52), convDis(50.80), convDis(22.75), convDis(148.17)); // Obra
            rect1 = new Rectangle2D.Double(convDis(106.36), convDis(50.80), convDis(184.41), convDis(148.17)); // Resto
            rectData = new Rectangle2D.Double(convDis(107.17), convDis(18.72), convDis(85.00), convDis(5.00)); // Data;

            stripper.setSortByPosition(true);
            stripper2.setSortByPosition(true);
            stripper.addRegion("rect0", rect0);
            stripper.addRegion("rect1", rect1);
            stripper.addRegion("rectData", rectData);
            stripper2.addRegion("rectTudo", rectTudo);

            int numOfPag = reader.getNumberOfPages();
            for (int i = 0; i < numOfPag; i++)
            {
                this.obrasAdicionadas = 0;
                stripper.extractRegions(reader.getPage(i));
                stripper2.extractRegions(reader.getPage(i));

                linhasTudo = retiraIndesejado(stripper2.getTextForRegion("rectTudo").split(System.lineSeparator()));
                linhasObras = retiraIndesejado(stripper.getTextForRegion("rect0").split(System.lineSeparator()));
                linhaData = stripper.getTextForRegion("rectData").replace(System.lineSeparator(), "");

                for(String linha : linhasTudo)
                {
                    if (linha.contains("Total :"))
                    {
                        String valorInformado = linha.split(" ")[2];

                        double valorInformadoDouble = Double.
                                parseDouble(Helper.corrigeSeparadorDouble(valorInformado));

                        if (Math.round(valorInformadoDouble) == Math.round(somaValores))
                        {
                            Verificacao.put(String.valueOf(Verificacao.size()), new String[]{"Passou"});
                        }
                        else
                        {
                            Verificacao.put(String.valueOf(Verificacao.size()), new String[]{"Não Passou"});
                        }
                        somaValores = 0;
                    }
                    else
                    {
                        ArrayList<String> linhaComDados = pegaDados(linha, linhasObras);

                        somaValores += Double.parseDouble(linhaComDados.get(linhaComDados.size() - 1));

                        linhaComDados.add(linhaData.replace("De: ", ""));
                        Resultados.put(String.valueOf(Resultados.size()), linhaComDados.toArray(String[]::new));
                    }
                }
            }
            reader.close();
        }
        cedulas.add(Resultados);
        cedulas.add(Verificacao);
        cedulas.add(Verificacao);
        return cedulas;
    }

    public static Double convDis(Double numero) {
        return (numero * 72) / 25.4;
    }

    public static String[] retiraIndesejado(String[] array) {
        String[] indesejados = new String[]
                {"Cliente", "(%) Base", "SubTotal", "Subtotal", "Total Bruto:", "Total Negativo:",
                 "Abatimentos:", "Royalty", "Saldo Devedor:"};
        List<String> newList = new ArrayList<>();
        for (String s : array) {
            if (Arrays.stream(indesejados).noneMatch(s::contains)) {
                newList.add(s);
            }
        }
        return newList.toArray(String[]::new);
    }

    private ArrayList<String> pegaDados(String linha, String[] obras)
    {
        ArrayList<String> linhaComDados = new ArrayList<>();

        for (int indOb = this.obrasAdicionadas; indOb < obras.length; indOb++)
        {
            if (linha.contains(obras[indOb]))
            {
                linhaComDados.add(obras[indOb]);
                this.obrasAdicionadas = indOb;
                break;
            }
        }

        String[] linhasSep = linha.split(" ");
        for (int i = 1; i <= 12; i++)
        {
            linhaComDados.add(1, Helper.corrigeSeparadorDouble(linhasSep[linhasSep.length - i]));
        }
        return linhaComDados;
    }
}
