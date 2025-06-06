package com.caio.pdf_conversions_api.Services;


import ConversoesAPI.Conversions.Helpers.PathsHelper;
import com.caio.pdf_conversions_api.Conversions.ConversionParallelProcessor;
import com.caio.pdf_conversions_api.Conversions.ConversionRunnable;
import com.caio.pdf_conversions_api.Conversions.ConversionThread;
import com.caio.pdf_conversions_api.Exceptions.*;
import com.caio.pdf_conversions_api.Export.CsvExportable;
import com.caio.pdf_conversions_api.Export.CsvExporter;
import com.caio.pdf_conversions_api.Helpers.ConversionsHelper;
import com.caio.pdf_conversions_api.Models.ConversionStatus;
import com.caio.pdf_conversions_api.Models.StartConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * A classe que gerencia o servićo de conversão. Criua e inicia uma nova conversão.
 */
@Service
public class ConversionServices {
    @Value("${conversion_progress_interval}")
    private int conversionProgressInterval;

    private final CloudStorageService cloudStorageService;

    @Autowired
    public ConversionServices(CloudStorageService cloudStorageService) {
        this.cloudStorageService = cloudStorageService;
    }

    /**
     * Inicia uma nova conversão como thread.
     *
     * @param conversion the conversion
     * @return the conversion thread
     * @throws ArquivoRepetidoException the arquivo repetido exception
     * @throws ConversionTypeNotFound   the conversion type not found
     * @throws CorruptFileException     the corrupt file exception
     * @throws InvalidFileException     the invalid file exception
     * @throws IOException              the io exception
     */
    public ConversionRunnable startConversion(StartConversion conversion) throws ArquivoRepetidoException, ConversionTypeNotFound, CorruptFileException, InvalidFileException, IOException {
        // Conversion Service changed to run on cloud

        // Saving the Files
        String conversionId = PathsHelper.createConversionId(conversion.getName());
        Path directory = Files.createTempDirectory(conversionId);

        for(MultipartFile file : conversion.getFiles()) {
            String fileName = file.getOriginalFilename();
            Path filePath = directory.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        ConversionThread conversionThread = ConversionsHelper.getConversionThread(conversion.getType(),
                String.valueOf(directory.toAbsolutePath()),
                conversionId,
                directory.toFile().list()
        );

        Thread thread = new Thread(conversionThread);

        if (conversionThread == null) return null;
        thread.start();
        return conversionThread;
    }

    public ConversionRunnable startConversionParallel(StartConversion conversion) throws ArquivoRepetidoException, ConversionTypeNotFound, CorruptFileException, InvalidFileException, IOException {
        // Conversion Service changed to run on cloud
        ConversionParallelProcessor conversionParallelProcessor = new ConversionParallelProcessor(conversion);
        Thread thread = new Thread(conversionParallelProcessor);
        thread.start();
        return conversionParallelProcessor;
    }

    @Async
    public void returnProgressThenDataAsync(ConversionRunnable conversionThread, SseEmitter emitter) {
        returnProgressThenData(conversionThread, emitter);
    }

    /**
     * Retorna o Progresso da conversão enquanto não é finalizada, e então os seus dados, quando a mesma é finalizada.
     *
     * @param conversionThread           the conversion thread
     * @param emitter                    the emitter
     */
    public void returnProgressThenData(ConversionRunnable conversionThread, SseEmitter emitter) {
        try {
            monitorConversion(conversionThread, emitter);
            sendFinalResult(conversionThread, emitter);
        } catch (Exception e) {
            emitter.completeWithError(e);
//            handleException(e, emitter);
        }
    }

    /**
     * Monitora o progresso da conversão e envia eventos SSE para o cliente.
     */
    private void monitorConversion(ConversionRunnable conversionThread, SseEmitter emitter) throws InterruptedException, IOException {
        Float progress = conversionThread.getConversionProgress();
        do{
            if (progress == -1f) {
                sendErrorEvent(conversionThread, emitter);
                return;
            }
            sendProgressEvent(progress, emitter);
            if (progress >= 100f) break;

            progress = conversionThread.getConversionProgress();
        } while (progress < 100f);
    }

    /**
     * Envia os dados finais da conversão.
     */
    private void sendFinalResult(ConversionRunnable conversionThread, SseEmitter emitter) throws IOException, InterruptedException {
        String dataCsvName = String.format("%s_data", conversionThread.getXlsName());
        String verificationCsvName = String.format("%s_verification", conversionThread.getXlsName());

        String dataFilePath = cloudStorageService.exportAndUploadData(
                conversionThread.getResultadosResultData(),
                dataCsvName
        );

        String verificationFilePath = cloudStorageService.exportAndUploadData(
                conversionThread.getVerificacaoResultData(),
                verificationCsvName
        );

//        String dataFilePath = this.exportToLocalCsv(conversionThread.getResultadosResultData(), dataCsvName);
//        String verificationFilePath = this.exportToLocalCsv(conversionThread.getVerificacaoResultData(), verificationCsvName);

        sendResultEvent(emitter, dataFilePath, verificationFilePath);
        sendCompletedEvent(emitter);
    }

    private void sendCompletedEvent(SseEmitter emitter) throws IOException {
        String json = "{" +
                "\"status\": \"" + ConversionStatus.COMPLETED.getEventName() + "\"," +
                "\"value\": \"" + "Conversion completed successfully!" + "\"" +
                "}";
        // Enviar evento de finalização
        emitter.send(SseEmitter.event()
                .name(ConversionStatus.COMPLETED.getEventName())
                .data(json));

        emitter.complete();
    }

    private void sendResultEvent(SseEmitter emitter, String dataFilePath, String verificationFilePath) throws IOException, InterruptedException {
        String json = "{" +
                "\"status\": \"" + ConversionStatus.RESULT.getEventName() + "\"," +
                "\"value\": " +
                    "{" +
                        " \"dataFilePath\": \"" + dataFilePath + "\"," +
                        " \"verificationFilePath\": \"" + verificationFilePath + "\"" +
                    "}" +
                "}";

        // Enviar o JSON como resultado final
        emitter.send(SseEmitter.event()
                .name(ConversionStatus.RESULT.getEventName())
                .data(json));

        Thread.sleep(this.conversionProgressInterval * 1000L);
    }


    /**
     * Envia um evento de progresso.
     */
    private void sendProgressEvent(Float progress, SseEmitter emitter) throws IOException, InterruptedException {
        String json = "{" +
                "\"status\": \"" + ConversionStatus.IN_PROGRESS.getEventName() + "\"," +
                "\"value\": " + progress.intValue() +
                "}";

        emitter.send(SseEmitter.event()
                .name(ConversionStatus.IN_PROGRESS.getEventName())
                .data(json));

        Thread.sleep(this.conversionProgressInterval * 1000L);
    }


    /**
     * Envia um evento de erro e finaliza a conexão.
     */
    private void sendErrorEvent(ConversionRunnable conversionThread, SseEmitter emitter) throws IOException {
        String conversionError = conversionThread.getError();
        String json = "{" +
                "\"status\": \"" + ConversionStatus.ERROR.getEventName() + "\"," +
                "\"value\": \"Error: " + conversionError.replace("\"", "'") + "\"" +
                "}";

        emitter.send(SseEmitter.event()
                .name(ConversionStatus.ERROR.getEventName())
                .data(json));

        emitter.complete();
    }


    /**
     * Trata exceções inesperadas e finaliza a conexão com erro.
     */
    private void handleException(Exception e, SseEmitter emitter) {
        try {
            String json = "{" +
                    "\"status\": \"" + ConversionStatus.ERROR.getEventName() + "\"," +
                    "\"value\": \"Unexpected error: " + e.getMessage() + "\"" +
                    "}";

            emitter.send(SseEmitter.event()
                    .name(ConversionStatus.ERROR.getEventName())
                    .data(json));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            emitter.completeWithError(e);
        }
    }

    //region ConversionsHelper

    public String exportToLocalCsv(List<? extends CsvExportable> resultados, String fileName) {
        String localPath = System.getProperty("user.dir") + "/exports/" + fileName + ".csv";

        try {
            // Cria diretório caso não exista
            File directory = new File(System.getProperty("user.dir") + "/exports");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Exporta os dados para CSV
            CsvExporter.exportToCsv(localPath, resultados);
            return localPath;
        } catch (Exception e) {
            throw new RuntimeException("Error while exporting CSV locally", e);
        }
    }

    //endregion
}
