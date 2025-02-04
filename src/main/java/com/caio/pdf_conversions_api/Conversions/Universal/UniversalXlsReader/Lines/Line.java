package com.caio.pdf_conversions_api.Universal.UniversalXlsReader.Lines;

import com.google.gson.annotations.SerializedName;

public class Line {
    @SerializedName(value = "REF")
    private String ref;
    @SerializedName(value = "Tipo")
    private String type;
    @SerializedName(value = "Formato")
    private String format;
    @SerializedName(value = "Album")
    private String album;
    @SerializedName(value = "Nome da Obra")
    private String songName;
    @SerializedName(value = "Preço")
    private double price;
    @SerializedName(value = "Quantidade")
    private int amount;
    @SerializedName(value = "% Royalty")
    private double royaltyPercentage;
    @SerializedName(value = "Valor Devido")
    private double dueAmount;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getRoyaltyPercentage() {
        return royaltyPercentage;
    }

    public void setRoyaltyPercentage(double royaltyPercentage) {
        this.royaltyPercentage = royaltyPercentage;
    }

    public double getDueAmount() {
        return dueAmount;
    }

    public void setDueAmount(double dueAmount) {
        this.dueAmount = dueAmount;
    }
}
