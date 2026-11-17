package br.com.diegopimenta.food_app.iam.application.command;

public record RegisterCustomerCommand(String email, String password, String fullName, String cpf) {

    @Override
    public String toString() {
        return "RegisterCustomerCommand[email=" + email +
                ", fullName=" + fullName + ", password=PROTECTED]";
    }
}
