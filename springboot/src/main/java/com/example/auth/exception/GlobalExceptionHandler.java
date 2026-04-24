package com.example.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Gere les exceptions globales de l'application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gere les erreurs simples de type RuntimeException.
     *
     * @param ex exception recue
     * @return message d'erreur
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRuntimeException(RuntimeException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("error", ex.getMessage());

        return response;
    }
}