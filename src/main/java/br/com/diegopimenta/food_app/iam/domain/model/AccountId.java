package br.com.diegopimenta.food_app.iam.domain.model;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "AccountId não pode ser nulo.");
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }
}
