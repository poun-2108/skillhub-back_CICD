import axios from "axios";

/**
 * Client Axios pour le backend Laravel (métier SkillHub).
 */
const api = axios.create({
    baseURL: "http://localhost:8001/api",
    headers: {
        "Content-Type": "application/json",
    },
});

/**
 * Ajoute automatiquement le token dans les requêtes.
 */
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

/**
 * Gestion des réponses.
 *
 * Si le token est absent ou expiré (401), on nettoie localStorage
 * pour resynchroniser l'état React avec la réalité.
 */
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;

        // Journaliser uniquement en développement pour ne pas exposer les données en production
        if (import.meta.env.DEV) {
            console.error("Erreur API Laravel :", status, error.response?.data);
        }

        // Token absent ou expiré : nettoyer la session locale
        if (status === 401) {
            const tokenExistait = !!localStorage.getItem('token');
            if (tokenExistait) {
                localStorage.removeItem('token');
                localStorage.removeItem('utilisateur');
                // Recharger la page pour remettre le contexte Auth à zéro
                window.location.href = '/';
            }
        }

        return Promise.reject(error);
    }
);

export default api;