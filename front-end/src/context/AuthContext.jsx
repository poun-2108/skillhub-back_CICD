import { createContext, useContext, useState } from 'react';
import authService from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {

    const [utilisateur, setUtilisateur] = useState(
        authService.getUtilisateur()
    );

    const login = async (email, password) => {
        const data = await authService.login(email, password);
        setUtilisateur(data.user);
        return data;
    };

    const register = async (nom, email, password, role) => {
        const data = await authService.register(nom, email, password, role);
        setUtilisateur(data.user);
        return data;
    };

    const logout = async () => {
        await authService.logout();
        setUtilisateur(null);
    };

    const estConnecte  = () => utilisateur !== null;
    const estFormateur = () => utilisateur !== null && utilisateur.role === 'formateur';
    const estApprenant = () => utilisateur !== null && utilisateur.role === 'apprenant';

    const valeur = {
        utilisateur,
        setUtilisateur,
        login,
        register,
        logout,
        estConnecte,
        estFormateur,
        estApprenant,
    };

    return (
        <AuthContext.Provider value={valeur}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}