import { beforeEach, describe, expect, it, vi } from "vitest";

const hoisted = vi.hoisted(() => {
    const state = {
        requestOk: null,
        requestErr: null,
        responseOk: null,
        responseErr: null,
    };

    const apiMock = {
        interceptors: {
            request: {
                use: vi.fn((onFulfilled, onRejected) => {
                    state.requestOk = onFulfilled;
                    state.requestErr = onRejected;
                }),
            },
            response: {
                use: vi.fn((onFulfilled, onRejected) => {
                    state.responseOk = onFulfilled;
                    state.responseErr = onRejected;
                }),
            },
        },
    };

    return {
        state,
        apiMock,
        createMock: vi.fn(() => apiMock),
    };
});

vi.mock("axios", () => ({
    default: {
        create: hoisted.createMock,
    },
}));

import api from "./axiosConfig";

describe("axiosConfig", () => {
    beforeEach(() => {
        localStorage.clear();
    });

    it("cree une instance axios avec la baseURL attendue", () => {
        expect(api).toBe(hoisted.apiMock);
        expect(hoisted.createMock).toHaveBeenCalledWith({
            baseURL: "http://localhost:8001/api",
            headers: {
                "Content-Type": "application/json",
            },
        });
    });

    it("ajoute Authorization quand un token est present", async () => {
        localStorage.setItem("token", "jwt-token");

        const config = { headers: {} };
        const result = await hoisted.state.requestOk(config);

        expect(result.headers.Authorization).toBe("Bearer jwt-token");
    });

    it("laisse la requete intacte sans token", async () => {
        const config = { headers: {} };
        const result = await hoisted.state.requestOk(config);

        expect(result.headers.Authorization).toBeUndefined();
    });

    it("propage l'erreur du request interceptor", async () => {
        const error = new Error("request failed");
        await expect(hoisted.state.requestErr(error)).rejects.toBe(error);
    });

    it("retourne directement la reponse en succes", () => {
        const payload = { data: { ok: true } };
        expect(hoisted.state.responseOk(payload)).toBe(payload);
    });

    it("nettoie la session sur erreur 401", async () => {
        localStorage.setItem("token", "t");
        localStorage.setItem("utilisateur", "{}");

        const error = { response: { status: 401, data: { message: "expired" } } };

        await expect(hoisted.state.responseErr(error)).rejects.toEqual(error);
        expect(localStorage.getItem("token")).toBeNull();
        expect(localStorage.getItem("utilisateur")).toBeNull();
    });

    it("ne redirige pas sur 401 quand aucun token n'existe", async () => {
        const error = { response: { status: 401 } };

        await expect(hoisted.state.responseErr(error)).rejects.toEqual(error);
        expect(localStorage.getItem("token")).toBeNull();
    });

    it("ne nettoie pas sur erreur hors 401", async () => {
        localStorage.setItem("token", "still-there");

        const error = { response: { status: 500 } };
        await expect(hoisted.state.responseErr(error)).rejects.toEqual(error);

        expect(localStorage.getItem("token")).toBe("still-there");
    });
});

