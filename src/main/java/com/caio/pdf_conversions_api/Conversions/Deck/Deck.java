package com.caio.pdf_conversions_api.Conversions.Deck;

import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Deck {

    private Rectangle2D rect0;
    private Rectangle2D rect1;
    private Rectangle2D rectData;
    private Rectangle2D rectTudo;
    private boolean changeRect;
    private boolean fileNameAsData;
    private String[] naoQuer;

    public List<Map<String, String[]>> retornaResultados(String PDFpath, File pasta) throws IOException {
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
        String[] linhasResto;
        String[] restoSep;
        String[] tudoSep;
        String[] linhasObras;
        String[] arquivosNaPasta = pasta.list();
        double totalObras;
        int indiceObras;
        int indiceResto;
        boolean possuiColunaPosicao;
        boolean passouPosicao;
        boolean comPreco;
        boolean comArtista;

        Map<String, String[]> resultados = new LinkedHashMap<>();
        resultados.put("0", new String[]{"Obra", "Tipo", "Recebido", "% Autor", "% Contr.", "Valor Bruto / Vendas / Exibição", "Abatimento / Recebido", "Valor Líquido/Pago", "Data"});

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
            PDDocument reader = Loader.loadPDF(Path.of(PDFpath, nomeDoArquivo).toFile());

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            PDFTextStripperByArea stripper2 = new PDFTextStripperByArea();

            naoQuer = new String[] {"% Autor", "Valor Bruto:", "Valor:", "Valor bruto:"};
            changeRect = true;
            fileNameAsData = false;
            int tipo = verificaTipo(reader, 0);
            if (tipo == 4){
                DeckEditoras deckEditoras = new DeckEditoras();
                List<List<String[]>> resultadosEditora = deckEditoras.retornaResultados(reader, nomeDoArquivo);

                for (String[] resultado : resultadosEditora.get(0))
                    resultados.put(String.valueOf(resultados.size()), resultado);

                for (String[] verificacao : resultadosEditora.get(1))
                    verifica.put(String.valueOf(verifica.size()), verificacao);

                continue;
            }

            if (tipo == 0) {
                rectTudo = new Rectangle2D.Double(convDis(6.35), convDis(45.77), convDis(284.43), convDis(151.61)); // Obra
                rect0 = new Rectangle2D.Double(convDis(6.35), convDis(45.77), convDis(49.21), convDis(151.61)); // Obra
                rect1 = new Rectangle2D.Double(convDis(186.00), convDis(45.77), convDis(104.77), convDis(151.61)); // Resto
                rectData = new Rectangle2D.Double(convDis(118.78), convDis(16.81), convDis(76.29), convDis(20.00)); // Data;
            } else if (changeRect) {
                rectTudo = new Rectangle2D.Double(convDis(6.35), convDis(52.90), convDis(284.43), convDis(141.26)); // Obra
                rect0 = new Rectangle2D.Double(convDis(6.35), convDis(52.90), convDis(64.69), convDis(141.26)); // Obra
                rect1 = new Rectangle2D.Double(convDis(180.00 ), convDis(52.90), convDis(193.79), convDis(141.26)); // Resto
                rectData = new Rectangle2D.Double(convDis(118.78), convDis(25.0), convDis(76.29), convDis(15.0)); // Data;
            }

            stripper.setSortByPosition(true);
            stripper2.setSortByPosition(true);
            stripper.addRegion("rect0", rect0);
            stripper.addRegion("rect1", rect1);
            stripper.addRegion("rectData", rectData);
            stripper2.addRegion("rectTudo", rectTudo);

            int numOfPag = reader.getNumberOfPages();
            totalObras = 0.0;
            indiceObras = 0;
            tipoObras = "";

            possuiColunaPosicao = false;
            passouPosicao = false;
            for (int i = 0; i < numOfPag; i++) {
                int tamanhoLinhaTipo3 = 0;
                indiceResto = 0;
                stripper.extractRegions(reader.getPage(i));
                stripper2.extractRegions(reader.getPage(i));

                linhasTudo = retiraIndesejado(stripper2.getTextForRegion("rectTudo").split(System.lineSeparator()));
                linhasObras = retiraIndesejado(stripper.getTextForRegion("rect0").split(System.lineSeparator()));
                linhasResto = retiraIndesejado(stripper.getTextForRegion("rect1").split(System.lineSeparator()));
                linhaData = stripper.getTextForRegion("rectData").replace(System.lineSeparator(), "");
                comPreco = false;
                comArtista = false;
                for (String s : linhasTudo) {

                    tudoSep = s.split(" ");
                    rateio = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 1]);

                    if (s.contains("Vendas % Autor % Contrato Recebido Valor Pago")) {
                        tipo = 3;
                        comPreco = false;
                    } else if (s.contains("Vendas Preço % Autor % Contrato Recebido Valor Pago")) {
                        if (s.contains("Artista"))
                            comArtista = true;
                        comPreco = true;
                    } else if (s.contains("Preço % Autor % Contrato Vendas Recebido Valor Pago")) {
                        comPreco = false;
                    }

                    if (s.contains("Posição"))
                        passouPosicao = true;
                    if (passouPosicao && !(s.contains("Tipo de Distribuição:") || s.contains("Tipo:")))
                        continue;
                    else
                        passouPosicao = false;
                    if (Arrays.stream(naoQuer).noneMatch(s::contains) && rateio.matches("-?[0-9.,]+.-?[0-9]+")) {
                        if (passouPosicao && tudoSep[0].matches("[0-9]+")) {
                            possuiColunaPosicao = true;
                            obra = "";
                            for (int pal = 1; pal < tudoSep.length - 2; pal ++) {
                                obra += tudoSep[pal] + " ";
                            }
                            obra.trim();
                            rateio = tudoSep[tudoSep.length - 1].replace(".", "")
                                                                .replace(",", ".");
                        } else {
                            obra = calibraCol(linhasObras, s);
                            abatimento = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 2]);
                            valBruto = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 3]);
                            porcContr = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 4]);
                            porcAutor = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 5]);
                            recebido = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 6]);
                        }
                        if (tipo == 0) {
                            data = linhaData.replace("De", "");
                            resultados.put(String.valueOf(resultados.size()), new String[]
                                    {obra, tipoObras, abatimento, valBruto, porcContr, porcAutor, recebido, rateio, data});
                        } else if (tipo == 3 && !possuiColunaPosicao) {
                            data = linhaData;
                            if (tamanhoLinhaTipo3 == 0) {
                                int indiceLinhasTudo = 2;
                                String[] linhasRestoSep = linhasResto[indiceLinhasTudo].split(" ");
                                while (!linhasRestoSep[linhasRestoSep.length - 1].matches("-?[0-9.,]+.-?[0-9]+")){
                                    indiceLinhasTudo++;
                                    linhasRestoSep = linhasResto[indiceLinhasTudo].split(" ");
                                }
                                tamanhoLinhaTipo3 = linhasRestoSep.length;
                            }
                            recebido = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 2]);
                            porcContr = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 3]);
                            porcAutor = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 4]);
                            if (tamanhoLinhaTipo3 > 4) {
                                valBruto = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 5]);
                            } else {
                                valBruto = "";
                            }

                            if (porcContr.equals("%")) {
                                porcContr = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 4]);
                                porcAutor = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 6]);
                            }

                            if (valBruto.equals("%")) {
                                valBruto = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 7]);
                            }
                            if (comPreco) {
                                if (comArtista)
                                    valBruto = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 6]);
                                else
                                    valBruto = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 8]);
                            }
                            if (tipoObras.equals("Sincronização")){
                                valBruto = "";
                                porcContr = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 4]);
                                porcAutor = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 6]);
                                System.out.println("s");
                            }

                            resultados.put(String.valueOf(resultados.size()), new String[]
                                    {obra, tipoObras, " ", porcAutor, porcContr, valBruto ,recebido, rateio, data});
                        } else {
                            if (obra.equals("AMULETO W")){
                                System.out.println("Tes");
                            }

                            data = linhaData;
                            String[] linhaRestoSep = new String[0];
                            if (indiceResto < linhasResto.length) {
                                if (linhasResto[indiceResto].matches(".*[a-zA-Z]+.*")) indiceResto++;
                                if (indiceResto < linhasResto.length) linhaRestoSep = linhasResto[indiceResto++].split(" ");
                            }
                            if (porcContr.equals("%")) {
                                porcContr = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 5]);
                                porcAutor = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 7]);
                                if (!porcAutor.matches("[0-9.]+"))
                                    porcAutor = "N.P.E";
                                if (!porcContr.matches("[0-9.]+"))
                                    porcContr = "N.P.E";
                            } else if (!possuiColunaPosicao) {
                                if (porcContr.matches("\\d+.\\d{2}%")){
                                    if (tudoSep[tudoSep.length - 3].contains("%")){
                                        valBruto = "";
                                        porcContr = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 3]);
                                        porcAutor = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 4]);
                                    }
                                    porcContr = porcContr.replace("%", "");
                                    porcAutor = porcAutor.replace("%", "");

                                } else {
                                    porcContr = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 4]);
                                    porcAutor = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 6]);
                                }
                            }

                            if (possuiColunaPosicao){
                                recebido = "";
                                if (linhaRestoSep.length == 4){
                                    recebido = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 2]);
                                    valBruto = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 3]);
                                    porcContr = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 4]);
                                    porcAutor = "";
                                }
                            } else {
                                recebido = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 2]);
                            }
                            if (tipoObras.equals("Sincronização"))
                                valBruto = "";

                            if (fileNameAsData)
                                data = nomeDoArquivo;

                            resultados.put(String.valueOf(resultados.size()), new String[]
                                    {obra, tipoObras, " ", porcAutor, porcContr, valBruto, recebido, rateio, data});
                        }
                        totalObras += Double.parseDouble(rateio);
                    } else if (s.contains("Valor bruto:") || s.contains("Valor:")) {
                        double valorArquivo = Double.parseDouble(rateio.replace("R$", ""));

                        if (valorArquivo - totalObras < 1.0) {
                            verifica.put(String.valueOf(verifica.size()), new String[]
                                    {"Soma da Obra/Arquivo Bateu. Obra: " + obra, "Valor informado:", String.valueOf(valorArquivo), "Valor Encontrado:", String.valueOf(totalObras), data});
                        } else {
                            verifica.put(String.valueOf(verifica.size()), new String[]
                                    {"Soma da Obra/Arquivo NÃO Bateu. Obra: " + obra, "Valor informado:", String.valueOf(valorArquivo), "Valor Encontrado:", String.valueOf(totalObras), data});
                        }

                        totalObras = 0.0;
                    } else if (s.contains("Tipo:")) {
                        tipoObras = s.replace("Tipo:", "").trim();
                    } else if (s.contains("Tipo de Distribuição:")) {
                        tipoObras = s.replace("Tipo de Distribuição:", "").trim();
                    }
                }
            }
            reader.close();
        }
        cedulas.add(resultados);
        cedulas.add(verifica);
        cedulas.add(verifica);
        return cedulas;
    }

    public String extraiNumeroDoTexto(String texto) {
        String texto_sem_letras = texto.replaceAll("[A-Z Ç]", "");
        char[] c = texto_sem_letras.toCharArray();
        char[] c2 = new char[5];
        c2[4] = c[c.length - 1];
        c2[3] = c[c.length - 2];
        c2[2] = c[c.length - 3];
        c2[1] = c[c.length - 4];
        c2[0] = c[c.length - 5];
        return String.valueOf(c2);
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

    public int verificaTipo(PDDocument doc, int vezes) throws IOException {
        String[] linhas;
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        Rectangle2D geral = new Rectangle2D.Double(convDis(0.00 ), convDis(0.00), convDis(68.39), convDis(44.19));
        stripper.addRegion("Geral", geral);
        stripper.setSortByPosition(true);
        stripper.extractRegions(doc.getPage(0));
        linhas = retiraIndesejado(stripper.getTextForRegion("Geral").split(System.lineSeparator()));
        if (linhas.length == 0){
            return verificaTipo(doc, 0.00, 24.35, ++vezes);
        }
        if (linhas[0].contains("Processamento:")) {
            return 1;
        } else if (linhas[0].contains("Editora:")) {
            return 4;
        } else {
            return 0;
        }
    }

    public int verificaTipo(PDDocument doc, double xOffset, double yOffset, int vezes) throws IOException {
        String[] linhas;
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        Rectangle2D geral = new Rectangle2D.Double(convDis(xOffset ), convDis(yOffset), convDis(68.39), convDis(44.19));
        stripper.addRegion("Geral", geral);
        stripper.setSortByPosition(true);
        stripper.extractRegions(doc.getPage(0));
        linhas = retiraIndesejado(stripper.getTextForRegion("Geral").split(System.lineSeparator()));
        if (linhas.length == 0){
            return verificaTipo(doc, 14.50 ,28.06, ++vezes);
        }
        if (linhas[0].contains("Processamento:") && vezes > 0) {
            rectTudo = new Rectangle2D.Double(convDis(6.0), convDis(yOffset), convDis(284.43), convDis(160.82)); // Obra
            rect0 = new Rectangle2D.Double(convDis(6.0), convDis(yOffset), convDis(63.69), convDis(160.82)); // Obra
            rect1 = new Rectangle2D.Double(convDis(80.00 ), convDis(yOffset), convDis(193.79), convDis(160.82)); // Resto
            rectData = new Rectangle2D.Double(convDis(118.78), convDis(yOffset), convDis(76.29), convDis(15.0)); // Data;

            String[] valoresNaoQuer = new String[] {"Autor:", "Beneficiário", "Endereço", "Bairro", "CEP:", "CPF:", "Demonstrativo de Pagamento", "Processamento"};
            List<String> naoQuerNovo = new ArrayList<>(Arrays.asList(naoQuer));
            Collections.addAll(naoQuerNovo, valoresNaoQuer);
            naoQuer = naoQuerNovo.toArray(String[]::new);
            fileNameAsData = true;

            changeRect = false;
            return 1;
        } else if (linhas[0].contains("Editora:")) {
            return 4;
        } else {
            return 0;
        }
    }

    public Double convDis(Double numero) {
        return (numero * 72) / 25.4;
    }

    public String[] retiraIndesejado(String[] array) {
        String[] indesejados = new String[]
                {"Saldo Inicial Adiantamento:", "Abatimento de Adiantamento:", "Valor Bruto:", "Saldo Final Adiantamento:",
                "Valor líquido:", "Banco:", "Endereço:", "Beneficiário", "Pseudônimo", "E-Mail:", "Total editora", "Agência:"};
        List<String> newList = new ArrayList<>();
        for (String s : array) {
            if (Arrays.stream(indesejados).noneMatch(s::contains)) {
                newList.add(s);
            }
        }
        return newList.toArray(String[]::new);
    }
    public void formataExportaPlanilha(List<Map<String, String[]>> entrada, String nomeSaida,
                                              String diretorioSaida, boolean fazerVerificacao)
            throws IOException, ParseException {

        Map<String, String[]> planilha = entrada.get(0);
        Map<String, String[]> verificacao;
        int tam = planilha.get("0").length;

        // Cria planilha
        @SuppressWarnings("resource")
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("PDFs");
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
        real.setDataFormat(df2.getFormat("R$ #,##0.00###;\\R$ -#,##0.00###"));
        real.setFont(fonte);
        real.setBorderLeft(BorderStyle.THIN);
        real.setBorderRight(BorderStyle.THIN);

        // Real Negrito
        XSSFCellStyle realNeg = workbook.createCellStyle();
        realNeg.setDataFormat(df2.getFormat("R$ #,##0.00###;\\R$ -#,##0.00###"));
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
                boolean sinais = obj.contains("+") || obj.contains("-");
                Cell cell = row.createCell(cellid++);
                if (obj.matches("-?[0-9.]+,-?[0-9]+") && !planilha.get(cedulaBase)[cellid - 1].contains("%")) {
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
                } else if (objectArr[0].equals("COD OBRA") || objectArr[objectArr.length - 1].equals("Data") || objectArr[0].equals("DATA")) {
                    cedulaBase = String.valueOf(key);
                    cell.setCellValue(obj);
                    cell.setCellStyle(indice);
                } else if (!obj.equals("") && !obj.equals(" ") && planilha.get(cedulaBase)[cellid - 1].contains("%")) {
                    if (obj.equals("N.P.E")) {
                        cell.setCellValue(obj);
                        cell.setCellStyle(style);
                    } else {
                        try {
                            cell.setCellValue(Double.parseDouble(obj) / 100);
                            cell.setCellStyle(porcentagem);
                        } catch (NumberFormatException e){
                            if (objectArr[0].equals("AMULETO W")){
                                System.out.println("tst");
                            }
                            cell.setCellValue("N.P.E");
                            cell.setCellStyle(style);
                            System.out.println("Erro em linha " + Arrays.toString(objectArr));
                        }
                    }
                } else if (obj.matches("[0-9-]+") && !planilha.get(cedulaBase)[cellid - 1].equals("Unidades")) {
                    cell.setCellValue(Long.parseLong(Helper.corrigeSeparadorDouble(obj)));
                    cell.setCellStyle(numeros);
                } else if(obj.matches("[0-9.-]{2,}") && obj.contains(".")) {
                    cell.setCellValue(Double.parseDouble(obj));
                    cell.setCellStyle(real);
                } else if(obj.split("E")[0].matches("[0-9,]+.[0-9]+") && obj.contains("E") && sinais) {
                    cell.setCellValue(Helper.converteDeNotacao(Helper.corrigeSeparadorDouble(obj)));
                    cell.setCellStyle(notacaoCientifica);
                } else{
                    cell.setCellValue(obj);
                    cell.setCellStyle(style);
                }
            }
        }

        if (fazerVerificacao) {
            verificacao = entrada.get(2);
            tam = Math.max(verificacao.get("0").length, tam);
            for (int i = 0; i < verificacao.size(); i++) {
                row = verifica.createRow(i);
                String[] linha = verificacao.get(String.valueOf(i));
                int idCedula = 0;
                for (String palavra : linha) {
                    Cell cell = row.createCell(idCedula++);
                    if (palavra.matches("-?[0-9.,]+.-?[0-9]+")) {
                        if (linha[0].equals("Total da Categoria") || linha[0].equals("TOTAL ")) {
                            cell.setCellValue(Double.parseDouble(palavra));
                            cell.setCellStyle(realNeg);
                        } else {
                            cell.setCellValue(Double.parseDouble(palavra));
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

        System.out.println("Terminando ajustes...");

        for (int i = 0; i < tam + 1; i++) {
            spreadsheet.autoSizeColumn(i);
            verifica.autoSizeColumn(i);
        }

        // Exporta o arquivo
        FileOutputStream out = new FileOutputStream(diretorioSaida + nomeSaida + ".xlsx");
        workbook.write(out);
        out.close();
        System.out.println("Conversão concluída com êxito. Nome do arquivo salvo: " + nomeSaida + ".xlsx");
    }
}
