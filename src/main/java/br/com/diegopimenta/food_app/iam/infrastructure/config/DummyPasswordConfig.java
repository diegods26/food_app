package br.com.diegopimenta.food_app.iam.infrastructure.config;

import br.com.diegopimenta.food_app.iam.application.port.out.PasswordHasher;
import br.com.diegopimenta.food_app.iam.domain.model.PasswordHash;
import br.com.diegopimenta.food_app.iam.domain.model.RawPassword;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DummyPasswordConfig {

    @Bean
    public PasswordHash dummyPasswordHash(PasswordHasher passwordHasher) {
        String randomPassword = UUID.randomUUID() + "Aa1";
        RawPassword rawPassword = new RawPassword(randomPassword);

        return passwordHasher.hash(rawPassword);
    }
}
