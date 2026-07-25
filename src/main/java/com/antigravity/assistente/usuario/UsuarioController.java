package com.antigravity.assistente.usuario;

import com.antigravity.assistente.usuario.dto.UsuarioRequest;
import com.antigravity.assistente.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST — Endpoints de Gestão de Usuários.
 * 
 * Agora utiliza DTOs: recebe UsuarioRequest e retorna UsuarioResponse.
 * A entidade JPA nunca transita pela rede.
 * 
 * Base URL: /api/v1/usuarios
 * 
 * @author Antigravity Team
 * @version 2.0
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:5173"})
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ==================== GET ====================

    /**
     * Lista todos os usuários cadastrados.
     * GET /api/v1/usuarios
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Busca um usuário específico pelo ID.
     * GET /api/v1/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    /**
     * Busca usuários pelo nome (parcial, case-insensitive).
     * GET /api/v1/usuarios/buscar?nome=João
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponse>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(usuarioService.buscarPorNome(nome));
    }

    /**
     * Filtra usuários por nível de acesso.
     * GET /api/v1/usuarios/nivel/{nivelAcesso}
     */
    @GetMapping("/nivel/{nivelAcesso}")
    public ResponseEntity<List<UsuarioResponse>> buscarPorNivel(@PathVariable String nivelAcesso) {
        return ResponseEntity.ok(usuarioService.buscarPorNivelAcesso(nivelAcesso));
    }

    // ==================== POST ====================

    /**
     * Cadastra um novo usuário no sistema.
     * POST /api/v1/usuarios
     * Body: { "nome": "...", "email": "...", "nivelAcesso": "OPERADOR" }
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse criado = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    // ==================== PUT ====================

    /**
     * Atualiza os dados de um usuário existente.
     * PUT /api/v1/usuarios/{id}
     * Body: { "nome": "...", "email": "...", "nivelAcesso": "GERENTE" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    // ==================== DELETE ====================

    /**
     * Remove um usuário do sistema.
     * DELETE /api/v1/usuarios/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Usuário removido com sucesso",
                "id", id.toString()
        ));
    }

    // ==================== Exception Handlers ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
    }
}
