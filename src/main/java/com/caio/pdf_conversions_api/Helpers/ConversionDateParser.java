package com.caio.pdf_conversions_api.Helpers;

import org.apache.commons.lang3.tuple.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class ConversionDateParser {

    // Mapping Portuguese and English months to numeric values
    protected Map<String, String> monthMap = new HashMap<>();
    protected Map<Integer, String> quarters = new HashMap<>();

    protected Map<Pattern, Pair<Integer, Integer>> datePatterns;

    // Static initializer block to initialize the maps
    public ConversionDateParser() {
        // Portuguese months
        monthMap.put("JANEIRO", "01");
        monthMap.put("FEVEREIRO", "02");
        monthMap.put("MARÇO", "03");
        monthMap.put("MARCO", "03");
        monthMap.put("ABRIL", "04");
        monthMap.put("MAIO", "05");
        monthMap.put("JUNHO", "06");
        monthMap.put("JULHO", "07");
        monthMap.put("AGOSTO", "08");
        monthMap.put("SETEMBRO", "09");
        monthMap.put("OUTUBRO", "10");
        monthMap.put("NOVEMBRO", "11");
        monthMap.put("DEZEMBRO", "12");
        /*
        monthMap.put("Janeiro", "01");
        monthMap.put("Fevereiro", "02");
        monthMap.put("Março", "03");
        monthMap.put("Abril", "04");
        monthMap.put("Maio", "05");
        monthMap.put("Junho", "06");
        monthMap.put("Julho", "07");
        monthMap.put("Agosto", "08");
        monthMap.put("Setembro", "09");
        monthMap.put("Outubro", "10");
        monthMap.put("Novembro", "11");
        monthMap.put("Dezembro", "12");
        */
        // English months
        monthMap.put("JANUARY", "01");
        monthMap.put("FEBRUARY", "02");
        monthMap.put("MARCH", "03");
        monthMap.put("APRIL", "04");
        monthMap.put("MAY", "05");
        monthMap.put("JUNE", "06");
        monthMap.put("JULY", "07");
        monthMap.put("AUGUST", "08");
        monthMap.put("SEPTEMBER", "09");
        monthMap.put("OCTOBER", "10");
        monthMap.put("NOVEMBER", "11");
        monthMap.put("DECEMBER", "12");
        /*
        monthMap.put("January", "01");
        monthMap.put("February", "02");
        monthMap.put("March", "03");
        monthMap.put("April", "04");
        monthMap.put("May", "05");
        monthMap.put("June", "06");
        monthMap.put("July", "07");
        monthMap.put("August", "08");
        monthMap.put("September", "09");
        monthMap.put("October", "10");
        monthMap.put("November", "11");
        monthMap.put("December", "12");
        */
        // Number Months
        monthMap.put("01", "01");
        monthMap.put("02", "02");
        monthMap.put("03", "03");
        monthMap.put("04", "04");
        monthMap.put("05", "05");
        monthMap.put("06", "06");
        monthMap.put("07", "07");
        monthMap.put("08", "08");
        monthMap.put("09", "09");
        monthMap.put("10", "10");
        monthMap.put("11", "11");
        monthMap.put("12", "12");

        // Trimester Months
        monthMap.put("1º TRIMESTRE", "01");
        monthMap.put("2º TRIMESTRE", "04");
        monthMap.put("3º TRIMESTRE", "07");
        monthMap.put("4º TRIMESTRE", "10");

        // Quarters
        quarters.put(1, "01");
        quarters.put(2, "04");
        quarters.put(3, "07");
        quarters.put(4, "10");
    }

    /**
     *  Seta o map contendo, em cada chave, o padrão de data e a posição do mes/ano na linha contendo a data.
     */
    public abstract void setDatePatterns();

    /**
     * Faz o parse de uma data para um padrão. Ex: "Jan 2024 - Mar 2024" -> "01/01/24".
     * @param date A string contendo a data.
     * @return A data convertida, ou a data inserida, em caso de falhas.
     */
    protected String convertDate(String date) {
        // Forcing DatePatterns to be setted
        if (this.datePatterns == null){
            setDatePatterns();
        }
        if (date.matches("\\d{2}/\\d{2}/\\d{4}"))
            return date;
        for (Map.Entry<Pattern, Pair<Integer, Integer>> entry : this.datePatterns.entrySet()) {
            Pattern pattern = entry.getKey();
            Pair<Integer, Integer> positions = entry.getValue();
            Matcher matcher = pattern.matcher(Helper.normalizeString(date));
            if (matcher.find()) {
                String month = matcher.group(positions.getLeft());
                String year = matcher.group(positions.getRight());

                if (month.matches("\\d{2}/\\d{2}/\\d{4}"))
                    return month;

                if (month.matches("\\d{2}.\\d{2}.\\d{4}"))
                    return month.replace(".", "/");

                return String.format("01/%s/%s", monthMap.get(month.toUpperCase()), year);
            }
        }
        System.out.println("Failed to Parse the Date: " + date);
        return date;
    }


}