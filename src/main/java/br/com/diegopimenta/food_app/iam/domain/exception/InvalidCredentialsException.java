package br.com.diegopimenta.food_app.iam.domain.exception;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("invalid_credentials", "E-mail ou senha inválidos");
    }
}
