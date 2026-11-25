package br.com.diegopimenta.food_app.iam.infrastructure.config;

import org.springframework.security.oauth2.jwt.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtConfigurationTest {

    private static final String ISSUER = "food-app";
    private static final String AUDIENCE = "food-app-api";

    private String secret;
    private SecretKey signingKey;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = "0123456789abcdef0123456789abcdef"
                .getBytes(StandardCharsets.UTF_8);

        secret = Encoders.BASE64.encode(keyBytes);
        signingKey = Keys.hmacShaKeyFor(keyBytes);

        JwtProperties properties = new JwtProperties(
                secret,
                15,
                ISSUER,
                AUDIENCE
        );

        decoder = new JwtConfiguration().jwtDecoder(properties);
    }

    @Test
    void shouldDecodeValidToken() {
        String token = generateToken(
                ISSUER,
                AUDIENCE,
                Instant.now().minusSeconds(30),
                Instant.now().plusSeconds(300)
        );

        Jwt jwt = decoder.decode(token);

        assertNotNull(jwt);
        assertEquals(ISSUER, jwt.getClaimAsString("iss"));
        assertTrue(jwt.getAudience().contains(AUDIENCE));
        assertNotNull(jwt.getSubject());
    }

    @Test
    void shouldRejectExpiredToken() {
        String token = generateToken(
                ISSUER,
                AUDIENCE,
                Instant.now().minusSeconds(300),
                Instant.now().minusSeconds(120)
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void shouldRejectTokenWithTamperedSignature() {
        String token = generateToken(
                ISSUER,
                AUDIENCE,
                Instant.now().minusSeconds(30),
                Instant.now().plusSeconds(300)
        );

        String[] parts = token.split("\\.");
        String signature = parts[2];

        char replacement = signature.charAt(0) == 'a' ? 'b' : 'a';

        parts[2] = replacement + signature.substring(1);

        String tamperedToken = String.join(".", parts);

        assertThrows(
                JwtException.class,
                () -> decoder.decode(tamperedToken)
        );
    }

    @Test
    void shouldRejectTokenWithInvalidIssuer() {
        String token = generateToken(
                "another-issuer",
                AUDIENCE,
                Instant.now().minusSeconds(30),
                Instant.now().plusSeconds(300)
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    @Test
    void shouldRejectTokenWithInvalidAudience() {
        String token = generateToken(
                ISSUER,
                "another-api",
                Instant.now().minusSeconds(30),
                Instant.now().plusSeconds(300)
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    private String generateToken(
            String issuer,
            String audience,
            Instant issuedAt,
            Instant expiresAt
    ) {
        return Jwts.builder()
                .subject("d6e2a7f0-0000-4000-8000-000000000001")
                .issuer(issuer)
                .audience()
                .add(audience)
                .and()
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }
}
