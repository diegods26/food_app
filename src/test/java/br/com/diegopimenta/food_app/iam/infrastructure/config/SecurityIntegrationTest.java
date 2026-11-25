package br.com.diegopimenta.food_app.iam.infrastructure.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class SecurityIntegrationTest {

    private static final String PROTECTED_ENDPOINT =
            "/api/v1/security-test";

    private static final String PUBLIC_ENDPOINT =
            "/api/v1/auth/customers/register";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void shouldReturnUnauthorizedWhenProtectedEndpointHasNoToken()
            throws Exception {

        mockMvc.perform(get(PROTECTED_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRequestWithValidTokenThroughSecurityFilter()
            throws Exception {

        String token = generateValidToken();

        mockMvc.perform(
                        get(PROTECTED_ENDPOINT)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get(PROTECTED_ENDPOINT)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer invalid.token.value"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowUnauthenticatedAccessToCustomerRegistrationRoute()
            throws Exception {

        mockMvc.perform(get(PUBLIC_ENDPOINT))
                .andExpect(status().isMethodNotAllowed());
    }

    private String generateValidToken() {

        byte[] keyBytes = Decoders.BASE64.decode(
                jwtProperties.jwtSecret()
        );

        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer(jwtProperties.jwtIssuer())
                .audience()
                .add(jwtProperties.jwtAudience())
                .and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .id(UUID.randomUUID().toString())
                .claim("roles", java.util.List.of("CUSTOMER"))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }
}
