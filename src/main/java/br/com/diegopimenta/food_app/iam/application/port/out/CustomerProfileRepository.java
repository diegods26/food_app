package br.com.diegopimenta.food_app.iam.application.port.out;

import br.com.diegopimenta.food_app.iam.domain.model.Cpf;
import br.com.diegopimenta.food_app.iam.domain.model.CustomerProfile;

public interface CustomerProfileRepository {

    boolean existsByCpf(Cpf cpf);
    void save(CustomerProfile customerProfile);
}
