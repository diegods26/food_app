package br.com.diegopimenta.food_app.iam.application.port.out;

import br.com.diegopimenta.food_app.iam.domain.model.Account;
import br.com.diegopimenta.food_app.iam.domain.model.Email;

public interface AccountRepository {

    boolean existsByEmail(Email email);
    void save(Account account);
}
