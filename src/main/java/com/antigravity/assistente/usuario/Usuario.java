package com.antigravity.assistente.usuario;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade Usuario - Sistema Milhão
 * 
 * Representa um usuário do sistema com controle de nível de acesso.
 * Mapeada para a tabela 'usuarios' no MySQL.
 * 
 * @author Antigravity Team
 * @version 1.0
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Column(nullable = false, unique = true, length = 200)
    private String email;

    /**
     * Nível de acesso do usuário no sistema.
     * Valores possíveis: ADMIN, GERENTE, OPERADOR, VISUALIZADOR
     */
    @NotBlank(message = "O nível de acesso é obrigatório")
    @Column(name = "nivel_acesso", nullable = false, length = 50)
    private String nivelAcesso;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // ---------- Lifecycle Hooks ----------

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
