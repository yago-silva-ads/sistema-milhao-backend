package com.antigravity.assistente.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade Usuario.
 * 
 * Spring Data JPA gera automaticamente a implementação CRUD
 * com conexão ao MySQL configurado em application.properties.
 * 
 * @author Antigravity Team
 * @version 1.0
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo e-mail (único no sistema).
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica se já existe um usuário com o e-mail informado.
     */
    boolean existsByEmail(String email);

    /**
     * Lista todos os usuários com determinado nível de acesso.
     */
    List<Usuario> findByNivelAcesso(String nivelAcesso);

    /**
     * Busca usuários cujo nome contenha o termo (case-insensitive).
     */
    List<Usuario> findByNomeContainingIgnoreCase(String nome);
}
