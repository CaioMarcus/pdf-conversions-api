package com.caio.pdf_conversions_api.Conversions.PDFs;

import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.LineData;
import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.PDFAreaStripper;
import com.caio.pdf_conversions_api.Conversions.ConversionThread;
import com.caio.pdf_conversions_api.Helpers.Helper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public abstract class BasePdfConversion extends ConversionThread {

    protected String currentDate;
    protected String currentFile;
//    protected String nomeDocumentoAtual;
//    protected PDDocument documentoAtual;
//    protected List<LineData> currentPageLines;
//    protected boolean skipPage;
    protected boolean extractDateEachPage;
    protected int currentPage = 0;
    protected int currentLine = 0;

    protected boolean stripperSetSortByPosition = true;
    protected Rectangle2D.Double stripperBounds;

    protected String[] unwantedPageLines;
    protected String[] unwantedLines;
    protected String[] indexLine;
    protected String verificationLine;
    protected String dateLine;
    protected boolean readDateEveryPage = false;

    protected double documentTotalSum;
    protected double acceptableDifferencePercentage = 5;

    protected BasePdfConversion(String pdfPath, String xlsName) {
        super(pdfPath, xlsName);
        setUnwantedPageLines();
        setUnwantedLines();
        setIndexLine();
        setDateLine();
        this.setVerificationLine();
    }

    protected BasePdfConversion(String pdfPath) {
        super(pdfPath);
        setUnwantedPageLines();
        setUnwantedLines();
        setIndexLine();
        setDateLine();
        this.setVerificationLine();
    }

    @Override
    public void run() {
        try {
            leDocumentos();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void leDocumentos() throws IOException {
        this.resultados.add(indexLine);

        for (String arquivo : this.arquivosNaPasta) {
            this.currentFile = arquivo;
            processFile(arquivo);
        }


        /*
        int numCores = Runtime.getRuntime().availableProcessors();
        ExecutorService fileExecutor = Executors.newFixedThreadPool(numCores / 2); // Parallel file processing
        List<Future<Void>> fileTasks = new ArrayList<>();
        for (String arquivo : this.arquivosNaPasta) {
            fileTasks.add(fileExecutor.submit(() -> {
                processFile(arquivo);
                return null;
            }));
        }

        for (Future<Void> task : fileTasks) {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        fileExecutor.shutdown();*/
    }

    private void processFile(String arquivo) {
        System.out.printf("Lendo documento %s%n", arquivo);
        Path filePath = Path.of(this.pdfPath, arquivo);
        StringBuffer date;

        try (PDDocument documentoAtual = Loader.loadPDF(filePath.toFile())) {
            resetDocumentAtributes();

            int totalPages = documentoAtual.getNumberOfPages();
            for (int currentPage = 1; currentPage < totalPages + 1; currentPage++) {
                System.out.printf("Lendo Página: %d%n", currentPage);
                List<LineData> currentPageLines = extractPageData(documentoAtual, currentPage);
                processLines(currentPageLines, arquivo);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected List<LineData> extractPageData(PDDocument document, int page) throws IOException {
        PDFAreaStripper stripper;

        if (this.stripperBounds == null)
            stripper = new PDFAreaStripper();
        else
            stripper = new PDFAreaStripper(this.stripperBounds);

        stripper.setSortByPosition(this.stripperSetSortByPosition);
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        return stripper.getLines(document);
    }

    protected void processLines(List<LineData> lines, String currentDocumentName) {
        for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
            this.currentLine = lineIdx;
            LineData line = lines.get(lineIdx);
            if (isLineFromUnwantedPage(line)){
                return;
            }

            if ((readDateEveryPage || this.currentDate == null) && isDateLine(line)){
                readDate(line);
                continue;
            }

            if (isLineUnwanted(line))
                continue;

            if (isVerificationLine(line)){
                this.doVerification(lines, line, currentDocumentName);
                return;
            }

            this.processLine(line);
        }
    }

    protected void readDate(LineData line){
        if (!extractDateEachPage && this.currentDate != null) return;
        this.currentDate = this.convertDate(line.getFullLine());
    }

    protected void addResult(Object[] result, Double value){
        if (this.resultados.getFirst().length != result.length)
            throw new RuntimeException("Size of Index Line different from result. This will break later.");

        this.resultados.add(result);
        this.documentTotalSum += value;
    }

    protected void doVerification(List<LineData> lines, LineData line, String currentDocumentName){
        String documentGivenValue = this.extractVerificationLine(line);

        double documentGivenValueDouble = Helper.ajustaNumero(documentGivenValue);
        String verificationResult = "VALORES BATERAM";

        if (!Helper.isClose(documentGivenValueDouble, this.documentTotalSum, this.acceptableDifferencePercentage)) {
            verificationResult = "VALORES NÃO BATERAM";
        }

        this.verificacao.add(new String[]{
            verificationResult,
            "INFORMADO:",
            String.valueOf(documentGivenValueDouble),
            "CALCULADO:",
            String.valueOf(this.documentTotalSum),
            "DATA:", this.currentDate,
            "DOCUMENT NAME: ",
            currentDocumentName
        });
    }

    protected boolean isDateLine(LineData line){
        if (dateLine == null) return false;
        return line.getFullLine().contains(this.dateLine);
    }

    protected boolean isVerificationLine(LineData line){
        if (verificationLine == null) return false;
        return line.getFullLine().contains(verificationLine);
    }

    protected boolean isLineFromUnwantedPage(LineData line){
        if (this.unwantedPageLines == null) return false;
        return Arrays.stream(this.unwantedPageLines)
                .anyMatch(unLin -> line.getFullLine().contains(unLin));
    }

    protected boolean isLineUnwanted(LineData line){
        if (this.unwantedLines == null) return false;
        return Arrays.stream(this.unwantedLines)
                .anyMatch(unLin -> line.getFullLine().contains(unLin));
    }

    protected void resetDocumentAtributes(){
        this.currentPage = 0;
        this.documentTotalSum = 0D;
        this.currentDate = null;
    }


    protected abstract String extractVerificationLine(LineData line);
    protected abstract void setIndexLine();
    protected abstract void setUnwantedPageLines();
    protected abstract void setUnwantedLines();
    protected abstract void setDateLine();
    protected abstract void setVerificationLine();
    protected abstract void processLine(LineData line);
    protected abstract boolean isDataLine(LineData line);
}
