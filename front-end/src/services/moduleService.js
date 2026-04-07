import api from './axiosConfig';

const moduleService = {

    /**
     * Récupérer les modules d'une formation.
     * GET /formations/:id/modules
     */
    getModules: async (formationId) => {
        const response = await api.get(`/formations/${formationId}/modules`);
        return response.data;
    },

    /**
     * Créer un module dans une formation (formateur uniquement).
     * POST /formations/:id/modules
     */
    creerModule: async (formationId, data) => {
        const response = await api.post(`/formations/${formationId}/modules`, data);
        return response.data;
    },

    /**
     * Modifier un module (formateur propriétaire uniquement).
     * PUT /modules/:id
     */
    modifierModule: async (id, data) => {
        const response = await api.put(`/modules/${id}`, data);
        return response.data;
    },

    /**
     * Supprimer un module (formateur propriétaire uniquement).
     * DELETE /modules/:id
     */
    supprimerModule: async (id) => {
        const response = await api.delete(`/modules/${id}`);
        return response.data;
    },

    /**
     * Marquer un module comme terminé (apprenant inscrit uniquement).
     * POST /modules/:id/terminer
     */
    terminerModule: async (id) => {
        const response = await api.post(`/modules/${id}/terminer`);
        return response.data;
    },
};

export default moduleService;