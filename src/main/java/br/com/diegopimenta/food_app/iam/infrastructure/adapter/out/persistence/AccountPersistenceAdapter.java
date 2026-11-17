package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import br.com.diegopimenta.food_app.iam.application.port.out.AccountRepository;
import br.com.diegopimenta.food_app.iam.domain.exception.EmailAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.model.Account;
import br.com.diegopimenta.food_app.iam.domain.model.Email;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class AccountPersistenceAdapter implements AccountRepository {

    private final AccountJpaRepository jpa;

    AccountPersistenceAdapter(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }


    @Override
    public boolean existsByEmail(Email email) {
        return jpa.existsByEmail(email.value());
    }

    @Override
    public void save(Account account) {
        try {
            jpa.saveAndFlush(AccountPersistenceMapper.toEntity(account));
        } catch (DataIntegrityViolationException e) {
            if (PersistenceErrors.violates(e, "uk_accounts_email")) {
                throw new EmailAlreadyInUseException();
            }

            throw e;
        }
    }
}
