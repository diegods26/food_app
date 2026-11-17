package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import br.com.diegopimenta.food_app.iam.domain.exception.CpfAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.exception.EmailAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.ThreadLocalRandom;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class PersistenceAdaptersIntegrationTest {

    @Autowired
    private AccountPersistenceAdapter accountAdapter;

    @Autowired
    private CustomerProfilePersistenceAdapter profileAdapter;

    @Test
    void shouldTranslateDuplicateEmailConstraint() {
        String email = "test-" + UUID.randomUUID() + "@email.com";

        Account first = createAccount(email);
        Account second = createAccount(email);

        accountAdapter.save(first);

        assertThrows(
                EmailAlreadyInUseException.class,
                () -> accountAdapter.save(second)
        );
    }

    @Test
    void shouldTranslateDuplicateCpfConstraint() {
        Account first = createAccount(
                "first-" + UUID.randomUUID() + "@email.com"
        );

        Account second = createAccount(
                "second-" + UUID.randomUUID() + "@email.com"
        );

        accountAdapter.save(first);
        accountAdapter.save(second);

        Cpf cpf = createUniqueCpf();

        CustomerProfile firstProfile = new CustomerProfile(
                first.id(),
                "Primeiro Cliente",
                cpf
        );

        CustomerProfile secondProfile = new CustomerProfile(
                second.id(),
                "Segundo Cliente",
                cpf
        );

        profileAdapter.save(firstProfile);

        assertThrows(
                CpfAlreadyInUseException.class,
                () -> profileAdapter.save(secondProfile)
        );
    }

    private Account createAccount(String email) {
        return Account.registerCustomer(
                new Email(email),
                new PasswordHash("test-hash"),
                Instant.parse("2026-11-17T12:00:00Z")
        );
    }

    private Cpf createUniqueCpf() {
        String base = String.format(
                "%09d",
                ThreadLocalRandom.current().nextInt(1_000_000_000)
        );

        int firstDigit = calculateCheckDigit(base, 10);

        String firstTenDigits = base + firstDigit;

        int secondDigit = calculateCheckDigit(firstTenDigits, 11);

        return new Cpf(base + firstDigit + secondDigit);
    }

    private int calculateCheckDigit(String digits, int weight) {
        int sum = 0;

        for (int i = 0; i < digits.length(); i++) {
            int digit = Character.getNumericValue(digits.charAt(i));
            sum += digit * (weight - i);
        }

        int remainder = sum % 11;

        return remainder < 2 ? 0 : 11 - remainder;
    }
}
