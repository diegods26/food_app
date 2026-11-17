package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmailTest {

    @Test
    void acceptsEmailWith120Characters() {
        String localPart = "a".repeat(108);
        String emailValue = localPart + "@example.com";

        Email email = new Email(emailValue);

        assertEquals(120, email.value().length());
    }

    @Test
    void rejectsEmailWithMoreThan120Characters() {
        String localPart = "a".repeat(109);
        String emailValue = localPart + "@example.com";

        assertThrows(
                InvalidValueException.class,
                () -> new Email(emailValue)
        );
    }

    @Test
    void normalizesEmail() {
        Email email = new Email("  TEST@EMAIL.COM  ");

        assertEquals("test@email.com", email.value());
    }
}
