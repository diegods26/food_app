package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidAccountStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    private static final Instant NOW = Instant.parse("2026-11-12T12:00:00Z");

    private static Account customer() {
        return Account.registerCustomer(
                new Email("ana@email.com"),
                new PasswordHash("hash"),
                NOW);
    }

    private static Account merchant() {
        return Account.registerMerchant(
                new Email("loja@email.com"),
                new PasswordHash("hash"),
                NOW);
    }

    @Test
    void customerStartsWithoutFailedLoginAttemptsOrLock() {
        Account account = customer();

        assertEquals(0, account.failedLoginAttempts());
        assertNull(account.lockedUntil());
    }

    @Test
    void merchantStartsWithoutFailedLoginAttemptsOrLock() {
        Account account = merchant();

        assertEquals(0, account.failedLoginAttempts());
        assertNull(account.lockedUntil());
    }

    @Test
    void customerStartsPendingAndCannotAuthenticateUntilEmailIsVerified() {
        Account account = customer();
        Instant now = Instant.now();
        assertEquals(AccountStatus.PENDING_VERIFICATION, account.status());
        assertFalse(account.canAuthenticate(now));

        account.verifyEmail();

        assertEquals(AccountStatus.ACTIVE, account.status());
        assertTrue(account.canAuthenticate(now));
    }

    @Test
    void merchantNeedsApprovalAfterVerifyingEmail() {
        Account account = merchant();
        Instant now = Instant.now();
        account.verifyEmail();
        assertEquals(AccountStatus.PENDING_APPROVAL, account.status());
        assertFalse(account.canAuthenticate(now));

        account.approve();
        assertTrue(account.canAuthenticate(now));
    }

    @Test
    void customerCannotBeApproved() {
        Account account = customer();
        account.verifyEmail();
        assertThrows(InvalidAccountStateException.class, account::approve);
    }

    @Test
    void cannotVerifyEmailTwice() {
        Account account = customer();
        account.verifyEmail();
        assertThrows(InvalidAccountStateException.class, account::verifyEmail);
    }

    @Test
    void emailIsNormalized() {
        assertEquals("ana@email.com", new Email("  Ana@Email.COM ").value());
    }

    @Test
    void customerReceivesCustomerRole() {
        Account account = customer();

        assertTrue(account.roles().contains(Role.CUSTOMER));
        assertFalse(account.roles().contains(Role.MERCHANT_OWNER));
    }

    @Test
    void merchantReceivesMerchantRole() {
        Account account = merchant();

        assertTrue(account.roles().contains(Role.MERCHANT_OWNER));
        assertFalse(account.roles().contains(Role.CUSTOMER));
    }

    @Test
    void shouldRestoreAccountWithLoginProtectionState() {
        Instant lockedUntil = Instant.parse("2026-11-17T15:00:00Z");

        Account account = Account.restore(
                new AccountId(UUID.randomUUID()),
                new Email("customer@email.com"),
                new PasswordHash("hashed-password"),
                AccountType.CUSTOMER,
                AccountStatus.ACTIVE,
                Set.of(Role.CUSTOMER),
                Instant.now(),
                3,
                lockedUntil
        );

        assertEquals(3, account.failedLoginAttempts());
        assertEquals(lockedUntil, account.lockedUntil());
    }

}
