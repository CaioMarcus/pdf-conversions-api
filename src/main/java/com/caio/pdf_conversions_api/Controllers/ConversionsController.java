package com.caio.pdf_conversions_api.Controllers;

import com.caio.pdf_conversions_api.Conversions.ConversionRunnable;
import com.caio.pdf_conversions_api.Conversions.ConversionThread;
import com.caio.pdf_conversions_api.Exceptions.*;
import com.caio.pdf_conversions_api.Models.StartConversion;
import com.caio.pdf_conversions_api.Services.ConversionServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * O controlador de conversões. Recebera as chamadas de conversão do cliente.
 */
@RestController
@RequestMapping("/Conversions")
public class ConversionsController {
    private final ConversionServices conversionService;
    @Value("${emitter_timeout_time}")
    private int emitterTimeout;

    public ConversionsController(ConversionServices conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Recebe uma chamada de conversão, e estabelece uma conexão SSE emitter, para enviar os dados para o cliente.
     *
     * @param name  the name
     * @param type  the type
     * @param files the files
     * @return the sse emitter
     */
    @PostMapping("/Convert")
    public SseEmitter Convert(@RequestParam("name") String name, @RequestParam("type") String type, @RequestParam("files") List<MultipartFile> files){
        StartConversion conversion = new StartConversion();
        conversion.setName(name);
        conversion.setType(type);
        conversion.setFiles(files);

        return getEcadSseEmmiter(conversion);
    }

    /*@PostMapping("/Debug")
    public SseEmitter Debug(@RequestParam("name") String name, @RequestParam("type") String type, @RequestParam("files") List<MultipartFile> files){
        StartConversion conversion = new StartConversion();
        conversion.setName(name);
        conversion.setType(type);
        conversion.setFiles(files);
        DebugHelper.DEBUG = true;
        return getEcadSseEmmiter(conversion);
    }*/

    private SseEmitter getEcadSseEmmiter(StartConversion conversion) {
        try {
            // Inicia o Thread da conversão solicitada.
            ConversionRunnable conversionThread = conversionService.startConversionParallel(conversion);
            SseEmitter emitter = new SseEmitter(emitterTimeout * 60000L);

            // Dispara a execução assíncrona com contexto preservado
            conversionService.returnProgressThenDataAsync(conversionThread, emitter);

            return emitter;
        } catch (ArquivoRepetidoException | ConversionTypeNotFound | CorruptFileException |
                 InvalidFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
