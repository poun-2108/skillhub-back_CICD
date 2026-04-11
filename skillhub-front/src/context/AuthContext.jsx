import { createContext, useContext, useState } from "react";
import authService from "../services/authService";

/**
 * Contexte d'authentification global.
 */
const AuthContext = createContext(null);

/**
 * Provider d'authentification.
 *
 * @param {object} props props React
 * @returns {JSX.Element}
 */
export function AuthProvider({ children }) {
    const [utilisateur, setUtilisateur] = useState(() => {
        // Si le token est absent, on ne charge pas l'utilisateur (état incohérent)
        const token = localStorage.getItem('token');
        if (!token) {
            localStorage.removeItem('utilisateur');
            return null;
        }
        return authService.getUtilisateur();
    });

    /**
     * Connexion utilisateur.
     *
     * @param {string} email email
     * @param {string} password mot de passe
     * @returns {Promise<object>}
     */
    const login = async (email, password) => {
        const data = await authService.login(email, password);
        setUtilisateur(data.user);
        return data;
    };

    /**
     * Inscription utilisateur.
     *
     * @param {string} nom nom
     * @param {string} email email
     * @param {string} password mot de passe
     * @param {string} role role
     * @returns {Promise<object>}
     */
    const register = async (nom, email, password, passwordConfirmation, role) => {
        const data = await authService.register(nom, email, password, passwordConfirmation, role);
        setUtilisateur(data.user);
        return data;
    };

    /**
     * Déconnexion.
     *
     * @returns {Promise<void>}
     */
    const logout = async () => {
        await authService.logout();
        setUtilisateur(null);
    };

    /**
     * Vérifie si un utilisateur est connecté.
     *
     * @returns {boolean}
     */
    const estConnecte = () => utilisateur !== null;

    /**
     * Vérifie si l'utilisateur est formateur.
     *
     * @returns {boolean}
     */
    const estFormateur = () =>
        utilisateur !== null && utilisateur.role === "formateur";

    /**
     * Vérifie si l'utilisateur est apprenant.
     *
     * @returns {boolean}
     */
    const estApprenant = () =>
        utilisateur !== null && utilisateur.role === "apprenant";

    const valeur = {
        utilisateur,
        setUtilisateur,
        login,
        register,
        logout,
        estConnecte,
        estFormateur,
        estApprenant
    };

    return (
        <AuthContext.Provider value={valeur}>
            {children}
        </AuthContext.Provider>
    );
}

/**
 * Hook pour utiliser le contexte auth.
 *
 * @returns {object}
 */
export function useAuth() {
    return useContext(AuthContext);
}