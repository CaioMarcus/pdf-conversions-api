package com.caio.pdf_conversions_api.Conversions;

import com.caio.pdf_conversions_api.Export.ResultData;
import com.caio.pdf_conversions_api.Export.VerificationData;
import com.caio.pdf_conversions_api.Helpers.ConversionDateParser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ConversionThread extends ConversionDateParser implements Runnable {
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

    protected ConversionThread(String pdfPath, String xlsName) {
        conversionProgress = 0f;
        this.pdfPath = pdfPath;
        this.xlsName = xlsName;
        this.arquivosNaPasta = new File(this.pdfPath).list();
    }

    protected ConversionThread(String pdfPath) {
        conversionProgress = 0f;
        this.pdfPath = pdfPath;
        this.arquivosNaPasta = new File(this.pdfPath).list();
    }

    protected void setConversionProgress(int currentFile){
        this.conversionProgress = ((currentFile + 1) / (float) arquivosNaPasta.length) * convertWeight;
    }
}
