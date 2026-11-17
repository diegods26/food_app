package br.com.diegopimenta.food_app.iam.domain.model;

import br.com.diegopimenta.food_app.iam.domain.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CpfTest {

    @Test
    void acceptsFormattedCpfAndStoresOnlyDigits() {
        assertEquals("52998224725", new Cpf("529.982.247-25").value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"111.111.111-11", "529.982.247-24", "123", ""})
    void rejectsInvalidCpf(String invalid) {
        assertThrows(InvalidValueException.class, () -> new Cpf(invalid));
    }
}
