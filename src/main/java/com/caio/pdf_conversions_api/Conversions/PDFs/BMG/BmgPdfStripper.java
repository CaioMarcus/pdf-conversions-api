package com.caio.pdf_conversions_api.Conversions.PDFs.BMG;

import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.CharData;
import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.LineData;
import com.caio.pdf_conversions_api.BaseDocumentReader.Stripper.PDFAreaStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;

public class BmgPdfStripper extends PDFAreaStripper {

    public BmgPdfStripper() {
    }

    public BmgPdfStripper(Rectangle2D.Double limitRegion) {
        super(limitRegion);
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        // Get Y position of the current text segment
        float currentY = textPositions.get(0).getYDirAdj();

        // Start a new line if currentLineData is null
        if (currentLineData == null) {
            currentLineData = new LineData();
        }

        // Process each segment in textPositions
        for (TextPosition text : textPositions) {
            float x = text.getXDirAdj();
            float y = text.getYDirAdj();
            float w = text.getWidthDirAdj();
            float h = text.getHeightDir();

            currentX = Float.min(currentX, x);
            currentY = Float.min(currentY, y);
            currentWidth = Float.max(currentWidth, w);
            currentHeight = Float.max(currentHeight, h);

            if (this.limitRegion != null && !isPositionInsideLimitRegion(x,y,w,h)){
                continue;
            }

            currentCharData = new CharData(text.getUnicode(), text.getXDirAdj(), text.getYDirAdj(), text.getWidthDirAdj(), text.getHeightDir());
            if (currentCharData.getLetter().equals(" "))
                currentCharData.setLetter(" ");

            this.currentLineData.addSegment(currentCharData);
        }
        if (currentCharData != null)
            currentCharData.setLetter(currentCharData.getLetter() + "    ");
    }
}
