package com.example.auth.controller;

import com.example.auth.AuthApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Tests du HomeController.
 *
 * @author Poun
 * @version 5.0
 */
@SpringBootTest(classes = AuthApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Vérifie que la route racine retourne HTTP 200 et le message attendu.
     */
    @Test
    void testHomeReturns200() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/")
        ).andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    /**
     * Vérifie que le message retourné contient "Docker" ou "fonctionne".
     */
    @Test
    void testHomeReturnsExpectedMessage() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/")
        ).andReturn();

        String body = result.getResponse().getContentAsString();
        Assertions.assertTrue(
                body.contains("Docker") || body.contains("fonctionne"),
                "Le message de la route / doit contenir 'Docker' ou 'fonctionne'"
        );
    }
}