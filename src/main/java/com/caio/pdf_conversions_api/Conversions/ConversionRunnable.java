package com.caio.pdf_conversions_api.Conversions;

import com.caio.pdf_conversions_api.Export.ResultData;
import com.caio.pdf_conversions_api.Export.VerificationData;

import java.util.List;

public interface ConversionRunnable extends Runnable {
    Float getConversionProgress();
    String getError();
    String getXlsName();
    List<ResultData> getResultadosResultData();
    List<VerificationData> getVerificacaoResultData();
}
