package com.caio.pdf_conversions_api.Helpers;

import com.caio.pdf_conversions_api.Conversions.ConversionThread;
import com.caio.pdf_conversions_api.Conversions.ConversionType;
import com.caio.pdf_conversions_api.Conversions.PDFs.Abramus.AbramusDigital;
import com.caio.pdf_conversions_api.Conversions.PDFs.BMG.DocumentoBMG;
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

            return switch (documentType) {
                case RELATORIO_ANALITICO -> new RelatorioAnalitico(conversionFilesPath, xlsName, filesToConvert);
                case UNIVERSAL -> new Universal(conversionFilesPath, xlsName, filesToConvert);
                case SONY_MUSIC -> new SonyMusic(conversionFilesPath, xlsName, filesToConvert);
                case SONY_MUSIC_PUBLISHING -> new SonyMusicPublishing(conversionFilesPath, xlsName, filesToConvert);
                case ABRAMUS_DIGITAL -> new AbramusDigital(conversionFilesPath, xlsName, filesToConvert);
                case WARNER -> new Warner(conversionFilesPath, xlsName, filesToConvert);
                case OUTRAS_EDITORAS -> new OutrasEditoras(conversionFilesPath, xlsName, filesToConvert);
                case BMG -> new DocumentoBMG(conversionFilesPath, xlsName, filesToConvert);
                default -> throw new ConversionTypeNotFound();
            };
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            throw new ConversionTypeNotFound();
        }
    }

}
