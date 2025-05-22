package com.caio.pdf_conversions_api.BaseDocumentReader.Stripper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LineData {
    private final List<CharData> lineContent = new ArrayList<CharData>();
    private String fullLine;
    private String[] lineSeparated;
    private double startX = Double.MAX_VALUE;
    private double startY = Double.MAX_VALUE;
    private double endX = 0D;
    private double endY = 0D;

    public void addSegment(CharData segmentContent) {
        this.lineContent.add(segmentContent);

        if (segmentContent.getX() < startX)
            startX = segmentContent.getX();
        if (segmentContent.getY() < startY)
            startY = segmentContent.getY();
        if (segmentContent.getX() + segmentContent.getWidth() > endX)
            endX = segmentContent.getX() + segmentContent.getWidth();
        if (segmentContent.getY() + segmentContent.getHeight() > endY)
            endY = segmentContent.getY() + segmentContent.getHeight();
    }

    public String getFullLine(){
        if (this.fullLine != null)
            return this.fullLine;

        StringBuilder string = new StringBuilder();

        for (CharData charData : this.lineContent) {
            string.append(charData.getLetter());
        }
        this.fullLine = string.toString().replaceAll("\\s{2,}", " ").trim();
        return fullLine;
    }

    public String[] getLineSeparated(int sensibility){
        if (this.lineSeparated != null)
            return this.lineSeparated;

        if (lineContent.isEmpty())
            return new String[]{};

        Float lastCharX = lineContent.get(0).getX();
        StringBuilder stringBuilder = new StringBuilder();
        for (CharData charData : lineContent) {
            if (charData.getX() - lastCharX > sensibility) {
                stringBuilder.append("    ");
            }
            if (charData.getLetter().isEmpty()) continue;
            stringBuilder.append(charData.getLetter());
            lastCharX = charData.getX();
        }
        this.lineSeparated = stringBuilder.toString().split("\\s{2,}");
        return this.lineSeparated;
    }

    public String[] getLineSeparated(){
        return this.getLineSeparated(10);
    }

    public String getStringFromPosition(double x, double width) {
        StringBuilder stringBuilder = new StringBuilder();

        this.lineContent.stream()
                .filter(line -> line.getX() >= x && line.getX() <= x + width)
                .forEach(charData -> stringBuilder.append(charData.getLetter()));

        return stringBuilder.toString();
    }

    public List<CharData> getLineContent() {
        return lineContent;
    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public double getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }
}
