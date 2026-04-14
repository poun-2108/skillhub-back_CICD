import { describe, expect, it, vi, beforeEach } from "vitest";

const { apiMock } = vi.hoisted(() => ({
    apiMock: {
        post: vi.fn(),
        get: vi.fn(),
    },
}));

vi.mock("./axiosConfig", () => ({
    default: apiMock,
}));

import authService from "./authService";

describe("authService", () => {
    beforeEach(() => {
        apiMock.post.mockReset();
        apiMock.get.mockReset();
        localStorage.clear();
    });

    it("normalise et stocke l'utilisateur lors du register", async () => {
        apiMock.post.mockResolvedValue({
            data: {
                token: "jwt-token",
                user: { nom: "Alice" },
            },
        });

        const result = await authService.register("Alice", "a@test.com", "pwd", "pwd", "apprenant");

        expect(apiMock.post).toHaveBeenCalledWith("/register", {
            nom: "Alice",
            email: "a@test.com",
            password: "pwd",
            password_confirmation: "pwd",
            role: "apprenant",
        });
        expect(result.user.name).toBe("Alice");
        expect(localStorage.getItem("token")).toBe("jwt-token");
    });

    it("register n'ecrit rien en local sans token ni user", async () => {
        apiMock.post.mockResolvedValue({ data: {} });

        const result = await authService.register("Alice", "a@test.com", "pwd", "pwd", "apprenant");

        expect(result.user).toBeNull();
        expect(localStorage.getItem("token")).toBeNull();
        expect(localStorage.getItem("utilisateur")).toBeNull();
    });

    it("normalise avec fallback name lors du login", async () => {
        apiMock.post.mockResolvedValue({
            data: {
                token: "jwt-login",
                user: { name: "Bob" },
            },
        });

        const result = await authService.login("b@test.com", "pwd");

        expect(apiMock.post).toHaveBeenCalledWith("/login", {
            email: "b@test.com",
            password: "pwd",
        });
        expect(result.user.nom).toBe("Bob");
    });

    it("met a jour le profil local", async () => {
        apiMock.get.mockResolvedValue({
            data: { user: { name: "Charlie" } },
        });

        const result = await authService.profile();

        expect(apiMock.get).toHaveBeenCalledWith("/profile");
        expect(result.user.nom).toBe("Charlie");
    });

    it("profile retourne user null si backend ne renvoie pas d'utilisateur", async () => {
        apiMock.get.mockResolvedValue({ data: {} });

        const result = await authService.profile();

        expect(result.user).toBeNull();
    });

    it("supprime la session locale lors du logout", async () => {
        localStorage.setItem("token", "x");
        localStorage.setItem("utilisateur", "{}");
        apiMock.post.mockResolvedValue({ data: { message: "ok" } });

        await authService.logout();

        expect(apiMock.post).toHaveBeenCalledWith("/logout", {});
        expect(localStorage.getItem("token")).toBeNull();
        expect(localStorage.getItem("utilisateur")).toBeNull();
    });

    it("envoie un FormData pour uploadPhoto", async () => {
        const fakeFile = new File(["a"], "photo.jpg", { type: "image/jpeg" });
        apiMock.post.mockResolvedValue({ data: { user: { nom: "Dana" } } });

        const result = await authService.uploadPhoto(fakeFile);

        expect(apiMock.post).toHaveBeenCalledWith(
            "/profil/photo",
            expect.any(FormData),
            { headers: { "Content-Type": "multipart/form-data" } }
        );
        expect(result.user.name).toBe("Dana");
    });

    it("retourne null si utilisateur local absent ou invalide", () => {
        expect(authService.getUtilisateur()).toBeNull();

        localStorage.setItem("utilisateur", "not-json");
        expect(authService.getUtilisateur()).toBeNull();
    });

    it("retourne l'utilisateur local si le JSON est valide", () => {
        localStorage.setItem("utilisateur", JSON.stringify({ nom: "Eva" }));
        expect(authService.getUtilisateur()).toEqual({ nom: "Eva" });
    });

    it("retourne token et etat de connexion", () => {
        expect(authService.estConnecte()).toBe(false);

        localStorage.setItem("token", "abc");
        expect(authService.getToken()).toBe("abc");
        expect(authService.estConnecte()).toBe(true);
    });

    it("clear vide le stockage", () => {
        localStorage.setItem("token", "abc");
        localStorage.setItem("utilisateur", "{}");

        authService.clear();

        expect(localStorage.getItem("token")).toBeNull();
        expect(localStorage.getItem("utilisateur")).toBeNull();
    });
});

