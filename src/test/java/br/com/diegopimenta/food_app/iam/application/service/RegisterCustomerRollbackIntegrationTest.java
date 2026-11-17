package br.com.diegopimenta.food_app.iam.application.service;

import br.com.diegopimenta.food_app.iam.application.command.RegisterCustomerCommand;
import br.com.diegopimenta.food_app.iam.application.port.in.RegisterCustomerUseCase;
import br.com.diegopimenta.food_app.iam.application.port.out.CustomerProfileRepository;
import br.com.diegopimenta.food_app.iam.domain.exception.CpfAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.model.Cpf;
import br.com.diegopimenta.food_app.iam.domain.model.CustomerProfile;
import br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence.AccountJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
public class RegisterCustomerRollbackIntegrationTest {

    @Autowired
    private RegisterCustomerUseCase registerCustomer;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @MockitoBean
    private CustomerProfileRepository profileRepository;

    @Test
    void shouldRollbackAccountWhenCustomerProfileSaveFails() {

        String email = "rollback-" + UUID.randomUUID() + "@email.com";

        String cpf = "529.982.247-25";

        when(profileRepository.existsByCpf(new Cpf(cpf)))
                .thenReturn(false);

        doThrow(new CpfAlreadyInUseException())
                .when(profileRepository)
                .save(any(CustomerProfile.class));

        RegisterCustomerCommand command = new RegisterCustomerCommand(
                email,
                "Senha12345",
                "Cliente Teste",
                cpf
        );

        assertThrows(
                CpfAlreadyInUseException.class,
                () -> registerCustomer.execute(command)
        );

        assertFalse(accountJpaRepository.existsByEmail(email));
    }
}
