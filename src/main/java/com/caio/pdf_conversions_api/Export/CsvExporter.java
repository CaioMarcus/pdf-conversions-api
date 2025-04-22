package com.caio.pdf_conversions_api.Export;

import java.io.*;
import java.util.List;

public class CsvExporter {


    private static final String verificationDataIndexLine =
            "status, " +
            "informed_total, " +
            "summed_total, " +
            "difference, " +
            "document_date, " +
            "document";

    public static void exportToCsv(String fileName, List<? extends CsvExportable> objects) {
        try (FileWriter writer = new FileWriter(fileName)) {
            if (!objects.isEmpty()) {
                if (objects.getFirst() instanceof ResultData) {
                    writer.write(resultDataIndexLine + "\n");
                } else if (objects.getFirst() instanceof VerificationData) {
                    writer.write(verificationDataIndexLine + "\n");
                }
                for (CsvExportable csvExportable : objects) {
                    writer.write(csvExportable.getCsvLine() + "\n");
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportToCsv(OutputStream outputStream, List<? extends CsvExportable> objects) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            if (!objects.isEmpty()) {
                if (objects.getFirst() instanceof ResultData) {
                    writer.write(resultDataIndexLine);
                    writer.newLine();
                } else if (objects.getFirst() instanceof VerificationData) {
                    writer.write(verificationDataIndexLine);
                    writer.newLine();
                }
                for (CsvExportable csvExportable : objects) {
                    writer.write(csvExportable.getCsvLine());
                    writer.newLine();
                }
            }
            writer.flush(); // Ensure all data is written before closing
        } catch (IOException e) {
            throw new RuntimeException("Error while exporting CSV", e);
        }
    }
}