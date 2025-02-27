package com.caio.pdf_conversions_api.Services;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public String exportAndUploadData(List<Object[]> resultados, List<Object[]> verificacao, String fileName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try (PipedOutputStream out = new PipedOutputStream();
             PipedInputStream in = new PipedInputStream(out);
             WritableByteChannel channel = storage.writer(BlobInfo.newBuilder(BUCKET_NAME, fileName + ".xlsx")
                     .setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                     .build())) {

            // Write Excel in a separate thread to avoid blocking
            executor.submit(() -> {
                try (SXSSFWorkbook documento = new SXSSFWorkbook(100)) { // Keep 100 rows in memory
                    criaPlanilhaEAdicionaDados(documento, resultados, verificacao);
                    documento.write(out);
                    documento.close();
                    out.close();
                } catch (IOException | ParseException e) {
                    throw new RuntimeException("Error while exporting data", e);
                }
            });

            // Stream the InputStream to GCS
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                channel.write(ByteBuffer.wrap(buffer, 0, bytesRead));
            }
            return generateSignedUrl(fileName + ".xlsx");
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
