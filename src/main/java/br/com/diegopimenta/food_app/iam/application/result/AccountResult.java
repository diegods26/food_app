package br.com.diegopimenta.food_app.iam.application.result;

import br.com.diegopimenta.food_app.iam.domain.model.Account;
import br.com.diegopimenta.food_app.iam.domain.model.AccountStatus;

import java.util.UUID;

public record AccountResult(UUID accountId, String email, AccountStatus status) {

    public static AccountResult from(Account account) {
        return new AccountResult(account.id().value(), account.email().value(), account.status());
    }
}
