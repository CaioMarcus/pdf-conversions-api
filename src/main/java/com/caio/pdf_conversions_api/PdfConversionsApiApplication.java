package com.caio.pdf_conversions_api;

import com.caio.pdf_conversions_api.Conversions.Sony.SonyMusic;
import com.caio.pdf_conversions_api.Helpers.ExportHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.text.ParseException;

@SpringBootApplication
public class PdfConversionsApiApplication {

	public static void main(String[] args) {
//		SpringApplication.run(PdfConversionsApiApplication.class, args);

		try {
			SonyMusic sonyMusic = new SonyMusic("D:\\Conversoes\\PDFs", "sonyteste");
			sonyMusic.run();
			ExportHelper.exportToCSV(sonyMusic.getResultados(), "D:\\Conversoes\\XLS\\sonyteste.csv");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

	}

}
