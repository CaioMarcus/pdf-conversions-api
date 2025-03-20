package com.caio.pdf_conversions_api.Helpers;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mariuszgromada.math.mxparser.Expression;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Helper {
    /**
     * Normaliza uma string, removendo caracteres especiais, como ç, ã, á, etc.
     * @param input A string a ser normalizada.
     * @return A string normalizada.
     */
    public static String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    public static double mmParaPx(double mm){
        return mm * 72 / 25.4;
    }

    public static String achaTermoEmComum(String linha, String[] vetorTermos, boolean removerTermoDaLista){
        int i = 0;
        String dado = "";
        for (; i < vetorTermos.length + 1; i++){
            if (i == vetorTermos.length)
                return "";
            dado = vetorTermos[i];
            if (dado == null || dado.equals(" "))
                continue;
            if (linha.contains(dado))
                break;
        }
        if (removerTermoDaLista)
            vetorTermos[i] = null;

        return dado;
    }

    public static String achaTermoEmComum(String linha, List<String> vetorTermos, boolean removerTermoDaLista){
        int i = 0;
        String dado = "";
        for (; i < vetorTermos.size() + 1; i++){
            if (i == vetorTermos.size())
                return "";
            dado = vetorTermos.get(i);
            if (dado == null || dado.equals(" "))
                continue;
            if (linha.contains(dado))
                break;
        }
        if (removerTermoDaLista)
            vetorTermos.set(i, null);

        return dado;
    }

    public static String corrigeSeparadorDouble(String number){
        if (Pattern.compile("\\s+").matcher(number).find()){
            Pattern pattern = Pattern.compile("(-?[0-9,]+[.]-?[0-9]+)");
            Matcher matcher = pattern.matcher(number);
            if (matcher.find()){
                number = matcher.group();
            }
            else {
                Pattern patternComma = Pattern.compile("(-?[0-9.]+,-?[0-9]+)");
                Matcher matcherComma = patternComma.matcher(number);
                if (matcherComma.find())
                    number = matcherComma.group();
            }
        }
        return number.replaceAll("[.,](?=.*[.,])", "").replace(",", ".");

    }

    public static String corrigeSeparadorInt(String number){
        number = number.replaceAll("\\.", "");
        try {
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + number);
        }
    }

    public static void exportaDados(String diretorioSaida, String nomeSaida, XSSFWorkbook documentoPlanilha) throws IOException {
        FileOutputStream out = new FileOutputStream(diretorioSaida + nomeSaida + ".xlsx");
        documentoPlanilha.write(out);
        out.close();
        System.out.println("Conversão concluída com êxito. Nome do arquivo salvo: " + nomeSaida + ".xlsx");
    }

    public static double ajustaNumero(String numero) {
        try {
            return Double.parseDouble(corrigeSeparadorDouble(numero));
        } catch (NumberFormatException e){
            Expression expressao = new Expression(numero);
            return (expressao.calculate() * 100);
        }
    }

    public static boolean isNumeroComVirgula(String palavra){
        return palavra.matches("^-?[0-9.]+,-?[0-9]+$");
    }

    public static boolean isNumeroComPonto(String palavra){
        return palavra.matches("^-?[0-9,]+.-?[0-9]+$");
    }

    public static boolean verificaRateioObra(String valor){
        String regexVirgula = "^-*(\\d{1,3}\\.*)*,\\d{2}$";
        String regexPonto = "^-*(\\d{1,3},*)*\\.\\d{2}$";
        return valor.matches(regexVirgula) || valor.matches(regexPonto);
    }

    public static boolean verificaData(String date) {
        String dateFormat = "dd/MM/yyyy";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        try {
            LocalDate.parse(date, formatter);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    public static Double converteDeNotacao(String numero) {
        String sinal = "";

        if (numero.contains("+")) {
            numero = numero.replace("+", "");
            sinal = "+";
        } else if (numero.contains("-")) {
            numero = numero.replace("-", "");
            sinal = "-";
        }
        String[] numeroSep = numero.split("E");
        double digitos = Double.parseDouble(numeroSep[0]);
        int fator = Integer.parseInt(numeroSep[1]);
        double numeroCompleto = digitos * Math.pow(10, fator);
        if (sinal.equals("-")) {
            numeroCompleto *= -1;
        }
        return numeroCompleto;
    }

    public static boolean isClose(double mainValue, double toCompare, double percentage) {
        double allowedDifference = mainValue * (percentage / 100);
        return Math.abs(mainValue - toCompare) <= allowedDifference;
    }
}
