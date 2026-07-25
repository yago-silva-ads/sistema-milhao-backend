package com.antigravity.assistente.auth;

import com.antigravity.assistente.profissional.Profissional;
import com.antigravity.assistente.profissional.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private ProfissionalRepository repository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * POST /auth/login
     * Angular envia: { "login": "email@...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        // O Angular auth.ts envia "login" e "password"
        String email = credentials.get("login");
        String senha = credentials.get("password");

        if (email != null && senha != null) {
            Optional<Profissional> profOpt = repository.findByEmail(email);
            
            if (profOpt.isPresent() && passwordEncoder.matches(senha, profOpt.get().getSenha())) {
                String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + UUID.randomUUID().toString();
                return ResponseEntity.ok(Map.of("token", token));
            }
        }

        return ResponseEntity.status(401).body(Map.of("erro", "Credenciais inválidas."));
    }
}
