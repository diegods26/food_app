package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.security;

import br.com.diegopimenta.food_app.iam.application.port.out.PasswordHasher;
import br.com.diegopimenta.food_app.iam.domain.model.PasswordHash;
import br.com.diegopimenta.food_app.iam.domain.model.RawPassword;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public PasswordHash hash(RawPassword rawPassword) {
        return new PasswordHash(encoder.encode(rawPassword.reveal()));
    }

    @Override
    public boolean matches(RawPassword rawPassword, PasswordHash passwordHash) {
        return encoder.matches(rawPassword.reveal(), passwordHash.value());
    }
}
