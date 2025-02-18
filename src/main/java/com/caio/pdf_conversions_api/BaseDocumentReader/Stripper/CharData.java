package com.caio.pdf_conversions_api.BaseDocumentReader.Stripper;

public class CharData {
    private String letter;

    private Float x;
    private Float y;
    private Float width;
    private Float height;

    public CharData(String letter, Float x, Float y, Float width, Float height) {
        this.letter = letter;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public String getLetter() {
        return letter;
    }

    public Float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }

    public Float getWidth() {
        return width;
    }

    public Float getHeight() {
        return height;
    }
}
