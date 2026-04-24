package com.example.auth.service;

import com.example.auth.AuthApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests du service de chiffrement AES/GCM des mots de passe.
 *
 * Couvre : encrypt, decrypt, cohérence encrypt→decrypt,
 * unicité des chiffrés (IV aléatoire), robustesse.
 *
 * @author Poun
 * @version 5.0
 */
@SpringBootTest(classes = AuthApplication.class)
@ActiveProfiles("test")
public class PasswordCryptoServiceTest {

    @Autowired
    private PasswordCryptoService passwordCryptoService;

    /**
     * Vérifie que encrypt retourne une valeur non nulle et non vide.
     */
    @Test
    void testEncryptNotNull() {
        String result = passwordCryptoService.encrypt("Azerty1234!@");
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isBlank());
    }

    /**
     * Vérifie que le chiffré ne contient pas le mot de passe en clair.
     */
    @Test
    void testEncryptDoesNotContainPlainText() {
        String plain = "Azerty1234!@";
        String encrypted = passwordCryptoService.encrypt(plain);
        Assertions.assertFalse(encrypted.contains(plain));
    }

    /**
     * Vérifie que decrypt(encrypt(x)) == x.
     */
    @Test
    void testDecryptRestoresOriginal() {
        String plain = "MonMotDePasse123!";
        String encrypted = passwordCryptoService.encrypt(plain);
        String decrypted = passwordCryptoService.decrypt(encrypted);
        Assertions.assertEquals(plain, decrypted);
    }

    /**
     * Vérifie que deux chiffrements du même texte donnent des résultats
     * différents (IV aléatoire à chaque appel).
     */
    @Test
    void testEncryptProducesDifferentCiphertextsForSameInput() {
        String plain = "Azerty1234!@";
        String enc1 = passwordCryptoService.encrypt(plain);
        String enc2 = passwordCryptoService.encrypt(plain);
        Assertions.assertNotEquals(enc1, enc2);
    }

    /**
     * Vérifie le chiffrement/déchiffrement avec des caractères spéciaux.
     */
    @Test
    void testEncryptDecryptWithSpecialChars() {
        String plain = "P@ss!w0rd#2024$";
        String encrypted = passwordCryptoService.encrypt(plain);
        String decrypted = passwordCryptoService.decrypt(encrypted);
        Assertions.assertEquals(plain, decrypted);
    }

    /**
     * Vérifie le chiffrement/déchiffrement d'une chaîne courte.
     */
    @Test
    void testEncryptDecryptShortPassword() {
        String plain = "Ab1!";
        String encrypted = passwordCryptoService.encrypt(plain);
        String decrypted = passwordCryptoService.decrypt(encrypted);
        Assertions.assertEquals(plain, decrypted);
    }

    /**
     * Vérifie que decrypt d'un texte invalide lève une RuntimeException.
     */
    @Test
    void testDecryptInvalidDataThrowsException() {
        Assertions.assertThrows(RuntimeException.class,
                () -> passwordCryptoService.decrypt("ceci-nest-pas-un-chiffre-valide!!!")
        );
    }

    /**
     * Vérifie le cycle complet avec un mot de passe très long.
     */
    @Test
    void testEncryptDecryptLongPassword() {
        String plain = "Aa1!".repeat(20); // 80 caractères
        String encrypted = passwordCryptoService.encrypt(plain);
        String decrypted = passwordCryptoService.decrypt(encrypted);
        Assertions.assertEquals(plain, decrypted);
    }
}