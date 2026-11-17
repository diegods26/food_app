package br.com.diegopimenta.food_app.iam.infrastructure.adapter.in.web;

import br.com.diegopimenta.food_app.iam.application.command.RegisterCustomerCommand;
import br.com.diegopimenta.food_app.iam.application.port.in.RegisterCustomerUseCase;
import br.com.diegopimenta.food_app.iam.application.result.AccountResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/customers")
public class CustomerAuthController {

    private final RegisterCustomerUseCase registerCustomer;

    public CustomerAuthController(RegisterCustomerUseCase registerCustomer) {
        this.registerCustomer = registerCustomer;
    }

    @PostMapping("/register")
    ResponseEntity<AccountResponse> register(
            @Valid @RequestBody RegisterCustomerRequest request) {
        AccountResult result = registerCustomer.execute(new RegisterCustomerCommand(
                request.email(),
                request.password(),
                request.fullName(),
                request.cpf()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(result));
    }

}
