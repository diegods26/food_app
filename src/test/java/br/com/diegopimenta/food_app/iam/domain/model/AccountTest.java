package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidAccountStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    private static final Instant NOW = Instant.parse("2026-11-12T12:00:00Z");

    private static Account customer() {
        return Account.registerCustomer(new Email("ana@email.com"), new PasswordHash("hash"), NOW);
    }

    private static Account merchant() {
        return Account.registerMerchant(new Email("loja@email.com"), new PasswordHash("hash"), NOW);
    }

    @Test
    void customerStartsPendingAndCannotAuthenticateUntilEmailIsVerified() {
        Account account = customer();
        assertEquals(AccountStatus.PENDING_VERIFICATION, account.status());
        assertFalse(account.canAuthenticate());

        account.verifyEmail();

        assertEquals(AccountStatus.ACTIVE, account.status());
        assertTrue(account.canAuthenticate());
    }

    @Test
    void merchantNeedsApprovalAfterVerifyingEmail() {
        Account account = merchant();
        account.verifyEmail();
        assertEquals(AccountStatus.PENDING_APPROVAL, account.status());
        assertFalse(account.canAuthenticate());

        account.approve();
        assertTrue(account.canAuthenticate());
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

}
