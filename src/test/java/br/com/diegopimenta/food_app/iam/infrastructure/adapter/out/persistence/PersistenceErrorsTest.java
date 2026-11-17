package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersistenceErrorsTest {

    @Test
    void shouldRecognizeEmailUniqueConstraint() {
        var exception = createException("uk_accounts_email");

        assertTrue(
                PersistenceErrors.violates(exception, "uk_accounts_email")
        );
    }

    @Test
    void shouldRecognizeCpfUniqueConstraint() {
        var exception = createException("uk_customer_profiles_cpf");

        assertTrue(
                PersistenceErrors.violates(exception, "uk_customer_profiles_cpf")
        );
    }

    @Test
    void shouldReturnFalseWhenConstraintNameIsDifferent() {
        var exception = createException("uk_accounts_email");

        assertFalse(
                PersistenceErrors.violates(exception, "uk_customer_profiles_cpf")
        );
    }

    @Test
    void shouldReturnFalseWhenHibernateConstraintExceptionIsAbsent() {
        var exception = new DataIntegrityViolationException(
                "Data integrity violation",
                new RuntimeException("Some database error")
        );

        assertFalse(
                PersistenceErrors.violates(exception, "uk_accounts_email")
        );
    }

    private DataIntegrityViolationException createException(
            String constraintName
    ) {
        var hibernateException = new ConstraintViolationException(
                "Unique constraint violation",
                (SQLException) null,
                constraintName
        );

        var intermediateException = new RuntimeException(
                "Persistence error",
                hibernateException
        );

        return new DataIntegrityViolationException(
                "Data integrity violation",
                intermediateException
        );
    }
}
