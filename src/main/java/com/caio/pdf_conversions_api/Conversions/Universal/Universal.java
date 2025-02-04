package com.caio.pdf_conversions_api.Conversions.Universal;


import com.caio.pdf_conversions_api.Helpers.ConversionDateParser;
import com.caio.pdf_conversions_api.Helpers.Helper;
import com.caio.pdf_conversions_api.Strippers.PDFLayoutTextStripperByArea;
import io.github.jonathanlink.PDFLayoutTextStripper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Universal extends ConversionDateParser {
    
    private int indiceFonteDeRenda;
    private String editoraAtual = "universal";
    private String[] linhaIndice = new String[]
            {"obra", "territorio", "fonte", "tipo",
            "unidade", "royalty_total", "percentual_titular", "royalty_titular", "editora", "periodo_royalty"};
    private String[] numeroCatalogo;
    private String[] tipoRenda;
    private List<String> erros = new ArrayList<>();

    private int indiceTerritorios, indiceInicioTerritorio;

    @Override
    public void setDatePatterns() {
        this.datePatterns = new LinkedHashMap<>(){
            {
                put(Pattern.compile("(\\d{2}) (\\w+) (\\d{4}) a (\\d{2}) (\\w+) (\\d{4})"), Pair.of(2,3));
            }
        };
    }

    public List<Map<String, String[]>> retornaResultados(String PDFpath, File pasta) throws Exception {
        String obraAtual = "";
        String[] arquivosNaPasta = pasta.list();
        String data = "";
        Map<String, String[]> Resultados = new LinkedHashMap<>();
        Resultados.put("0", linhaIndice);
        Map<String, String[]> verifica = new LinkedHashMap<>();

        List<Map<String, String[]>> cedulas = new ArrayList<>();

        assert arquivosNaPasta != null;
        for (String nomeDoArquivo : arquivosNaPasta) {
            System.out.println("Lendo Arquivo " + nomeDoArquivo);
            PDDocument reader = null;
            PDDocument readerTerritorio = null;
            try {
                reader = Loader.loadPDF(new File(PDFpath + nomeDoArquivo));

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                PDFTextStripperByArea stripper2 = new PDFTextStripperByArea();
                PDFTextStripperByArea stripper3 = new PDFTextStripperByArea();
                PDFTextStripperByArea stripper4 = new PDFTextStripperByArea();
                PDFLayoutTextStripperByArea layoutStripper = new PDFLayoutTextStripperByArea();
                PDFLayoutTextStripperByArea layoutStripper2 = new PDFLayoutTextStripperByArea();

                Rectangle2D rect1 = new Rectangle2D.Double(0, 104, 169, 434);
                Rectangle2D rect2 = new Rectangle2D.Double(504, 104, 337, 434);
                Rectangle2D rect3 = new Rectangle2D.Double(504, 41, 337, 34);
                Rectangle2D rect4 = new Rectangle2D.Double(370, 104, 44.61, 434);
                Rectangle2D rect5 = new Rectangle2D.Double(370, 104, 840, 434);
                Rectangle2D rectFonteRenda = new Rectangle2D.Double(256.79, 104, 112, 434);
                Rectangle2D rectFonteRendaTudo = new Rectangle2D.Double(256.79, 104, 569, 434);
                Rectangle2D rectNumeroCatalogo = new Rectangle2D.Double(406, 104, 85, 434);
                Rectangle2D rectTerritorioExploracao = new Rectangle2D.Double(152, 125, 105, 413);
                Rectangle2D rectTerritorioEFonteDeRenda = new Rectangle2D.Double(169, 125, 400, 413);

                stripper.setSortByPosition(true);
                stripper2.setSortByPosition(true);
                stripper3.setSortByPosition(true);
                stripper4.setSortByPosition(true);

                stripper.addRegion("rect1", rect1);
                stripper.addRegion("rect2", rect2);
                stripper.addRegion("rect3", rect3);
                stripper.addRegion("rect4", rect4);
                stripper.addRegion("rectFonteRenda", rectFonteRenda);
                stripper.addRegion("numeroCatalogo", rectNumeroCatalogo);
                stripper2.addRegion("rect5", rect5);
                stripper3.addRegion("rectFonteRendaTudo", rectFonteRendaTudo);
                layoutStripper.addRegion("TerritorioExploracao", rectTerritorioExploracao);
                stripper4.addRegion("TerritorioEFonteDeRenda", rectTerritorioEFonteDeRenda);
                layoutStripper2.addRegion("TerritorioEFonteDeRenda", rectTerritorioEFonteDeRenda);
                //layoutStripper.addRegion("TerritorioEFonteDeRenda", rectTerritorioExploracao);

                double somatorio = 0.0;
                double somatorioParaObra = 0.0;

                for (int i = 0; i < reader.getNumberOfPages(); i++) {
                    System.out.println(nomeDoArquivo + " " + i);
                    int indiceObra = 0;
                    int indiceRenda = 0;
                    int indiceTerritorioEFonte = 0;
                    int indiceTerritorioExploracao = 0;
                    indiceFonteDeRenda = 0;
                    int indiceRendaTudo = 0;


                    if (i == 215) {
                        System.out.println("");
                    }
                    /*
                    PDDocument finalReader = reader;
                    int finalI = i;
                    Thread lerStripper = new Thread(() -> {
                        try {
                            stripper.extractRegions(finalReader.getPage(finalI));
                        } catch (Exception ignore){
                        }
                    });
                    Thread lerStripper2 = new Thread(() -> {
                        try {
                            stripper2.extractRegions(finalReader.getPage(finalI));
                        } catch (Exception ignore){
                        }
                    });
                    Thread lerStripper3 = new Thread(() -> {
                        try {
                            stripper3.extractRegions(finalReader.getPage(finalI));
                        } catch (Exception ignore){
                        }
                    });
                    Thread lerStripper4 = new Thread(() -> {
                        try {
                            stripper4.extractRegions(finalReader.getPage(finalI));
                        } catch (Exception ignore){
                        }
                    });
                    Thread lerStripperLayout = new Thread(() -> {
                        try {
                            layoutStripper.extractRegions(finalReader.getPage(finalI));
                        } catch (Exception ignore){
                        }
                    });
                    Thread lerStripperLayout2 = new Thread(() -> {
                        try {
                            layoutStripper2.extractRegions(finalReader.getPage(finalI));
                        } catch (Exception ignore){
                        }
                    });
                    lerStripper.start();
                    lerStripper2.start();
                    lerStripper3.start();
                    lerStripper4.start();
                    lerStripperLayout.start();
                    lerStripperLayout2.start();
                    try{
                        lerStripper.join();
                        lerStripper2.join();
                        lerStripper3.join();
                        lerStripper4.join();
                        lerStripperLayout.join();
                        lerStripperLayout2.join();
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    */
                    try {
                        stripper.extractRegions(reader.getPage(i));
                        stripper2.extractRegions(reader.getPage(i));
                        stripper3.extractRegions(reader.getPage(i));
                        stripper4.extractRegions(reader.getPage(i));
                        layoutStripper.extractRegions(reader.getPage(i));
                        layoutStripper2.extractRegions(reader.getPage(i));
                    }catch (Exception e){
                        System.out.println("a");
                    }
                    String linhaData = stripper.getTextForRegion("rect3").replace(System.lineSeparator(), "");
                    String[] rendaTudo = stripper2.getTextForRegion("rect5").split(System.lineSeparator());
                    tipoRenda = stripper.getTextForRegion("rect4").split(System.lineSeparator());

                    if (linhaData.split(" ").length != 1) {
                        data = convertDate(linhaData);
                    } else if (i == 0) {
                        stripper.extractRegions(reader.getPage(1));
                        stripper2.extractRegions(reader.getPage(1));
                        linhaData = stripper.getTextForRegion("rect3").replace(System.lineSeparator(), "");
                        tipoRenda = stripper.getTextForRegion("rect4").split(System.lineSeparator());
                        rendaTudo = stripper2.getTextForRegion("rect5").split(System.lineSeparator());
                        data = convertDate(linhaData);
                        i++;
                    }

                    String[] obras = stripper.getTextForRegion("rect1").split(System.lineSeparator());
                    String[] resto = stripper.getTextForRegion("rect2").split(System.lineSeparator());
                    String[] fonteDeRenda = stripper.getTextForRegion("rectFonteRenda").split(System.lineSeparator());
                    String[] territorioExploracao = layoutStripper.getTextForRegion("TerritorioExploracao").split("\n");
                    String[] territoriosAteData = stripper4.getTextForRegion("TerritorioEFonteDeRenda").split(System.lineSeparator());
                    String[] territoriosAteDataComEspacos = layoutStripper2.getTextForRegion("TerritorioEFonteDeRenda").split("\n");
                    //String[] territorioComTudo = stripper4.getTextForRegion("TerritorioEFonteDeRenda").split(System.lineSeparator());
                    //String[] territorioComTudotst = layoutStripper.getTextForRegion("TerritorioEFonteDeRenda").split("\n");
                    //var tst = pegaTerritorios(readerTerritorio, i);
                    List<String> fonteDeRendaTudo = List.of(stripper3.getTextForRegion("rectFonteRendaTudo").split(System.lineSeparator()));

                    if (obras.length > 0 && obras[1].contains("Sumário finan"))
                        continue;

                    numeroCatalogo = stripper.getTextForRegion("numeroCatalogo").split(System.lineSeparator());

                    fonteDeRendaTudo = consertFonteRendaTudo(fonteDeRendaTudo);

                    if (i == 5105 && nomeDoArquivo.equals("Suel - 1Âº 2016 - 1Âº 2021.pdf"))
                        System.out.println("asdf");

                    List<String> listaTipoDeRenda = montaColunaDeRenda(rendaTudo, tipoRenda);
                    List<String> listaFonteDeRenda = pegaFonteDeRenda2(fonteDeRendaTudo, fonteDeRenda);
                    List<String> territoriosComTudo = montaColunaTerritorioERenda(territoriosAteData);

                    List<String> territoriosComTudoComEspacos = montaColunaTerritorioERenda(territoriosAteDataComEspacos);
                    String renda;
                    try {
                        possuiTerritorioInicio(territoriosComTudoComEspacos.get(0), true);
                    } catch (Exception ignore){
                    }

                    List<String> territorios = corrigeTerritorios(territorioExploracao, territoriosComTudo);
                    if (territorios.size() == 0){
                        erros.add("Falha ao detectar o território. Verifique detalhadamente as obras na pagina " + (i + 1) + "; Arquivo: " + nomeDoArquivo + " Indice Obra " + (Resultados.size() - 1));
                        indiceInicioTerritorio += 20;
                        territorios.add(String.format("N/D (PAG. %d); Arq: %s", (i + 1), nomeDoArquivo));
                    }

                    for (int j = 0; j < resto.length; j++) {
                        String restoAtual = resto[j];
                        String proximaLinhaResto = " ";

                        if (j != resto.length - 1) {
                            proximaLinhaResto = resto[j + 1];
                        } else if (restoAtual.contains("Totais finais")) {
                            String[] restoSep = restoAtual.split(" ");
                            String numeroConv = restoSep[restoSep.length - 1].replace("R$", "").replace(",", "");
                            double valorTotal = Double.parseDouble(numeroConv);

                            if (Math.round(valorTotal) != Math.round(somatorio)) {
                                verifica.put(String.valueOf(verifica.size()), new String[]
                                        {"NÃO BATEU", "SOMA REALIZADA:", String.valueOf(somatorio), "VALOR CONSTADO:", numeroConv, data});
                            }
                            somatorio = 0.0;
                            break;
                        }

                        if (resto[j].contains("Total da obra") && j != resto.length - 1 && !proximaLinhaResto.contains("Totais finais")) {
                            if (obras.length > 2)
                                indiceObra++;
                            try {
                                while (!verificaMaiuscula(obras[indiceObra])) {
                                    indiceObra++;
                                }
                            } catch (IndexOutOfBoundsException e) {
                                //reader.close();
                                erros.add("Ocorreu um erro na página: " + (i + 1) + ". Modifique o XLS para correção. Arquivo: " + nomeDoArquivo + " Indice Obra: " + (Resultados.size() - 1));
                                indiceObra = obras.length - 2;
                            }

                        } else if (!resto[j].contains("Total da obra")) {
                            obraAtual = obras[indiceObra];
                            String linhaTerritorio = null;
                            try {
                                linhaTerritorio = territorios.get(indiceTerritorioExploracao);
                            } catch (IndexOutOfBoundsException e){
                                System.out.println("a");
                            }
                            String[] linhaRestoSep = restoAtual.split(" ");
                            String PosM1 = linhaRestoSep[linhaRestoSep.length - 1];
                            String PosM2 = linhaRestoSep[linhaRestoSep.length - 2];
                            String PosM3 = linhaRestoSep[linhaRestoSep.length - 3];
                            String PosM4 = linhaRestoSep[linhaRestoSep.length - 4];

                            if (linhaRestoSep.length == 4) {
                                PosM4 = " ";
                            }

                            PosM1 = PosM1.replace(",", "");
                            PosM3 = PosM3.replace(",", "");

                            Resultados.put(String.valueOf(Resultados.size()), new String[]
                                    {obraAtual, linhaTerritorio, listaFonteDeRenda.get(indiceRendaTudo),
                                    listaTipoDeRenda.get(indiceRenda), PosM4, PosM3, PosM2, PosM1, editoraAtual, data});

                            somatorio += Double.parseDouble(PosM1);
                            somatorioParaObra += Double.parseDouble(PosM1);
                            indiceRenda++;
                            indiceTerritorioEFonte++;
                            indiceRendaTudo++;

                            if (indiceTerritorioEFonte < territoriosComTudoComEspacos.size() &&
                                    possuiTerritorioInicio(territoriosComTudoComEspacos.get(indiceTerritorioEFonte), false)){
                                indiceTerritorioExploracao++;
                            }

                        }
                    /* Usar Para fazer a verificação detalhada;
                    if (resto[j].contains("Total da obra")) {
                        String[] linhaCortada = resto[j].split(" ");
                        String totalObraString = linhaCortada[linhaCortada.length - 1]
                                .replace("R$", "")
                                .replace(",", "");
                        double totalObra = Double.parseDouble(totalObraString);
                        verifica.put(String.valueOf(verifica.size()), new String[]
                                {obraAtual, String.valueOf(somatorio), "Pagina" + String.valueOf(i), String.valueOf(somatorioParaObra), totalObraString, data});
                        if (Math.round(somatorioParaObra) != Math.round(totalObra)) {
                            verifica.put(String.valueOf(verifica.size()), new String[]
                                    {"NÃO BATEU", obraAtual, String.valueOf(somatorio), "Pagina" + String.valueOf(i), String.valueOf(somatorioParaObra), totalObraString, data});
                        }
                        somatorioParaObra = 0.0;
                    }
                   */
                    }
                }
                reader.close();
            } catch (Exception e){
                if (reader != null) reader.close();
                throw e;
            }
        }
        System.out.println("Validando Dados");
        cedulas.add(Resultados);
        cedulas.add(verifica);
        cedulas.add(verifica);
        return cedulas;
    }

    private boolean possuiTerritorioInicio(String linha, boolean linhaInicio){
        char[] linhaChars = linha.toCharArray();
        for (int i = 0; i < linhaChars.length; i++) {
            if (linhaChars[i] != ' ') {
                if (linhaInicio) {
                    indiceInicioTerritorio = i;
                    return true;
                }
                if (i + 3 <= indiceInicioTerritorio)
                    return false;
                return i - 10 <= indiceInicioTerritorio;
            }
        }
        return false;
    }



    /*
    private static List<String> pegaFonteDeRenda(String[] linhasFonteRendaTudo, String[] linhasFonteRenda){
        if (linhasFonteRenda.length == 0) return null;
        List<String> resultados = new ArrayList<>();
        int indice = 0;
        int indiceTudo = 0;
        String proxLinha = linhasFonteRendaTudo[0];
        while (indiceTudo < linhasFonteRendaTudo.length ){
            String fonteDeRenda = "";
            while(proxLinha.contains("Total da obra") || proxLinha.contains("Totais finais")){
                indiceTudo++;
                proxLinha = linhasFonteRendaTudo[indiceTudo];
            }
            boolean proximaLinhaComValor = false;
            while(!proximaLinhaComValor){
                if (indiceTudo == linhasFonteRendaTudo.length){
                    indiceTudo++;
                    break;
                }
                if (Arrays.stream(linhasFonteRenda).anyMatch(proxLinha::contains)){
                    fonteDeRenda += " " + linhasFonteRenda[indice];
                    indice++;
                }
                if (indiceTudo == linhasFonteRendaTudo.length - 1) proxLinha = "";
                else proxLinha = linhasFonteRendaTudo[indiceTudo + 1];
                String[] proxLinhaSep = proxLinha.split(" ");
                proximaLinhaComValor = proxLinhaSep[proxLinhaSep.length - 1].matches("[0-9.,-]+");
                indiceTudo++;
            }
            if (fonteDeRenda.equals("")) continue;
            resultados.add(fonteDeRenda.replaceFirst(" ", ""));
        }
        return resultados;
    }
    */

    private List<String> pegaFonteDeRenda2(List<String> linhasFonteRendaTudo, String[] linhasFonteRenda){
        if (linhasFonteRenda.length == 0) return null;
        List<String> resultados = new ArrayList<>();
        String proxLinha, linhaMontada = "", rendaAtual;
        String[] proxLinhaSep;
        int indiceRenda = 0;
        for (int i = 0; i < linhasFonteRendaTudo.size(); i++){
            rendaAtual = linhasFonteRenda[indiceRenda];
            if (i < linhasFonteRendaTudo.size() - 1) {
                proxLinha = linhasFonteRendaTudo.get(i + 1);
                proxLinhaSep = proxLinha.split(" ");
            } else{
                linhaMontada += rendaAtual + " ";
                resultados.add(linhaMontada);
                break;
            }
            if (Arrays.stream(linhasFonteRenda).anyMatch(proxLinha::contains)) {
                linhaMontada += rendaAtual + " ";
                indiceRenda++;
                if (proxLinhaSep[proxLinhaSep.length - 1].matches("[0-9.,-]+")) {
                    resultados.add(linhaMontada);
                    linhaMontada = "";
                }
            }
        }
        return resultados;
    }

    private boolean verificaLinhaFonteDeRenda(String proxLinha, String[] linhasFonteRenda){
        return Arrays.stream(numeroCatalogo).noneMatch(proxLinha::equals) &&
                Arrays.stream(tipoRenda).noneMatch(proxLinha::equals) &&
                Arrays.stream(linhasFonteRenda).anyMatch(proxLinha::contains);
    }

    private List<String> consertFonteRendaTudo(List<String> fonteDeRenda){
        List<String> fonteRenda = new ArrayList<>(fonteDeRenda);
        for (int i = fonteRenda.size() - 1; i >= 0; i--){
            String renda = fonteRenda.get(i);
            if (Arrays.asList(numeroCatalogo).contains(renda) || Arrays.asList(tipoRenda).contains(renda)
            || renda.contains("Total da obra") || renda.contains("Totais finais")
            || verificaCombinacoes(tipoRenda, numeroCatalogo, renda)){
                fonteRenda.remove(i);
            }
        }
        return fonteRenda;
    }

    private boolean verificaCombinacoes(String[] listaA, String[] listaB, String renda){
        for (String palavraA : listaA){
            for (String palavraB : listaB){
                String palavrasJuntas = palavraA + " " + palavraB;
                if (renda.equals(palavrasJuntas)) return true;
            }
        }
        return false;
    }

    public boolean verificaMaiuscula(String s) {
        for (int letra = 0; letra < s.length(); letra++) {
            if (Character.isLowerCase(s.charAt(letra))) {
                return false;
            }
        }
        return true;
    }

    public void formataExportaPlanilhaUn(List<Map<String, String[]>> entrada, String nomeSaida,
                                             String diretorioSaida, boolean fazerResumo, boolean fazerVerificacao)
            throws IOException, ParseException {

        Map<String, String[]> planilha = entrada.get(0);
        Map<String, String[]> resumo = null;
        Map<String, String[]> verificacao = null;
        int tam = planilha.get("0").length;

        // Cria planilha
        @SuppressWarnings("resource")
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("PDFs");
        XSSFSheet resume = workbook.createSheet("Resumo");
        XSSFSheet verifica = workbook.createSheet("verificacao");
        XSSFRow row;

        // Estilo todas as cedulas
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont fonte = workbook.createFont();
        fonte.setFontHeightInPoints((short) 15);
        style.setFont(fonte);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Estilo cedulas do indice
        XSSFCellStyle indice = workbook.createCellStyle();
        XSSFFont negrito = workbook.createFont();
        indice.setAlignment(HorizontalAlignment.CENTER);
        negrito.setFontHeightInPoints((short) 15);
        negrito.setBold(true);
        indice.setFont(negrito);
        indice.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        indice.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        indice.setBorderLeft(BorderStyle.THIN);
        indice.setBorderRight(BorderStyle.THIN);

        // Estilo cedulas Data
        XSSFCellStyle data = workbook.createCellStyle();
        CreationHelper cr = workbook.getCreationHelper();
        data.setDataFormat(cr.createDataFormat().getFormat("dd/mm/yyyy"));
        data.setFont(fonte);
        style.setFont(fonte);
        data.setAlignment(HorizontalAlignment.RIGHT);
        data.setBorderLeft(BorderStyle.THIN);
        data.setBorderRight(BorderStyle.THIN);

        // Estilo Numeros
        XSSFCellStyle numeros = workbook.createCellStyle();
        DataFormat df = workbook.createDataFormat();
        numeros.setDataFormat(df.getFormat("#,##0.#;\\#,##0.#"));
        numeros.setFont(fonte);
        numeros.setBorderLeft(BorderStyle.THIN);
        numeros.setBorderRight(BorderStyle.THIN);

        // Real
        XSSFCellStyle real = workbook.createCellStyle();
        DataFormat df2 = workbook.createDataFormat();
        real.setDataFormat(df2.getFormat("R$ #,##0.#####;\\R$ -#,##0.#####"));
        real.setFont(fonte);
        real.setBorderLeft(BorderStyle.THIN);
        real.setBorderRight(BorderStyle.THIN);

        // Real Negrito
        XSSFCellStyle realNeg = workbook.createCellStyle();
        realNeg.setDataFormat(df2.getFormat("R$ #,##0.#####;\\R$ -#,##0.#####"));
        realNeg.setFont(negrito);
        realNeg.setBorderLeft(BorderStyle.THIN);
        realNeg.setBorderRight(BorderStyle.THIN);

        //SemRateio
        XSSFCellStyle semRateio = workbook.createCellStyle();
        semRateio.setFont(negrito);
        semRateio.setBorderLeft(BorderStyle.THIN);
        semRateio.setBorderRight(BorderStyle.THIN);

        //AlinharCentro
        XSSFCellStyle centro = workbook.createCellStyle();
        centro.setFont(fonte);
        centro.setAlignment(HorizontalAlignment.CENTER);

        //Porcentagem
        XSSFCellStyle porcentagem = workbook.createCellStyle();
        DataFormat df3 = workbook.createDataFormat();
        porcentagem.setDataFormat(df3.getFormat("0.00%"));
        porcentagem.setFont(fonte);
        porcentagem.setBorderLeft(BorderStyle.THIN);
        porcentagem.setBorderRight(BorderStyle.THIN);

        //Notação Científica
        XSSFCellStyle notacaoCientifica = workbook.createCellStyle();
        DataFormat dfnotacao = workbook.createDataFormat();
        notacaoCientifica.setDataFormat(dfnotacao.getFormat("0.0#E+00"));
        notacaoCientifica.setFont(fonte);
        notacaoCientifica.setBorderLeft(BorderStyle.THIN);
        notacaoCientifica.setBorderRight(BorderStyle.THIN);

        // Parte Funcional

        // Transforma os valores dos objetos em cédulas
        Set<String> keyid = planilha.keySet();
        int rowid = 0;
        String cedulaBase = "0";
        System.out.println("Adicionando Elementos...");

        for (String key : keyid) {
            row = spreadsheet.createRow(rowid++);
            String[] objectArr = planilha.get(key);
            int cellid = 0;

            for (String obj : objectArr) {
                boolean sinais = false;
                if (obj == null){
                    obj = "";
                }
                if (obj.contains("+") || obj.contains("-")) {
                    sinais = true;
                }
                Cell cell = row.createCell(cellid++);
                if (obj.matches("-?[0-9.]+,-?[0-9]+") && !planilha.get(cedulaBase)[cellid - 1].contains("percentual_titular")) {
                    if (objectArr[0].equals("Total da Categoria") || objectArr[0].equals("TOTAL ")) {
                        cell.setCellValue(Double.parseDouble(Helper.corrigeSeparadorDouble(obj)));
                        cell.setCellStyle(realNeg);
                    } else {
                        cell.setCellValue(Double.parseDouble(Helper.corrigeSeparadorDouble(obj)));
                        cell.setCellStyle(real);
                    }
                } else if (Helper.verificaData(obj)){
                    cell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").parse(obj));
                    cell.setCellStyle(data);
                } else if (objectArr[3].equals("") || objectArr[0].equals("Total da Categoria")
                        || objectArr[0].equals("Totais por Rubrica") || objectArr[0].equals("TOTAL ")) {
                    cell.setCellValue(obj);
                    cell.setCellStyle(semRateio);
                } else if (objectArr[0].equals("obra") || objectArr[objectArr.length - 1].equals("data") || objectArr[0].equals("data")) {
                    cedulaBase = String.valueOf(key);
                    cell.setCellValue(obj);
                    cell.setCellStyle(indice);
                } else if (!obj.equals(" ") && planilha.get(cedulaBase)[cellid - 1].contains("percentual_titular")) {
                    cell.setCellValue(Double.parseDouble(obj)/100);
                    cell.setCellStyle(porcentagem);
                } else if (obj.matches("[0-9-]+") && !planilha.get(cedulaBase)[cellid - 1].equals("unidade")) {
                    cell.setCellValue(Integer.parseInt(Helper.corrigeSeparadorDouble(obj)));
                    cell.setCellStyle(numeros);
                } else if(obj.matches("[0-9.-]{2,}") && obj.contains(".")) {
                    cell.setCellValue(Double.parseDouble(obj));
                    cell.setCellStyle(real);
                } else if(obj.split("E")[0].matches("[0-9,]+.[0-9]+") && obj.contains("E") && sinais) {
                    cell.setCellValue(Helper.converteDeNotacao(Helper.corrigeSeparadorDouble(obj)));
                    cell.setCellStyle(notacaoCientifica);
                }else{
                    cell.setCellValue(obj);
                    cell.setCellStyle(style);
                }
            }
        }

        if (fazerResumo) {
            resumo = entrada.get(1);
            tam = Math.max(resumo.get("1").length, tam);
            for (int i = 0; i < resumo.size(); i++) {
                row = resume.createRow(i);
                String[] linha = resumo.get(String.valueOf(i));
                int idCedula = 0;
                for (String palavra : linha) {
                    Cell cell = row.createCell(idCedula++);
                    if (palavra.matches("-?[0-9.]+,-?[0-9]+")) {
                        if (linha[0].equals("Total da Categoria") || linha[0].equals("TOTAL ")) {
                            cell.setCellValue(Double.parseDouble(Helper.corrigeSeparadorDouble(palavra)));
                            cell.setCellStyle(realNeg);
                        } else {
                            cell.setCellValue(Double.parseDouble(Helper.corrigeSeparadorDouble(palavra)));
                            cell.setCellStyle(real);
                        }
                    } else if (Helper.verificaData(palavra)) {
                        cell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").parse(palavra));
                        cell.setCellStyle(data);
                    } else if (linha[0].equals("TOTAL ")) {
                        cell.setCellValue(palavra);
                        cell.setCellStyle(semRateio);
                    } else if (linha[0].equals("Totais por Rubrica") || linha[0].equals("OBRA")) {
                        cell.setCellValue(palavra);
                        cell.setCellStyle(indice);
                    } else if (idCedula == 5) {
                        cell.setCellValue(palavra);
                        cell.setCellStyle(centro);
                    } else {
                        cell.setCellValue(palavra);
                        cell.setCellStyle(style);
                    }
                }
            }
        }

        if (fazerVerificacao) {
            verificacao = entrada.get(2);
            for (int i = 0; i < verificacao.size(); i++) {
                row = verifica.createRow(i);
                String[] linha = verificacao.get(String.valueOf(i));
                int idCedula = 0;
                for (String palavra : linha) {
                    Cell cell = row.createCell(idCedula++);
                    if (palavra.matches("-?[0-9.]+,-?[0-9]+")) {
                        cell.setCellValue(Double.parseDouble(Helper.corrigeSeparadorDouble(palavra)));
                        cell.setCellStyle(real);
                    } else {
                        cell.setCellValue(palavra);
                        cell.setCellStyle(style);
                    }
                }
            }
        }

        System.out.println("Terminando ajustes...");



        for (int i = 0; i < tam + 1; i++) {
            spreadsheet.autoSizeColumn(i);
            resume.autoSizeColumn(i);
            verifica.autoSizeColumn(i);
        }

        // Exporta o arquivo
        FileOutputStream out = new FileOutputStream(diretorioSaida + nomeSaida + ".xlsx");
        workbook.write(out);
        out.close();
        if (erros.size() == 0){
            System.out.println("Conversão concluída com êxito. Nome do arquivo salvo: " + nomeSaida + ".xlsx");
        }
        else {
            System.out.println("Conversão concluída com ERROS. Nome do arquivo salvo: " + nomeSaida + ".xlsx; Erros:\n");
            for (String erro : erros) System.out.println(erro);
        }


    }
    
    public String retornaData(String[] dataCrua) {
    	String primeiroMes = " ";
        String segundoMes = " ";
    	
    	String[] meses = new String[] { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto",
                "Setembro", "Outubro", "Novembro", "Dezembro" };
    	
    	for (int mes = 0; mes < meses.length; mes++) {
            try {
                if (dataCrua[dataCrua.length - 2].equalsIgnoreCase(meses[mes])) {
                    segundoMes = String.valueOf(mes + 1);
                }
            } catch (IndexOutOfBoundsException e){
                System.out.println("a");
            }
            if (dataCrua[dataCrua.length - 6].equalsIgnoreCase(meses[mes])) {
                primeiroMes = String.valueOf(mes + 1);
            }
            if (!primeiroMes.equals(" ") && !segundoMes.equals(" ")) {
                break;
            }
        }
    	String data = dataCrua[dataCrua.length - 7] + "/" + primeiroMes + "/" + dataCrua[dataCrua.length - 5]
                + " a " + dataCrua[dataCrua.length - 3] + "/" + segundoMes + "/" + dataCrua[dataCrua.length - 1];
    	
    	return data;
    }
    public String calibraCol(String[] col, String linhaTudo) {
        int i = 0;
        String linhaTudoCortada = linhaTudo.replace(" ", "");
        String colunaCortada = col[i].replace(" ", "");
        while (!linhaTudoCortada.contains(colunaCortada)) {
            i++;
            colunaCortada = col[i].replace(" ", "");
        }
        return col[i];
    }

    public boolean verificaItemEmComum(String[] um, String dois) {
        for (String i : um) {
            if (dois.contains(i)) {
                return true;
            }
        }
        return false;
    }

    public List<String> montaColunaDeRenda(String[] rendaComTudo, String[] somenteRenda) {
        String rendaAtual = "";
        boolean adicionarNaLista = false;
        List<String> rendasOrganizadas = new ArrayList<>();
        for (int g = 0; g < rendaComTudo.length; g++) {
            String[] rendaTudoSep = rendaComTudo[g].split(" ");
            String valor = rendaTudoSep[rendaTudoSep.length - 1];

            if (verificaItemEmComum(somenteRenda, rendaComTudo[g])) {
                if (rendaTudoSep.length < 4 && !rendaComTudo[g].contains("Total da obra")) {
                    rendaAtual += " " + calibraCol(somenteRenda, rendaComTudo[g]);
                } else if (!rendaComTudo[g].contains("Total da obra")) {
                    if (adicionarNaLista) {
                        rendasOrganizadas.add(rendaAtual);
                        rendaAtual = "";
                    }
                    rendaAtual = calibraCol(somenteRenda, rendaComTudo[g]);
                    adicionarNaLista = true;
                }
                if (g == rendaComTudo.length - 1) {
                    rendasOrganizadas.add(rendaAtual);
                }
            } else if(adicionarNaLista) {
                rendasOrganizadas.add(rendaAtual);
                rendaAtual = "";
                adicionarNaLista = false;
            }
        }
        return rendasOrganizadas;
    }

    public List<String> montaColunaTerritorioERenda(String[] territoriosAteRecebimentos){
        List<String> resultados = new ArrayList<>();
        String linhaAtual = "", territorioAtual = "";
        int indiceTerritorio = 0;
        for (int i = 0; i < territoriosAteRecebimentos.length; i++){
            boolean pegarLinha = false;
            String territorioComTudo = territoriosAteRecebimentos[i].stripTrailing();
            String[] territorioComTudoSeparado = territorioComTudo.split(" ");
            String valor = territorioComTudoSeparado[territorioComTudoSeparado.length - 1];

            if (valor.matches("\\d{2}/\\d{2}-\\d{2}/\\d{2}") ||
                    valor.matches("[A-Za-z(]+\\d{2}/\\d{2}-\\d{2}/\\d{2}")
                    || valor.matches("1-1")
                    || valor.matches("1-\\d{2}/\\d{2}")
                    || valor.matches("\\d{2}/\\d{2}-1")
            )
                resultados.add(territorioComTudo);

        }
        return resultados;
    }
    private String pegaTerritorioEmString(String[] linhaTerritorioSep, String[] territorios){
        List<String> territoriosAdicionados = new ArrayList<>();
        String territorio = "";
        for (String palavraTerritorio : linhaTerritorioSep){
            String palavraEncontrada = Arrays.stream(territorios).filter(x -> x.contains(palavraTerritorio)).findFirst().orElse(null);
            if (palavraEncontrada != null && !territoriosAdicionados.contains(palavraEncontrada)){
                territorio += palavraEncontrada + " ";
                territoriosAdicionados.add(palavraEncontrada);
            }
        }
        return territorio.trim();
    }
    /*
    private static String[] corrigeTerritorio(String[] listaTerritorios){
        List<String> territoriosEditados = new ArrayList<>();



    }
    */
    private List<String> pegaTerritorios(PDDocument readerTerritorio, int paginaAtual) throws IOException {
        PDPage pagina = readerTerritorio.getPage(0);
        pagina.getCropBox().setUpperRightX((float) (10 ));
        pagina.getCropBox().setUpperRightY((float) (10));
        PDFLayoutTextStripper stripper = new PDFLayoutTextStripper();
        stripper.setSortByPosition(false);

        stripper.setStartPage(paginaAtual);
        stripper.setEndPage(paginaAtual + 1);
        String[] tst = stripper.getText(readerTerritorio).split("\n");
        return null;
    }

    private List<String> corrigeTerritorios(String[] territorios, List<String> territoriosAteData){
        List<String> listaMontada = new ArrayList<>();
        StringBuilder territorioAtual = new StringBuilder();
        String linhaAnteriorComTerritorio = "";
        indiceTerritorios = -1;
        for (int i = 0; i < territorios.length; i++){
            String palavra = territorios[i].trim();

            if (palavra.isEmpty()) {
                if (territorioAtual.length() > 0)
                    listaMontada.add(territorioAtual.toString().trim());
                territorioAtual.setLength(0);
                linhaAnteriorComTerritorio = "";
            } else if (!possuiTerritorioInicio(territorios[i], false))
                continue;
            else {
                pegaLinhaConrreta(territoriosAteData, palavra);
                if (indiceTerritorios > 0 && territoriosAteData.get(indiceTerritorios - 1).equals(linhaAnteriorComTerritorio)) {
                    listaMontada.add(territorioAtual.toString().trim());
                    territorioAtual.setLength(0);
                }
                //Gambiarra. Preciso considerar quando tem um territorio curto, e logo embaixo um longo (mais de 1 linha); Ver pela data de recebimento.
                if (territorioAtual.toString().equals("SPAIN ")) {
                    listaMontada.add(territorioAtual.toString().trim());
                    territorioAtual.setLength(0);
                    territorioAtual.append(palavra).append(" ");
                } else
                    territorioAtual.append(palavra).append(" ");
                if (i >= territorios.length - 1)
                    listaMontada.add(territorioAtual.toString().trim());

                try {
                    linhaAnteriorComTerritorio = territoriosAteData.get(indiceTerritorios);
                } catch (IndexOutOfBoundsException e){
                    System.out.println(e);
                }

            }

        }
        return listaMontada;
    }

    private void pegaLinhaConrreta(List<String> terr, String stringAchar){
        for(int indice = indiceTerritorios + 1; indice < terr.size(); indice++){
            String[] terrSep = terr.get(indice).split(" ");
            if (stringAchar.contains(terrSep[0]) && (terrSep[terrSep.length - 1].matches("\\d{2}/\\d{2}-\\d{2}/\\d{2}")
                    || terrSep[terrSep.length - 1].matches("1-1")
                    || terrSep[terrSep.length - 1].matches("1-\\d{2}/\\d{2}")
                    || terrSep[terrSep.length - 1].matches("\\d{2}/\\d{2}-1")
            ) && terrSep[0].length() > 1) {
                indiceTerritorios = indice;
                return;
            }
        }
    }

    private String pegaTerritorio(String[] linhaAtualSep, List<String> territorios){
        if (territorios.stream().anyMatch(linhaAtualSep[0]::contains))
            return territorios.get(indiceTerritorios++);
        return "";
    }

    public List<String> getErros(){
        if (erros.size() == 0)
            return null;
        return erros;
    }
}
