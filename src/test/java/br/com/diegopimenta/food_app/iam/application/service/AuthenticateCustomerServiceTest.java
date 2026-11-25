package br.com.diegopimenta.food_app.iam.application.service;

import br.com.diegopimenta.food_app.iam.application.command.AuthenticateCustomerCommand;
import br.com.diegopimenta.food_app.iam.application.port.out.AccountRepository;
import br.com.diegopimenta.food_app.iam.application.port.out.PasswordHasher;
import br.com.diegopimenta.food_app.iam.domain.exception.InvalidCredentialsException;
import br.com.diegopimenta.food_app.iam.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticateCustomerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private AuthenticateCustomerService service;
    private PasswordHash dummyPasswordHash;

    private final Email email = new Email("customer@email.com");
    private final RawPassword rawPassword = new RawPassword("Password123");

    @BeforeEach
    void setUp() {
        dummyPasswordHash = new PasswordHash("dummy-test-hash");

        service = new AuthenticateCustomerService(
                accountRepository,
                passwordHasher,
                dummyPasswordHash
        );
    }

    @Test
    void shouldAuthenticateActiveCustomer() {
        Account account = createActiveAccount();

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(account));

        when(passwordHasher.matches(any(RawPassword.class), any(PasswordHash.class)))
                .thenReturn(true);

        when(accountRepository.resetLoginFailures(
                eq(email),
                any(Instant.class)
        )).thenReturn(true);

        var result = service.execute(
                new AuthenticateCustomerCommand(
                        email.value(),
                        rawPassword.reveal()
                )
        );

        assertNotNull(result);
        assertEquals(account.id().value(), result.accountId());
        assertEquals(email.value(), result.email());
        assertEquals(AccountStatus.ACTIVE, result.status());

        verify(accountRepository).findByEmail(email);
        verify(passwordHasher).matches(any(RawPassword.class), any(PasswordHash.class));
    }

    @Test
    void shouldRejectWhenAccountDoesNotExist() {
        when(accountRepository.findByEmail(any(Email.class)))
                .thenReturn(Optional.empty());

        when(passwordHasher.matches(
                any(RawPassword.class),
                any(PasswordHash.class)
        )).thenReturn(false);

        AuthenticateCustomerCommand command =
                new AuthenticateCustomerCommand(
                        "unknown@email.com",
                        "ValidPassword123!"
                );

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.execute(command)
        );

        verify(passwordHasher, times(1)).matches(
                any(RawPassword.class),
                eq(dummyPasswordHash)
        );
    }

    @Test
    void shouldRejectWhenPasswordDoesNotMatch() {
        Account account = createActiveAccount();

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(account));

        when(passwordHasher.matches(any(RawPassword.class), any(PasswordHash.class)))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.execute(
                        new AuthenticateCustomerCommand(
                                email.value(),
                                rawPassword.reveal()
                        )
                )
        );
    }

    @Test
    void shouldRejectWhenAccountIsNotActive() {
        Account account = Account.registerCustomer(
                email,
                new PasswordHash("hashed-password"),
                Instant.now()
        );

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(account));

        when(passwordHasher.matches(any(RawPassword.class), any(PasswordHash.class)))
                .thenReturn(true);

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.execute(
                        new AuthenticateCustomerCommand(
                                email.value(),
                                rawPassword.reveal()
                        )
                )
        );
    }

    @Test
    void shouldRecordFailedLoginAttemptWhenPasswordDoesNotMatch() {
        Account account = createActiveAccount();

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(account));

        when(passwordHasher.matches(any(RawPassword.class), any(PasswordHash.class)))
                .thenReturn(false);

        ArgumentCaptor<Instant> attemptAtCaptor =
                ArgumentCaptor.forClass(Instant.class);

        ArgumentCaptor<Instant> lockUntilCaptor =
                ArgumentCaptor.forClass(Instant.class);

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.execute(
                        new AuthenticateCustomerCommand(
                                email.value(),
                                rawPassword.reveal()
                        )
                )
        );

        verify(accountRepository).recordFailedLoginAttempt(
                eq(email),
                eq(5),
                attemptAtCaptor.capture(),
                lockUntilCaptor.capture()
        );

        Instant attemptAt = attemptAtCaptor.getValue();
        Instant lockUntil = lockUntilCaptor.getValue();

        assertEquals(
                Duration.ofMinutes(15),
                Duration.between(attemptAt, lockUntil)
        );

        verify(accountRepository, never())
                .resetLoginFailures(any(Email.class), any(Instant.class));
    }

    @Test
    void shouldRejectWhenAccountIsLocked() {
        Instant now = Instant.now();

        Account account = createAccountWithLoginProtectionState(
                5,
                now.plus(Duration.ofMinutes(15))
        );

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(account));

        when(passwordHasher.matches(any(RawPassword.class), any(PasswordHash.class)))
                .thenReturn(true);

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.execute(
                        new AuthenticateCustomerCommand(
                                email.value(),
                                rawPassword.reveal()
                        )
                )
        );

        verify(accountRepository, never())
                .resetLoginFailures(any(Email.class), any(Instant.class));

        verify(accountRepository, never())
                .recordFailedLoginAttempt(
                        any(Email.class),
                        anyInt(),
                        any(Instant.class),
                        any(Instant.class)
                );
    }

    @Test
    void shouldAuthenticateWhenAccountLockHasExpired() {
        Instant now = Instant.now();

        Account account = createAccountWithLoginProtectionState(
                5,
                now.minus(Duration.ofSeconds(1))
        );

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(account));

        when(passwordHasher.matches(any(RawPassword.class), any(PasswordHash.class)))
                .thenReturn(true);

        when(accountRepository.resetLoginFailures(
                eq(email),
                any(Instant.class)
        )).thenReturn(true);

        var result = service.execute(
                new AuthenticateCustomerCommand(
                        email.value(),
                        rawPassword.reveal()
                )
        );

        assertNotNull(result);
        assertEquals(account.id().value(), result.accountId());

        verify(accountRepository).resetLoginFailures(
                eq(email),
                any(Instant.class)
        );
    }

    @Test
    void shouldRejectAuthenticationWhenLoginFailuresCannotBeReset() {
        Account account = createActiveAccount();

        when(accountRepository.findByEmail(email))
                .thenReturn(Optional.of(account));

        when(passwordHasher.matches(any(RawPassword.class), any(PasswordHash.class)))
                .thenReturn(true);

        when(accountRepository.resetLoginFailures(
                eq(email),
                any(Instant.class)
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.execute(
                        new AuthenticateCustomerCommand(
                                email.value(),
                                rawPassword.reveal()
                        )
                )
        );

        verify(accountRepository).resetLoginFailures(
                eq(email),
                any(Instant.class)
        );
    }

    private Account createActiveAccount() {
        Account account = Account.registerCustomer(
                email,
                new PasswordHash("hashed-password"),
                Instant.now()
        );

        account.verifyEmail();

        return account;
    }

    private Account createAccountWithLoginProtectionState(
            int failedLoginAttempts,
            Instant lockedUntil
    ) {
        return Account.restore(
                AccountId.generate(),
                email,
                new PasswordHash("hashed-password"),
                AccountType.CUSTOMER,
                AccountStatus.ACTIVE,
                Set.of(Role.CUSTOMER),
                Instant.now(),
                failedLoginAttempts,
                lockedUntil
        );
    }
}
