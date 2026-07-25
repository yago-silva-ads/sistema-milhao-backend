package com.antigravity.assistente.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (qualquer um pode acessar)
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/profissionais/cadastrar").permitAll()
                .requestMatchers("/profissionais/verificar").permitAll()
                .requestMatchers("/profissionais/esqueci-senha").permitAll()
                .requestMatchers("/profissionais/resetar-senha").permitAll()
                .requestMatchers("/pagamento/**").permitAll()
                .requestMatchers("/webhook/**").permitAll()
                // Todos os outros precisam de autenticação (futuro JWT)
                .anyRequest().permitAll() // TODO: mudar para .authenticated() quando JWT estiver pronto
            );
        return http.build();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // SÓ aceita requests desses domínios (bloqueia qualquer outro)
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",                          // Dev local
                "https://sistema-milhao.vercel.app",             // Produção Vercel
                "https://sistema-milhao-git-master-bxtnaldo0z-7450s-projects.vercel.app" // Preview
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
