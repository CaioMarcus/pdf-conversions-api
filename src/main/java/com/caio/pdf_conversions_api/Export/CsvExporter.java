package com.caio.pdf_conversions_api.Export;

import java.io.*;
import java.util.List;

public class CsvExporter {

    public static void exportToCsv(String fileName, List<? extends CsvExportable> objects) {
        try (FileWriter writer = new FileWriter(fileName)) {
            exportData(objects, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportToCsv(OutputStream outputStream, List<? extends CsvExportable> objects) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            exportData(objects, writer);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error while exporting CSV", e);
        }
    }

    private static void exportData(List<? extends CsvExportable> objects, Writer writer) throws IOException {
        if (!objects.isEmpty()) {
            writer.write(objects.getFirst().getIndexLine());
            for (CsvExportable csvExportable : objects) {
                writer.write(csvExportable.getCsvLine() + "\n");
            }
        }
    }
}