package br.com.diegopimenta.food_app.iam.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record JwtProperties(
        String jwtSecret,
        long jwtTtlMinutes,
        String jwtIssuer,
        String jwtAudience
) {

    public JwtProperties {
        if (jwtSecret == null || jwtSecret.isBlank())
            throw new IllegalArgumentException("JWT_SECRET não pode ser vazio.");

        if (jwtTtlMinutes <= 0)
            throw new IllegalArgumentException("JWT_TTL_MINUTES deve ser maior que zero.");

        if (jwtIssuer == null || jwtIssuer.isBlank())
            throw new IllegalArgumentException("JWT_ISSUER não pode ser vazio.");

        if (jwtAudience == null || jwtAudience.isBlank())
            throw new IllegalArgumentException("JWT_AUDIENCE não pode ser vazio.");
    }
}
