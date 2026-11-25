package br.com.diegopimenta.food_app.iam.application.port.out;

import br.com.diegopimenta.food_app.iam.domain.model.Account;
import br.com.diegopimenta.food_app.iam.domain.model.Email;

import java.time.Instant;
import java.util.Optional;

public interface AccountRepository {

    boolean existsByEmail(Email email);

    Optional<Account> findByEmail(Email email);

    void save(Account account);

    boolean recordFailedLoginAttempt(
            Email email,
            int maxAttempts,
            Instant now,
            Instant lockUntil
    );

    boolean resetLoginFailures(
            Email email,
            Instant now
    );
}
