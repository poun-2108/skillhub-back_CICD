import api from './axiosConfig';

/**
 * Normalise l'utilisateur reçu du backend.
 * Le backend Laravel renvoie surtout "nom", mais certaines pages utilisent aussi "name".
 *
 * @param {object|null} user
 * @returns {object|null}
 */
function normaliserUtilisateur(user) {
    if (!user) {
        return null;
    }

    return {
        ...user,
        nom: user.nom || user.name || '',
        name: user.name || user.nom || '',
    };
}

const authService = {
    /**
     * Inscription utilisateur.
     *
     * @param {string} nom
     * @param {string} email
     * @param {string} password
     * @param {string} role
     * @returns {Promise<object>}
     */
    async register(nom, email, password, passwordConfirmation, role) {
        const payload = {
            nom,
            email,
            password,
            password_confirmation: passwordConfirmation,
            role
        };

        const reponse = await api.post('/register', payload);

        const utilisateurNormalise = normaliserUtilisateur(reponse.data.user);

        if (reponse.data.token) {
            localStorage.setItem('token', reponse.data.token);
        }

        if (utilisateurNormalise) {
            localStorage.setItem('utilisateur', JSON.stringify(utilisateurNormalise));
        }

        return {
            ...reponse.data,
            user: utilisateurNormalise
        };
    },

    /**
     * Connexion utilisateur.
     *
     * @param {string} email
     * @param {string} password
     * @returns {Promise<object>}
     */
    async login(email, password) {
        const payload = {
            email,
            password
        };

        const reponse = await api.post('/login', payload);

        const utilisateurNormalise = normaliserUtilisateur(reponse.data.user);

        if (reponse.data.token) {
            localStorage.setItem('token', reponse.data.token);
        }

        if (utilisateurNormalise) {
            localStorage.setItem('utilisateur', JSON.stringify(utilisateurNormalise));
        }

        return {
            ...reponse.data,
            user: utilisateurNormalise
        };
    },

    /**
     * Profil utilisateur connecté.
     *
     * @returns {Promise<object>}
     */
    async profile() {
        const reponse = await api.get('/profile');

        const utilisateurNormalise = normaliserUtilisateur(reponse.data.user);

        if (utilisateurNormalise) {
            localStorage.setItem('utilisateur', JSON.stringify(utilisateurNormalise));
        }

        return {
            ...reponse.data,
            user: utilisateurNormalise
        };
    },

    /**
     * Déconnexion utilisateur.
     *
     * @returns {Promise<object>}
     */
    async logout() {
        const reponse = await api.post('/logout', {});

        localStorage.removeItem('token');
        localStorage.removeItem('utilisateur');

        return reponse.data;
    },

    /**
     * Upload photo de profil.
     *
     * @param {File} fichier
     * @returns {Promise<object>}
     */
    async uploadPhoto(fichier) {
        const formData = new FormData();
        formData.append('photo', fichier);

        const reponse = await api.post('/profil/photo', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });

        const utilisateurNormalise = normaliserUtilisateur(reponse.data.user);

        if (utilisateurNormalise) {
            localStorage.setItem('utilisateur', JSON.stringify(utilisateurNormalise));
        }

        return {
            ...reponse.data,
            user: utilisateurNormalise
        };
    },

    /**
     * Retourne l'utilisateur local.
     *
     * @returns {object|null}
     */
    getUtilisateur() {
        const utilisateur = localStorage.getItem('utilisateur');

        if (!utilisateur) {
            return null;
        }

        try {
            return JSON.parse(utilisateur);
        } catch (error) {
            return null;
        }
    },

    /**
     * Retourne le token local.
     *
     * @returns {string|null}
     */
    getToken() {
        return localStorage.getItem('token');
    },

    /**
     * Vérifie si un token existe.
     *
     * @returns {boolean}
     */
    estConnecte() {
        return !!localStorage.getItem('token');
    },

    /**
     * Nettoyage local.
     */
    clear() {
        localStorage.removeItem('token');
        localStorage.removeItem('utilisateur');
    }
};

export default authService;