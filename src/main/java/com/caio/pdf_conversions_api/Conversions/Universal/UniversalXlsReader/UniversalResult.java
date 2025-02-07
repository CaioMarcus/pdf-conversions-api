package com.caio.pdf_conversions_api.Conversions.Universal.UniversalXlsReader;


import com.caio.pdf_conversions_api.Conversions.Universal.UniversalXlsReader.Lines.Line;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UniversalResult {
    private String ref;
    private List<Line> lines;
    @SerializedName(value = "Valor Total")
    private double totalValue;
    @SerializedName(value = "Valor Somado")
    private double sumValue;
    @SerializedName(value = "Status da Conversão")
    private String valueStatus;
    @SerializedName(value = "Relatório de Pagamentos")
    private String tipo;
    @SerializedName(value = "Beneficiario")
    private String beneficiario;
    @SerializedName(value = "Data")
    private String date;

    public UniversalResult() {
        this.lines = new ArrayList<>();
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public Double getSumValue() {
        return sumValue;
    }

    public void addLine(Line line){
        this.lines.add(line);
//        this.ref = line.getRef();
        this.sumValue += line.getDueAmount();
    }

    public void setValueStatus(String valueStatus) {
        this.valueStatus = valueStatus;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getBeneficiario() {
        return beneficiario;
    }

    public void setBeneficiario(String beneficiario) {
        this.beneficiario = beneficiario;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
