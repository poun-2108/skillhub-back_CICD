package com.example.auth.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

/**
 * Tests unitaires du EmailService.
 * Vérifie l'envoi d'email de vérification dans chaque branche.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8000");
    }

    // ── sendVerificationEmail avec mailSender disponible ─────────────────────

    @Test
    void envoieEmailQuandMailSenderPresent() {
        emailService.sendVerificationEmail("user@test.com", "token-abc");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        Assertions.assertNotNull(sent.getTo());
        Assertions.assertEquals("user@test.com", sent.getTo()[0]);
    }

    @Test
    void emailContientLienDeVerification() {
        emailService.sendVerificationEmail("user@test.com", "mon-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String text = captor.getValue().getText();
        Assertions.assertNotNull(text);
        Assertions.assertTrue(text.contains("mon-token"));
        Assertions.assertTrue(text.contains("http://localhost:8000"));
    }

    @Test
    void emailContientSujetAttendu() {
        emailService.sendVerificationEmail("user@test.com", "tok");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        Assertions.assertNotNull(captor.getValue().getSubject());
    }

    // ── sendVerificationEmail sans mailSender (mode dev) ─────────────────────

    @Test
    void nEnvoisPasEmailSiMailSenderNull() {
        EmailService devService = new EmailService();
        ReflectionTestUtils.setField(devService, "baseUrl", "http://localhost:8000");
        // mailSender reste null (non injecté)

        // Doit s'exécuter sans exception
        Assertions.assertDoesNotThrow(
                () -> devService.sendVerificationEmail("dev@test.com", "token-dev")
        );
    }

    // ── sendVerificationEmail — exception silencieuse ─────────────────────────

    @Test
    void ignoreExceptionLorsEnvoiEchoue() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Ne doit pas propager l'exception
        Assertions.assertDoesNotThrow(
                () -> emailService.sendVerificationEmail("user@test.com", "token-fail")
        );
    }
}
