package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
    boolean existsByEmail(String email);
}
