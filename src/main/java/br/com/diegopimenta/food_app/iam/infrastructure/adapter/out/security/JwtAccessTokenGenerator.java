package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.security;

import br.com.diegopimenta.food_app.iam.application.port.out.AccessTokenGenerator;
import br.com.diegopimenta.food_app.iam.application.result.IssuedAccessToken;
import br.com.diegopimenta.food_app.iam.domain.model.AccountId;
import br.com.diegopimenta.food_app.iam.domain.model.Role;
import br.com.diegopimenta.food_app.iam.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtAccessTokenGenerator implements AccessTokenGenerator {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtAccessTokenGenerator(JwtProperties properties) {
        this.properties = properties;

        byte[] keyBytes = Decoders.BASE64.decode(properties.jwtSecret());

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public IssuedAccessToken generate(
            AccountId accountId,
            Set<Role> roles,
            Instant issuedAt)
    {
        Instant expiresAt = issuedAt.plus(
                Duration.ofMinutes(properties.jwtTtlMinutes())
        );

        String token = Jwts.builder()
                .subject(accountId.value().toString())
                .issuer(properties.jwtIssuer())
                .audience()
                    .add(properties.jwtAudience())
                    .and()
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .id(UUID.randomUUID().toString())
                .claim(
                        "roles",
                        roles.stream()
                                .map(Enum::name)
                                .sorted()
                                .toList()
                )
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        return new IssuedAccessToken(token, expiresAt);
    }
}
