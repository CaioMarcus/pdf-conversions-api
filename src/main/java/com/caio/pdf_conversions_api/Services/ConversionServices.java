package com.caio.pdf_conversions_api.Services;


import ConversoesAPI.Conversions.Helpers.PathsHelper;
import com.caio.pdf_conversions_api.Conversions.ConversionThread;
import com.caio.pdf_conversions_api.Conversions.ConversionType;
import com.caio.pdf_conversions_api.Conversions.RelatorioAnalitico.RelatorioAnalitico;
import com.caio.pdf_conversions_api.Conversions.Universal.UniversalXlsReader.DocumentType;
import com.caio.pdf_conversions_api.Exceptions.*;
import com.caio.pdf_conversions_api.Models.StartConversion;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * A classe que gerencia o servićo de conversão. Criua e inicia uma nova conversão.
 */
@Service
public class ConversionServices {

    @Value("${result_retry_amount}")
    private int resultRetryAmount;

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

        ConversionThread conversionThread = getConversionThread(conversion.getType(), String.valueOf(directory.toAbsolutePath()));
        if (conversionThread == null) return null;
        conversionThread.run();
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
    public void returnProgressThenData(ConversionThread conversionThread, int conversionProgressInterval, SseEmitter emitter){
        Float progress = conversionThread.getConversionProgress();
        // Definindo o que retornar quando ocorre erro no emitter.
        emitter.onError((e) -> {
            System.out.println("Completed: " + e);
        });

        try {
            // Loop para enviar dados enquanto a conversão não é concluída.
            while(progress < 100f){
                // Caso a conversão tenha dado erro, seu progresso será -1.
                if (progress == -1f) {
                    // Envia o erro para o cliente.
                    String conversionError = conversionThread.getError();
                    emitter.send(SseEmitter.event()
                            .name("Error")
                            .data("Error: " + conversionError)
                    );
                    // Seta o emitter como completo.
                    emitter.complete();
                }
                else {
                    // Envia o progresso atual da conversão.
                    emitter.send(SseEmitter
                            .event()
                            .name("Progress")
                            .data(progress.intValue())
                    );
                }
                // Espera 1 segundo antes de enviar o próximo evento.
                Thread.sleep(conversionProgressInterval * 1000L);
                // Lê o progresso atual da conversão.
                progress = conversionThread.getConversionProgress();
            }

            // Envia o resultado da conversão para o cliente. O dado é enviado 3x, para o caso de o cliente não conseguir ler da primeira vez
            String result = ""; //TODO: Implement way to get result.

            //Saving on file
            File file = new File("D:/Trabalho/out.json");
            try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
                out.print(result);
            }

            /*// Envia o progresso de 100% para o cliente, pois a conversão irá sair do while quando concluida.
            emitter.send(SseEmitter.event()
                    .name("Progress")
                    .data(99)
            );
            Thread.sleep(conversionProgressInterval * 1000L);

            System.out.println("Sending Data");

            emitter.send(SseEmitter
                    .event()
                    .name("Result")
                    .data(result)
            );

            // Espera 1s antes de enviar o dado novamente.
            Thread.sleep(conversionProgressInterval * 1000L);

            emitter.send(SseEmitter
                    .event()
                    .name("RepeatedFiles")
                    .data(((NewEcadConversion)conversionThread).getArquivosRepetidos())
            );

            // Envia o progresso de 100% para o cliente, pois a conversão irá sair do while quando concluida.
            emitter.send(SseEmitter.event()
                    .name("Progress")
                    .data(progress.intValue())
            );
            Thread.sleep(conversionProgressInterval * 1000L);*/

            emitter.complete();
        } catch (Exception e){
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
    private ConversionThread getConversionThread(String type, String conversionFilesPath) throws ConversionTypeNotFound {
        try{
            String adjustedType = type.toUpperCase().replace(" ", "_");
            ConversionType documentType = ConversionType.valueOf(adjustedType);
            switch (documentType){
                case ConversionType.RELATORIO_ANALITICO:
                    return new RelatorioAnalitico(conversionFilesPath);
            }
            throw new ConversionTypeNotFound();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            throw new ConversionTypeNotFound();
        }




    }


    //endregion
}
