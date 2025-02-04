package com.caio.pdf_conversions_api.Conversions.Warner;

import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
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
import java.util.stream.Collectors;

public class WarnerMusicTipo2 {

    public static List<Map<String, String[]>> retornaResultados(String PDFpath, String[] arquivosNaPasta) throws IOException {
        String obra = "";
        String tipoObra = "";
        String[] tudoSep;
        String valor = "";
        String porcVendas = "";
        String numVendas = "";
        String data = "";
        String obraSep[];
        String M2 = "";
        String M3 = "";
        String precoBase = "";
        String territorio = "";
        String configuracaoObra = "";

        boolean arquivoDif;
        boolean quebrar = false;
        boolean lerLinha = false;
        boolean comecar;
        boolean fezMargens;
        int indiceObras;
        int tipo = 0;

        Map<String, String[]> Resultados = new LinkedHashMap<>();
        Resultados.put("0", new String[]
                {"Obra", "Configuração Da Obra", "Territorio", "Tipo Da Obra", "Vendas", "Preço Base" ,"Royalty %/% Vendas", "Capa", "Direitos", "Data"});

        String[] meses = new String[]{"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto",
                "Setembro", "Outubro", "Novembro", "Dezembro"};

        Map<String, String[]> verifica = new LinkedHashMap<>();
        List<Map<String, String[]>> cedulas = new ArrayList<>();

        assert arquivosNaPasta != null;
        for (String nomeDoArquivo : arquivosNaPasta) {
            System.out.println("Lendo Arquivo " + nomeDoArquivo);
            comecar = false;
            arquivoDif = false;

            PDDocument reader = Loader.loadPDF(new File(PDFpath + nomeDoArquivo));

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            PDFTextStripperByArea stripper2 = new PDFTextStripperByArea();

            Rectangle2D rect0 = new Rectangle2D.Double(0, 100, 860, 683); // Tudo
            Rectangle2D rect1 = new Rectangle2D.Double(0, 100, 195, 683); // Obras
            Rectangle2D rect2 = new Rectangle2D.Double(400, 100, 614, 683); // Resto
            Rectangle2D rect3 = new Rectangle2D.Double(150, 0, 350, 50); // Data
            Rectangle2D rect4 = new Rectangle2D.Double(0, 0, 860, 100); // Cabecalho
            Rectangle2D rect5 = new Rectangle2D.Double(Helper.mmParaPx(68.51), Helper.mmParaPx(36.49), Helper.mmParaPx(51.85), Helper.mmParaPx(173.06)); // Cabecalho
            arquivoDif = true;

            stripper.setSortByPosition(true);
            stripper2.setSortByPosition(true);

            stripper2.addRegion("rect0", rect0);
            stripper2.addRegion("rect4", rect4);
            stripper.addRegion("rect1", rect1);
            stripper.addRegion("rect2", rect2);
            stripper.addRegion("rect3", rect3);
            stripper.addRegion("rect5", rect5);

            int numberOfPages = reader.getNumberOfPages();
            fezMargens = false;
            for (int i = 0; i < numberOfPages; i++) {
                indiceObras = 0;
                stripper.extractRegions(reader.getPage(i));
                stripper2.extractRegions(reader.getPage(i));

                String[] tudo = stripper2.getTextForRegion("rect0").split(System.lineSeparator());
                String[] obras = stripper.getTextForRegion("rect1").split(System.lineSeparator());
                String[] datas = stripper.getTextForRegion("rect3").split(System.lineSeparator());
                String[] cabecalho = stripper2.getTextForRegion("rect4").split(System.lineSeparator());
                String[] tipos = stripper.getTextForRegion("rect5").split(System.lineSeparator());

                if (cabecalho[1].contains("Av. das Américas") && !fezMargens) {
                    tipo = 0;
                    fezMargens = true;
                } else if (cabecalho[0].equals("ROYALTIES SUMMARY") && !fezMargens) {
                    tipo = 1;
                    rect1 = new Rectangle2D.Double(250, 115, 92, 683);
                    rect0 = new Rectangle2D.Double(0, 115, 860, 683);
                    stripper.addRegion("rect1", rect1);
                    stripper2.addRegion("rect0", rect0);
                    stripper.extractRegions(reader.getPage(i));
                    stripper2.extractRegions(reader.getPage(i));
                    obras = stripper.getTextForRegion("rect1").split(System.lineSeparator());
                    tudo = stripper2.getTextForRegion("rect0").split(System.lineSeparator());
                    fezMargens = true;
                } else if (!fezMargens){
                    tipo = 2;
                    rect1 = new Rectangle2D.Double(0, 100, 330, 683);
                    stripper.addRegion("rect1", rect1);
                    stripper.extractRegions(reader.getPage(i));
                    obras = stripper.getTextForRegion("rect1").split(System.lineSeparator());
                    fezMargens = true;
                }
                if (tipo != 1) {
                    String[] datasSep = datas[0].split(" ");
                    data = datasSep[datasSep.length - 3] + " " + datasSep[datasSep.length - 2] + " " + datasSep[datasSep.length - 1];
                }

                for (int j = 0; j < tudo.length; j++) {
                    tudoSep = tudo[j].split(" ");
                    if (tipo == 1 && tudo[j].equals(" ")) {
                        tudo = ArrayUtils.remove(tudo, j);
                        if (j == tudo.length) {
                            quebrar = true;
                        } else {
                            tudoSep = tudo[j].split(" ");
                        }
                    }
                    if (quebrar || tudo[j].contains("Royaltor") ) {
                        break;
                    }
                    valor = tudoSep[tudoSep.length - 1];
                    if (tudoSep.length > 1) {
                        M2 = tudoSep[tudoSep.length - 2];
                    }

                    if (valor.matches("-?[0-9.()]+.-?[0-9()]+") && !tudo[j].contains("Total") && M2.matches("-?[0-9.()]+.-?[0-9()]+")) {
                        if (valor.contains("(") && valor.contains("("))
                            valor = "-" + valor.replace("(", "").replace(")", "");
                        if (tipo == 0) {
                            numVendas = tudoSep[tudoSep.length - 6].replace(".", "");
                            precoBase = tudoSep[tudoSep.length - 5];
                            porcVendas = tudoSep[tudoSep.length - 4];
                            M2 = tudoSep[tudoSep.length - 2];
                            if (porcVendas.contains(",")) {
                                valor = Helper.corrigeSeparadorDouble(valor);
                                numVendas = numVendas.replace(",", "");
                                porcVendas = Helper.corrigeSeparadorDouble(porcVendas);
                            }
                            if (precoBase.contains(",")) {
                                precoBase = Helper.corrigeSeparadorDouble(precoBase);
                            }

                        } else if(tipo == 1) {
                            data = tudoSep[0] + " " + tudoSep[1];
                            obra = obras[j];
                            valor = Helper.corrigeSeparadorDouble(tudoSep[tudoSep.length - 2] + tudoSep[tudoSep.length - 1]);
                            numVendas = "";
                            porcVendas = tudoSep[tudoSep.length - 31].replace("%", "");
                        } else {
                            numVendas = tudoSep[tudoSep.length - 7].replace(",", "");
                            porcVendas = tudoSep[tudoSep.length - 5];
                            if (porcVendas.contains("RECEIPTS")) {
                                porcVendas = tudoSep[tudoSep.length - 3].replace("%", "");
                            }
                        }
                        numVendas = numVendas.replace("(", "").replace(")", "");
                        valor = valor.replace("(", "").replace(")", "");
                        if (tipo == 0) {
                            territorio = Helper.achaTermoEmComum(tudo[j], obras, false);
                            tipoObra = Helper.achaTermoEmComum(tudo[j], tipos, false);
                            Resultados.put(String.valueOf(Resultados.size()), new String[]
                                    {obra, configuracaoObra, territorio, tipoObra, numVendas, precoBase, porcVendas, M2, valor, data});
                        }
                        else
                            Resultados.put(String.valueOf(Resultados.size()), new String[]
                                    {obra, "", "","", numVendas, "", porcVendas, "", valor, data});
                    } else if(j == 0 || tudo[j - 1].contains("Total")) {
                        if (tipo == 0) {
                            configuracaoObra = tudo[j].replace(tudo[j].split(" ")[0], "");
                            obra = tudo[j + 1].replaceFirst(".", "");
                        } else if (tipo == 2){
                            obraSep = calibraCol(obras, tudo[j]).split(" ");
                            String[] finalTudoSep = obraSep;
                            obra = Arrays.stream(obraSep)
                                    .filter(x -> Arrays.asList(finalTudoSep).indexOf(x) > 0)
                                    .map(String::toString).collect(Collectors.joining(" "));
                        }
                    }
                }
            }
            reader.close();
        }

        cedulas.add(Resultados);
        return cedulas;
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


    public static void formataExportaPlanilhaW2(List<Map<String, String[]>> entrada, String nomeSaida,
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
        real.setDataFormat(df2.getFormat("R$ #,##0.00;\\R$ -#,##0.00"));
        real.setFont(fonte);
        real.setBorderLeft(BorderStyle.THIN);
        real.setBorderRight(BorderStyle.THIN);

        // Real Negrito
        XSSFCellStyle realNeg = workbook.createCellStyle();
        realNeg.setDataFormat(df2.getFormat("R$ #,##0.00;\\R$ -#,##0.00"));
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
        System.gc();
        // Transforma os valores dos objetos em cédulas
        Set<String> keyid = planilha.keySet();
        int rowid = 0;
        String cedulaBase = "0";
        System.out.println("Adicionando Elementos...");

        for (String key : keyid) {
            row = spreadsheet.createRow(rowid++);
            row.setHeight((short) 340);
            String[] objectArr = planilha.get(key);
            int cellid = 0;

            for (String obj : objectArr) {
                boolean sinais = false;
                if (obj.contains("+") || obj.contains("-")) {
                    sinais = true;
                }
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
                } else if (objectArr[3].equals("") || objectArr[0].equals("Total da Categoria")
                        || objectArr[0].equals("Totais por Rubrica") || objectArr[0].equals("TOTAL ")) {
                    cell.setCellValue(obj);
                    cell.setCellStyle(semRateio);
                } else if (objectArr[0].equals("COD OBRA") || objectArr[objectArr.length - 1].equals("Data") || objectArr[0].equals("DATA")) {
                    cedulaBase = String.valueOf(key);
                    cell.setCellValue(obj);
                    cell.setCellStyle(indice);
                } else if (!obj.equals(" ") && planilha.get(cedulaBase)[cellid - 1].contains("%")) {
                    try{
                        cell.setCellValue(Double.parseDouble(obj)/100);
                        cell.setCellStyle(porcentagem);
                    } catch(NumberFormatException e){
                        cell.setCellValue(obj);
                        cell.setCellStyle(style);
                    }
                } else if (obj.matches("[0-9-]+") && !planilha.get(cedulaBase)[cellid - 1].equals("Unidades")) {
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
        System.gc();
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
        System.out.println("Conversão concluída com êxito. Nome do arquivo salvo: " + nomeSaida + ".xlsx");

    }
}
