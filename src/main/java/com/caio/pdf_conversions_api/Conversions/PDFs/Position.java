package com.caio.pdf_conversions_api.Conversions.PDFs;

public class Position {
    private double x;
    private double w;

    public Position(double x, double w) {
        this.w = w;
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public double getW() {
        return w;
    }

}
