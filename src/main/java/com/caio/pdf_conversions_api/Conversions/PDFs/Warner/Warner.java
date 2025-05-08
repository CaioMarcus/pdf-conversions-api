package com.caio.pdf_conversions_api.Conversions.PDFs.Warner;

import com.caio.pdf_conversions_api.Conversions.ConversionThread;
import com.caio.pdf_conversions_api.Exceptions.ConversionException;
import com.caio.pdf_conversions_api.Export.ResultData;
import com.caio.pdf_conversions_api.Export.VerificationData;
import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class Warner extends ConversionThread {
    private String editoraAtual = "warner";
    private String[] titulo = new String[]
                    {"obra", "tipo", /*"Cód. de Id.",*/ "periodo_fato_gerador",
                    "sub_fonte", "territorio", "fonte",
                    "unidade", "royalty_total", "percentual_titular", "royalty_titular", "editora_atual", "periodo_royalty"};

    public Warner(String pdfPath, String xlsName, String[] filesToConvert) {
        super(pdfPath, xlsName, filesToConvert);
    }

    @Override
    public void setDatePatterns() {
        this.datePatterns = new LinkedHashMap<>(){
            {
                put(Pattern.compile("(\\d{4}) (\\d{2})-(\\d{4}) (\\d{2})"), Pair.of(2,1));
            }
        };
    }

    public void retornaResultados() throws IOException {

        String[] meses = new String[] { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto",
                "Setembro", "Outubro", "Novembro", "Dezembro" };


        for (int fileIndex = 0; fileIndex < arquivosNaPasta.length; fileIndex++) {
            String nomeDoArquivo = arquivosNaPasta[fileIndex];
            System.out.println("Lendo Arquivo " + nomeDoArquivo);
            PDDocument reader = Loader.loadPDF(Path.of(this.pdfPath, nomeDoArquivo).toFile());

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            PDFTextStripperByArea stripper2 = new PDFTextStripperByArea();
            PDFTextStripperByArea stripper3 = new PDFTextStripperByArea();

            Rectangle2D rect1 = new Rectangle2D.Double(0, 68.2, 250, 540.00);
            Rectangle2D rect2 = new Rectangle2D.Double(250.67, 68.2, 92, 540.00);
            Rectangle2D rect3 = new Rectangle2D.Double(342.67, 68.2, 73, 540.00);
            Rectangle2D rect4 = new Rectangle2D.Double(415.67, 68.2, 92, 540.00);
            Rectangle2D rect5 = new Rectangle2D.Double(507.67, 68.2, 273, 540.00);
            //PrimeiraParte 
            Rectangle2D rect6 = new Rectangle2D.Double(61, 68.2, 92, 540.00);
            Rectangle2D rect7 = new Rectangle2D.Double(153, 68.2, 49, 540.00);
            Rectangle2D rect8 = new Rectangle2D.Double(202, 68.2, 49, 540.00);
            Rectangle2D rect9 = new Rectangle2D.Double(0, 68.2, 840, 540.00);
            //Data
            Rectangle2D rect10 = new Rectangle2D.Double(137.3, 26.98, 175.8, 14);

            stripper.setSortByPosition(true);
            stripper2.setSortByPosition(true);
            stripper3.setSortByPosition(true);

            stripper.addRegion("rect1", rect1);
            stripper.addRegion("rect2", rect2);
            stripper.addRegion("rect3", rect3);
            stripper.addRegion("rect4", rect4);
            stripper.addRegion("rect5", rect5);
            //Primeira parte
            stripper2.addRegion("rect6", rect6);
            stripper2.addRegion("rect7", rect7);
            stripper2.addRegion("rect8", rect8);
            stripper3.addRegion("rect9", rect9);
            //Data
            stripper.addRegion("rect10", rect10);

            String obra = "";
            String subTotAntes = "NadaAinda";
            String obraAntiga = "NenhumaObraDefinida";
            double somatorio = 0.0;
            double somatorioSub = 0.0;
            int numberOfPages = reader.getNumberOfPages();
            boolean pagInicial = true;
            String data = null;

            for (int i = 0; i < numberOfPages; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                int indiceObra = 0;
                int indiceTudo = 0;
                int indiceProvedor = 0;
                int indicePais = 0;
                int indiceFontes = 0;
                int indiceReceita = 0;
//                int indiceCodigo = 0;
                int indicePeriodo = 0;

                stripper.extractRegions(reader.getPage(i));
                stripper2.extractRegions(reader.getPage(i));
                stripper3.extractRegions(reader.getPage(i));

                String[] obras = stripper.getTextForRegion("rect1").split(System.lineSeparator());
                String[] provedor = stripper.getTextForRegion("rect2").split(System.lineSeparator());
                String[] pais = stripper.getTextForRegion("rect3").split(System.lineSeparator());
                String[] fontes = stripper.getTextForRegion("rect4").split(System.lineSeparator());
                String[] resto = ajustaResto(stripper.getTextForRegion("rect5").split(System.lineSeparator()));
                String[] tipoReceita = stripper2.getTextForRegion("rect6").split(System.lineSeparator());
//                String[] codigo = stripper2.getTextForRegion("rect7").split(System.lineSeparator());
                String[] periodo = stripper2.getTextForRegion("rect8").split(System.lineSeparator());
                String[] tudo = stripper3.getTextForRegion("rect9").split(System.lineSeparator());
                data = convertDate(stripper.getTextForRegion("rect10").replace(System.lineSeparator(), ""));

                for (int j = 0; j < resto.length; j++) {
                    if (obras.length == 0 || tudo[0].contains("Impostos Do Exterior") || tudo[0].contains("Contrato") || provedor[0].contains("Período:")
                            || tudo[0].contains("Impostos") || tudo[1].contains("Imposto De Renda") || obras[obras.length - 1].equals("WarnerChappell.com")) {
                        break;
                    }

                    String[] restoSep = resto[j].split(" ");

                    if (resto[j].contains("Sub-total")) {
                        while (!tudo[indiceTudo].contains(resto[j])) {
                            indiceTudo++;
                        }
                        boolean meioDaObra = false;
                        int indiceObraAnt = indiceObra;

                        if (pagInicial) {
                            indiceObra += 2;
                            pagInicial = false;
                        }
                        String proximaLinhaObra = obras[indiceObra + 1];
                        while (!verificaObra(obras[indiceObra]) || proximaLinhaObra.contains("Contrato")) {
                            indiceObra++;
                            if (indiceObra < obras.length - 1)
                                proximaLinhaObra = obras[indiceObra + 1];
                            else
                                proximaLinhaObra = "";
                            if (indiceObra == obras.length - 1 && !tudo[indiceTudo].contains(obras[indiceObra])) {
                                meioDaObra = true;
                                indiceObra = indiceObraAnt;
                                break;
                            }
                        }
                        if (meioDaObra) {
                            obra = obras[indiceObra] + " " + obras[indiceObra + 1];
                            indiceObra += 2;
                        } else {
                            if (verificaMaiuscula(proximaLinhaObra)) {
                                obra = obras[indiceObra] + " " + obras[indiceObra + 1];
                                indiceObra++;
                            } else {
                                obra = obras[indiceObra];
                            }
                        }

                        if (subTotAntes.equals("NadaAinda")) {
                            subTotAntes = restoSep[restoSep.length - 1];
                            obraAntiga = obra;

                        } else if (!obraAntiga.equals(obra)) {
                            double valorSubTot = Double.parseDouble(Helper.corrigeSeparadorDouble(subTotAntes));
                            somatorioSub += valorSubTot;

                            VerificationData verificationData = new VerificationData();

                            if (Math.round(somatorio) != Math.round(valorSubTot)) {
                                verificationData.setStatus("Arquivo Não Bateu");
                            } else {
                                verificationData.setStatus("Arquivo Bateu");
                            }
                            verificationData.setInformed_total(valorSubTot);
                            verificationData.setSummed_total(somatorio);
                            verificationData.setDifference(valorSubTot - somatorio);
                            verificationData.setDocument(nomeDoArquivo);
                            verificationData.setDocument_date(data);
                            this.verificacaoResultData.add(verificationData);

                            subTotAntes = restoSep[restoSep.length - 1];
                            somatorio = 0.0;
                            obraAntiga = obra;
                        }
                        if (j < resto.length - 2)
                            j += 2;
                        else
                            j++;
                    }

                    restoSep = resto[j].split(" ");

                    if (restoSep[restoSep.length - 1].matches("-?[0-9.]+,-?[0-9]+") && restoSep.length > 2 && !resto[j].contains("Total Do")) {
                        boolean tudoAtt = false;
                        boolean provedorCalib = false;
                        boolean paisCalib = false;
                        boolean fontesCalib = false;
                        boolean receitaCalib = false;
                        boolean codigoCalib = false;
                        boolean periodoCalib = false;
                        boolean calib = false;
                        boolean fonteVazia = false;

                        int indiceAntFonte = indiceFontes;
                        while (!calib) {

                            if (tudo[indiceTudo].contains(resto[j]) && !tudoAtt && tudo[indiceTudo].split(" ").length > 4) {
                                tudoAtt = true;
                            } else if (!tudoAtt) {
                                indiceTudo++;
                            }

                            if (tudoAtt) {
                                if (tudo[indiceTudo].contains(provedor[indiceProvedor]) && !provedorCalib) {
                                    provedorCalib = true;
                                } else if (!provedorCalib) {
                                    indiceProvedor++;
                                }
                                if (tudo[indiceTudo].contains(pais[indicePais]) && !paisCalib && pais[indicePais].length() > 3) {
                                    paisCalib = true;
                                } else if (!paisCalib) {
                                    indicePais++;
                                }
                                if (tudo[indiceTudo].contains(fontes[indiceFontes]) && !fontesCalib && fontes[indiceFontes].length() > 1) {
                                    fontesCalib = true;
                                } else if (!fontesCalib) {
                                    if (indiceFontes == fontes.length - 1) {
                                        fonteVazia = true;
                                        fontesCalib = true;
                                    } else {
                                        indiceFontes++;
                                    }
                                }
                                if (tudo[indiceTudo].contains(tipoReceita[indiceReceita]) && !receitaCalib && Arrays.stream(pais).noneMatch(tipoReceita[indiceReceita]::equals)
                                        && !tipoReceita[indiceReceita].equals(obra)) {
                                    receitaCalib = true;
                                } else if (!receitaCalib) {
                                    if (tipoReceita[indiceReceita].contains(obra)) {
                                        indiceReceita += 2;
                                    } else {
                                        indiceReceita++;
                                    }
                                }
//                    			if (tudo[indiceTudo].contains(codigo[indiceCodigo]) && !codigoCalib && codigo[indiceCodigo].matches("[0-9]+")) {
//                    				codigoCalib = true;
//                    			} else if (!codigoCalib) {
//                    				indiceCodigo++;
//                    			}
                                if (tudo[indiceTudo].contains(periodo[indicePeriodo]) && !periodoCalib && verificaData(periodo[indicePeriodo], "/")) {
                                    periodoCalib = true;
                                } else if (!periodoCalib) {
                                    indicePeriodo++;
                                }
                            }

                            if (provedorCalib && paisCalib && fontesCalib && receitaCalib && /*codigoCalib &&*/ periodoCalib) {
                                calib = true;
                            }

                        }
                        String fontesEntrar;
                        String receitaEntrar = tipoReceita[indiceReceita];
//                    	String codigoEntrar = codigo[indiceCodigo];
                        String periodoEntrar = periodo[indicePeriodo];
                        String provedorEntrar = provedor[indiceProvedor];
                        String paisEntrar = pais[indicePais];
                        if (fonteVazia) {
                            fontesEntrar = " ";
                            indiceFontes = indiceAntFonte;
                        } else {
                            fontesEntrar = fontes[indiceFontes];
                        }
                        String uni = restoSep[restoSep.length - 4];
                        String valRec = restoSep[restoSep.length - 3];
                        String taxa = restoSep[restoSep.length - 2];
                        String valPag = restoSep[restoSep.length - 1];
                        if (receitaEntrar.equals("Italy")) {
                            System.out.println("Porra");
                        }
                        ResultData resultData = new ResultData();

                        resultData.setNet_revenue(Helper.ajustaNumero(valPag));
                        resultData.setPercent_owned(Helper.ajustaNumero(taxa));
                        resultData.setGross_revenue(Helper.ajustaNumero(valRec));
                        resultData.setUnits(Integer.parseInt(Helper.corrigeSeparadorInt(uni)));
                        resultData.setDistributor(fontesEntrar);
                        resultData.setCountry(paisEntrar);
                        resultData.setSales_date(periodoEntrar);
//                        resultData.setCatalog_id(codigoEn);
                        resultData.setSales_date(periodoEntrar);
                        resultData.setType(receitaEntrar);
                        resultData.setTrack_name(obra.replace("-", ""));
                        resultData.setSource(editoraAtual);
                        resultData.setStatement_date(data);
                        resultData.setPath(nomeDoArquivo);
                        //TODO: Set Artist

                        resultadosResultData.add(resultData);

                    	/*Resultados.put(String.valueOf(Resultados.size()), new String[]{
                                obra.replace("-", ""),
                                receitaEntrar,
//                                codigoEntrar,
                                periodoEntrar,
                                provedorEntrar,
                                paisEntrar,
                                fontesEntrar,
                                uni,
                                valRec,
                                taxa, valPag, editoraAtual, data});*/

                        somatorio += Double.parseDouble(Helper.corrigeSeparadorDouble(valPag));
                        indiceObra++;

                    } else if (resto[j].contains("Total Do")) {
                        pagInicial = true;
                        if (i == numberOfPages - 1) {
                            double valorSubTot = Double.parseDouble(Helper.corrigeSeparadorDouble(subTotAntes));
                            somatorioSub += valorSubTot;

                            VerificationData verificationData = new VerificationData();

                            if (Math.round(somatorio) != Math.round(valorSubTot)) {
                                verificationData.setStatus("Arquivo Não Bateu");
                            } else {
                                verificationData.setStatus("Arquivo Bateu");
                            }
                            verificationData.setInformed_total(valorSubTot);
                            verificationData.setSummed_total(somatorio);
                            verificationData.setDifference(valorSubTot - somatorio);
                            verificationData.setDocument(nomeDoArquivo);
                            verificationData.setDocument_date(data);
                            this.verificacaoResultData.add(verificationData);
                        }
                    }
                }
            }
            reader.close();
            this.setConversionProgressByFileReaded(fileIndex);
        }
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
        
        //Inclinado a direita
        XSSFCellStyle esquerda = workbook.createCellStyle();
        esquerda.setFont(fonte);
        esquerda.setAlignment(HorizontalAlignment.RIGHT);

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
                if (obj.contains("+") || obj.contains("-")) {
                    sinais = true;
                }
                Cell cell = row.createCell(cellid++);
                if (obj.matches("-?[0-9.]+,-?[0-9]+") && !planilha.get(cedulaBase)[cellid - 1].contains("Taxa de Royalties")) {
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
                } else if (!obj.equals(" ") && planilha.get(cedulaBase)[cellid - 1].equals("percentual_titular")) {
                    cell.setCellValue(Double.parseDouble(Helper.corrigeSeparadorDouble(obj))/100);
                    cell.setCellStyle(porcentagem);
                } else if (obj.matches("[0-9]+") && !planilha.get(cedulaBase)[cellid - 1].equals("unidade")) {
                    cell.setCellValue(Integer.parseInt(Helper.corrigeSeparadorDouble(obj)));
                    cell.setCellStyle(numeros);
                } else if(obj.matches("[0-9.-]{2,}") && obj.contains(".")) {
                    cell.setCellValue(Double.parseDouble(obj));
                    cell.setCellStyle(real);
                } else if(verificaNotacao(obj) && obj.contains("E") && sinais) {
                    cell.setCellValue(Helper.converteDeNotacao(Helper.corrigeSeparadorDouble(obj)));
                    cell.setCellStyle(notacaoCientifica);
                } else if (planilha.get(cedulaBase)[cellid - 1].equals("data")) {
                	cell.setCellValue(obj);
                    cell.setCellStyle(esquerda);
                } else {
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
        System.out.println("Conversão concluída com êxito. Nome do arquivo salvo: " + nomeSaida + ".xlsx");

    }
    
    public boolean verificaNotacao(String num) {
        if (num.contains("E") && !num.equals("E")) {
            String[] numSep = num.split("E");
            if (numSep.length == 1) {
                return false;
            }
            String num2semSinal = numSep[1].replace("+", "").replace("-", "");
            if (numSep[0].matches("[0-9,.]+") && num2semSinal.matches("[0-9]+")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean verificaData(String data, String sep) {
		SimpleDateFormat df = new SimpleDateFormat("MM" + sep +"yyyy");
		if (data.equals("-")) {
			return false;
		}
		String date = data.split("-")[0];
		
		try {
			df.parse(date);
			String[] datasplit;
			if (sep.equals(".")) {
				datasplit = date.split("\\.");
			} else {
				datasplit = date.split(sep);
			}
			if (datasplit[0].length() == 2 && datasplit[1].length() == 2 ) {
				return true;
			}
		} catch (ParseException e) {
			return false;
		}
		return false;
	}

    public boolean verificaMaiuscula(String s) {
        if (s.isEmpty() || s.equals(" "))
            return false;
        for (int letra = 0; letra < s.length(); letra++) {
            if (Character.isLowerCase(s.charAt(letra))) {
                return false;
            }
        }
        return true;
    }

    public boolean verificaObra(String linha){
        if (linha.trim().equals("-")) return true;
        return verificaMaiuscula(linha) && linha.endsWith("-");
    }

    public String[] ajustaResto(String[] resto){
        List<String> ajustado = new ArrayList<>();
        for(String linha : resto){
            if (linha.matches("[a-zA-Z]{0,2} /"))
                continue;
            ajustado.add(linha);
        }
        return ajustado.toArray(String[]::new);
    }

    @Override
    public void run() {
        try {
            this.retornaResultados();
        } catch (Exception e) {
            this.conversionProgress = -1f;
            if (e instanceof ConversionException)
                this.error = e.getMessage();
            throw new RuntimeException(e);
        }
    }
}
