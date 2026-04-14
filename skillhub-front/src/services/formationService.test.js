import { describe, expect, it, vi, beforeEach } from "vitest";

const { apiMock } = vi.hoisted(() => ({
    apiMock: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}));

vi.mock("./axiosConfig", () => ({
    default: apiMock,
}));

import formationService from "./formationService";

describe("formationService", () => {
    beforeEach(() => {
        apiMock.get.mockReset();
        apiMock.post.mockReset();
        apiMock.put.mockReset();
        apiMock.delete.mockReset();
    });

    it("getFormations transmet les filtres", async () => {
        apiMock.get.mockResolvedValue({ data: [{ id: 1 }] });
        const data = await formationService.getFormations({ niveau: "debutant" });

        expect(apiMock.get).toHaveBeenCalledWith("/formations", { params: { niveau: "debutant" } });
        expect(data).toEqual([{ id: 1 }]);
    });

    it("getFormation recupere une formation", async () => {
        apiMock.get.mockResolvedValue({ data: { id: 2 } });
        await formationService.getFormation(2);
        expect(apiMock.get).toHaveBeenCalledWith("/formations/2");
    });

    it("creerFormation poste les donnees", async () => {
        apiMock.post.mockResolvedValue({ data: { ok: true } });
        const payload = { titre: "React" };
        await formationService.creerFormation(payload);
        expect(apiMock.post).toHaveBeenCalledWith("/formations", payload);
    });

    it("modifierFormation envoie un put", async () => {
        apiMock.put.mockResolvedValue({ data: { ok: true } });
        await formationService.modifierFormation(3, { titre: "JS" });
        expect(apiMock.put).toHaveBeenCalledWith("/formations/3", { titre: "JS" });
    });

    it("supprimerFormation envoie un delete", async () => {
        apiMock.delete.mockResolvedValue({ data: { ok: true } });
        await formationService.supprimerFormation(4);
        expect(apiMock.delete).toHaveBeenCalledWith("/formations/4");
    });

    it("getMesFormations appelle l endpoint protege", async () => {
        apiMock.get.mockResolvedValue({ data: [] });
        await formationService.getMesFormations();
        expect(apiMock.get).toHaveBeenCalledWith("/formateur/mes-formations");
    });
});

