package com.caio.pdf_conversions_api.Conversions;

import com.caio.pdf_conversions_api.Export.ResultData;
import com.caio.pdf_conversions_api.Export.VerificationData;
import com.caio.pdf_conversions_api.Helpers.ConversionDateParser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ConversionThread extends ConversionDateParser implements ConversionRunnable {
    protected String pdfPath;
    protected String xlsName;

    protected String error;
    protected String[] arquivosNaPasta;

    // Progress
    protected Float conversionProgress;
    protected float convertWeight = 100f;
    protected float adjustmentWeight = 0f;

    //Resultados
    protected List<Object[]> resultados = new ArrayList<>();
    protected List<Object[]> verificacao = new ArrayList<>();

    // Novo Tipo de Resultados
    protected List<ResultData> resultadosResultData = new ArrayList<>();
    protected List<VerificationData> verificacaoResultData = new ArrayList<>();

    protected ConversionThread(String pdfPath, String xlsName, String[] filesToConvert) {
        conversionProgress = 0f;
        this.pdfPath = pdfPath;
        this.xlsName = xlsName;
        this.arquivosNaPasta = filesToConvert;
    }

    protected ConversionThread(String pdfPath, String[] filesToConvert) {
        conversionProgress = 0f;
        this.pdfPath = pdfPath;
        this.arquivosNaPasta = filesToConvert;
    }

    protected void setConversionProgressByFileReaded(int currentFile){
        this.conversionProgress = ((currentFile + 1) / (float) arquivosNaPasta.length) * convertWeight;
    }

    public String getError() {
        return error == null ? "Erro Desconhecido. Contate o Administrador" : error;
    }
}
