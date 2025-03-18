package com.caio.pdf_conversions_api;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.text.ParseException;

@SpringBootApplication
public class PdfConversionsApiApplication {

	public static void main(String[] args) throws IOException, ParseException {
		SpringApplication.run(PdfConversionsApiApplication.class, args);


    }

}
