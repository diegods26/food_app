package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

public final class PersistenceErrors {

    private PersistenceErrors() { }

    static boolean violates(DataIntegrityViolationException ex, String constraintName) {
        Throwable current = ex;

        while (current != null) {

            if (current instanceof ConstraintViolationException violation)
                return constraintName.equals(violation.getConstraintName());

            current = current.getCause();
        }

        return false;
    }
}
