package com.caio.pdf_conversions_api.Conversions;

import lombok.Data;

import java.util.concurrent.CompletableFuture;

@Data
public abstract class ConversionThread implements Runnable {
    protected String error;
    protected Float conversionProgress;
    protected float convertWeight;
    protected float adjustmentWeight;

    protected ConversionThread() {
        conversionProgress = 0f;
        convertWeight = 80f;
        adjustmentWeight = 19f;
    }
}
