package com.caio.pdf_conversions_api.Conversions.XLS.Sony;

import com.caio.pdf_conversions_api.Conversions.XLS.FieldValue;

public enum SonyMusicPublishingFields implements FieldValue {
    PAIS_LINE("PaisLine"),
    FONTE_PAGADORA_LINE("FontePagadoraLine"),
    MOVIMENTACAO_LINE("MovimentacaoLine"),
    TIPO_LINE("TipoLine"),
    TIPO_WITH_GRAVADORA_LINE("TipoWithGravadoraLine"),
    TITULO_OBRA_LINE("TituloObraLine"),
    TITULO_WITH_ARTIST_LINE("TituloWithArtistLine"),
    REFERENCIA_AUTORAL_LINE("ReferenciaAutoralLine"),
    REFERENCIA_AUTORAL_COM_PERIODO_SHOW("ReferenciaAutoralComPeriodoShowLine"),
    QTD_LINE("QtdLine"),
    QTD_LINE_COM_SHOW("QtdLineComShow"),
    INTERPRETE_LINE("InterpreteLine"),
    INTERPRETE_LINE_COM_SHOW("InterpreteLineComShow"),
    NOME_SHOW_LINE("NomeShowLine"),
    VLR_RENDIMENTO_LINE("VlrRendimentoLine"),
    ROYALTY_LINE("RoyaltyLine"),
    VLR_LIQUIDO_LINE("VlrLiquidoLine");

    private String value;

    SonyMusicPublishingFields(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
