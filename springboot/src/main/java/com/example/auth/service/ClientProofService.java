package com.example.auth.service;

import com.example.auth.dto.ClientProofRequest;
import com.example.auth.dto.ClientProofResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Service simulant le comportement du client TP3.
 *
 * Ce service construit :
 * - un nonce
 * - un timestamp
 * - le message canonique
 * - la signature HMAC
 *
 * @author Poun
 * @version 3.2
 */
@Service
public class ClientProofService {

    /**
     * Service HMAC.
     */
    private final HmacService hmacService;

    /**
     * Constructeur.
     *
     * @param hmacService service HMAC
     */
    public ClientProofService(HmacService hmacService) {
        this.hmacService = hmacService;
    }

    /**
     * Génère une preuve client complète.
     *
     * @param request données client
     * @return preuve complète à envoyer au serveur
     */
    public ClientProofResponse buildProof(ClientProofRequest request) {
        String nonce = UUID.randomUUID().toString();
        long timestamp = Instant.now().getEpochSecond();
        String message = hmacService.buildMessage(request.getEmail(), nonce, timestamp);
        String hmac = hmacService.hmacSha256(request.getPassword(), message);

        ClientProofResponse response = new ClientProofResponse();
        response.setEmail(request.getEmail());
        response.setNonce(nonce);
        response.setTimestamp(timestamp);
        response.setMessage(message);
        response.setHmac(hmac);

        return response;
    }
}