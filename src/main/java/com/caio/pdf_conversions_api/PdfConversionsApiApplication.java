package com.caio.pdf_conversions_api;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PdfConversionsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdfConversionsApiApplication.class, args);
    }

}
