package com.antigravity.assistente.usuario;

import com.antigravity.assistente.usuario.dto.UsuarioMapper;
import com.antigravity.assistente.usuario.dto.UsuarioRequest;
import com.antigravity.assistente.usuario.dto.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Camada de Serviço — Regras de Negócio para Usuários.
 * 
 * Agora opera com DTOs: recebe UsuarioRequest, retorna UsuarioResponse.
 * A entidade JPA nunca vaza para o Controller.
 * 
 * @author Antigravity Team
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;

    // Níveis de acesso permitidos no Sistema Milhão
    private static final List<String> NIVEIS_VALIDOS = List.of(
            "ADMIN", "GERENTE", "OPERADOR", "VISUALIZADOR"
    );

    // ---------- CREATE ----------

    /**
     * Cadastra um novo usuário no sistema.
     * 
     * @param request Dados de entrada validados
     * @return UsuarioResponse com dados persistidos
     * @throws IllegalArgumentException se e-mail já existir ou nível inválido
     */
    public UsuarioResponse cadastrar(UsuarioRequest request) {
        log.info("Cadastrando novo usuário: {}", request.email());

        // Regra 1: E-mail deve ser único
        if (usuarioRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw new IllegalArgumentException(
                    "Já existe um usuário cadastrado com o e-mail: " + request.email()
            );
        }

        // Regra 2: Nível de acesso deve ser válido
        validarNivelAcesso(request.nivelAcesso());

        // Converter DTO → Entidade e persistir
        Usuario usuario = mapper.toEntity(request);
        Usuario salvo = usuarioRepository.save(usuario);

        log.info("Usuário cadastrado com sucesso. ID: {}", salvo.getId());
        return mapper.toResponse(salvo);
    }

    // ---------- READ ----------

    /**
     * Retorna todos os usuários cadastrados.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return mapper.toResponseList(usuarioRepository.findAll());
    }

    /**
     * Busca um usuário pelo ID.
     * 
     * @param id Identificador do usuário
     * @return UsuarioResponse encontrado
     * @throws IllegalArgumentException se não encontrado
     */
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        return mapper.toResponse(usuario);
    }

    /**
     * Busca usuários por nível de acesso.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscarPorNivelAcesso(String nivelAcesso) {
        return mapper.toResponseList(
                usuarioRepository.findByNivelAcesso(nivelAcesso.toUpperCase())
        );
    }

    /**
     * Busca usuários cujo nome contenha o termo.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscarPorNome(String nome) {
        return mapper.toResponseList(
                usuarioRepository.findByNomeContainingIgnoreCase(nome)
        );
    }

    // ---------- UPDATE ----------

    /**
     * Atualiza os dados de um usuário existente.
     * 
     * @param id      ID do usuário a ser atualizado
     * @param request Novos dados
     * @return UsuarioResponse atualizado
     */
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        log.info("Atualizando usuário ID: {}", id);

        Usuario existente = buscarEntidadePorId(id);

        // Regra: Se o e-mail mudou, verificar unicidade
        String novoEmail = request.email().toLowerCase().trim();
        if (!existente.getEmail().equals(novoEmail)) {
            if (usuarioRepository.existsByEmail(novoEmail)) {
                throw new IllegalArgumentException(
                        "O e-mail informado já está em uso: " + request.email()
                );
            }
        }

        // Validar nível de acesso
        validarNivelAcesso(request.nivelAcesso());

        // Aplicar atualizações via Mapper
        mapper.updateEntity(existente, request);

        Usuario atualizado = usuarioRepository.save(existente);
        return mapper.toResponse(atualizado);
    }

    // ---------- DELETE ----------

    /**
     * Remove um usuário do sistema pelo ID.
     * 
     * @param id ID do usuário
     * @throws IllegalArgumentException se não encontrado
     */
    public void deletar(Long id) {
        log.info("Removendo usuário ID: {}", id);
        Usuario usuario = buscarEntidadePorId(id);
        usuarioRepository.delete(usuario);
        log.info("Usuário removido com sucesso. ID: {}", id);
    }

    // ---------- Métodos Internos ----------

    /**
     * Busca a entidade interna (não exposta ao Controller).
     */
    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado com ID: " + id
                ));
    }

    /**
     * Valida se o nível de acesso informado é permitido.
     */
    private void validarNivelAcesso(String nivelAcesso) {
        if (nivelAcesso == null || !NIVEIS_VALIDOS.contains(nivelAcesso.toUpperCase().trim())) {
            throw new IllegalArgumentException(
                    "Nível de acesso inválido: '" + nivelAcesso + "'. "
                    + "Valores permitidos: " + NIVEIS_VALIDOS
            );
        }
    }
}
