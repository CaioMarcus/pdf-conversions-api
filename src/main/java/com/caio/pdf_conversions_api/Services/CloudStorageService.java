package com.caio.pdf_conversions_api.Services;

import com.caio.pdf_conversions_api.Export.CsvExportable;
import com.caio.pdf_conversions_api.Export.CsvExporter;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.caio.pdf_conversions_api.Helpers.ExportHelper.criaPlanilhaEAdicionaDados;

@Service
public class CloudStorageService {
    private final String BUCKET_NAME;
    private final Storage storage;

    public CloudStorageService(
            @Value("${app.bucket_to_save_files}") String BUCKET_NAME
    ) {
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

            // Gera e escreve o CSV em uma thread separada
            executor.submit(() -> {
                try {
                    CsvExporter.exportToCsv(csvFileName, data); // Cria o CSV temporariamente

                    // Lê o CSV gerado e o envia para o OutputStream
                    try (FileInputStream fileInputStream = new FileInputStream(csvFileName)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error while exporting data", e);
                }
            });

            // Transmite o InputStream para o Google Cloud Storage
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                channel.write(ByteBuffer.wrap(buffer, 0, bytesRead));
            }

            return generateSignedUrl(csvFileName);
        } catch (IOException e) {
            throw new RuntimeException("Error while exporting and uploading data", e);
        } finally {
            executor.shutdown();
        }
    }

    private String generateSignedUrl(String fileName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, fileName).build();
        return storage.signUrl(blobInfo, 12, java.util.concurrent.TimeUnit.HOURS).toString();
    }
}
