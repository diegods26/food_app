package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import br.com.diegopimenta.food_app.iam.domain.exception.CpfAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.exception.EmailAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PersistenceAdaptersIntegrationTest {

    @Autowired
    private AccountPersistenceAdapter accountAdapter;

    @Autowired
    private CustomerProfilePersistenceAdapter profileAdapter;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    private Account createActiveAccount(String email) {
        return Account.restore(
                AccountId.generate(),
                new Email(email),
                new PasswordHash("hashed-password"),
                AccountType.CUSTOMER,
                AccountStatus.ACTIVE,
                Set.of(Role.CUSTOMER),
                Instant.now(),
                0,
                null
        );
    }

    @Test
    void shouldIncrementFailedLoginAttempts() {
        String email = "failed-" + UUID.randomUUID() + "@email.com";

        Account account = createActiveAccount(email);
        accountAdapter.save(account);

        Instant now = Instant.now();

        boolean updated = accountAdapter.recordFailedLoginAttempt(
                account.email(),
                5,
                now,
                now.plus(Duration.ofMinutes(15))
        );

        Account restored = accountAdapter
                .findByEmail(account.email())
                .orElseThrow();

        assertTrue(updated);
        assertEquals(1, restored.failedLoginAttempts());
        assertNull(restored.lockedUntil());
    }

    @Test
    void shouldLockAccountAfterMaximumFailedAttempts() {
        String email = "lock-" + UUID.randomUUID() + "@email.com";

        Account account = createActiveAccount(email);
        accountAdapter.save(account);

        Instant now = Instant.now();
        Instant lockUntil = now.plus(Duration.ofMinutes(15));

        for (int i = 0; i < 5; i++) {
            boolean updated = accountAdapter.recordFailedLoginAttempt(
                    account.email(),
                    5,
                    now,
                    lockUntil
            );

            assertTrue(updated);
        }

        Account restored = accountAdapter
                .findByEmail(account.email())
                .orElseThrow();

        assertEquals(5, restored.failedLoginAttempts());
        assertEquals(normalizeToPostgresPrecision(lockUntil), restored.lockedUntil());
    }

    @Test
    void shouldNotIncrementAttemptsWhileAccountIsLocked() {
        String email = "locked-" + UUID.randomUUID() + "@email.com";

        Account account = createActiveAccount(email);
        accountAdapter.save(account);

        Instant now = Instant.now();
        Instant lockUntil = now.plus(Duration.ofMinutes(15));

        for (int i = 0; i < 5; i++) {
            accountAdapter.recordFailedLoginAttempt(
                    account.email(),
                    5,
                    now,
                    lockUntil
            );
        }

        boolean updated = accountAdapter.recordFailedLoginAttempt(
                account.email(),
                5,
                now.plus(Duration.ofMinutes(1)),
                now.plus(Duration.ofMinutes(16))
        );

        Account restored = accountAdapter
                .findByEmail(account.email())
                .orElseThrow();

        assertFalse(updated);
        assertEquals(5, restored.failedLoginAttempts());
        assertEquals(normalizeToPostgresPrecision(lockUntil), restored.lockedUntil());
    }

    @Test
    void shouldRestartCounterAfterLockExpiration() {
        String email = "expired-" + UUID.randomUUID() + "@email.com";

        Account account = createActiveAccount(email);
        accountAdapter.save(account);

        Instant now = Instant.now();
        Instant lockUntil = now.plus(Duration.ofMinutes(15));

        for (int i = 0; i < 5; i++) {
            accountAdapter.recordFailedLoginAttempt(
                    account.email(),
                    5,
                    now,
                    lockUntil
            );
        }

        Instant afterExpiration = lockUntil.plusSeconds(1);
        Instant nextLockUntil = afterExpiration.plus(Duration.ofMinutes(15));

        boolean updated = accountAdapter.recordFailedLoginAttempt(
                account.email(),
                5,
                afterExpiration,
                nextLockUntil
        );

        Account restored = accountAdapter
                .findByEmail(account.email())
                .orElseThrow();

        assertTrue(updated);
        assertEquals(1, restored.failedLoginAttempts());
        assertNull(restored.lockedUntil());
    }

    @Test
    void shouldResetFailedLoginAttempts() {
        String email = "reset-" + UUID.randomUUID() + "@email.com";

        Account account = createActiveAccount(email);
        accountAdapter.save(account);

        Instant now = Instant.now();

        for (int i = 0; i < 3; i++) {
            accountAdapter.recordFailedLoginAttempt(
                    account.email(),
                    5,
                    now,
                    now.plus(Duration.ofMinutes(15))
            );
        }

        boolean updated = accountAdapter.resetLoginFailures(
                account.email(),
                now
        );

        Account restored = accountAdapter
                .findByEmail(account.email())
                .orElseThrow();

        assertTrue(updated);
        assertEquals(0, restored.failedLoginAttempts());
        assertNull(restored.lockedUntil());
    }

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

    @Test
    void shouldFindAccountByEmail() {

        String email = "find-" + UUID.randomUUID() + "@email.com";

        Account account = Account.registerCustomer(
                new Email(email),
                new PasswordHash("test-password-hash"),
                Instant.now()
        );

        accountAdapter.save(account);

        Optional<Account> result =
                accountAdapter.findByEmail(new Email(email));

        assertTrue(result.isPresent());

        assertEquals(account.id(), result.get().id());
        assertEquals(email, result.get().email().value());
    }

    @Test
    void shouldPersistAndRestoreLoginProtectionState() {
        Account account = Account.registerCustomer(
                new Email("login-protection-" + UUID.randomUUID() + "@email.com"),
                new PasswordHash("hashed-password"), Instant.now()
        );

        // O estado inicial deve ser zero e null.
        assertEquals(0, account.failedLoginAttempts());
        assertNull(account.lockedUntil());

        accountJpaRepository.saveAndFlush(
                AccountPersistenceMapper.toEntity(account)
        );

        Account restored = accountAdapter
                .findByEmail(account.email())
                .orElseThrow();

        assertEquals(0, restored.failedLoginAttempts());
        assertNull(restored.lockedUntil());
    }

    @Test
    void shouldNotResetFailedLoginAttemptsWhileAccountIsLocked() {
        String email = "reset-locked-" + UUID.randomUUID() + "@email.com";

        Account account = createActiveAccount(email);
        accountAdapter.save(account);

        Instant now = Instant.now();
        Instant lockUntil = now.plus(Duration.ofMinutes(15));

        // Atinge o limite de tentativas e bloqueia a conta.
        for (int i = 0; i < 5; i++) {
            accountAdapter.recordFailedLoginAttempt(
                    account.email(),
                    5,
                    now,
                    lockUntil
            );
        }

        // Tenta zerar as tentativas antes do fim do bloqueio.
        boolean updated = accountAdapter.resetLoginFailures(
                account.email(),
                now.plus(Duration.ofMinutes(1))
        );

        Account restored = accountAdapter
                .findByEmail(account.email())
                .orElseThrow();

        assertFalse(updated);
        assertEquals(5, restored.failedLoginAttempts());
        assertEquals(
                normalizeToPostgresPrecision(lockUntil),
                restored.lockedUntil()
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

    private Instant normalizeToPostgresPrecision(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MICROS)
                .plusNanos(
                        instant.getNano() % 1_000 >= 500 ? 1_000 : 0
                );
    }
}
