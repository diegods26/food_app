package br.com.diegopimenta.food_app.iam.domain.model;

public record PasswordHash(String value) {

    public PasswordHash {
        if (value == null || value.isBlank())
            throw new IllegalStateException("Password hash não pode ser vazio.");
    }

    @Override
    public String toString() {
        return "PasswordHash[PROTECTED]";
    }
}
