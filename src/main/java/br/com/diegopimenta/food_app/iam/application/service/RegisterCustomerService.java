package br.com.diegopimenta.food_app.iam.application.service;

import br.com.diegopimenta.food_app.iam.application.command.RegisterCustomerCommand;
import br.com.diegopimenta.food_app.iam.application.port.in.RegisterCustomerUseCase;
import br.com.diegopimenta.food_app.iam.application.port.out.AccountRepository;
import br.com.diegopimenta.food_app.iam.application.port.out.CustomerProfileRepository;
import br.com.diegopimenta.food_app.iam.application.port.out.PasswordHasher;
import br.com.diegopimenta.food_app.iam.application.result.AccountResult;
import br.com.diegopimenta.food_app.iam.domain.exception.CpfAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.exception.EmailAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class RegisterCustomerService implements RegisterCustomerUseCase {

    private final AccountRepository accountRepository;
    private final CustomerProfileRepository profileRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    public RegisterCustomerService(AccountRepository accountRepository,
                                   CustomerProfileRepository profileRepository,
                                   PasswordHasher passwordHasher,
                                   Clock clock) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AccountResult execute(RegisterCustomerCommand command) {
        Email email = new Email(command.email());
        Cpf cpf = new Cpf(command.cpf());
        RawPassword rawPassword = new RawPassword(command.password());

        if (accountRepository.existsByEmail(email))
            throw new EmailAlreadyInUseException();

        if (profileRepository.existsByCpf(cpf))
            throw new CpfAlreadyInUseException();

        Account account = Account
                .registerCustomer(email, passwordHasher.hash(rawPassword), clock.instant());
        CustomerProfile profile = new CustomerProfile(account.id(), command.fullName(), cpf);

        accountRepository.save(account);
        profileRepository.save(profile);

        return AccountResult.from(account);
    }

}
