package com.caio.pdf_conversions_api;


import com.caio.pdf_conversions_api.Conversions.PDFs.RelatorioAnalitico.RelatorioAnalitico;
import com.caio.pdf_conversions_api.Conversions.PDFs.Sony.SonyMusic;
import com.caio.pdf_conversions_api.Conversions.PDFs.Sony.SonyMusicPublishing;
import com.caio.pdf_conversions_api.Helpers.ExportHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.text.ParseException;

@SpringBootApplication
public class PdfConversionsApiApplication {

	public static void main(String[] args) throws IOException, ParseException {
//		SpringApplication.run(PdfConversionsApiApplication.class, args);

        /*SonyMusicPublishing sonyMusic = new SonyMusicPublishing("D:\\Conversoes\\PDFs");
        sonyMusic.run();
        ExportHelper.exportData(sonyMusic.getResultados(),sonyMusic.getVerificacao(),"D:\\Conversoes\\XLS\\", "rogerio_flausiano_2021_11");*/

        /*SonyMusicPublishing sonyMusic = new SonyMusicPublishing("D:\\Conversoes\\PDFs");
        sonyMusic.run();
        ExportHelper.exportData(sonyMusic.getResultados(), sonyMusic.getVerificacao(),"D:\\Conversoes\\XLS\\", "marcio_buzelin_11_2021");*/

        RelatorioAnalitico relatorioAnalitico = new RelatorioAnalitico("D:\\Conversoes\\PDFs", "");
        relatorioAnalitico.run();
        ExportHelper.exportData(relatorioAnalitico.getResultados(), relatorioAnalitico.getVerificacao(),"D:\\Conversoes\\XLS\\", "relatorio_analitico_teste");
    }

}
