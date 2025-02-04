package com.caio.pdf_conversions_api.Conversions.Deck;

import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Deck2
{
    private int[] tamanhoEsperadoDasColunas = new int[7];
    private String ultimaLinha;
    private String currenType = "";
    private int tipo;

    public List<Map<String, String[]>> retornaResultados(String PDFpath, File pasta) throws IOException
    {
        java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.OFF);

        String rateio;
        String obra = "";
        String data = "";
        String linhaData;
        String tipoObras;
        String abatimento;
        String valBruto;
        String porcContr;
        String porcAutor;
        String recebido;
        String[] linhasTudo = new String[0];
        String[] linhasResto = new String[0];
        String[] restoSep;
        String[] tudoSep;
        String[] linhasObras;
        String[] arquivosNaPasta = pasta.list();
        double totalObras;
        int indiceObras;
        boolean possuiColunaPosicao;
        boolean passouPosicao;
        boolean comPreco;

        //String[] naoQuer = new String[] {"% Autor", "Valor Bruto:", "Valor:", "Valor bruto:"};

        Map<String, String[]> Resultados = new LinkedHashMap<>();
        //Resultados.put("0", new String[]{"Obra", "Tipo", "Recebido", "% Autor", "% Contr.", "Valor Bruto / Vendas / Exibição", "Abatimento / Recebido", "Valor Líquido/Pago", "Data"});

        Map<String, String[]> verifica = new LinkedHashMap<>();
        List<Map<String, String[]>> cedulas = new ArrayList<>();

        assert arquivosNaPasta != null;
        for (String nomeDoArquivo : arquivosNaPasta) {
            abatimento = "";
            valBruto = "";
            porcContr = "";
            porcAutor = "";
            recebido = "";

            System.out.println("Lendo Arquivo " + nomeDoArquivo);
            PDDocument reader = Loader.loadPDF(new File(PDFpath + nomeDoArquivo));

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            PDFTextStripperByArea stripper2 = new PDFTextStripperByArea();

            Rectangle2D rect0;
            Rectangle2D rect1;
            Rectangle2D rectData;
            Rectangle2D rectTudo;

            if (this.tipo == 0 || this.tipo == 2) {
                //rectTudo = new Rectangle2D.Double(convDis(6.35), convDis(45.77), convDis(284.43), convDis(151.61)); // Obra
                rect0 = new Rectangle2D.Double(convDis(6.35), convDis(45.77), convDis(49.21), convDis(151.61)); // Obra
                rect1 = new Rectangle2D.Double(convDis(170.00), convDis(45.77), convDis(120.77), convDis(10.00)); // Resto
                rectTudo = new Rectangle2D.Double(convDis(170.00), convDis(45.77), convDis(120.77), convDis(151.61)); // Resto2
                rectData = new Rectangle2D.Double(convDis(118.78), convDis(16.81), convDis(76.29), convDis(20.00)); // Data;
            } else {
                rectTudo = new Rectangle2D.Double(convDis(6.35), convDis(56.89), convDis(284.43), convDis(136.26)); // Obra
                rect0 = new Rectangle2D.Double(convDis(6.35), convDis(56.89), convDis(64.69), convDis(136.26)); // Obra
                rect1 = new Rectangle2D.Double(convDis(180.00), convDis(56.89), convDis(193.79), convDis(136.26)); // Resto
                rectData = new Rectangle2D.Double(convDis(118.78), convDis(25.0), convDis(76.29), convDis(15.0)); // Data;
            }

            stripper.setSortByPosition(true);
            stripper2.setSortByPosition(true);
            stripper.addRegion("rect0", rect0);
            //stripper.addRegion("rect1", rect1);
            stripper.addRegion("rectData", rectData);
            stripper2.addRegion("rectTudo", rectTudo);

            int numOfPag = reader.getNumberOfPages();
            totalObras = 0.0;
            indiceObras = 0;
            tipoObras = "";

            possuiColunaPosicao = false;
            passouPosicao = false;
            float incremento = 2;

            boolean chegouTitulo = false;
            ArrayList<String> titulo = new ArrayList<>();
            String[] tituloSemObras = new String[0];
            boolean tituloDefinido = false;
            boolean definirTipo = true;
            int tamanhoAnterior = 1;
            boolean pularPagina = false;
            for (int i = 0; i < numOfPag; i++)
            {
                boolean terminou = false;
                rect1 = new Rectangle2D.Double(convDis(170.00), convDis(45.77), convDis(120.77), convDis(10.00)); // Resto
                stripper.addRegion("rect1", rect1);
                stripper.extractRegions(reader.getPage(i));
                linhaData = stripper.getTextForRegion("rectData").replace(System.lineSeparator(), "");
                linhasObras = retiraIndesejado(stripper.getTextForRegion("rect0").split(System.lineSeparator()));
                data = linhaData.replace("De", "");
                Arrays.fill(this.tamanhoEsperadoDasColunas, 2);

                while (!terminou)
                {
                    int tamanhoLinhaTipo3 = 0;
                    stripper.extractRegions(reader.getPage(i));

                    linhasResto = retiraIndesejado(stripper.getTextForRegion("rect1").split(System.lineSeparator()));
                    //linhaData = stripper.getTextForRegion("rectData").replace(System.lineSeparator(), "");

                    if (linhasResto.length > 0 && linhasResto[linhasResto.length - 1].contains("Valor Liquido"))
                    {
                        if (linhasResto.length > 1)
                        {
                            rect1 = defineTituloComoPrimeira(rect1, reader, i);
                            tamanhoAnterior = 0;
                            Arrays.fill(this.tamanhoEsperadoDasColunas, 2);
                        }
                        if (definirTipo)
                        {
                            getObraType(reader, stripper, rect1, i);
                            definirTipo = false;
                        }
                        titulo = reorganizaTitulo(linhasResto[0]);
                        tituloSemObras = titulo.toArray(String[]::new);
                        titulo.add(0, "Tipo");
                        titulo.add(0, "Obras");
                        titulo.add("Data");
                        if (!tituloDefinido)
                        {
                            Resultados.put("0", titulo.toArray(String[]::new));
                            tituloDefinido = true;
                        }
                    }
                    else
                    {
                        definirTipo = true;
                    }

                    if (linhasResto.length > tamanhoAnterior && tamanhoAnterior > 0)
                    {
                        //rect1 = addDocumentLine(rect1, reader, i, linhasResto.length);
                        List<String> linha = this.getDataFromLine(rect1, reader, i, tituloSemObras);
                        if (!linha.get(1).contains(".com.br") && !linha.get(0).contains("DECK PRODUC?ES ARTISTICAS LTDA.") )
                        {
                            linha.add(0, this.currenType);
                            linha.add(data);
                            Resultados.put(String.valueOf(Resultados.size()), linha.toArray(String[]::new));
                        }
                    }
                    tamanhoAnterior = linhasResto.length;
                    rect1 = new Rectangle2D.Double(rect1.getX(), rect1.getY(), rect1.getWidth(), rect1.getHeight() + incremento); // Resto
                    stripper.removeRegion("rect1");
                    stripper.addRegion("rect1", rect1);

                    if (linhasResto.length > 0 && linhasResto[linhasResto.length - 1].contains(".com.br"))
                    {
                        terminou = true;
                        stripper.removeRegion("rect1");
                    }
                }
            }
        reader.close();
        }
        cedulas.add(Resultados);
        return cedulas;
    }

    private Rectangle2D defineTituloComoPrimeira(Rectangle2D rectBase, PDDocument doc, int pag) throws IOException
    {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        Rectangle2D rect = rectBase;
        String[] linhas = new String[0];
        boolean reorganizado = false;
        boolean passouTituloAntigo = false;
        double incrementoBaixo = 2;

        while(!reorganizado)
        {
            stripper.removeRegion("rect1");
            stripper.addRegion("rect1", rect);
            stripper.extractRegions(doc.getPage(pag));
            linhas = stripper.getTextForRegion("rect1").split(System.lineSeparator());

            if (linhas[0].contains("Valor Liquido") && passouTituloAntigo)
            {
                reorganizado = true;
            }
            else if (!linhas[0].contains("Valor Liquido"))
            {
                passouTituloAntigo = true;
                rect = new Rectangle2D.Double(rect.getX(), rect.getY() + incrementoBaixo, rect.getWidth(), rect.getHeight() - incrementoBaixo);
            }
            else
            {
                rect = new Rectangle2D.Double(rect.getX(), rect.getY() + incrementoBaixo, rect.getWidth() , rect.getHeight() - incrementoBaixo);
            }
        }
        return rect;
    }

    public static Rectangle2D addDocumentLine(Rectangle2D rectBase, PDDocument doc, int pag, int tamanhoAtual) throws IOException
    {
        String[] linhas = new String[0];
        boolean chegouNovaLinha = false;
        double incrementoBaixo = 1;
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        Rectangle2D rect = rectBase;
        while (!chegouNovaLinha)
        {
            stripper.removeRegion("rect1");
            stripper.addRegion("rect1", rect);
            stripper.extractRegions(doc.getPage(pag));
            linhas = stripper.getTextForRegion("rect1").split(System.lineSeparator());

            if (linhas.length > tamanhoAtual) chegouNovaLinha = true;
            else rect = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight() + incrementoBaixo);
        }
        return rect;
    }

    public List<String> getDataFromLine(Rectangle2D rectBase, PDDocument doc, int pag, String[] titulo) throws IOException {
        String[] linhas = new String[0];
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        Rectangle2D rect = new Rectangle2D.Double(rectBase.getX(), rectBase.getY(), 10, rectBase.getHeight());
        double incrementoX = 5;
        boolean fimDasColunas = false;
        int indicePalavraTitulo = 0;
        List<String> linhaCompleta = new ArrayList<>();
        linhaCompleta.add(pegaNomeObra(doc, stripper, rect, pag));
        while (!fimDasColunas)
        {
            boolean chegouPalavraTitulo = false, saiuPalavraTitulo = false;
            while (!chegouPalavraTitulo || !saiuPalavraTitulo)
            {
                stripper.removeRegion("rect1");
                stripper.addRegion("rect1", rect);
                stripper.extractRegions(doc.getPage(pag));
                String[] linhasAtual = retiraIndesejado(stripper.getTextForRegion("rect1").split(System.lineSeparator()));

                if (linhasAtual[0].equalsIgnoreCase(titulo[indicePalavraTitulo]))
                {
                    rect = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth() + incrementoX, rect.getHeight());
                    linhas = linhasAtual;
                    chegouPalavraTitulo = true;
                    if (titulo[indicePalavraTitulo].equals(titulo[titulo.length - 1]))
                    {
                        break;
                    }
                }
                else if (chegouPalavraTitulo)
                {
                    saiuPalavraTitulo = true;
                    rect = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth() - incrementoX, rect.getHeight());
                }
                else
                {
                    rect = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth() + incrementoX, rect.getHeight());
                    linhas = linhasAtual;
                }
            }

            if (linhas.length == this.tamanhoEsperadoDasColunas[indicePalavraTitulo])
            {
                linhaCompleta.add(Helper.corrigeSeparadorDouble(linhas[linhas.length - 1]));
                this.tamanhoEsperadoDasColunas[indicePalavraTitulo] ++;
            }
            else
            {
                linhaCompleta.add(" ");
            }

            if (linhas[0].equals(titulo[titulo.length - 1]))
                fimDasColunas = true;
            else
            {
                indicePalavraTitulo++;
                rect = new Rectangle2D.Double(rect.getX() + rect.getWidth(), rect.getY(), 10, rect.getHeight());
            }
        }
        return linhaCompleta;
    }
    private String pegaNomeObra(PDDocument doc, PDFTextStripperByArea stripper, Rectangle2D rect, int pag)
            throws IOException
    {
        rect = new Rectangle2D.Double(convDis(6.35), rect.getY(), convDis(49.21), rect.getHeight());
        stripper.removeRegion("rect1");
        stripper.addRegion("rect1", rect);
        stripper.extractRegions(doc.getPage(pag));
        String[] linhasAtual = stripper.getTextForRegion("rect1").split(System.lineSeparator());
        return linhasAtual[linhasAtual.length - 1];
    }

    private void getObraType(PDDocument doc, PDFTextStripperByArea stripper, Rectangle2D rect, int pag)
            throws IOException
    {
        rect = new Rectangle2D.Double(convDis(6.35), rect.getY(), convDis(49.21), rect.getHeight());
        stripper.removeRegion("rect1");
        stripper.addRegion("rect1", rect);
        stripper.extractRegions(doc.getPage(pag));
        String[] linhasAtual = stripper.getTextForRegion("rect1").split(System.lineSeparator());
        this.currenType = linhasAtual[linhasAtual.length - 2].replace("Tipo: ", "");
    }

        /*
    public static String[] getDataFromLine(Double[] rectValues) throws IOException {
        boolean endOfLine = false;
        float incremento = 2;
        while (!endOfLine)
        {
            Rectangle2D rect = new Rectangle2D.Double(convDis(rectValues[0]), convDis(rectValues[1]), convDis(10.00), convDis(rectValues[3]));
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.addRegion("rect", rect);



        }

    }
    */
    public static int[] indexarColunas(String[] titulo)
    {
        int[] posicoes = new int[7];
        int pos = 0;
        String[][] palavrasChave = new String[][]{   {"Valor Liquido", "Valor Pago"}, {"Abatimento", "Recebido"},
                                                {"Vendas", "Valor Bruto"}, {"% Contrato", "% Contr."}, {"% Autor"},
                                                {"Preço", "Recebido"}, {"Vendas"}};

        for (int i = 0; i < palavrasChave.length; i++)
        {
            for (int j = titulo.length - 1 - pos; j >= 0; j--)
            {
                if (Arrays.stream(palavrasChave[i]).anyMatch(titulo[j]::equalsIgnoreCase))
                {
                   posicoes[pos] = j;
                   pos++;
                   break;
                }
                else if (j == 0)
                {
                    posicoes[pos] = -1;
                    pos++;
                }
            }
        }
        return posicoes;
    }

    public ArrayList<String> reorganizaTitulo(String titulo)
    {
        String[] primeiraPalavraDasCompostas = new String[]{"%", "Valor"};
        String[] tituloSep = titulo.split(" ");
        ArrayList<String> tituloOrganizado = new ArrayList<>();
        boolean pularUm = false;

        if (this.tipo == 2)
        {

        }
        else
        {
            for (int i = 0; i < tituloSep.length; i++) {
                if (Arrays.stream(primeiraPalavraDasCompostas).anyMatch(tituloSep[i]::equalsIgnoreCase)) {
                    tituloOrganizado.add(tituloSep[i] + " " + tituloSep[i + 1]);
                    i++;
                } else {
                    tituloOrganizado.add(tituloSep[i]);
                }
            }
        }
        return tituloOrganizado;
    }

    public static String calibraCol(String[] col, String linhaTudo) {
        int i = 0;
        String linhaTudoCortada = linhaTudo.replace(" ", "");
        String colunaCortada = col[i].replace(" ", "");
        while (!linhaTudoCortada.contains(colunaCortada)) {
            i++;
            colunaCortada = col[i].replace(" ", "");
        }
        return col[i];
    }

    private void verificaTipo(PDDocument doc) throws IOException {
        String[] linhas;
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        Rectangle2D geral = new Rectangle2D.Double(convDis(0.00 ), convDis(0.00), convDis(68.39), convDis(44.19));
        stripper.addRegion("Geral", geral);
        stripper.setSortByPosition(true);
        stripper.extractRegions(doc.getPage(0));
        linhas = retiraIndesejado(stripper.getTextForRegion("Geral").split(System.lineSeparator()));
        if (linhas[0].contains("Processamento:")) {
            this.tipo =  1;
        } else if (linhas[0].contains("Artista:")) {
            this.tipo =  2;
        } else {
            this.tipo = 0;
        }
    }

    public static Double convDis(Double numero) {
        return (numero * 72) / 25.4;
    }

    public static String[] retiraIndesejado(String[] array) {
        String[] indesejados = new String[]
                {"Saldo Inicial Adiantamento:", "Abatimento de Adiantamento:", "Valor Bruto:",
                 "Saldo Final Adiantamento:", "Valor líquido:", "Banco:", "Endereço:", "Beneficiário",
                 "Pseudônimo", "E-Mail:", "Total editora", "Agência:", "C/C:"};
        List<String> newList = new ArrayList<>();
        for (String s : array) {
            if (Arrays.stream(indesejados).noneMatch(s::contains)) {
                newList.add(s);
            }
        }
        return newList.toArray(String[]::new);
    }

}
