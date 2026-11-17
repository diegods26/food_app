package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import br.com.diegopimenta.food_app.iam.domain.model.*;

public final class AccountPersistenceMapper {

    private AccountPersistenceMapper() { }

    static AccountJpaEntity toEntity(Account a) {
        return new AccountJpaEntity(
                a.id().value(),
                a.email().value(),
                a.passwordHash().value(),
                a.type(),
                a.status(),
                a.roles(),
                a.createdAt()
        );
    }

    static Account toDomain(AccountJpaEntity e) {
        return Account.restore(
                new AccountId(e.getId()),
                new Email(e.getEmail()),
                new PasswordHash(e.getPasswordHash()),
                e.getType(),
                e.getStatus(),
                e.getRoles(),
                e.getCreatedAt()
        );
    }

    static CustomerProfileJpaEntity toEntity(CustomerProfile p) {
        return new CustomerProfileJpaEntity(
                p.accountId().value(),
                p.fullName(),
                p.cpf().value()
        );
    }
}
