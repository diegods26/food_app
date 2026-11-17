package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomerProfileTest {

    @Test
    void acceptsFullNameWith120Characters() {
        String fullName = "A".repeat(120);

        CustomerProfile profile = new CustomerProfile(
                AccountId.generate(),
                fullName,
                new Cpf("529.982.247-25")
        );

        assertEquals(120, profile.fullName().length());
    }

    @Test
    void rejectsFullNameWithMoreThan120Characters() {
        String fullName = "A".repeat(121);

        assertThrows(
                InvalidValueException.class,
                () -> new CustomerProfile(
                        AccountId.generate(),
                        fullName,
                        new Cpf("529.982.247-25")
                )
        );
    }

    @Test
    void removesSpacesAtBeginningAndEndOfName() {
        CustomerProfile profile = new CustomerProfile(
                AccountId.generate(),
                "   João Silva   ",
                new Cpf("529.982.247-25")
        );

        assertEquals("João Silva", profile.fullName());
    }
}
