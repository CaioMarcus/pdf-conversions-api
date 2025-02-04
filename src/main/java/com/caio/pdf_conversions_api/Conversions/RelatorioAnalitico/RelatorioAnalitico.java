package com.caio.pdf_conversions_api.Conversions.RelatorioAnalitico;


import com.caio.pdf_conversions_api.Conversions.ConversionThread;
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

public class RelatorioAnalitico extends ConversionThread {
    int ultimoIndiceEditora;
    int indiceObraAtual;
    int quantiaObras;
    private String classificacaoConexo;
    private String pdfPath;
    String[] linhasEditora;
    String[] linhasObra;
    String[] arrayObraAtual;

    public RelatorioAnalitico(String pdfPath) {
        super(pdfPath);
    }

    @Override
    public void run() {
        try {
            this.retornaResultadosInteger();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setDatePatterns() {

    }

    private List<Map<Integer, String[]>> retornaResultadosInteger()
            throws IOException {
        // Inicializando variáveis
        File pasta = new File(pdfPath);
        String[] arquivosNaPasta = pasta.list();
        String[] linhasTudo;
        String[] palavras;
        String[] linhaConstruida = new String[0];
        String codigoObra;
        String codigoISWC;
        String nomeObra;
        // Inicializando map que guardará as linhas do documento
        Map<Integer, String[]> Resultados = new LinkedHashMap<>();
        Map<String, String[]> Editoras = new LinkedHashMap<>();
        // Inicializando a lista que guardará o Map
        List<Map<Integer, String[]>> cedulas = new ArrayList<>();

        assert arquivosNaPasta != null;
        for (String nomeDoArquivo : arquivosNaPasta) {
            // Atribuindo valores a variáveis que vão ser utilizadas
            quantiaObras = 0;

            System.out.println("Lendo Arquivo " + nomeDoArquivo);
            PDDocument reader = null;
            try {
                // Recebendo o Documento
                reader = Loader.loadPDF(Path.of(pdfPath, nomeDoArquivo).toFile());
                // Declarando os Strippers
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                PDFTextStripperByArea stripper2 = new PDFTextStripperByArea();
                PDFTextStripperByArea stripperObra = new PDFTextStripperByArea();
                // Declarando os Retângulos
                Rectangle2D rect0;
                Rectangle2D rect1;
                //Rectangle2D rectData;
                Rectangle2D rectTudo;

                // Verifica o tipo do documento
                int tipo = VerificaTipo(reader);
                // Monta os Retangulos para Extrair o texto
                if (tipo == 0) {
                    // Monta os Retângulos
                    rectTudo = new Rectangle2D.Double(ConvDis(0.0), ConvDis(54.51), ConvDis(210.00), ConvDis(222.51)); // Tudo
                    rect0 = new Rectangle2D.Double(ConvDis(27.02), ConvDis(54.51), ConvDis(52.41), ConvDis(222.51)); // Editora
                    rect1 = new Rectangle2D.Double(ConvDis(50.00), ConvDis(54.51), ConvDis(77.00), ConvDis(222.51)); // Obra
                    // Adiciona o índice da planílha
                    Resultados.put(Resultados.size(), new String[]{"COD.OBRA", "ISWC", "TÍTULO PRINCIPAL DA OBRA", "COD.TITULAR", "TITULAR", "PSEUDONIMO", "COD.CAE", "ASSOCIAÇÃO", "CAT", "%", "DATA"});
                    //Editoras.put(String.valueOf(Editoras.size()), new String[]{"COD.OBRA", "ISWC", "TÍTULO PRINCIPAL DA OBRA", "EDITORA", "LINK", "DATA"});
                    //rectData = new Rectangle2D.Double(convDis(118.78), convDis(16.81), convDis(76.29), convDis(20.00)); // Data;
                } else if (tipo == 2){
                    // Monta os Retângulos
                    rectTudo = new Rectangle2D.Double(ConvDis(10.05), ConvDis(32.28), ConvDis(189.97), ConvDis(249.80)); // Tudo
                    rect0 = new Rectangle2D.Double(ConvDis(27.02), ConvDis(32.28), ConvDis(52.41), ConvDis(249.80)); // Editora
                    rect1 = new Rectangle2D.Double(ConvDis(66.15), ConvDis(32.28), ConvDis(101.24), ConvDis(249.80)); // Obra
                    // Adiciona o índice da planílha
                    Resultados.put(Resultados.size(), new String[]{"COD.OBRA", "ISWC", "TÍTULO PRINCIPAL DA OBRA"});
                } else {
                    // Monta os Retângulos
                    rectTudo = new Rectangle2D.Double(ConvDis(0.0), ConvDis(52.80), ConvDis(210.00), ConvDis(228.86)); // Tudo
                    rect0 = new Rectangle2D.Double(ConvDis(28.72), ConvDis(52.80), ConvDis(24.08), ConvDis(228.86)); // ISRC
                    rect1 = new Rectangle2D.Double(ConvDis(83.50), ConvDis(52.80), ConvDis(59.27), ConvDis(228.86)); // Obra
                    // Adiciona o índice da planílha
                    Resultados.put(Resultados.size(), new String[]{"COD.OBRA", "ISRC/GRA", "TÍTULO PRINCIPAL DA OBRA", "CLASSIFICAÇÃO"});
                }
                // Organiza os strippers pela posição do PDF, e adiciona as regioes dos retângulos a eles
                stripper.setSortByPosition(true);
                stripper2.setSortByPosition(true);
                stripper.addRegion("rect0", rect0);
                stripperObra.addRegion("rect1", rect1);
                //stripper.addRegion("rectData", rectData);
                stripper2.addRegion("rectTudo", rectTudo);
                // Pega o numero de Paginas do arquivo
                int numOfPag = reader.getNumberOfPages();
                // Laço para ler cada página
                for (int i = 0; i < numOfPag; i++) {
                    ultimoIndiceEditora = 0;
                    indiceObraAtual = -1;
                    // Extrai os dados da página para o stripper
                    stripper.extractRegions(reader.getPage(i));
                    stripper2.extractRegions(reader.getPage(i));
                    stripperObra.extractRegions(reader.getPage(i));
                    // Atribui os dados dos retângulos as variáveis
                    linhasTudo = stripper2.getTextForRegion("rectTudo").split(System.lineSeparator());
                    linhasEditora = stripper.getTextForRegion("rect0").split(System.lineSeparator());
                    linhasObra = stripperObra.getTextForRegion("rect1").split(System.lineSeparator());
                    // Laço para ler cada linha
                    for (int indicelinhas = 0; indicelinhas < linhasTudo.length; indicelinhas++) {
                        String linha = linhasTudo[indicelinhas];
                        // Separa a linha em um vetor de palavras
                        palavras = linha.split(" ");

                        // Verifica se acabaram as obras do documento
                        if (linha.contains("CATEGORIAS AUTORAIS......:")) {
                            Resultados.put(Resultados.size(), new String[]{"", "", "", "", "", "", "", "", "", "", ""});
                            Resultados.put(Resultados.size(), new String[]{"", "", "", "", "", "Total Obras Computadas: ", "", "", "", "", String.valueOf(quantiaObras)});
                            break;
                        }

                        if (tipo == 1){
                            if (verificaLinhaObra(palavras, tipo)) {
                                arrayObraAtual = retornaArrayLinha(linha, tipo);
                                quantiaObras++;
                                classificacaoConexo = linhasTudo[++indicelinhas];
                                Resultados.put(Resultados.size(), new String[]{ arrayObraAtual[0], arrayObraAtual[1], arrayObraAtual[2], classificacaoConexo});
                            }
                        } else if(tipo == 2){
                            if (verificaLinhaObra(palavras, tipo)) {
                                codigoObra = palavras[1];
                                codigoISWC = palavras[2];
                                if (!codigoISWC.matches("T\\d{10}")){
                                    codigoISWC = "";
                                }
                                Resultados.put(Resultados.size(), new String[]{ codigoObra, codigoISWC,
                                        linha
                                            .replace(String.format("%s %s %s", palavras[0], codigoObra, codigoISWC), "")
                                            .replace(String.format(" %s", palavras[palavras.length - 1]), "")
                                            .trim()
                                });
                            }
                        }else {
                            // Verifica se a linha atual é uma obra
                            if (verificaLinhaObra(palavras, tipo)) {
                                arrayObraAtual = retornaArrayLinha(linha, tipo);
                                quantiaObras++;
                            } else {
                                Resultados.put(Resultados.size(), montaLinha(linha));
                            }
                        }



                        /*else if (fazerEditora && verificaLinhaEditora(palavras, tipo)) {

                            if (editora != null) {
                                String link = palavras[palavras.length - 1];
                                Editoras.put(String.valueOf(Editoras.size()), new String[]{linhaConstruida[0],
                                        linhaConstruida[1], linhaConstruida[2], editora, link, linhaConstruida[3]});
                            }
                        }*/
                    }
                }
                // Fecha o documento
                reader.close();
            } catch (Exception e){
                if (reader != null) reader.close();
                throw e;
            }
        }
        // Adiciona o map com os dados na lista de cedulas
        cedulas.add(Resultados);
        //cedulas.add(Editoras);
        return cedulas;
    }

    private String[] montaLinha(String linha){
        boolean chegouCae = false;

        int indexLinha = 1;

        String codigo = "";
        String editora = retornaEditora(linha); // Titular também

        StringBuilder pseudonimo = new StringBuilder();
        StringBuilder associacao = new StringBuilder();
        String CAE = "";
        String tipoObra = "";
        String porcentagem = "";

        String linhaRefinada = linha.replace(editora + " ", "");
        String[] linhaRefinadaSep = linhaRefinada.split(" ");

        codigo = linhaRefinadaSep[0];

        //Verifica se o pseudonimo é igual ao nome do titular/editora
        if (linhaRefinadaSep[1].matches("\\d{5}.\\d{2}.\\d{2}.\\d{2}")){
            CAE = linhaRefinadaSep[1];
            pseudonimo.append(editora);
            indexLinha++;
            chegouCae = true;
        }
        int tamanhoLinha = linhaRefinadaSep.length;
        for (; indexLinha < tamanhoLinha; indexLinha++){
            //Pega a linha atual
            String linhaAtual = linhaRefinadaSep[indexLinha];

            //Verifica se chegou no CAE
            if (!chegouCae && linhaAtual.matches("\\d{5}.\\d{2}.\\d{2}.\\d{2}")){
                CAE = linhaAtual;
                chegouCae = true;
            } else if (!chegouCae){ //Verifica se está antes do CAE (Vai ser pseudonimo)
                pseudonimo.append(linhaAtual + " ");
            }
            else if (indexLinha < tamanhoLinha - 1 && (linhaRefinadaSep[indexLinha + 1].equals("100,") || linhaRefinadaSep[indexLinha + 1].matches("\\d{1,3},\\d{2}"))){ //Verifica se chegou nos ultimos itens da linha
                tipoObra = linhaAtual;
                porcentagem = linhaRefinadaSep[indexLinha + 1];
                break;
            } else { //Se não for nada acima, só pode ser associação
                associacao.append(linhaAtual);
            }
        }
        if (!chegouCae){
            String supostoValor = linhaRefinadaSep[linhaRefinadaSep.length - 2];
            if (verificaData(supostoValor)){
                porcentagem = linhaRefinadaSep[linhaRefinadaSep.length - 3];
                tipoObra = linhaRefinadaSep[linhaRefinadaSep.length - 4];
            } else {
                porcentagem = supostoValor;
                tipoObra = linhaRefinadaSep[linhaRefinadaSep.length - 3];
            }
            pseudonimo = new StringBuilder();
        }

        return new String[] {arrayObraAtual[0], arrayObraAtual[1], arrayObraAtual[2], codigo, editora, pseudonimo.toString(), CAE, associacao.toString(), tipoObra, porcentagem, arrayObraAtual[3]};
    }

    String retornaEditora(String linhaAtual) {
        for (int i = ultimoIndiceEditora; i < linhasEditora.length; i++) {
            if (linhaAtual.contains(linhasEditora[i])) {
                ultimoIndiceEditora++;
                return linhasEditora[i];
            }
        }
        return null;
    }

    String[] retornaArrayLinha(String linha, int tipo) {
        // Declaração de Variáveis
        String[] linha_tudo = linha.split(" ");
        boolean passou_situacao = false;
        String codigo_ecad = linha_tudo[0];
        String ISRC = " ";
        String obra = "";
        String data = "";
        int indice_inicial;
        // Seleciona os tipos do documento
        if (tipo == 0) {
            // Pega a data
            data = linha_tudo[linha_tudo.length - 1];
            if (!verificaData(data)) data = "";
            // Verifica se a linha tem o codigo ISWC ou não e seleciona o intervalo que começa o nome da obra
            if (!linha_tudo[1].equals("")) {
                ISRC = linha_tudo[1];
                indice_inicial = 2;
            } else {
                ISRC = " -   .   .   - ";
                indice_inicial = 12;
            }
            // Pegando o nome da obra
            obra = retornaStringObra(linha, indice_inicial);

        } else {
            // Laço para pegar os dados
            for (int i = 0; i < linha_tudo.length; i++) {
                if (passou_situacao) {
                    // Se chegou na obra, concatenar até ela acabar, e quando acabar, quebrar o loop;
                    if (i < linha_tudo.length - 2) {
                        obra += linha_tudo[i] + " ";
                    } else {
                        break;
                    }
                } else {
                    // Identificar quando chegar na obra
                    if (linha_tudo[i].equals("LIBERADO") || linha_tudo[i].equals("DUPLICIDADE")
                            || (linha_tudo[i].equals("VALIDAÇÃO") && linha_tudo[i - 1].equals("DE")
                            && linha_tudo[i - 2].equals("PENDENTE"))) {
                        passou_situacao = true;
                    } else if (i == 1) {
                        ISRC = linha_tudo[i];
                    }
                }
            }
        }
        // Retornar uma string com os resultados da linha
        return new String[] {codigo_ecad, ISRC, obra, data};
    }

    String retornaStringObra(String linha, int indiceInicial) {
        if (indiceInicial == 2)
            indiceInicial = 16;
        else
            indiceInicial = 15;
        for (int i = indiceObraAtual + 1; i < linhasObra.length; i++){
            String obraEditora = linhasObra[i];
            if (linha.contains(obraEditora)){
                indiceObraAtual = i;
                return obraEditora;
            }
        }
        return null;
    }

    int VerificaTipo(PDDocument doc) throws IOException {
        // Atribuições
        String[] linhas;
        // Montagem dos retângulos
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        Rectangle2D rect = new Rectangle2D.Double(ConvDis(0.0), ConvDis(0.0), ConvDis(210.00), ConvDis(54.51)); // Tudo
        // Configuração do Stripper
        stripper.addRegion("rect", rect);
        stripper.setSortByPosition(true);
        stripper.extractRegions(doc.getPage(0));
        // Recebendo as informações da página em uma varipavel
        linhas = stripper.getTextForRegion("rect").split(System.lineSeparator());
        String primeiraLinha = Helper.normalizeString(linhas[0].toUpperCase());
        // Retorna o tipo do documento
        if (primeiraLinha.contains("TITULAR AUTORAL"))
            return 0;
        else if (primeiraLinha.contains("RELATORIO DE OBRAS MUSICAIS CADASTRADA"))
            return 2;
        else
            return 1;
    }

    Double ConvDis(Double numero) {
        return (numero * 72) / 25.4;
    }

    boolean verificaData(String date) {
        // Cria o formato da data
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        // Verifica se a dara é do formato desejado
        try {
            df.parse(date);
            if (date.length() == 10) {
                return true;
            }
        } catch (ParseException e) {
            return false;
        }
        return false;
    }

    boolean verificaLinhaObra(String[] linha_sep, int tipo) {
        // Atribuições
        String codigo;
        //Separa o tipo do documento
        if (tipo == 0) {
            if (linha_sep[1].equals(""))
                codigo = linha_sep[2] + linha_sep[5] + linha_sep[8] + linha_sep[11];
            else if(linha_sep[1].equals("-"))
                codigo = linha_sep[1] + linha_sep[2] + linha_sep[5] + linha_sep[8];
            else
                codigo = linha_sep[1].replaceAll("[0-9]+", "").replace("T", "");

            return codigo.equals("-..-");
        } else if (tipo == 2){
            return verificaData(linha_sep[0]);
        } else {
            return (linha_sep[linha_sep.length - 1].equals("X") || linha_sep[linha_sep.length - 1].equals("NÃO")
                    || linha_sep[linha_sep.length - 1].equals("SIM"));
        }
    }

    boolean verificaLinhaEditora(String[] linhaSep, int tipo) {
        if (tipo != 0) return false;
        for (int i = linhaSep.length - 1; i > 0; i--){
            if (linhaSep[i].matches("[0-9,.]+") && linhaSep[i - 1].equals("E")){
                return true;
            }
        }
        return false;
    }

    public void formataExportaPlanilhaIntegerMap(List<Map<Integer, String[]>> entrada, String nomeSaida,
                                       String diretorioSaida, boolean fazerEditora)
            throws IOException, ParseException {

        Map<Integer, String[]> planilha = entrada.get(0);
        int tam = planilha.get(0).length;

        Map<Integer, String[]> planilhaEdit = null;
        int tamEdit = 0;

        if (fazerEditora) {
            planilhaEdit = entrada.get(1);
            try {
                tamEdit = planilhaEdit.get(0).length;
            } catch(NullPointerException e){
                planilhaEdit = null;
                fazerEditora = false;
            }
        }

        // Cria planilha
        @SuppressWarnings("resource")
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("PDFs");
        //XSSFSheet spreadsheetEdit = workbook.createSheet("PDFs + Editoras");
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

        // Parte Funcional

        // Transforma os valores dos objetos em cédulas
        Set<Integer> keyid = planilha.keySet();
        int rowid = 0;
        System.out.println("Adicionando Elementos...");

        for (Integer key : keyid) {
            row = spreadsheet.createRow(rowid++);
            String[] objectArr = planilha.get(key);
            int cellid = 0;
            for (String obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                if (Helper.verificaData(obj)){
                    cell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").parse(obj));
                    cell.setCellStyle(data);
                } else if (objectArr[0].equals("COD.OBRA") || objectArr[0].equals("COD.ECAD")) {
                    cell.setCellValue(obj);
                    cell.setCellStyle(indice);
                } else {
                    cell.setCellValue(obj);
                    cell.setCellStyle(style);
                }
            }
        }
        /*
        if (fazerEditora) {
            Set<Integer> keyidEdit = planilhaEdit.keySet();
            int rowidEdit = 0;
            System.out.println("Adicionando Elementos...");

            for (Integer key : keyidEdit) {
                row = spreadsheetEdit.createRow(rowidEdit++);
                String[] objectArr = planilhaEdit.get(key);
                int cellid = 0;
                for (String obj : objectArr) {
                    Cell cell = row.createCell(cellid++);
                    if (Helper.verificaData(obj)) {
                        cell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").parse(obj));
                        cell.setCellStyle(data);
                    } else if (objectArr[0].equals("COD.OBRA") || objectArr[0].equals("COD.ECAD")) {
                        cell.setCellValue(obj);
                        cell.setCellStyle(indice);
                    } else {
                        cell.setCellValue(obj);
                        cell.setCellStyle(style);
                    }
                }
            }
            for (int i = 0; i < tamEdit + 1; i++) {
                spreadsheetEdit.autoSizeColumn(i);
            }
        }
        */

        System.out.println("Terminando ajustes...");

        for (int i = 0; i < tam + 1; i++) {
            spreadsheet.autoSizeColumn(i);
        }

        // Exporta o arquivo
        FileOutputStream out = new FileOutputStream(diretorioSaida + nomeSaida + ".xlsx");
        workbook.write(out);
        out.close();
        System.out.println("Conversão concluída com êxito. Nome do arquivo salvo: " + nomeSaida + ".xlsx");
    }

    public void formataExportaPlanilha(List<Map<String, String[]>> entrada, String nomeSaida,
                                              String diretorioSaida, boolean fazerEditora)
            throws IOException, ParseException {

        Map<String, String[]> planilha = entrada.get(0);
        int tam = planilha.get("0").length;

        Map<String, String[]> planilhaEdit = null;
        int tamEdit = 0;

        if (fazerEditora) {
            planilhaEdit = entrada.get(1);
            try {
                tamEdit = planilhaEdit.get("0").length;
            } catch(NullPointerException e){
                planilhaEdit = null;
                fazerEditora = false;
            }
        }

        // Cria planilha
        @SuppressWarnings("resource")
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("PDFs");
        XSSFSheet spreadsheetEdit = workbook.createSheet("PDFs + Editoras");
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

        // Parte Funcional

        // Transforma os valores dos objetos em cédulas
        Set<String> keyid = planilha.keySet();
        int rowid = 0;
        System.out.println("Adicionando Elementos...");

        for (String key : keyid) {
            row = spreadsheet.createRow(rowid++);
            String[] objectArr = planilha.get(key);
            int cellid = 0;
            for (String obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                if (Helper.verificaData(obj)){
                    cell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").parse(obj));
                    cell.setCellStyle(data);
                } else if (objectArr[0].equals("COD.OBRA") || objectArr[0].equals("COD.ECAD")) {
                    cell.setCellValue(obj);
                    cell.setCellStyle(indice);
                } else {
                    cell.setCellValue(obj);
                    cell.setCellStyle(style);
                }
            }
        }

        if (fazerEditora) {
            Set<String> keyidEdit = planilhaEdit.keySet();
            int rowidEdit = 0;
            System.out.println("Adicionando Elementos...");

            for (String key : keyidEdit) {
                row = spreadsheetEdit.createRow(rowidEdit++);
                String[] objectArr = planilhaEdit.get(key);
                int cellid = 0;
                for (String obj : objectArr) {
                    Cell cell = row.createCell(cellid++);
                    if (Helper.verificaData(obj)) {
                        cell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").parse(obj));
                        cell.setCellStyle(data);
                    } else if (objectArr[0].equals("COD.OBRA") || objectArr[0].equals("COD.ECAD")) {
                        cell.setCellValue(obj);
                        cell.setCellStyle(indice);
                    } else {
                        cell.setCellValue(obj);
                        cell.setCellStyle(style);
                    }
                }
            }
            for (int i = 0; i < tamEdit + 1; i++) {
                spreadsheetEdit.autoSizeColumn(i);
            }
        }
        System.out.println("Terminando ajustes...");

        for (int i = 0; i < tam + 1; i++) {
            spreadsheet.autoSizeColumn(i);
        }

        // Exporta o arquivo
        FileOutputStream out = new FileOutputStream(diretorioSaida + nomeSaida + ".xlsx");
        workbook.write(out);
        out.close();
        System.out.println("Conversão concluída com êxito. Nome do arquivo salvo: " + nomeSaida + ".xlsx");
    }

}
