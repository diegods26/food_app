package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidValueException;

import java.nio.charset.StandardCharsets;

public class RawPassword {

    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 72;

    private final String value;

    public RawPassword(String value) {
        if (value == null || value.length() < MIN_LENGTH)
            throw new InvalidValueException("weak_password",
                "Senha deve ter no mínimo " + MIN_LENGTH + " caracteres.");

        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_LENGTH)
            throw new InvalidValueException("weak_password",
                "Senha deve ter no máximo " + MAX_LENGTH + " bytes.");

        boolean hasLetter = value.chars().anyMatch(Character::isLetter);
        boolean hasDigit = value.chars().anyMatch(Character::isDigit);

        if (!hasLetter || !hasDigit)
            throw new InvalidValueException("weak_password",
                "Senha deve conter pelo menos uma letra e um número.");

        this.value = value;
    }

    public String reveal() {
        return value;
    }

    @Override
    public String toString() {
        return "RawPassword[PROTECTED]";
    }
}
