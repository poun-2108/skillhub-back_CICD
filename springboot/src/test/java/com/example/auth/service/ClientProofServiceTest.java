package com.example.auth.service;

import com.example.auth.dto.ClientProofRequest;
import com.example.auth.dto.ClientProofResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests du service de preuve client.
 *
 * @author Poun
 * @version 3.2
 */
public class ClientProofServiceTest {

    /**
     * Vérifie qu'une preuve complète est construite.
     */
    @Test
    void testBuildProofSuccess() {
        HmacService hmacService = new HmacService();
        ClientProofService clientProofService = new ClientProofService(hmacService);

        ClientProofRequest request = new ClientProofRequest();
        request.setEmail("poun@gmail.com");
        request.setPassword("Azerty1234!@");

        ClientProofResponse response = clientProofService.buildProof(request);

        Assertions.assertEquals("poun@gmail.com", response.getEmail());
        Assertions.assertNotNull(response.getNonce());
        Assertions.assertTrue(response.getTimestamp() > 0);
        Assertions.assertNotNull(response.getMessage());
        Assertions.assertNotNull(response.getHmac());
        Assertions.assertFalse(response.getHmac().isBlank());
    }

    /**
     * Vérifie que le message contient bien email, nonce et timestamp.
     */
    @Test
    void testBuildProofMessageContainsFields() {
        HmacService hmacService = new HmacService();
        ClientProofService clientProofService = new ClientProofService(hmacService);

        ClientProofRequest request = new ClientProofRequest();
        request.setEmail("poun@gmail.com");
        request.setPassword("Azerty1234!@");

        ClientProofResponse response = clientProofService.buildProof(request);

        Assertions.assertTrue(response.getMessage().startsWith("poun@gmail.com:"));
        Assertions.assertTrue(response.getMessage().contains(response.getNonce()));
        Assertions.assertTrue(response.getMessage().endsWith(String.valueOf(response.getTimestamp())));
    }
}