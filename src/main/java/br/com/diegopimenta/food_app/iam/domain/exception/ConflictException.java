package br.com.diegopimenta.food_app.iam.domain.exception;

public class ConflictException extends DomainException {

    public ConflictException(String code, String message) {
        super(code, message);
    }
}
