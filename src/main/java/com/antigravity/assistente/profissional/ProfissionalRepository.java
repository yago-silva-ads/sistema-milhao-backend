package com.antigravity.assistente.profissional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    @Query("SELECT p FROM Profissional p WHERE p.email = ?1 ORDER BY p.id DESC")
    List<Profissional> findAllByEmail(String email);

    default Optional<Profissional> findByEmail(String email) {
        List<Profissional> results = findAllByEmail(email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
