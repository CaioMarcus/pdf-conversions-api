package ConversoesAPI.Conversions.Models.JsonOut;

import lombok.Data;

import java.util.List;

/**
 * O conversionJson. O modelo básico de retorno de dados de conversões.
 */
@Data
public abstract class ConversionJson {
    protected List<String[]> resultados;
    protected List<String[]> resumo;
    protected List<String[]> verificacao;
}
