package io.bclub.util;

import java.util.regex.Pattern;

public abstract class CPF {

    private static final Pattern PATTERN = Pattern.compile("[^0-9]");

    private CPF() { }

    public static boolean isValid(String value) {
        value = PATTERN.matcher(value).replaceAll("");

        if (value.length() != 11)
            return false;

        String numDig = value.substring(0, 9);

        return gerarDigitoVerificador(numDig).equals(value.substring(9, 11));
    }

    private static String gerarDigitoVerificador(String value){
        return obterDV(value, false, 2);
    }

    private static String obterDV(String fonte, boolean dezPorX, int quantidadeDigitos) {
        if (quantidadeDigitos > 1) {
            String parcial = obterDV(fonte, dezPorX);
            return parcial + obterDV(fonte + parcial, dezPorX, --quantidadeDigitos);
        } else {
            return obterDV(fonte, dezPorX);
        }
    }

    private static String obterDV(String fonte, boolean dezPorX) {
        //validar fonte
        int peso = fonte.length() + 1;
        int dv = 0;

        for (int i = 0; i < fonte.length(); i++) {
            dv += Integer.parseInt(fonte.substring(i, i + 1)) * peso--;
        }

        dv = dv % 11;

        if (dv > 1) {
            return String.valueOf(11 - dv);
        } else if (dv == 1 && dezPorX) {
            return "X";
        }

        return "0";
    }
}
