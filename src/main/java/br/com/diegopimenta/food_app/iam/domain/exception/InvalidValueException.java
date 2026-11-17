package br.com.diegopimenta.food_app.iam.domain.exception;

public class InvalidValueException extends DomainException{

    public InvalidValueException(String code, String message) {
        super(code, message);
    }
}
