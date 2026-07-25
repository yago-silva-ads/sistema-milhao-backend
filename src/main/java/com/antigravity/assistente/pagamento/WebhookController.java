package com.antigravity.assistente.pagamento;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.antigravity.assistente.profissional.Profissional;
import com.antigravity.assistente.profissional.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/webhook")
@CrossOrigin(origins = "*")
public class WebhookController {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @PostMapping("/mercadopago")
    public ResponseEntity<?> receberNotificacao(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestBody(required = false) Map<String, Object> body) {

        System.out.println("🔔 WEBHOOK MERCADO PAGO RECEBIDO!");
        System.out.println("   Type: " + type);
        System.out.println("   Data ID: " + dataId);
        System.out.println("   Body: " + body);

        try {
            if ("payment".equals(type) && dataId != null) {
                MercadoPagoConfig.setAccessToken(accessToken);

                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(Long.parseLong(dataId));

                System.out.println("   Status: " + payment.getStatus());
                System.out.println("   Referência externa: " + payment.getExternalReference());

                // Se o pagamento foi APROVADO
                if ("approved".equals(payment.getStatus())) {
                    String emailReferencia = payment.getExternalReference();

                    if (emailReferencia != null) {
                        Optional<Profissional> profissional = profissionalRepository.findByEmail(emailReferencia);

                        if (profissional.isPresent()) {
                            Profissional p = profissional.get();
                            p.setStatusPagamento("ATIVO");
                            profissionalRepository.save(p);
                            System.out.println("   ✅ CONTA ATIVADA: " + emailReferencia);
                        } else {
                            System.out.println("   ⚠️ Profissional não encontrado: " + emailReferencia);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("   ❌ Erro no webhook: " + e.getMessage());
            e.printStackTrace();
        }

        // SEMPRE retorna 200 para o Mercado Pago
        return ResponseEntity.ok().build();
    }
}
