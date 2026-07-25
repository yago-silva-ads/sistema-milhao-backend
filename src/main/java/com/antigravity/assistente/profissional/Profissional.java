package com.antigravity.assistente.profissional;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "profissionais")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String servico;

    @Column(nullable = false)
    private Double preco;

    @Column(name = "link_whatsapp")
    private String linkWhatsapp;

    @Column(name = "status_pagamento")
    private String statusPagamento = "PENDENTE";

    @Column(name = "codigo_verificacao")
    private String codigoVerificacao;
}
