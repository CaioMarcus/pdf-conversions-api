package com.caio.pdf_conversions_api.Conversions;

import com.caio.pdf_conversions_api.Helpers.ConversionDateParser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ConversionThread extends ConversionDateParser implements Runnable {
    protected String pdfPath;
    protected String error;
    protected String[] arquivosNaPasta;

    // Progress
    protected Float conversionProgress;
    protected float convertWeight;
    protected float adjustmentWeight;

    protected ConversionThread(String pdfPath) {
        conversionProgress = 0f;
        convertWeight = 80f;
        adjustmentWeight = 19f;
        this.pdfPath = pdfPath;
        this.arquivosNaPasta = new File(this.pdfPath).list();
    }
}
