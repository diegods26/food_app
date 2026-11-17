package br.com.diegopimenta.food_app.iam.infrastructure.adapter.in.web;

import br.com.diegopimenta.food_app.iam.application.result.AccountResult;

import java.util.UUID;

public record AccountResponse(UUID accountId, String email, String status) {

    static AccountResponse from(AccountResult result) {
        return new AccountResponse(result.accountId(), result.email(), result.status().name());
    }
}
