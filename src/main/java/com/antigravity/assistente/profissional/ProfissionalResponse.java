package com.antigravity.assistente.profissional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO (Data Transfer Object) — retorna dados SEM a senha.
 * NUNCA retorne a Entity Profissional diretamente pro frontend.
 */
public record ProfissionalResponse(
        Long id,
        String nome,
        String email,
        String servico,
        Double preco,
        String linkWhatsapp,
        String statusPagamento
) {
    /**
     * Converte Entity → DTO (esconde senha e código de verificação)
     */
    public static ProfissionalResponse fromEntity(Profissional p) {
        return new ProfissionalResponse(
                p.getId(),
                p.getNome(),
                p.getEmail(),
                p.getServico(),
                p.getPreco(),
                p.getLinkWhatsapp(),
                p.getStatusPagamento()
        );
    }

    public static List<ProfissionalResponse> fromList(List<Profissional> profissionais) {
        return profissionais.stream()
                .map(ProfissionalResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
