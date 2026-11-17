package br.com.diegopimenta.food_app.iam.domain.model;

public enum AccountStatus {
    PENDING_VERIFICATION, // aguardando confirmar e-mail
    PENDING_APPROVAL,     // (empresa) e-mail ok, aguardando aprovação
    ACTIVE,
    SUSPENDED
}
