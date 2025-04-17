package com.caio.pdf_conversions_api.Helpers;

import com.caio.pdf_conversions_api.Conversions.ConversionThread;
import com.caio.pdf_conversions_api.Conversions.ConversionType;
import com.caio.pdf_conversions_api.Conversions.PDFs.Abramus.AbramusDigital;
import com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras.OutrasEditoras;
import com.caio.pdf_conversions_api.Conversions.PDFs.RelatorioAnalitico.RelatorioAnalitico;
import com.caio.pdf_conversions_api.Conversions.PDFs.Sony.SonyMusic;
import com.caio.pdf_conversions_api.Conversions.PDFs.Sony.SonyMusicPublishing;
import com.caio.pdf_conversions_api.Conversions.PDFs.Universal.Universal;
import com.caio.pdf_conversions_api.Conversions.PDFs.Warner.Warner;
import com.caio.pdf_conversions_api.Exceptions.ConversionTypeNotFound;

public class ConversionsHelper {

    /**
     * Cria o thread de conversão baseado no tipo de conversão informado, setando o caminho dos arquivos como o informado.
     *
     * @param type
     * @param conversionFilesPath
     * @return
     * @throws ConversionTypeNotFound
     */
    public static ConversionThread getConversionThread(String type, String conversionFilesPath, String xlsName, String[] filesToConvert) throws ConversionTypeNotFound {
        try {
            String adjustedType = type.toUpperCase().replace(" ", "_");
            ConversionType documentType = ConversionType.valueOf(adjustedType);

            if (documentType == ConversionType.RELATORIO_ANALITICO)
                return new RelatorioAnalitico(conversionFilesPath, xlsName, filesToConvert);
            if (documentType == ConversionType.UNIVERSAL)
                return new Universal(conversionFilesPath, xlsName, filesToConvert);
            if (documentType == ConversionType.SONY_MUSIC)
                return new SonyMusic(conversionFilesPath, xlsName, filesToConvert);
            if (documentType == ConversionType.SONY_MUSIC_PUBLISHING)
                return new SonyMusicPublishing(conversionFilesPath, xlsName, filesToConvert);
            if (documentType == ConversionType.ABRAMUS_DIGITAL)
                return new AbramusDigital(conversionFilesPath, xlsName, filesToConvert);
            if (documentType == ConversionType.WARNER)
                return new Warner(conversionFilesPath, xlsName, filesToConvert);
            if (documentType == ConversionType.OUTRAS_EDITORAS)
                return new OutrasEditoras(conversionFilesPath, xlsName, filesToConvert);

            throw new ConversionTypeNotFound();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            throw new ConversionTypeNotFound();
        }
    }

}
