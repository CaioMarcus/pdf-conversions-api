package com.caio.pdf_conversions_api.Conversions.WarnerNovo;


import com.caio.pdf_conversions_api.Conversions.Warner.WarnerMusicTipo2;
import com.caio.pdf_conversions_api.Conversions.WarnerNovo.DadosArquivo.LinhaDado;
import com.caio.pdf_conversions_api.Conversions.WarnerNovo.DadosArquivo.Obra;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarnerNovo {

    private PDFTextStripperByArea leitorDados;
    private PDFTextStripperByArea leitorObra;
    private PDFTextStripperByArea leitorTudo;
    private PDDocument documentoAtual;
    private String diretorioArquivos, dataAtual;
    private String[] arquivos;
    private String[] linhasTudo;
    private String[] linhasTerritorio;
    private String[] linhasTipo;
    private String[] linhasObras;
    private String[] linhasConfig;
    private List<Obra> obras;
    private int indiceTerritorio, indiceTipo;
    private boolean rodarWarnerTipo2;

    public WarnerNovo(String diretorioArquivos) throws IOException {
        this.diretorioArquivos = diretorioArquivos;
        this.arquivos = new File(diretorioArquivos).list();
        this.obras = new ArrayList<>();
        this.rodarWarnerTipo2 = false;
    }

    public void leDocumentos() throws IOException {
        for (String arquivo : arquivos){
            this.documentoAtual = Loader.loadPDF(new File(this.diretorioArquivos + arquivo));
            this.montaLeitor();

            if (this.rodarWarnerTipo2){
                this.documentoAtual.close();
                this.rodarWarnerTipo2(arquivo);
                this.rodarWarnerTipo2 = false;
                continue;
            }

            for (int numeroPagina = 0; numeroPagina < this.documentoAtual.getNumberOfPages(); numeroPagina++){
                System.out.println("Lendo documento " + arquivo + "; Página: " + (numeroPagina + 1));
                this.extraiDadosPagina(numeroPagina);
                this.montaObrasPaginaAtual();
            }
            this.documentoAtual.close();
        }
    }

    private void montaLeitor() throws IOException {
        this.leitorDados = new PDFTextStripperByArea();
        this.leitorObra = new PDFTextStripperByArea();
        this.leitorTudo = new PDFTextStripperByArea();

        this.leitorDados.setSortByPosition(true);
        this.leitorObra.setSortByPosition(true);
        this.leitorTudo.setSortByPosition(true);

        Rectangle2D linhasTudo = null;
        Rectangle2D linhaData = null;
        Rectangle2D linhasTerritorio = null;
        Rectangle2D linhasTipo = null;
        Rectangle2D linhasObras = null;
        Rectangle2D linhasConfig = null;

        String tipo = verificaTipoDoc();

        if (tipo.equals("Royalties")) {
            linhasTudo = new Rectangle2D.Double(Helper.mmParaPx(0), Helper.mmParaPx(35.89), Helper.mmParaPx(297), Helper.mmParaPx(173.72));
            linhaData = new Rectangle2D.Double(Helper.mmParaPx(134.28), Helper.mmParaPx(10.77), Helper.mmParaPx(71.18), Helper.mmParaPx(5.00));

            linhasTerritorio = new Rectangle2D.Double(Helper.mmParaPx(0), Helper.mmParaPx(35.89), Helper.mmParaPx(48.85), Helper.mmParaPx(173.72));
            linhasTipo = new Rectangle2D.Double(Helper.mmParaPx(48.89), Helper.mmParaPx(35.89), Helper.mmParaPx(24.00), Helper.mmParaPx(173.72));

            linhasObras = new Rectangle2D.Double(Helper.mmParaPx(36.39), Helper.mmParaPx(35.89), Helper.mmParaPx(69.31), Helper.mmParaPx(173.72));
            linhasConfig = new Rectangle2D.Double(Helper.mmParaPx(105.84), Helper.mmParaPx(35.89), Helper.mmParaPx(53.44), Helper.mmParaPx(172.00));
        } else if (tipo.equals("Relatorio Analitico M2") || tipo.equals("Direitos Artisticos M2")){
            linhasTudo = new Rectangle2D.Double(Helper.mmParaPx(0), Helper.mmParaPx(35.78), Helper.mmParaPx(600), Helper.mmParaPx(173.72));
            linhaData = new Rectangle2D.Double(Helper.mmParaPx(149.00), Helper.mmParaPx(10.77), Helper.mmParaPx(71.18), Helper.mmParaPx(5.00));

            linhasTerritorio = new Rectangle2D.Double(Helper.mmParaPx(0), Helper.mmParaPx(35.78), Helper.mmParaPx(60.57), Helper.mmParaPx(173.72));
            linhasTipo = new Rectangle2D.Double(Helper.mmParaPx(60.59), Helper.mmParaPx(35.78), Helper.mmParaPx(24.00), Helper.mmParaPx(173.72));

            linhasObras = new Rectangle2D.Double(Helper.mmParaPx(36.39), Helper.mmParaPx(35.78), Helper.mmParaPx(80.00), Helper.mmParaPx(173.72));
            linhasConfig = new Rectangle2D.Double(Helper.mmParaPx(116.38), Helper.mmParaPx(35.78), Helper.mmParaPx(60.80), Helper.mmParaPx(173.72));
        } else if (tipo.equals("Direitos Artisticos M1")){
            this.rodarWarnerTipo2 = true;
            return;
        } else {
            linhasTudo = new Rectangle2D.Double(Helper.mmParaPx(0), Helper.mmParaPx(35.78), Helper.mmParaPx(251.00), Helper.mmParaPx(173.72));
            linhaData = new Rectangle2D.Double(Helper.mmParaPx(143.80), Helper.mmParaPx(4.10), Helper.mmParaPx(71.18), Helper.mmParaPx(5.00));

            linhasTerritorio = new Rectangle2D.Double(Helper.mmParaPx(0), Helper.mmParaPx(35.78), Helper.mmParaPx(68.50), Helper.mmParaPx(173.72));
            linhasTipo = new Rectangle2D.Double(Helper.mmParaPx(68.51), Helper.mmParaPx(35.78), Helper.mmParaPx(51.85), Helper.mmParaPx(173.72));

            linhasObras =  new Rectangle2D.Double(Helper.mmParaPx(0), Helper.mmParaPx(35.78), Helper.mmParaPx(68.50), Helper.mmParaPx(173.72));
            linhasConfig = new Rectangle2D.Double(Helper.mmParaPx(68.51), Helper.mmParaPx(35.78), Helper.mmParaPx(51.85), Helper.mmParaPx(173.72));
        }

        this.leitorTudo.addRegion("linhasTudo", linhasTudo);
        this.leitorTudo.addRegion("linhaData", linhaData);

        this.leitorDados.addRegion("linhasTerritorio", linhasTerritorio);
        this.leitorDados.addRegion("linhasTipo", linhasTipo);

        this.leitorObra.addRegion("linhasObras", linhasObras);
        this.leitorObra.addRegion("linhasConfig", linhasConfig);
    }

    private String verificaTipoDoc() throws IOException {
        Rectangle2D tipoDocumento = new Rectangle2D.Double(Helper.mmParaPx(0), Helper.mmParaPx(9.44), Helper.mmParaPx(297), Helper.mmParaPx(8));
        this.leitorTudo.addRegion("tipoDocumento", tipoDocumento);
        this.leitorTudo.extractRegions(this.documentoAtual.getPage(0));
        String linhaTipo = this.leitorTudo.getTextForRegion("tipoDocumento").replace(System.lineSeparator(), "");

        this.leitorTudo.removeRegion("tipoDocumento");

        if (linhaTipo.toUpperCase().contains("ROYALTIES DO PERÍODO"))
            return "Royalties";
        else {
            tipoDocumento = new Rectangle2D.Double(Helper.mmParaPx(252.38), Helper.mmParaPx(0), Helper.mmParaPx(44.05), Helper.mmParaPx(208.71));
            this.leitorTudo.addRegion("tipoDocumento", tipoDocumento);
            this.leitorTudo.extractRegions(this.documentoAtual.getPage(0));
            linhaTipo = this.leitorTudo.getTextForRegion("tipoDocumento").replace(System.lineSeparator(), "");
            this.leitorTudo.removeRegion("tipoDocumento");
            if (linhaTipo.isBlank())
                return "Direitos Artisticos M1";
            return "Direitos Artisticos M2";
        }
    }

    private void extraiDadosPagina(int pagina) throws IOException {
        this.leitorObra.extractRegions(documentoAtual.getPage(pagina));
        this.leitorDados.extractRegions(documentoAtual.getPage(pagina));
        this.leitorTudo.extractRegions(documentoAtual.getPage(pagina));

        this.linhasTudo = this.leitorTudo.getTextForRegion("linhasTudo").split(System.lineSeparator());
        this.dataAtual = this.leitorTudo.getTextForRegion("linhaData").replace(System.lineSeparator(), "");

        this.linhasTerritorio = this.leitorDados.getTextForRegion("linhasTerritorio").split(System.lineSeparator());
        this.linhasTipo = this.leitorDados.getTextForRegion("linhasTipo").split(System.lineSeparator());

        this.linhasObras = this.leitorObra.getTextForRegion("linhasObras").split(System.lineSeparator());
        this.linhasConfig = this.leitorObra.getTextForRegion("linhasConfig").split(System.lineSeparator());
    }

    private void montaObrasPaginaAtual(){
        this.indiceTerritorio = 0;
        this.indiceTipo = 0;
        Obra obraAtual = null;
        for (String linha : linhasTudo){
            if (linha.contains("Total"))
                continue;
            String[] linhaSep = linha.split(" ");
            String posM1 = linhaSep[linhaSep.length - 1].replace(",", "");
            if (posM1.matches("-?[(]?[\\d]+\\.[\\d{2}]+[)]?")){
                if (posM1.contains("(") && posM1.contains("("))
                    posM1 = "-" + posM1.replace("(", "").replace(")", "");
                String territorio = getIntersecaoListaEString(linha, linhasTerritorio);
                String tipo = getIntersecaoListaEString(linha, linhasTipo);

                Integer vendas;

                Double capa = Double.parseDouble(linhaSep[linhaSep.length - 2]);
                Double direitos = Double.parseDouble(posM1);

                Double precoBase = Double.parseDouble(linhaSep[linhaSep.length - 6].replace(",", ""));

                String porcentagemVendas = linhaSep[linhaSep.length - 5] + " " + linhaSep[linhaSep.length - 4] + " " +
                        linhaSep[linhaSep.length - 3];

                try{
                    vendas = Integer.parseInt(linhaSep[linhaSep.length - 7].replace(",", "")
                            .replace("(", "").replace(")", ""));
                } catch (NumberFormatException e){
                    vendas = Integer.parseInt(linhaSep[linhaSep.length - 6].replace(",", "")
                            .replace("(", "").replace(")", ""));
                    precoBase = 0.00;
                }


                LinhaDado linhaDado = new LinhaDado(territorio, tipo, vendas, precoBase, porcentagemVendas, capa, direitos, this.dataAtual);
                obraAtual.adicionaLinhaDado(linhaDado);
            } else {
                if (obraAtual != null)
                    obras.add(obraAtual);
                obraAtual = new Obra(getIntersecaoListaEString(linha.replace(linhaSep[0] + " ", ""), linhasObras),
                        getIntersecaoListaEString(linha.replace(linhaSep[0] + " " + linhaSep[1] + " ", ""), linhasConfig));
            }
        }
        if (obraAtual != null && obraAtual.getLinhasDados().size() > 0)
            obras.add(obraAtual);
    }

    private void rodarWarnerTipo2(String nomeDocumentoAtual) throws IOException {
        List<Map<String, String[]>> planilha = WarnerMusicTipo2.retornaResultados(this.diretorioArquivos, new String[]{nomeDocumentoAtual});
        String nomeObra, nomeAntigo = "";
        Obra obra = null;
        planilha.get(0).remove("0");
        for (String key : planilha.get(0).keySet()) {
            String[] linha = planilha.get(0).get(key);
            nomeObra = linha[0];

            if (!nomeObra.equals(nomeAntigo)) {
                if (obra != null)
                    obras.add(obra);
                obra = new Obra(linha[0], linha[1]);
                nomeAntigo = nomeObra;
            }

            LinhaDado linhaDado = new LinhaDado(linha[2], linha[3], Integer.parseInt(linha[4].replace(",", "")),
                    Double.parseDouble(linha[5]), linha[6], Double.parseDouble(Helper.corrigeSeparadorDouble(linha[7])), Double.parseDouble(linha[8]), linha[9]);

            obra.adicionaLinhaDado(linhaDado);
        }
        obras.add(obra);
    }
    
    private void cleanUnusedVariables() {
    	this.arquivos = null;
    	this.dataAtual = null;
    	this.documentoAtual = null;
    	this.leitorDados = null;
    	this.leitorObra = null;
    	this.leitorTudo = null;
    	this.linhasConfig = null;
    	this.linhasObras = null;
    	this.linhasTerritorio = null;
    	this.linhasTipo = null;
    	this.linhasTudo = null;
    	System.gc();
    }
    
    public void formataExportaPlanilha(String nomeSaida, String diretorioSaida) throws IOException, ParseException {
    	cleanUnusedVariables();
    	// Cria planilha
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet pdfs = workbook.createSheet("PDFs");

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

        //Porcentagem
        XSSFCellStyle porcentagem = workbook.createCellStyle();
        DataFormat df3 = workbook.createDataFormat();
        porcentagem.setDataFormat(df3.getFormat("#00.00%"));
        porcentagem.setFont(fonte);
        porcentagem.setBorderLeft(BorderStyle.THIN);
        porcentagem.setBorderRight(BorderStyle.THIN);
        porcentagem.setVerticalAlignment(VerticalAlignment.CENTER);

        // Estilo Numeros
        XSSFCellStyle numeros = workbook.createCellStyle();
        DataFormat df = workbook.createDataFormat();
        numeros.setDataFormat(df.getFormat("0.00;\\-0.00"));
        numeros.setFont(fonte);
        numeros.setAlignment(HorizontalAlignment.LEFT);
        numeros.setBorderLeft(BorderStyle.THIN);
        numeros.setBorderRight(BorderStyle.THIN);

        // Estilo Numeros Sem Casas Decimais
        XSSFCellStyle numerosSemCasaDecimal = workbook.createCellStyle();
        DataFormat dfSemCasaDecimal = workbook.createDataFormat();
        numerosSemCasaDecimal.setDataFormat(dfSemCasaDecimal.getFormat("0"));
        numerosSemCasaDecimal.setFont(fonte);
        numerosSemCasaDecimal.setAlignment(HorizontalAlignment.LEFT);
        numerosSemCasaDecimal.setBorderLeft(BorderStyle.THIN);
        numerosSemCasaDecimal.setBorderRight(BorderStyle.THIN);

        // Parte Funcional

        // Transforma os valores dos objetos em cédulas
        System.out.println("Adicionando Elementos...");

        XSSFRow row;
        int rowid = 0;

        row = pdfs.createRow(rowid++);

        Cell cellObra = row.createCell(0);
        Cell cellConfig = row.createCell(1);
        Cell cellTerritorio = row.createCell(2);
        Cell cellTipo = row.createCell(3);
        Cell cellVendas = row.createCell(4);
        Cell cellPrecoBase = row.createCell(5);
        Cell cellPorcentagemVendas = row.createCell(6);
        Cell cellCapa = row.createCell(7);
        Cell cellDireitos = row.createCell(8);
        Cell cellData = row.createCell(9);

        cellObra.setCellStyle(indice);
        cellConfig.setCellStyle(indice);
        cellTerritorio.setCellStyle(indice);
        cellTipo.setCellStyle(indice);
        cellVendas.setCellStyle(indice);
        cellPrecoBase.setCellStyle(indice);
        cellPorcentagemVendas.setCellStyle(indice);
        cellCapa.setCellStyle(indice);
        cellDireitos.setCellStyle(indice);
        cellData.setCellStyle(indice);

        cellObra.setCellValue("Obra");
        cellConfig.setCellValue("Configuração");
        cellTerritorio.setCellValue("Territorio");
        cellTipo.setCellValue("Tipo");
        cellVendas.setCellValue("Vendas");
        cellPrecoBase.setCellValue("Preco Base");
        cellPorcentagemVendas.setCellValue("Porcentagem Vendas/Porcentagem Royalties");
        cellCapa.setCellValue("Capa");
        cellDireitos.setCellValue("Direitos/Royalties");
        cellData.setCellValue("Data");

        for (int i = 0; i < obras.size(); i++) {
            Obra obraAtual = obras.get(i);
            List<LinhaDado> dados = obraAtual.getLinhasDados();
            for (LinhaDado dado : dados) {
                row = pdfs.createRow(rowid++);

                cellObra = row.createCell(0);
                cellConfig = row.createCell(1);
                cellTerritorio = row.createCell(2);
                cellTipo = row.createCell(3);
                cellVendas = row.createCell(4);
                cellPrecoBase = row.createCell(5);
                cellPorcentagemVendas = row.createCell(6);
                cellCapa = row.createCell(7);
                cellDireitos = row.createCell(8);
                cellData = row.createCell(9);

                cellObra.setCellStyle(style);
                cellConfig.setCellStyle(style);
                cellTerritorio.setCellStyle(style);
                cellTipo.setCellStyle(style);
                cellVendas.setCellStyle(numerosSemCasaDecimal);
                cellPrecoBase.setCellStyle(numeros);
                cellPorcentagemVendas.setCellStyle(porcentagem);
                cellCapa.setCellStyle(numeros);
                cellDireitos.setCellStyle(numeros);
                cellData.setCellStyle(style);

                cellObra.setCellValue(obraAtual.getNome());
                cellConfig.setCellValue(obraAtual.getConfiguracao());
                cellTerritorio.setCellValue(dado.getPais());
                cellTipo.setCellValue(dado.getTipo());
                cellVendas.setCellValue(dado.getVendas());
                cellPrecoBase.setCellValue(dado.getPrecoBase());
                cellPorcentagemVendas.setCellValue(dado.getPorcentagemVendas());
                cellCapa.setCellValue(dado.getCapa());
                cellDireitos.setCellValue(dado.getDireitos());
                cellData.setCellValue(dado.getData());
            }

        }
        
        //Cleaning unused Objects
        cellObra = null;
        cellConfig = null;
        cellTerritorio = null;
        cellTipo = null;
        cellVendas = null;
        cellPrecoBase = null;
        cellPorcentagemVendas = null;
        cellCapa = null;
        cellDireitos = null;
        cellData = null;
        
        style = null;
        indice = null;
        data = null;
        porcentagem = null;
        numeros = null;
        numerosSemCasaDecimal = null;
        
        cr = null;
        df = null;
        df3 = null;
        dfSemCasaDecimal = null;
        
        obras = null;
        
        System.gc();
        
        System.out.println("Terminando ajustes...");

        for (int i = 0; i < 10; i++) {
            pdfs.autoSizeColumn(i);
        }

        // Exporta o arquivo
        FileOutputStream out = new FileOutputStream(diretorioSaida + nomeSaida + ".xlsx");
        workbook.write(out);
        out.close();
        System.out.println("Conversão concluída com êxito. Nome do arquivo salvo: " + nomeSaida + ".xlsx");
    }

    private String getIntersecaoListaEString(String linha, String[] lista){
        int i = 0;
        String dado = "";
        for (; i < lista.length; i++){
            dado = lista[i];
            if (dado == null)
                continue;
            if (linha.contains(dado))
                break;
        }
        lista[i] = null;
        return dado;
    }
}
