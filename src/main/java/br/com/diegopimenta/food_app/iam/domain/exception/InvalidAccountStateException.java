package br.com.diegopimenta.food_app.iam.domain.exception;

public class InvalidAccountStateException extends DomainException {

    public InvalidAccountStateException(String message) {
        super("invalid_account_state", message);
    }
}
