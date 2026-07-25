package com.antigravity.assistente.usuario.dto;

import com.antigravity.assistente.usuario.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper — conversão bidirecional entre Usuario (entidade) e DTOs.
 * 
 * Centraliza toda a lógica de transformação, mantendo Controller
 * e Service desacoplados da representação externa.
 * 
 * @author Antigravity Team
 * @version 1.0
 */
@Component
public class UsuarioMapper {

    /**
     * Converte um UsuarioRequest (entrada do frontend) para a entidade JPA.
     */
    public Usuario toEntity(UsuarioRequest request) {
        return Usuario.builder()
                .nome(request.nome().trim())
                .email(request.email().toLowerCase().trim())
                .nivelAcesso(request.nivelAcesso().toUpperCase().trim())
                .build();
    }

    /**
     * Atualiza uma entidade existente com os dados do request,
     * sem alterar id, criadoEm ou outros campos de auditoria.
     */
    public void updateEntity(Usuario existente, UsuarioRequest request) {
        existente.setNome(request.nome().trim());
        existente.setEmail(request.email().toLowerCase().trim());
        existente.setNivelAcesso(request.nivelAcesso().toUpperCase().trim());
    }

    /**
     * Converte a entidade JPA para o DTO de resposta.
     */
    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getNivelAcesso(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm()
        );
    }

    /**
     * Converte uma lista de entidades para lista de DTOs de resposta.
     */
    public List<UsuarioResponse> toResponseList(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(this::toResponse)
                .toList();
    }
}
