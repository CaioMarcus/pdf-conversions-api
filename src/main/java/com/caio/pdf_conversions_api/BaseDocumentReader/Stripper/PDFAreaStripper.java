package com.caio.pdf_conversions_api.BaseDocumentReader.Stripper;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PDFAreaStripper extends PDFTextStripper {

    private List<LineData> lines = new ArrayList<>();
    protected LineData currentLineData = null;
    private Map.Entry<String, Float[]> lastSegment;
    protected CharData currentCharData;

    private final float SENSIBILITY;
    protected float currentX = 0;
    private float currentY = 0;
    protected float currentWidth = 0;
    protected float currentHeight = 0;

    protected Rectangle2D.Double limitRegion;

    public PDFAreaStripper(float sensibility){
        super();
        resetPositions();
        this.SENSIBILITY = sensibility;
    }

    public PDFAreaStripper(){
        super();
        resetPositions();
        this.SENSIBILITY = 5f;
    }

    public PDFAreaStripper(Rectangle2D.Double limitRegion){
        super();
        resetPositions();
        this.SENSIBILITY = 5f;
        this.limitRegion = limitRegion;
    }

    public PDFAreaStripper(float sensibility, Rectangle2D.Double limitRegion){
        super();
        resetPositions();
        this.SENSIBILITY = sensibility;
        this.limitRegion = limitRegion;
    }

    @Override
    protected void writeLineSeparator() throws IOException {
        super.writeLineSeparator();

        if (!this.currentLineData.getLineContent().isEmpty())
            lines.add(currentLineData);
        currentLineData = null;
        lastSegment = null;

        resetPositions();
    }

    @Override
    public String getArticleEnd() {

        if (!this.currentLineData.getLineContent().isEmpty() && !this.lines.contains(currentLineData)) {
            lines.add(currentLineData);
        }

        currentLineData = null;
        lastSegment = null;

        resetPositions();

        return super.getArticleEnd();
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
            this.currentLineData.addSegment(currentCharData);
        }
        if (currentCharData != null)
            currentCharData.setLetter(currentCharData.getLetter() + "    ");
    }

    protected boolean isPositionInsideLimitRegion(float x, float y, float w, float h){
        return x > this.limitRegion.getX() &&
                y > this.limitRegion.getY() &&
                x + w < this.limitRegion.getX() + this.limitRegion.getWidth() &&
                y + h < this.limitRegion.getY() + this.limitRegion.getHeight();
    }

    private void resetPositions(){
        currentX = Float.MAX_VALUE;
        currentY = Float.MAX_VALUE;
        currentWidth = 0f;
        currentHeight = 0f;
    }

    public List<LineData> getLines(PDDocument document) {
        this.lines.clear();
        try {
            this.getText(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines.stream()
                .sorted(Comparator.comparingDouble(LineData::getStartY))
                .collect(Collectors.toList());
    }



    public List<LineData> getLines(PDDocument document, Rectangle2D.Double region) {
        if(!this.lines.contains(this.currentLineData))
            this.lines.add(this.currentLineData);

        return this.lines
                .stream()
                .filter(line ->
                        line.getStartX() >= region.getX() &&
                        line.getStartY() >= region.getY() &&
                        line.getEndX() <= region.getX() + region.getWidth() &&
                        line.getEndY() <= region.getY() + region.getHeight()
                )
                .collect(Collectors.toList());
    }
}
