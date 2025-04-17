package com.caio.pdf_conversions_api.Services;

import com.caio.pdf_conversions_api.Export.CsvExportable;
import com.caio.pdf_conversions_api.Export.CsvExporter;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CloudStorageService {
    private final String BUCKET_NAME;
    private final Storage storage;

    public CloudStorageService(@Value("${app.bucket_to_save_files}") String BUCKET_NAME) {
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.BUCKET_NAME = BUCKET_NAME;
    }

    public String exportAndUploadData(List<? extends CsvExportable> data, String fileName) {
        String csvFileName = fileName + ".csv";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            System.out.println("Iniciando a exportação do CSV...");
            CsvExporter.exportToCsv(baos, data); // Escreve CSV direto no buffer
            System.out.println("CSV exportado para memória.");

            // Escreve o conteúdo na GCS
            try (WritableByteChannel channel = storage.writer(
                    BlobInfo.newBuilder(BUCKET_NAME, csvFileName)
                            .setContentType("text/csv")
                            .build())) {
                channel.write(ByteBuffer.wrap(baos.toByteArray()));
            }

            System.out.println("Upload concluído para: " + csvFileName);
            return generateSignedUrl(csvFileName);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao exportar e enviar arquivo", e);
        }
    }

    private String generateSignedUrl(String fileName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, fileName).build();
        return storage.signUrl(blobInfo, 12, java.util.concurrent.TimeUnit.HOURS).toString();
    }
}
