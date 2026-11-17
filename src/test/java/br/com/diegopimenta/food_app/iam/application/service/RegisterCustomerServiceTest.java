package br.com.diegopimenta.food_app.iam.application.service;

import br.com.diegopimenta.food_app.iam.application.command.RegisterCustomerCommand;
import br.com.diegopimenta.food_app.iam.application.port.out.AccountRepository;
import br.com.diegopimenta.food_app.iam.application.port.out.CustomerProfileRepository;
import br.com.diegopimenta.food_app.iam.application.port.out.PasswordHasher;
import br.com.diegopimenta.food_app.iam.application.result.AccountResult;
import br.com.diegopimenta.food_app.iam.domain.exception.EmailAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegisterCustomerServiceTest {

    @Mock
    AccountRepository accounts;

    @Mock
    CustomerProfileRepository profiles;

    @Mock
    PasswordHasher hasher;

    private RegisterCustomerService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC);
        service = new RegisterCustomerService(accounts, profiles, hasher, clock);
    }

    private static RegisterCustomerCommand validCommand() {
        return new RegisterCustomerCommand(
                "joao@email.com",
                "Senha12345",
                "João Silva",
                "529.982.247-25");
    }

    @Test
    void registersCustomerPendingVerification() {
        when(hasher.hash(any())).thenReturn(new PasswordHash("$2a$12$hash"));

        AccountResult result = service.execute(validCommand());

        assertEquals(AccountStatus.PENDING_VERIFICATION, result.status());
        assertEquals("joao@email.com", result.email());
        verify(accounts).save(any(Account.class));
        verify(profiles).save(any(CustomerProfile.class));
    }

    @Test
    void rejectsDuplicateEmailWithoutHashingPassword() {
        when(accounts.existsByEmail(new Email("joao@email.com"))).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> service.execute(validCommand()));

        verifyNoInteractions(hasher);
        verify(accounts, never()).save(any());
    }
}
