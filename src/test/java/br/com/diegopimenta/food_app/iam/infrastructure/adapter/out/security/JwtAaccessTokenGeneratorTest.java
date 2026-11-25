package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.security;

import br.com.diegopimenta.food_app.iam.application.result.IssuedAccessToken;
import br.com.diegopimenta.food_app.iam.domain.model.AccountId;
import br.com.diegopimenta.food_app.iam.domain.model.Role;
import br.com.diegopimenta.food_app.iam.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JwtAaccessTokenGeneratorTest {

    private static final String SECRET =
            "Zm9vZC1hcHAtbG9jYWwtZGV2ZWxvcG1lbnQtc2VjcmV0LWtleS0zMi1ieXRlcy1taW5pbXVt";

    private static final String ISSUER = "food-app";
    private static final String AUDIENCE = "food-app-api";

    private JwtAccessTokenGenerator generator;
    private SecretKey verificationKey;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                SECRET,
                15,
                ISSUER,
                AUDIENCE
        );

        generator = new JwtAccessTokenGenerator(properties);

        verificationKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(SECRET)
        );
    }

    @Test
    void shouldGenerateValidAccessToken() {
        AccountId accountId = AccountId.generate();
        Instant issuedAt = Instant.parse("2026-11-24T12:00:00Z");

        IssuedAccessToken result = generator.generate(
                accountId,
                Set.of(Role.CUSTOMER),
                issuedAt
        );

        Claims claims = parseClaims(result.token());

        assertEquals(accountId.value().toString(), claims.getSubject());
        assertEquals(ISSUER, claims.getIssuer());
        assertTrue(claims.getAudience().contains(AUDIENCE));
        assertEquals(issuedAt, claims.getIssuedAt().toInstant());
        assertEquals(
                issuedAt.plusSeconds(900),
                claims.getExpiration().toInstant()
        );

        assertEquals(
                issuedAt.plusSeconds(900),
                result.expiresAt()
        );
    }

    @Test
    void shouldIncludeAccountRolesInToken() {
        AccountId accountId = AccountId.generate();

        IssuedAccessToken result = generator.generate(
                accountId,
                Set.of(Role.CUSTOMER),
                Instant.now()
        );

        Claims claims = parseClaims(result.token());

        assertEquals(
                Set.of("CUSTOMER"),
                Set.copyOf(claims.get("roles", java.util.List.class))
        );
    }

    @Test
    void shouldGenerateDifferentTokenIds() {
        AccountId accountId = AccountId.generate();
        Instant issuedAt = Instant.now();

        IssuedAccessToken first = generator.generate(
                accountId,
                Set.of(Role.CUSTOMER),
                issuedAt
        );

        IssuedAccessToken second = generator.generate(
                accountId,
                Set.of(Role.CUSTOMER),
                issuedAt
        );

        Claims firstClaims = parseClaims(first.token());
        Claims secondClaims = parseClaims(second.token());

        assertNotEquals(firstClaims.getId(), secondClaims.getId());
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentAccounts() {
        Instant issuedAt = Instant.now();

        IssuedAccessToken first = generator.generate(
                AccountId.generate(),
                Set.of(Role.CUSTOMER),
                issuedAt
        );

        IssuedAccessToken second = generator.generate(
                AccountId.generate(),
                Set.of(Role.CUSTOMER),
                issuedAt
        );

        assertNotEquals(first.token(), second.token());
    }

    private Claims parseClaims(String token) {
        Instant testTime = Instant.parse("2026-11-24T12:05:00Z");

        return Jwts.parser()
                .verifyWith(verificationKey)
                .clock(() -> Date.from(testTime))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
