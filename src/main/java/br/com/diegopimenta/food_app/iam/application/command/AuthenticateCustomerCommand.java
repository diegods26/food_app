package br.com.diegopimenta.food_app.iam.application.command;

public record AuthenticateCustomerCommand(String email, String password) {
}
