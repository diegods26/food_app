package br.com.diegopimenta.food_app.iam.application.result;

import java.time.Instant;

public record IssuedAccessToken(String token, Instant expiresAt) {
}
