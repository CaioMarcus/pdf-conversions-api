package com.caio.pdf_conversions_api.BaseDocumentReader;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StripperRegion {
    private PDFTextStripperByArea stripper;
    private Map<String, Rectangle2D.Double> regions;
    private PDPage lastPage;

    public StripperRegion(Map<String, Rectangle2D.Double> regions, boolean sortByPosition) throws IOException {
        this.stripper = new PDFTextStripperByArea();
        this.regions = regions;

        this.stripper.setSortByPosition(sortByPosition);

        // Adding regionons to stripper
        for (Map.Entry<String, Rectangle2D.Double> entry : regions.entrySet()) {
            stripper.addRegion(entry.getKey(), entry.getValue());
        }
    }


    public Map<String, String[]> getRegionsContent(PDPage page) throws IOException {
        stripper.extractRegions(page);

        Map<String, String[]> result = new HashMap<>();

        for(Map.Entry<String, Rectangle2D.Double> entry : regions.entrySet()){
            result.put(entry.getKey(), stripper.getTextForRegion(entry.getKey()).split(System.lineSeparator()));
        }

        return result;
    }


}
