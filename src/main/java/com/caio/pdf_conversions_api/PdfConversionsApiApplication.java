package com.caio.pdf_conversions_api;


import com.caio.pdf_conversions_api.Conversions.PDFs.Abramus.AbramusDigital;
import com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras.OutrasEditoras;
import com.caio.pdf_conversions_api.Conversions.PDFs.Warner.Warner;
import com.caio.pdf_conversions_api.Export.CsvExporter;
import com.caio.pdf_conversions_api.Helpers.ExportHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
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

        /*RelatorioAnalitico relatorioAnalitico = new RelatorioAnalitico("D:\\Conversoes\\PDFs", "");
        relatorioAnalitico.run();
        ExportHelper.exportData(relatorioAnalitico.getResultados(), relatorioAnalitico.getVerificacao(),"D:\\Conversoes\\XLS\\", "relatorio_analitico_teste");*/

		//AbramusDigital abramusDigital = new AbramusDigital("D:\\Conversoes\\PDFs");
		//abramusDigital.run();
		//ExportHelper.exportData(abramusDigital.getResultados(), abramusDigital.getVerificacao(), "D:\\Conversoes\\XLS\\", "teste");

//		OutrasEditoras outrasEditoras = new OutrasEditoras("D:\\Conversoes\\PDFs");
//		outrasEditoras.run();
//		CsvExporter.exportToCsv("D:\\Conversoes\\XLS\\marcos_esteves_yasmim_music_warner_dados.csv", outrasEditoras.getResultadosResultData());
//		CsvExporter.exportToCsv("D:\\Conversoes\\XLS\\marcos_esteves_yasmim_music_warner_verification.csv", outrasEditoras.getVerificacaoResultData());

		Warner warner = new Warner();
		warner.retornaResultados("D:\\Conversoes\\PDFs", new File("D:\\Conversoes\\PDFs"));

    }

}
