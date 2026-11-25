package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidAccountStateException;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Account {

    private final AccountId id;
    private final Email email;
    private final PasswordHash passwordHash;
    private final AccountType type;
    private AccountStatus status;
    private final Set<Role> roles;
    private final Instant createdAt;
    private final int failedLoginAttempts;
    private final Instant lockedUntil;

    private Account(AccountId id, Email email, PasswordHash passwordHash,
                    AccountType accountType, AccountStatus status,
                    Set<Role> roles, Instant createdAt,
                    int failedLoginAttempts, Instant lockedUntil) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.type = Objects.requireNonNull(accountType);
        this.status = Objects.requireNonNull(status);
        this.roles = Set.copyOf(roles);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
    }

    // Fabricas
    public static Account registerCustomer(Email email, PasswordHash hash, Instant now) {
        return new Account(
                AccountId.generate(),
                email,
                hash,
                AccountType.CUSTOMER,
                AccountStatus.PENDING_VERIFICATION,
                Set.of(Role.CUSTOMER),
                now,
                0,
                null
                );
    }

    public static Account registerMerchant(Email email, PasswordHash hash, Instant now) {
        return new Account(
                AccountId.generate(),
                email,
                hash,
                AccountType.MERCHANT,
                AccountStatus.PENDING_VERIFICATION,
                Set.of(Role.MERCHANT_OWNER),
                now,
                0,
                null
        );
    }

    /** Uso exclusivo de adaptadores de persistência: reidrata sem aplicar regras de criação. */
    public static Account restore(AccountId id, Email email, PasswordHash hash,
                                  AccountType type, AccountStatus status,
                                  Set<Role> roles, Instant createdAt,
                                  int failedLoginAttempts, Instant lockedUntil)
    {
       return new Account(id, email, hash, type, status, roles, createdAt, failedLoginAttempts, lockedUntil);
    }

    // ---------- Comportamento (máquina de estados) ----------
    public void verifyEmail() {
        if (status != AccountStatus.PENDING_VERIFICATION)
            throw new InvalidAccountStateException("E-mail já verificado ou conta em estado incompatível.");

        status = (type == AccountType.CUSTOMER) ? AccountStatus.ACTIVE : AccountStatus.PENDING_APPROVAL;
    }

    public void approve() {
        if (type != AccountType.MERCHANT || status != AccountStatus.PENDING_APPROVAL)
            throw new InvalidAccountStateException("Só empresas aguardando aprovação podem ser aprovadas.");

        status = AccountStatus.ACTIVE;
    }

    public void suspend() {
        status = AccountStatus.SUSPENDED;
    }

    public boolean canAuthenticate(Instant now) {
        return status == AccountStatus.ACTIVE
                && (lockedUntil == null || !lockedUntil.isAfter(now));
    }

    public AccountId id() { return id; }
    public Email email() { return email; }
    public PasswordHash passwordHash() { return passwordHash; }
    public AccountType type() { return type; }
    public AccountStatus status() { return status; }
    public Set<Role> roles() { return roles; }
    public Instant createdAt() { return createdAt; }
    public int failedLoginAttempts() { return failedLoginAttempts; }
    public Instant lockedUntil() { return lockedUntil; }
}
