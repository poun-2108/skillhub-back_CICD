import { beforeEach, describe, expect, it, vi } from "vitest";

let requestOk;
let responseOk;
let responseErr;

const apiMock = {
    interceptors: {
        request: {
            use: vi.fn((onFulfilled) => {
                requestOk = onFulfilled;
            }),
        },
        response: {
            use: vi.fn((onFulfilled, onRejected) => {
                responseOk = onFulfilled;
                responseErr = onRejected;
            }),
        },
    },
};

const createMock = vi.fn(() => apiMock);

vi.mock("axios", () => ({
    default: {
        create: createMock,
    },
}));

import api from "./axiosConfig";

describe("axiosConfig", () => {
    beforeEach(() => {
        localStorage.clear();
        createMock.mockClear();
    });

    it("cree une instance axios avec la baseURL attendue", () => {
        expect(api).toBe(apiMock);
        expect(createMock).toHaveBeenCalledWith({
            baseURL: "http://localhost:8001/api",
            headers: {
                "Content-Type": "application/json",
            },
        });
    });

    it("ajoute Authorization quand un token est present", async () => {
        localStorage.setItem("token", "jwt-token");

        const config = { headers: {} };
        const result = await requestOk(config);

        expect(result.headers.Authorization).toBe("Bearer jwt-token");
    });

    it("laisse la requete intacte sans token", async () => {
        const config = { headers: {} };
        const result = await requestOk(config);

        expect(result.headers.Authorization).toBeUndefined();
    });

    it("retourne directement la reponse en succes", () => {
        const payload = { data: { ok: true } };
        expect(responseOk(payload)).toBe(payload);
    });

    it("nettoie la session sur erreur 401", async () => {
        localStorage.setItem("token", "t");
        localStorage.setItem("utilisateur", "{}");

        const error = { response: { status: 401, data: { message: "expired" } } };

        await expect(responseErr(error)).rejects.toEqual(error);
        expect(localStorage.getItem("token")).toBeNull();
        expect(localStorage.getItem("utilisateur")).toBeNull();
    });

    it("ne nettoie pas sur erreur hors 401", async () => {
        localStorage.setItem("token", "still-there");

        const error = { response: { status: 500 } };
        await expect(responseErr(error)).rejects.toEqual(error);

        expect(localStorage.getItem("token")).toBe("still-there");
    });
});

