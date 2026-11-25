package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface  AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
    boolean existsByEmail(String email);

    Optional<AccountJpaEntity> findByEmail(String email);

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query(value = """
            UPDATE accounts
            SET failed_login_attempts =
                CASE
                    WHEN locked_until IS NOT NULL
                        AND locked_until <= :now
                    THEN 1
                    ELSE failed_login_attempts + 1
                END,
                
                locked_until =
                CASE
                    WHEN
                        CASE
                            WHEN locked_until IS NOT NULL
                                AND locked_until <= :now
                            THEN 1
                            ELSE failed_login_attempts + 1
                        END >= :maxAttempts
                    THEN CAST(:lockUntil AS TIMESTAMP WITH TIME ZONE)
                    ELSE NULL
                END
                
            WHERE email = :email
                AND status = 'ACTIVE'
                AND (
                    locked_until IS NULL
                    OR locked_until <= :now
                )
            """, nativeQuery = true)
    int recordFailedLoginAttempt(
            @Param("email") String email,
            @Param("maxAttempts") int maxAttempts,
            @Param("now")Instant now,
            @Param("lockUntil") Instant lockUntil
            );

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query(value = """
            UPDATE accounts
            SET failed_login_attempts = 0,
                locked_until = NULL
            WHERE email = :email
                AND status = 'ACTIVE'
                AND (
                    locked_until IS NULL
                    OR locked_until <= :now
                )
            """, nativeQuery = true)
    int resetLoginFailures(
            @Param("email") String email,
            @Param("now") Instant now
    );
}
