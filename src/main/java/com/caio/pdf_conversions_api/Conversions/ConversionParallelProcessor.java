package com.caio.pdf_conversions_api.Conversions;

import com.caio.pdf_conversions_api.Exceptions.ConversionTypeNotFound;
import com.caio.pdf_conversions_api.Export.ResultData;
import com.caio.pdf_conversions_api.Export.VerificationData;
import com.caio.pdf_conversions_api.Helpers.ConversionsHelper;
import com.caio.pdf_conversions_api.Models.StartConversion;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Getter
public class ConversionParallelProcessor implements ConversionRunnable {

    private List<Future<?>> futures = new ArrayList<>();
    private List<ConversionThread> conversionsThreads = new ArrayList<>();
    private String error;
    private String xlsName;
    private StartConversion conversion;
    private ExecutorService executor;

    public ConversionParallelProcessor(StartConversion startConversion) {
        this.conversion = startConversion;
        this.xlsName = startConversion.getName();
    }

    private void startConversion() {
        try {
            // Saving the Files
            String conversionId = ConversoesAPI.Conversions.Helpers.PathsHelper.createConversionId(conversion.getName());
            Path directory = Files.createTempDirectory(conversionId);
            this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for(MultipartFile file : conversion.getFiles()) {
                String fileName = file.getOriginalFilename();
                if (fileName == null || !fileName.endsWith(".pdf")) {
                    continue;
                }
                Path filePath = directory.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                try {
                    ConversionThread conversionThread = ConversionsHelper.getConversionThread(
                            conversion.getType(),
                            String.valueOf(directory.toAbsolutePath()),
                            conversionId,
                            new String[]{ fileName }
                    );
                    Future<?> conversionExecution = this.executor.submit(conversionThread);
                    this.futures.add(conversionExecution);
                    this.conversionsThreads.add(conversionThread);
                } catch (ConversionTypeNotFound e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Float getConversionProgress() {
        int totalThreads = this.conversionsThreads.size();
        int completedThreads = 0;

        for (ConversionThread thread : this.conversionsThreads) {
            if (thread.getConversionProgress() == 100) {
                completedThreads++;
            } else if (thread.getConversionProgress() == -1) {
                this.error = thread.getError();
                this.executor.shutdownNow();
                return -1f;
            }
        }
        return (completedThreads * 100f) / totalThreads;
    }

    @Override
    public String getError() {
        return this.error;
    }

    @Override
    public String getXlsName() {
        return this.xlsName;
    }

    @Override
    public List<ResultData> getResultadosResultData() {
        return conversionsThreads.stream()
                .flatMap(c -> c.getResultadosResultData().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<VerificationData> getVerificacaoResultData() {
        return conversionsThreads.stream()
                .flatMap(c -> c.getVerificacaoResultData().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void run() {
        startConversion();
    }
}
