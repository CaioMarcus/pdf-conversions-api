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

public class DeckEditoras {
    public List<Map<String, String[]>> retornaResultados(String PDFpath, File pasta) throws IOException
    {
        String[] linhasTudo = new String[0];
        String[] linhasObras;
        String[] linhasTipo;
        String linhaData;
        double somaValores = 0;

        String[] arquivosNaPasta = pasta.list();
        Map<String, String[]> Verificacao = new LinkedHashMap<>();
        Map<String, String[]> Resultados = new LinkedHashMap<>();
        Resultados.put("0", new String[] {
                "Cod. Obra", "Titulo", "Tipo", "Vendas", "Recebido", "% Autor", "% Contr.", "Valor Adm.", "Data"
        });
        List<Map<String, String[]>> cedulas = new ArrayList<>();

        assert arquivosNaPasta != null;
        for (String nomeDoArquivo : arquivosNaPasta)
        {
            PDDocument reader = Loader.loadPDF(Path.of(PDFpath, nomeDoArquivo).toFile());
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            PDFTextStripperByArea stripper2 = new PDFTextStripperByArea();

            Rectangle2D rect0;
            Rectangle2D rectTipo;
            Rectangle2D rectData;
            Rectangle2D rectTudo;

            rectTudo = new Rectangle2D.Double(convDis(6.35), convDis(54.00), convDis(284.16), convDis(144.73)); // Obra
            rect0 = new Rectangle2D.Double(convDis(18.52), convDis(54.00), convDis(22.75), convDis(144.73)); // Obra
            rectTipo = new Rectangle2D.Double(convDis(133.61), convDis(54.00), convDis(7.94), convDis(144.73)); // Tipo
            rectData = new Rectangle2D.Double(convDis(84.15), convDis(17.56), convDis(102.00), convDis(5.00)); // Data;

            stripper.setSortByPosition(true);
            stripper2.setSortByPosition(true);
            stripper.addRegion("rect0", rect0);
            stripper.addRegion("rectTipo", rectTipo);
            stripper.addRegion("rectData", rectData);
            stripper2.addRegion("rectTudo", rectTudo);

            int numOfPag = reader.getNumberOfPages();
            for (int i = 0; i < numOfPag; i++)
            {
                int indiceTipo = 0;
                stripper.extractRegions(reader.getPage(i));
                stripper2.extractRegions(reader.getPage(i));

                linhasTipo = retiraIndesejado(stripper.getTextForRegion("rectTipo").split(System.lineSeparator()));
                linhasTudo = retiraIndesejado(stripper2.getTextForRegion("rectTudo").split(System.lineSeparator()));
                linhasObras = retiraIndesejado(stripper.getTextForRegion("rect0").split(System.lineSeparator()));
                linhaData = stripper.getTextForRegion("rectData").replace(System.lineSeparator(), "");

                for (int ind = 0; ind < linhasTudo.length; ind++)
                {
                    String[] linhaSep = linhasTudo[ind].split(" ");

                    if (linhasTudo[ind].contains("Valor:"))
                    {
                        String valorInformado = linhaSep[1];

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
                    } else
                    {
                        String codObra = linhaSep[0];
                        String obra = linhasObras[ind];
                        String tipo = linhasTipo[ind];

                        String posM1 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 1]);
                        String posM2 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 2]);
                        String posM3 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 3]);
                        String posM4 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 4]);
                        String posM5 = " ";

                        if (!tipo.equals("STV"))
                        {
                            posM5 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 5]);;
                        }
                        if (tipo.toCharArray().length > 4)
                        {
                            tipo = "Não possivel ler";
                        }
                        if (!posM5.matches("[0-9.]+") && !posM5.equals(" "))
                        {
                            posM5 = "Não possivel ler";
                            posM4 = "Não possivel ler";
                        }

                        somaValores += Double.parseDouble(Helper.corrigeSeparadorDouble(posM1));

                        Resultados.put(String.valueOf(Resultados.size()), new String[]{
                                codObra, obra, tipo, posM5, posM4, posM3, posM2, posM1, linhaData
                        });
                    }

                    /*
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
                    */
                }
            }
            reader.close();
        }
        cedulas.add(Resultados);
        cedulas.add(Verificacao);
        cedulas.add(Verificacao);
        return cedulas;
    }

    public List<List<String[]>> retornaResultados(PDDocument reader, String nomeDocumento) throws IOException
    {
        String[] linhasTudo;
        String[] linhasObras;
        String[] linhasTipo;
        String linhaData;

        List<String[]> Resultados = new ArrayList<>();
        List<String[]> Verificacao = new ArrayList<>();
        List<List<String[]>> cedulas = new ArrayList<>();

        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        PDFTextStripperByArea stripper2 = new PDFTextStripperByArea();

        Rectangle2D rect0;
        Rectangle2D rectTipo;
        Rectangle2D rectData;
        Rectangle2D rectTudo;
        double somaValores = 0;

        rectTudo = new Rectangle2D.Double(convDis(6.35), convDis(54.00), convDis(284.16), convDis(144.73)); // Obra
        rect0 = new Rectangle2D.Double(convDis(18.52), convDis(54.00), convDis(22.75), convDis(144.73)); // Obra
        rectTipo = new Rectangle2D.Double(convDis(133.61), convDis(54.00), convDis(7.94), convDis(144.73)); // Tipo
        rectData = new Rectangle2D.Double(convDis(84.15), convDis(17.56), convDis(102.00), convDis(5.00)); // Data;

        stripper.setSortByPosition(true);
        stripper2.setSortByPosition(true);
        stripper.addRegion("rect0", rect0);
        stripper.addRegion("rectTipo", rectTipo);
        stripper.addRegion("rectData", rectData);
        stripper2.addRegion("rectTudo", rectTudo);

        int numOfPag = reader.getNumberOfPages();
        for (int i = 0; i < numOfPag; i++)
        {
            int indiceTipo = 0;
            stripper.extractRegions(reader.getPage(i));
            stripper2.extractRegions(reader.getPage(i));

            linhasTipo = retiraIndesejado(stripper.getTextForRegion("rectTipo").split(System.lineSeparator()));
            linhasTudo = retiraIndesejado(stripper2.getTextForRegion("rectTudo").split(System.lineSeparator()));
            linhasObras = retiraIndesejado(stripper.getTextForRegion("rect0").split(System.lineSeparator()));
            linhaData = stripper.getTextForRegion("rectData").replace(System.lineSeparator(), "");

            for (int ind = 0; ind < linhasTudo.length; ind++)
            {
                String[] linhaSep = linhasTudo[ind].split(" ");

                if (linhasTudo[ind].contains("Valor:"))
                {
                    String valorInformado = linhaSep[1];
                    double valorInformadoDouble = Double.parseDouble(Helper.corrigeSeparadorDouble(valorInformado));

                    if (Math.round(valorInformadoDouble) == Math.round(somaValores))
                        Verificacao.add(new String[]{"Passou", "Documento: ", nomeDocumento});
                    else
                        Verificacao.add(new String[]{"Não Passou", "Documento: ", nomeDocumento});

                    somaValores = 0;
                } else
                {
                    String codObra = linhaSep[0];
                    String obra = linhasObras[ind];
                    String tipo = linhasTipo[ind];

                    String posM1 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 1]);
                    String posM2 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 2]);
                    String posM3 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 3]);
                    String posM4 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 4]);
                    String posM5 = " ";

                    if (!tipo.equals("STV"))
                        posM5 = Helper.corrigeSeparadorDouble(linhaSep[linhaSep.length - 5]);;

                    if (tipo.toCharArray().length > 4)
                        tipo = "Não possivel ler";

                    if (!posM5.matches("[0-9.]+") && !posM5.equals(" ")) {
                        posM5 = "Não possivel ler";
                        posM4 = "Não possivel ler";
                    }
                    double valorP = Double.parseDouble(posM1);
                    somaValores += valorP;

                    Resultados.add(new String[]{
                            "(EDITORA) " + obra + " Cod. Obra: " + codObra, tipo, " ", posM3, posM2, posM5, posM4 , posM1, linhaData
                    });
                }

                /*
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
                */
            }
        }
        reader.close();
        cedulas.add(Resultados);
        cedulas.add(Verificacao);
        return cedulas;
    }
    public Double convDis(Double numero) {
        return (numero * 72) / 25.4;
    }

    public String[] retiraIndesejado(String[] array) {
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
    /*
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
    */
}
