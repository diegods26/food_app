package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "customer_profiles")
public class CustomerProfileJpaEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 11)
    private String cpf;

    protected CustomerProfileJpaEntity() { }

    CustomerProfileJpaEntity(UUID accountId, String fullName, String cpf) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.cpf = cpf;
    }
}
