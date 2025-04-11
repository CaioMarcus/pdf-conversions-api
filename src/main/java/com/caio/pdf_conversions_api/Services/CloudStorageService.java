package com.caio.pdf_conversions_api.Services;

import com.caio.pdf_conversions_api.Export.CsvExportable;
import com.caio.pdf_conversions_api.Export.CsvExporter;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String csvFileName = fileName + ".csv";

        try (PipedOutputStream out = new PipedOutputStream();
             PipedInputStream in = new PipedInputStream(out);
             WritableByteChannel channel = storage.writer(BlobInfo.newBuilder(BUCKET_NAME, csvFileName)
                     .setContentType("text/csv")
                     .build())) {

            // Gera e transmite o CSV em uma thread separada
            executor.submit(() -> {
                try {
                    System.out.println("Iniciando a exportação do CSV...");
                    CsvExporter.exportToCsv(out, data); // Agora escreve diretamente no OutputStream
                    out.close(); // Fecha a stream para indicar fim dos dados
                    System.out.println("CSV exportado e stream fechada.");
                } catch (IOException e) {
                    throw new RuntimeException("Erro ao exportar CSV", e);
                }
            });

            // Transmite os dados do InputStream para o Google Cloud Storage
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                channel.write(ByteBuffer.wrap(buffer, 0, bytesRead));
            }

            System.out.println("Upload concluído para: " + csvFileName);
            return generateSignedUrl(csvFileName);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao exportar e enviar arquivo", e);
        } finally {
            executor.shutdown();
        }
    }

    private String generateSignedUrl(String fileName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, fileName).build();
        return storage.signUrl(blobInfo, 12, java.util.concurrent.TimeUnit.HOURS).toString();
    }
}
