package com.caio.pdf_conversions_api.Services;


import ConversoesAPI.Conversions.Helpers.PathsHelper;
import com.caio.pdf_conversions_api.Conversions.ConversionThread;
import com.caio.pdf_conversions_api.Conversions.ConversionType;

import com.caio.pdf_conversions_api.Conversions.PDFs.Abramus.AbramusDigital;
import com.caio.pdf_conversions_api.Conversions.PDFs.RelatorioAnalitico.RelatorioAnalitico;
import com.caio.pdf_conversions_api.Conversions.PDFs.Sony.SonyMusic;
import com.caio.pdf_conversions_api.Conversions.PDFs.Warner.Warner;
import com.caio.pdf_conversions_api.Conversions.Universal.Universal;
import com.caio.pdf_conversions_api.Exceptions.*;
import com.caio.pdf_conversions_api.Export.CsvExportable;
import com.caio.pdf_conversions_api.Export.CsvExporter;
import com.caio.pdf_conversions_api.Helpers.ExportHelper;
import com.caio.pdf_conversions_api.Models.ConversionStatus;
import com.caio.pdf_conversions_api.Models.StartConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A classe que gerencia o servićo de conversão. Criua e inicia uma nova conversão.
 */
@Service
public class ConversionServices {

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
     * @throws EcadSemApOuSdException   the ecad sem ap ou sd exception
     * @throws InvalidFileException     the invalid file exception
     * @throws IOException              the io exception
     */
    public ConversionThread StartConversion(StartConversion conversion) throws ArquivoRepetidoException, ConversionTypeNotFound, CorruptFileException, EcadSemApOuSdException, InvalidFileException, IOException {
        // Conversion Service changed to run on cloud

        // Saving the Files
        String conversionId = PathsHelper.createConversionId(conversion.getName());
        Path directory = Files.createTempDirectory(conversionId);

        for(MultipartFile file : conversion.getFiles()) {
            String fileName = file.getOriginalFilename();
            Path filePath = directory.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        ConversionThread conversionThread = getConversionThread(conversion.getType(),
                String.valueOf(directory.toAbsolutePath()),
                conversionId
        );

        Thread thread = new Thread(conversionThread);

        if (conversionThread == null) return null;
        thread.start();
        return conversionThread;
    }

//    public void SaveFiles(String conversionId, List<MultipartFile> files) throws IOException {
//        // Save the files to the desired location
//        for(MultipartFile file : files) {
//            String fileName = file.getOriginalFilename();
//            String path = PathsHelper.GetConversionPath(conversionId) + File.separator + fileName;
//            Files.copy(file.getInputStream(), Path.of(path), StandardCopyOption.REPLACE_EXISTING);
//        }
//    }

    /**
     * Retorna o Progresso da conversão enquanto não é finalizada, e então os seus dados, quando a mesma é finalizada.
     *
     * @param conversionThread           the conversion thread
     * @param conversionProgressInterval the conversion progress interval
     * @param emitter                    the emitter
     */
    @Async
    public void returnProgressThenData(ConversionThread conversionThread, int conversionProgressInterval, SseEmitter emitter) {
        try {
            monitorConversion(conversionThread, conversionProgressInterval, emitter);
            sendFinalResult(conversionThread, emitter);
        } catch (Exception e) {
            handleException(e, emitter);
        }
    }

    /**
     * Monitora o progresso da conversão e envia eventos SSE para o cliente.
     */
    private void monitorConversion(ConversionThread conversionThread, int interval, SseEmitter emitter) throws InterruptedException, IOException {
        while (true) {
            Float progress = conversionThread.getConversionProgress();

            if (progress == -1f) {
                sendErrorEvent(conversionThread, emitter);
                return;
            }

            sendProgressEvent(progress, emitter);

            if (progress >= 100f) break;

            Thread.sleep(interval * 1000L);
        }
    }

    /**
     * Envia os dados finais da conversão.
     */
    private void sendFinalResult(ConversionThread conversionThread, SseEmitter emitter) throws IOException {
        String dataCsvName = String.format("%s_data", conversionThread.getXlsName());
        String verificationCsvName = String.format("%s_verification", conversionThread.getXlsName());

        /*String dataFilePath = cloudStorageService.exportAndUploadData(
                conversionThread.getResultadosResultData(),
                dataCsvName
        );

        String verificationFilePath = cloudStorageService.exportAndUploadData(
                conversionThread.getVerificacaoResultData(),
                verificationCsvName
        );*/

        String dataFilePath = this.exportToLocalCsv(conversionThread.getResultadosResultData(), dataCsvName);
        String verificationFilePath = this.exportToLocalCsv(conversionThread.getVerificacaoResultData(), verificationCsvName);

        sendProgressEvent(100f, emitter);

        // Criando JSON com os caminhos dos arquivos
        Map<String, String> resultData = new HashMap<>();
        resultData.put("dataFilePath", dataFilePath);
        resultData.put("verificationFilePath", verificationFilePath);

        // Enviar o JSON como resultado final
        emitter.send(SseEmitter.event()
                .name(ConversionStatus.RESULT.getEventName())
                .data(resultData));

        // Enviar evento de finalização
        emitter.send(SseEmitter.event()
                .name(ConversionStatus.COMPLETED.getEventName())
                .data("Conversion completed successfully!"));

        emitter.complete();
    }


    /**
     * Envia um evento de progresso.
     */
    private void sendProgressEvent(Float progress, SseEmitter emitter) throws IOException {
        String json = "{" +
                "\"status\": \"" + ConversionStatus.IN_PROGRESS.getEventName() + "\"," +
                "\"value\": " + progress.intValue() +
                "}";

        emitter.send(SseEmitter.event()
                .name(ConversionStatus.IN_PROGRESS.getEventName())
                .data(json));
    }


    /**
     * Envia um evento de erro e finaliza a conexão.
     */
    private void sendErrorEvent(ConversionThread conversionThread, SseEmitter emitter) throws IOException {
        String conversionError = conversionThread.getError();
        String json = "{" +
                "\"status\": \"" + ConversionStatus.ERROR.getEventName() + "\"," +
                "\"value\": \"Error: " + conversionError + "\"" +
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

    /**
     * Cria o thread de conversão baseado no tipo de conversão informado, setando o caminho dos arquivos como o informado.
     *
     * @param type
     * @param conversionFilesPath
     * @return
     * @throws ConversionTypeNotFound
     */
    private ConversionThread getConversionThread(String type, String conversionFilesPath, String xlsName) throws ConversionTypeNotFound {
        try{
            String adjustedType = type.toUpperCase().replace(" ", "_");
            ConversionType documentType = ConversionType.valueOf(adjustedType);

            if (documentType == ConversionType.RELATORIO_ANALITICO)
                return new RelatorioAnalitico(conversionFilesPath, xlsName);
            if (documentType == ConversionType.UNIVERSAL)
                return new Universal(conversionFilesPath, xlsName);
            if (documentType == ConversionType.SONY_MUSIC)
                return new SonyMusic(conversionFilesPath, xlsName);
            if (documentType == ConversionType.ABRAMUS_DIGITAL)
                return new AbramusDigital(conversionFilesPath);

            throw new ConversionTypeNotFound();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            throw new ConversionTypeNotFound();
        }
    }

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
