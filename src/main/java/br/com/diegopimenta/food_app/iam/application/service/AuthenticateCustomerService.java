package br.com.diegopimenta.food_app.iam.application.service;

import br.com.diegopimenta.food_app.iam.application.command.AuthenticateCustomerCommand;
import br.com.diegopimenta.food_app.iam.application.port.in.AuthenticateCustomerUseCase;
import br.com.diegopimenta.food_app.iam.application.port.out.AccountRepository;
import br.com.diegopimenta.food_app.iam.application.port.out.PasswordHasher;
import br.com.diegopimenta.food_app.iam.application.result.AccountResult;
import br.com.diegopimenta.food_app.iam.domain.exception.InvalidCredentialsException;
import br.com.diegopimenta.food_app.iam.domain.exception.InvalidValueException;
import br.com.diegopimenta.food_app.iam.domain.model.Account;
import br.com.diegopimenta.food_app.iam.domain.model.Email;
import br.com.diegopimenta.food_app.iam.domain.model.PasswordHash;
import br.com.diegopimenta.food_app.iam.domain.model.RawPassword;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class AuthenticateCustomerService implements AuthenticateCustomerUseCase {

    private final AccountRepository accountRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordHash dummyPasswordHash;

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_LOCK_DURATION =
            Duration.ofMinutes(15);

    public AuthenticateCustomerService(AccountRepository accountRepository,
                                       PasswordHasher passwordHasher,
                                       PasswordHash dummyPasswordHash) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
        this.dummyPasswordHash = dummyPasswordHash;
    }

    @Override
    public AccountResult execute(AuthenticateCustomerCommand command) {

        Email email;
        RawPassword rawPassword;

        try {
            email = new Email(command.email());
            rawPassword = new RawPassword(command.password());
        }catch (InvalidValueException ex) {
            throw new InvalidCredentialsException();
        }

        Optional<Account> accountOptional =
                accountRepository.findByEmail(email);

        PasswordHash hashToCompare = accountOptional
                .map(Account::passwordHash)
                .orElse(dummyPasswordHash);

        boolean passwordMatches =
                passwordHasher.matches(rawPassword, hashToCompare);

        if (accountOptional.isEmpty())
            throw new InvalidCredentialsException();

        Account account = accountOptional.get();

        Instant now = Instant.now();

        if (!account.canAuthenticate(now))
            throw new InvalidCredentialsException();

        // Senha incorreta: registrar tentativa
        if (!passwordMatches) {
            accountRepository.recordFailedLoginAttempt(
                    email,
                    MAX_FAILED_LOGIN_ATTEMPTS,
                    now,
                    now.plus(LOGIN_LOCK_DURATION)
            );

            throw new InvalidCredentialsException();
        }

        boolean resetItIfSuccessful = accountRepository.resetLoginFailures(
                email,
                now
        );

        if (!resetItIfSuccessful)
            throw new InvalidCredentialsException();

        return AccountResult.from(account);
    }
}
