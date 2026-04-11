import api from './axiosConfig';

const moduleService = {
    async getModules(formationId) {
        const response = await api.get(`/formations/${formationId}/modules`);
        return response.data;
    },

    async creerModule(formationId, data) {
        const response = await api.post(`/formations/${formationId}/modules`, data);
        return response.data;
    },

    async modifierModule(id, data) {
        const response = await api.put(`/modules/${id}`, data);
        return response.data;
    },

    async supprimerModule(id) {
        const response = await api.delete(`/modules/${id}`);
        return response.data;
    },

    async terminerModule(id) {
        const response = await api.post(`/modules/${id}/terminer`);
        return response.data;
    },

    /**
     * Récupère les IDs des modules déjà terminés par l'apprenant pour une formation.
     * GET /formations/:id/modules-termines
     */
    async getMesModulesTermines(formationId) {
        const response = await api.get(`/formations/${formationId}/modules-termines`);
        return response.data;
    },
};

export default moduleService;