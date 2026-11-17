package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import br.com.diegopimenta.food_app.iam.domain.model.AccountStatus;
import br.com.diegopimenta.food_app.iam.domain.model.AccountType;
import br.com.diegopimenta.food_app.iam.domain.model.Role;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_roles", joinColumns = @JoinColumn(name = "account_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AccountJpaEntity() { }

    AccountJpaEntity(UUID id, String email, String passwordHash,
                     AccountType type, AccountStatus status,
                     Set<Role> roles, Instant createdAt)
    {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.type = type;
        this.status = status;
        this.roles = new HashSet<>(roles);
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    String getEmail() { return email; }
    String getPasswordHash() { return passwordHash; }
    AccountType getType() { return type; }
    AccountStatus getStatus() { return status; }
    Set<Role> getRoles() { return roles; }
    Instant getCreatedAt() { return createdAt; }
}
