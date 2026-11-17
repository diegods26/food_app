package br.com.diegopimenta.food_app.iam.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
        @NotBlank @Size(max = 120) String email,
        @NotBlank @Size(max = 128) String password,
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(max = 14) String cpf)
{
    @Override
    public String toString() {
        return "RegisterCustomerRequest[email=" + email + ", password=PROTECTED]";
    }
}
