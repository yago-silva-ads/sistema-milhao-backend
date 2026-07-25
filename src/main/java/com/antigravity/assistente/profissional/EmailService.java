package com.antigravity.assistente.profissional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Serviço de envio de e-mail.
 * Em desenvolvimento: loga no console + tenta enviar via SMTP.
 * Se o SMTP falhar, o código ainda aparece no terminal.
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void enviarCodigoVerificacao(String destinatario, String codigo) {
        // Sempre loga no console (para testes em dev)
        System.out.println("========================================");
        System.out.println("📧 CÓDIGO DE VERIFICAÇÃO");
        System.out.println("   Para: " + destinatario);
        System.out.println("   Código: " + codigo);
        System.out.println("========================================");

        // Se tiver SMTP configurado, envia de verdade
        if (mailSender != null) {
            try {
                SimpleMailMessage mensagem = new SimpleMailMessage();
                mensagem.setTo(destinatario);
                mensagem.setSubject("Sistema Milhão - Código de Verificação");
                mensagem.setText("Olá!\n\nSeu código de verificação é: " + codigo + "\n\nUse este código para ativar sua conta.");
                mailSender.send(mensagem);
                System.out.println("✅ E-mail enviado com sucesso!");
            } catch (Exception e) {
                System.out.println("⚠️ SMTP não configurado. Use o código do console acima.");
            }
        }
    }
}
