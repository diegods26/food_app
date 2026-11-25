package br.com.diegopimenta.food_app.iam.application.port.out;

import br.com.diegopimenta.food_app.iam.application.result.IssuedAccessToken;
import br.com.diegopimenta.food_app.iam.domain.model.AccountId;
import br.com.diegopimenta.food_app.iam.domain.model.Role;

import java.time.Instant;
import java.util.Set;

public interface AccessTokenGenerator {

    IssuedAccessToken generate(
            AccountId accountId,
            Set<Role> roles,
            Instant issuedAt
    );
}
