package br.com.diegopimenta.food_app.iam.application.port.out;

import br.com.diegopimenta.food_app.iam.domain.model.PasswordHash;
import br.com.diegopimenta.food_app.iam.domain.model.RawPassword;

public interface PasswordHasher {

    PasswordHash hash(RawPassword rawPassword);
    boolean matches(RawPassword rawPassword, PasswordHash passwordHash);
}
