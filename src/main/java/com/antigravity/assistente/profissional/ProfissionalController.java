package com.antigravity.assistente.profissional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    @Autowired
    private ProfissionalRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrar(@RequestBody Profissional profissional) {
        if (repository.findByEmail(profissional.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("E-mail já está em uso.");
        }

        // Se o frontend não mandou senha, gera uma senha padrão temporária
        if (profissional.getSenha() == null || profissional.getSenha().isEmpty()) {
            profissional.setSenha(passwordEncoder.encode("123456"));
        } else {
            profissional.setSenha(passwordEncoder.encode(profissional.getSenha()));
        }

        // Gera código de verificação de 6 dígitos
        String codigo = String.valueOf(100000 + (int)(Math.random() * 900000));
        profissional.setCodigoVerificacao(codigo);

        // Status inicial
        if (profissional.getStatusPagamento() == null) {
            profissional.setStatusPagamento("ATIVO");
        }

        Profissional salvo = repository.save(profissional);

        // Envia código por email
        try {
            emailService.enviarCodigoVerificacao(salvo.getEmail(), codigo);
        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
        }

        return ResponseEntity.ok(salvo);
    }

    @PostMapping("/verificar")
    public ResponseEntity<?> verificar(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String codigo = payload.get("codigo");

        if (email == null || codigo == null) {
            return ResponseEntity.badRequest().body("E-mail e código são obrigatórios.");
        }

        Optional<Profissional> prof = repository.findByEmail(email);

        if (prof.isPresent() && codigo.equals(prof.get().getCodigoVerificacao())) {
            Profissional p = prof.get();
            p.setStatusPagamento("ATIVO");
            p.setCodigoVerificacao(null);
            repository.save(p);
            return ResponseEntity.ok(Map.of("mensagem", "Conta verificada com sucesso!"));
        }
        return ResponseEntity.badRequest().body(Map.of("erro", "E-mail ou código inválido."));
    }

    /**
     * POST /profissionais/esqueci-senha
     * Envia código de reset por email
     */
    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> esqueciSenha(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "E-mail é obrigatório."));
        }

        Optional<Profissional> profOpt = repository.findByEmail(email);
        if (profOpt.isEmpty()) {
            // Não revelamos se o email existe ou não (segurança)
            return ResponseEntity.ok(Map.of("mensagem", "Se o e-mail existir, você receberá um código de recuperação."));
        }

        Profissional p = profOpt.get();
        String codigo = String.valueOf(100000 + (int)(Math.random() * 900000));
        p.setCodigoVerificacao(codigo);
        repository.save(p);

        try {
            emailService.enviarCodigoVerificacao(email, codigo);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email de reset: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("mensagem", "Se o e-mail existir, você receberá um código de recuperação."));
    }

    /**
     * POST /profissionais/resetar-senha
     * Valida código e atualiza a senha
     */
    @PostMapping("/resetar-senha")
    public ResponseEntity<?> resetarSenha(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String codigo = payload.get("codigo");
        String novaSenha = payload.get("novaSenha");

        if (email == null || codigo == null || novaSenha == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Todos os campos são obrigatórios."));
        }

        Optional<Profissional> profOpt = repository.findByEmail(email);
        if (profOpt.isPresent() && codigo.equals(profOpt.get().getCodigoVerificacao())) {
            Profissional p = profOpt.get();
            p.setSenha(passwordEncoder.encode(novaSenha));
            p.setCodigoVerificacao(null);
            repository.save(p);
            return ResponseEntity.ok(Map.of("mensagem", "Senha atualizada com sucesso!"));
        }

        return ResponseEntity.badRequest().body(Map.of("erro", "Código inválido ou expirado."));
    }

    @GetMapping
    public ResponseEntity<List<Profissional>> listarTodos() {
        return ResponseEntity.ok(repository.findAll());
    }
}
