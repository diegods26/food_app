package br.com.diegopimenta.food_app.iam.application.port.in;

import br.com.diegopimenta.food_app.iam.application.command.AuthenticateCustomerCommand;
import br.com.diegopimenta.food_app.iam.application.result.AccountResult;

public interface AuthenticateCustomerUseCase {

    AccountResult execute(AuthenticateCustomerCommand command);
}
