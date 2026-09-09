package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "O nome completo é obrigatório")
        @Size(min = 3, max = 255, message = "O nome deve conter entre 3 e 255 caracteres")
        String fullName,

        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        @Pattern(regexp = "^\\d{11}$|^\\d{14}$", message = "O documento deve conter 11 dígitos (CPF) ou 14 dígitos (CNPJ), apenas números")
        String documentNumber,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email
) {
}
