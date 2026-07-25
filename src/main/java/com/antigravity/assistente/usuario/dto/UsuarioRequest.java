package com.antigravity.assistente.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada — dados recebidos do frontend para criar/atualizar um usuário.
 * 
 * Isola a camada de apresentação da entidade JPA,
 * garantindo que campos internos (id, criadoEm, etc.) nunca sejam manipulados externamente.
 * 
 * @author Antigravity Team
 * @version 1.0
 */
public record UsuarioRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "O nível de acesso é obrigatório")
        String nivelAcesso

) {}
