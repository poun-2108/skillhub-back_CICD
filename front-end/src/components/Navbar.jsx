import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate }           from 'react-router-dom';
import { useAuth }                     from '../context/AuthContext';
import ModalAuth                       from './ModalAuth';
import MessagerieModal                 from './MessagerieModal';
import './Navbar.css';

export default function Navbar() {
    const { utilisateur, logout, estConnecte } = useAuth();
    const navigate = useNavigate();

    // ── États UI ──────────────────────────────────────────────
    const [modalOuverte,      setModalOuverte]     = useState(false);
    const [menuOuvert,        setMenuOuvert]        = useState(false);
    const [messagerieOuverte, setMessagerieOuverte] = useState(false);

    // ── Badge messages non lus ────────────────────────────────
    const [nonLus,    setNonLus]    = useState(0);
    const intervalRef               = useRef(null);

    /**
     * Polling toutes les 5s pour récupérer les messages non lus.
     * S'arrête automatiquement si l'utilisateur se déconnecte.
     */
    useEffect(() => {
        if (!estConnecte()) {
            setNonLus(0);
            return;
        }

        const fetchNonLus = async () => {
            try {
                const token = localStorage.getItem('token');
                const res   = await fetch('http://localhost:8000/api/messages/non-lus', {
                    headers: { Authorization: `Bearer ${token}` },
                });
                if (res.ok) {
                    const data = await res.json();
                    setNonLus(data.non_lus ?? 0);
                }
            } catch {
                // Silencieux — pas d'alerte si le réseau coupe momentanément
            }
        };

        fetchNonLus();
        intervalRef.current = setInterval(fetchNonLus, 5000);

        // Nettoyage à la déconnexion ou démontage du composant
        return () => clearInterval(intervalRef.current);

    }, [estConnecte()]);

    // ── Handlers ──────────────────────────────────────────────

    const handleLogout = async () => {
        await logout();
        setMenuOuvert(false);
        setNonLus(0);
        navigate('/');
    };

    const fermerMenu = () => setMenuOuvert(false);

    // Redirige vers le dashboard selon le rôle de l'utilisateur
    const allerAuDashboard = () => {
        fermerMenu();
        navigate(
            utilisateur?.role === 'formateur'
                ? '/dashboard/formateur'
                : '/dashboard/apprenant'
        );
    };

    // Ouvre la messagerie et remet le badge à 0 visuellement
    const ouvrirMessagerie = () => {
        setMessagerieOuverte(true);
        setNonLus(0);
        fermerMenu();
    };

    // ── Rendu ─────────────────────────────────────────────────
    return (
        <>
            <nav className="navbar">
                <div className="navbar-container">

                    {/* Logo */}
                    <Link to="/" className="navbar-logo" onClick={fermerMenu}>
                        Skill<span className="navbar-logo-hub">Hub</span>
                    </Link>

                    {/* Liens de navigation */}
                    <div className={`navbar-liens ${menuOuvert ? 'navbar-liens-ouvert' : ''}`}>

                        <Link to="/"           className="navbar-lien" onClick={fermerMenu}>Accueil</Link>
                        <Link to="/formations" className="navbar-lien" onClick={fermerMenu}>Formations</Link>
                        <a href="#apropos"     className="navbar-lien" onClick={fermerMenu}>A propos</a>
                        <a href="#contact"     className="navbar-lien" onClick={fermerMenu}>Contact</a>

                        <div className="navbar-separateur" />

                        {estConnecte() ? (
                            <>
                                {/* ── Icône messagerie avec badge ── */}
                                <button
                                    className="navbar-messagerie-btn"
                                    onClick={ouvrirMessagerie}
                                    title="Messagerie"
                                >
                                    {/* Icône enveloppe */}
                                    <svg
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    >
                                        <rect x="2" y="4" width="20" height="16" rx="2"/>
                                        <polyline points="2,4 12,13 22,4"/>
                                    </svg>

                                    {/* Badge rouge — visible uniquement si non_lus > 0 */}
                                    {nonLus > 0 && (
                                        <span className="navbar-badge">
                                            {nonLus > 99 ? '99+' : nonLus}
                                        </span>
                                    )}
                                </button>

                                {/* ── Profil utilisateur → dashboard ── */}
                                <button className="navbar-profil" onClick={allerAuDashboard}>
                                    {utilisateur?.photo_profil ? (
                                        <img
                                            src={`http://localhost:8000${utilisateur.photo_profil}`}
                                            alt="profil"
                                            className="navbar-avatar"
                                        />
                                    ) : (
                                        <span className="navbar-avatar-initiales">
                                            {utilisateur?.nom?.slice(0, 2).toUpperCase()}
                                        </span>
                                    )}
                                    {utilisateur?.nom}
                                </button>

                                {/* ── Bouton déconnexion ── */}
                                <button
                                    className="navbar-btn-deconnexion"
                                    onClick={handleLogout}
                                >
                                    Se deconnecter
                                </button>
                            </>
                        ) : (
                            /* ── Bouton connexion (non connecté) ── */
                            <button
                                className="navbar-btn-connexion"
                                onClick={() => { setModalOuverte(true); fermerMenu(); }}
                            >
                                Se connecter
                            </button>
                        )}
                    </div>

                    {/* Burger menu mobile */}
                    <button
                        className={`navbar-burger ${menuOuvert ? 'navbar-burger-ouvert' : ''}`}
                        onClick={() => setMenuOuvert(!menuOuvert)}
                        aria-label="Menu"
                    >
                        <span></span>
                        <span></span>
                        <span></span>
                    </button>

                </div>
            </nav>

            {/* Modal connexion / inscription */}
            {modalOuverte && (
                <ModalAuth
                    mode="login"
                    onFermer={() => setModalOuverte(false)}
                />
            )}

            {/* Modal messagerie — s'affiche par-dessus tout */}
            {messagerieOuverte && (
                <MessagerieModal
                    onFermer={() => setMessagerieOuverte(false)}
                />
            )}
        </>
    );
}