package com.antigravity.assistente.pagamento;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pagamento")
@CrossOrigin(origins = "*")
public class PagamentoController {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @PostMapping("/criar-assinatura")
    public ResponseEntity<?> criarAssinatura(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String nome = body.get("nome");

            MercadoPagoConfig.setAccessToken(accessToken);

            // Item da assinatura
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Acesso Sistema Milhão — Plano Premium")
                    .description("Assinatura mensal - Perfil visível + Vitrine + Agendamento")
                    .quantity(1)
                    .currencyId("BRL")
                    .unitPrice(new BigDecimal("29.90"))
                    .build();

            // Dados do pagador
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(email)
                    .build();

            // Criar a preferência
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .payer(payer)
                    .externalReference(email)
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Retorna a URL do checkout
            return ResponseEntity.ok(Map.of(
                    "init_point", preference.getInitPoint(),
                    "sandbox_init_point", preference.getSandboxInitPoint(),
                    "preference_id", preference.getId()
            ));

        } catch (MPApiException e) {
            System.err.println("❌ ERRO MERCADO PAGO API:");
            System.err.println("   Status: " + e.getStatusCode());
            System.err.println("   Message: " + e.getApiResponse().getContent());
            return ResponseEntity.status(500).body(Map.of(
                    "erro", "Erro MP: " + e.getApiResponse().getContent()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "erro", "Erro ao criar pagamento: " + e.getMessage()
            ));
        }
    }
}
