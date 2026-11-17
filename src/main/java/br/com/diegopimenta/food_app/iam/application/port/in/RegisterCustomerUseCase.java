package br.com.diegopimenta.food_app.iam.application.port.in;

import br.com.diegopimenta.food_app.iam.application.command.RegisterCustomerCommand;
import br.com.diegopimenta.food_app.iam.application.result.AccountResult;

public interface RegisterCustomerUseCase {

    AccountResult execute(RegisterCustomerCommand command);
}
