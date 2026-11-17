package br.com.diegopimenta.food_app.iam.domain.exception;

public class EmailAlreadyInUseException extends ConflictException {

    public EmailAlreadyInUseException() {
        super("email_already_in_use", "Não foi possível concluir o cadastro com os dados informados.");
    }
}
