package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidValueException;

import java.util.Objects;

public record CustomerProfile(AccountId accountId, String fullName, Cpf cpf) {

    public CustomerProfile {
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(cpf);

        if (fullName == null)
            throw new InvalidValueException("invalid_name", "Nome inválido.");

        String normalizedName = fullName.trim();

        if (normalizedName.isBlank() || fullName.length() > 120)
            throw new InvalidValueException("invalid_name", "Nome inválido.");

        fullName = normalizedName;
    }
}
