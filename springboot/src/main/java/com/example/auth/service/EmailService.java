package com.example.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service d envoi d emails de confirmation.
 *
 * @author Nirina
 * @version 1.1
 */
@Service
public class EmailService {

    @Value("${app.base.url:http://localhost:8000}")
    private String baseUrl;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Envoie un email de confirmation.
     * Si mailSender est null ou erreur, ignore silencieusement.
     *
     * @param email email destinataire
     * @param token token de verification
     */
    public void sendVerificationEmail(String email, String token) {
        try {
            if (mailSender == null) {
                System.out.println("Mode dev : email non envoye a " + email);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Verification de votre compte Moustass CloudSec");
            message.setText("Cliquez sur ce lien pour verifier votre compte : "
                    + baseUrl + "/api/auth/verify-email?token=" + token);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Email non envoye (mode dev) : " + e.getMessage());
        }
    }
}