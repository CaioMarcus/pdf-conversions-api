package com.caio.pdf_conversions_api.Conversions.PDFs.OutrasEditoras.Models;

import com.caio.pdf_conversions_api.Conversions.PDFs.Position;
import lombok.Getter;

import java.awt.geom.Rectangle2D;
import java.util.Map;
@Getter
public class OutrasEditorasDocumento {
    private String tipoDoc = "";
    private String indexLine = "";
    private Map<OutrasEditorasColumn, Position> columns;

    public OutrasEditorasDocumento(Map<OutrasEditorasColumn, Position> columns) {
        this.columns = columns;
    }

    public OutrasEditorasDocumento(Map<OutrasEditorasColumn, Position> columns, String indexLine) {
        this.columns = columns;
        this.indexLine = indexLine;
    }

    public Map<OutrasEditorasColumn, Position> getColumns() {
        return this.columns;
    }

    public Position getColumnPosition(OutrasEditorasColumn column) {
        return this.columns.get(column);
    }
}
