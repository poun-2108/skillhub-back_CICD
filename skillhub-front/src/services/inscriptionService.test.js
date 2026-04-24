import { describe, expect, it, vi, beforeEach } from "vitest";

const { apiMock } = vi.hoisted(() => ({
    apiMock: {
        get: vi.fn(),
        post: vi.fn(),
        delete: vi.fn(),
    },
}));

vi.mock("./axiosConfig", () => ({
    default: apiMock,
}));

import inscriptionService from "./inscriptionService";

describe("inscriptionService", () => {
    beforeEach(() => {
        apiMock.get.mockReset();
        apiMock.post.mockReset();
        apiMock.delete.mockReset();
    });

    it("sInscrire", async () => {
        apiMock.post.mockResolvedValue({ data: { ok: true } });
        const result = await inscriptionService.sInscrire(5);
        expect(apiMock.post).toHaveBeenCalledWith("/formations/5/inscription");
        expect(result).toEqual({ ok: true });
    });

    it("seDesinscrire", async () => {
        apiMock.delete.mockResolvedValue({ data: { ok: true } });
        const result = await inscriptionService.seDesinscrire(5);
        expect(apiMock.delete).toHaveBeenCalledWith("/formations/5/inscription");
        expect(result).toEqual({ ok: true });
    });

    it("mesFormations retourne inscriptions", async () => {
        apiMock.get.mockResolvedValue({ data: { inscriptions: [{ id: 1 }] } });
        const result = await inscriptionService.mesFormations();
        expect(apiMock.get).toHaveBeenCalledWith("/apprenant/formations");
        expect(result).toEqual([{ id: 1 }]);
    });

    it("mesFormations retourne tableau vide par defaut", async () => {
        apiMock.get.mockResolvedValue({ data: {} });
        const result = await inscriptionService.mesFormations();
        expect(result).toEqual([]);
    });
});

