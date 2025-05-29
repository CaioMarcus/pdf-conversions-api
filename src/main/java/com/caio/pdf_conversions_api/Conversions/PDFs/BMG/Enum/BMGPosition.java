package com.caio.pdf_conversions_api.Conversions.PDFs.BMG.Enum;

import com.caio.pdf_conversions_api.Conversions.PDFs.Base.LinePosition;

public enum BMGPosition implements LinePosition {
    SongName(37.97, 90.00),
    Units(336.95, 37.45),
    CatalogNumber(263.59, 74.89),
    Period(221.68, 42.98),
    SourceName(154.68, 68.00),
    Country(123.23, 32.50),
    IncomeType(37.97, 87.44);

    private final Double x;
    private final Double w;

    BMGPosition(Double x, Double w) {
        this.x = x;
        this.w = w;
    }

    @Override
    public Double getX() {
        return this.x;
    }

    @Override
    public Double getW() {
        return this.w;
    }
}
