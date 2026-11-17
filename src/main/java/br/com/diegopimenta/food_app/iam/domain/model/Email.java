package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidValueException;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern FORMAT =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final int MAX_LENGTH = 120;

    public Email {
        if (value == null)
            throw new InvalidValueException("invalid_email", "E-mail inválido.");

        value = value.trim().toLowerCase(Locale.ROOT);

        if (value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches())
            throw new InvalidValueException("invalid_email", "E-mail inválido.");
    }
}
