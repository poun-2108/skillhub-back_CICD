import api from './axiosConfig';

const inscriptionService = {
    /**
     * Inscrire l'apprenant connecté à une formation.
     * POST /formations/:id/inscription
     */
    sInscrire: async (formationId) => {
        const response = await api.post(`/formations/${formationId}/inscription`);
        return response.data;
    },

    /**
     * Désinscrire l'apprenant connecté d'une formation.
     * DELETE /formations/:id/inscription
     */
    seDesinscrire: async (formationId) => {
        const response = await api.delete(`/formations/${formationId}/inscription`);
        return response.data;
    },

    /**
     * Récupérer les formations suivies par l'apprenant connecté.
     * GET /apprenant/formations
     *
     * Le backend renvoie un objet :
     * {
     *   message: "...",
     *   inscriptions: [...]
     * }
     *
     * On retourne directement le tableau pour le dashboard.
     */
    mesFormations: async () => {
        const response = await api.get('/apprenant/formations');
        return response.data.inscriptions || [];
    },
};

export default inscriptionService;