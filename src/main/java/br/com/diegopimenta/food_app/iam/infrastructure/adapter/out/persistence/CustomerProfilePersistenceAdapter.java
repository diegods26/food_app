package br.com.diegopimenta.food_app.iam.infrastructure.adapter.out.persistence;

import br.com.diegopimenta.food_app.iam.application.port.out.CustomerProfileRepository;
import br.com.diegopimenta.food_app.iam.domain.exception.CpfAlreadyInUseException;
import br.com.diegopimenta.food_app.iam.domain.model.Cpf;
import br.com.diegopimenta.food_app.iam.domain.model.CustomerProfile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerProfilePersistenceAdapter implements CustomerProfileRepository {

    private final CustomerProfileJpaRepository jpa;

    CustomerProfilePersistenceAdapter(CustomerProfileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existsByCpf(Cpf cpf) {
        return jpa.existsByCpf(cpf.value());
    }

    @Override
    public void save(CustomerProfile customerProfile) {
        try {
            jpa.saveAndFlush(AccountPersistenceMapper.toEntity(customerProfile));
        } catch (DataIntegrityViolationException e) {
            if (PersistenceErrors.violates(e, "uk_customer_profiles_cpf")) {
                throw new CpfAlreadyInUseException();
            }

            throw e;
        }
    }
}
