package com.example.auth.dto;

/**
 * DTO utilise pour demander un changement de mot de passe.
 *
 * Ce fichier contient :
 * - l'ancien mot de passe
 * - le nouveau mot de passe
 *
 * @author Poun
 * @version 5.0
 */
public class ChangePasswordRequest {

    /**
     * Ancien mot de passe saisi par l'utilisateur.
     */
    private String oldPassword;

    /**
     * Nouveau mot de passe saisi par l'utilisateur.
     */
    private String newPassword;

    /**
     * Retourne l'ancien mot de passe.
     *
     * @return ancien mot de passe
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * Modifie l'ancien mot de passe.
     *
     * @param oldPassword ancien mot de passe
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * Retourne le nouveau mot de passe.
     *
     * @return nouveau mot de passe
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Modifie le nouveau mot de passe.
     *
     * @param newPassword nouveau mot de passe
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}