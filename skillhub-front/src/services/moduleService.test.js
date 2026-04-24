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

import moduleService from "./moduleService";

describe("moduleService", () => {
    beforeEach(() => {
        apiMock.get.mockReset();
        apiMock.post.mockReset();
        apiMock.put.mockReset();
        apiMock.delete.mockReset();
    });

    it("getModules", async () => {
        apiMock.get.mockResolvedValue({ data: [] });
        await moduleService.getModules(10);
        expect(apiMock.get).toHaveBeenCalledWith("/formations/10/modules");
    });

    it("creerModule", async () => {
        apiMock.post.mockResolvedValue({ data: {} });
        await moduleService.creerModule(12, { titre: "M1" });
        expect(apiMock.post).toHaveBeenCalledWith("/formations/12/modules", { titre: "M1" });
    });

    it("modifierModule", async () => {
        apiMock.put.mockResolvedValue({ data: {} });
        await moduleService.modifierModule(2, { titre: "M2" });
        expect(apiMock.put).toHaveBeenCalledWith("/modules/2", { titre: "M2" });
    });

    it("supprimerModule", async () => {
        apiMock.delete.mockResolvedValue({ data: {} });
        await moduleService.supprimerModule(2);
        expect(apiMock.delete).toHaveBeenCalledWith("/modules/2");
    });

    it("terminerModule", async () => {
        apiMock.post.mockResolvedValue({ data: {} });
        await moduleService.terminerModule(3);
        expect(apiMock.post).toHaveBeenCalledWith("/modules/3/terminer");
    });

    it("getMesModulesTermines", async () => {
        apiMock.get.mockResolvedValue({ data: {} });
        await moduleService.getMesModulesTermines(14);
        expect(apiMock.get).toHaveBeenCalledWith("/formations/14/modules-termines");
    });
});

