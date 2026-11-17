package br.com.diegopimenta.food_app.iam.domain.exception;

public class CpfAlreadyInUseException extends ConflictException {

    public CpfAlreadyInUseException() {
        super("cpf_already_in_use", "Não foi possível concluir o cadastro com os dados informados.");
    }
}
