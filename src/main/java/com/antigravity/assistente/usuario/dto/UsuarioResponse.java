package com.antigravity.assistente.usuario.dto;

import java.time.LocalDateTime;

/**
 * DTO de saída — dados retornados ao frontend.
 * 
 * Expõe apenas os campos seguros e relevantes para a interface,
 * sem vazamento de detalhes internos da entidade.
 * 
 * @author Antigravity Team
 * @version 1.0
 */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String nivelAcesso,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {}
