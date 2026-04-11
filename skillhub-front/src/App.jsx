import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import AccueilPage from './pages/AccueilPage';
import CataloguePage from './pages/CataloguePage';
import DetailFormationPage from './pages/DetailFormationPage';
import DashboardFormateurPage from './pages/DashboardFormateurPage';
import DashboardApprenantPage from './pages/DashboardApprenantPage';
import ApprendrePage from './pages/ApprendrePage';
import ProfilPage from './pages/ProfilPage';

function RoutePrivee({ children }) {
    const { estConnecte } = useAuth();
    return estConnecte() ? children : <Navigate to="/" />;
}

function RouteFormateur({ children }) {
    const { estFormateur } = useAuth();
    return estFormateur() ? children : <Navigate to="/" />;
}

function RouteApprenant({ children }) {
    const { estApprenant } = useAuth();
    return estApprenant() ? children : <Navigate to="/" />;
}

function AppRoutes() {
    return (
        <Routes>
            {/* Routes publiques */}
            <Route path="/"              element={<AccueilPage />} />
            <Route path="/formations"    element={<CataloguePage />} />
            <Route path="/formation/:id" element={<DetailFormationPage />} />

            {/* Profil — accessible par tout utilisateur connecté */}
            <Route
                path="/profil"
                element={
                    <RoutePrivee>
                        <ProfilPage />
                    </RoutePrivee>
                }
            />

            {/* Dashboard formateur */}
            <Route
                path="/dashboard/formateur"
                element={
                    <RoutePrivee>
                        <RouteFormateur>
                            <DashboardFormateurPage />
                        </RouteFormateur>
                    </RoutePrivee>
                }
            />

            {/* Dashboard apprenant */}
            <Route
                path="/dashboard/apprenant"
                element={
                    <RoutePrivee>
                        <RouteApprenant>
                            <DashboardApprenantPage />
                        </RouteApprenant>
                    </RoutePrivee>
                }
            />

            {/* Page de suivi formation */}
            <Route
                path="/apprendre/:id"
                element={
                    <RoutePrivee>
                        <RouteApprenant>
                            <ApprendrePage />
                        </RouteApprenant>
                    </RoutePrivee>
                }
            />

            {/* Redirection par défaut */}
            <Route path="*" element={<Navigate to="/" />} />
        </Routes>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <AppRoutes />
            </AuthProvider>
        </BrowserRouter>
    );
}