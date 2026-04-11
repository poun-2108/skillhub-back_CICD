import api from './axiosConfig';

const formationService = {

    /**
     * Récupérer toutes les formations avec filtres optionnels.
     * GET /formations
     */
    getFormations: async (filtres = {}) => {
        const response = await api.get('/formations', { params: filtres });
        return response.data;
    },

    /**
     * Récupérer le détail d'une formation.
     * GET /formations/:id
     */
    getFormation: async (id) => {
        const response = await api.get(`/formations/${id}`);
        return response.data;
    },

    /**
     * Créer une nouvelle formation (formateur uniquement).
     * POST /formations
     */
    creerFormation: async (data) => {
        const response = await api.post('/formations', data);
        return response.data;
    },

    /**
     * Modifier une formation existante (formateur propriétaire uniquement).
     * PUT /formations/:id
     */
    modifierFormation: async (id, data) => {
        const response = await api.put(`/formations/${id}`, data);
        return response.data;
    },

    /**
     * Supprimer une formation (formateur propriétaire uniquement).
     * DELETE /formations/:id
     */
    supprimerFormation: async (id) => {
        const response = await api.delete(`/formations/${id}`);
        return response.data;
    },

    /**
     * Récupérer uniquement les formations du formateur connecté.
     * GET /formateur/mes-formations
     */
    getMesFormations: async () => {
        const response = await api.get('/formateur/mes-formations');
        return response.data;
    },
};

export default formationService;