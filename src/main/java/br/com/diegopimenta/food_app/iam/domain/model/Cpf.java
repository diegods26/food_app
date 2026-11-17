package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidValueException;

public record Cpf(String value) {

    public Cpf {
        if (value == null)
            throw new InvalidValueException("invalid_cpf", "CPF inválido");

        String digits = value.replaceAll("\\D", "");

        if (!isValid(digits))
            throw new InvalidValueException("invalid_cpf", "CPF inválido");

        value = digits;
    }

    private static boolean isValid(String d) {
        if (d.length() != 11 || d.chars().distinct().count() == 1)
            return false;

        return checkDigit(d, 9) == d.charAt(9) - '0'
                && checkDigit(d, 10) == d.charAt(10) - '0';
    }

    private static int checkDigit(String d, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (d.charAt(i) - '0') * (length + 1 - i);
        }

        int mod = (sum * 10) % 11;
        return mod == 10 ? 0 : mod;
    }
}
