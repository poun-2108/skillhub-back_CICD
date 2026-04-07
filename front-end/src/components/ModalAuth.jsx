import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Bouton from './Bouton';
import './ModalAuth.css';

/**
 * Modal d'authentification.
 * Contient les formulaires de connexion et d'inscription.
 * Props :
 * - mode      : 'login' | 'register' — formulaire affiché par défaut
 * - onFermer  : fonction appelée pour fermer la modal
 */
export default function ModalAuth({ mode = 'login', onFermer }) {
    const { login, register } = useAuth();
    const navigate = useNavigate();

    const [onglet, setOnglet] = useState(mode);

    // Champs login
    const [email,    setEmail]    = useState('');
    const [password, setPassword] = useState('');

    // Champs register
    const [nom,          setNom]          = useState('');
    const [emailReg,     setEmailReg]     = useState('');
    const [passwordReg,  setPasswordReg]  = useState('');
    const [role,         setRole]         = useState('apprenant');

    const [erreur,      setErreur]      = useState('');
    const [chargement,  setChargement]  = useState(false);

    // Fermer la modal en cliquant sur l'arrière-plan
    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
            onFermer();
        }
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setErreur('');
        setChargement(true);

        try {
            const data = await login(email, password);
            onFermer();

            if (data.user.role === 'formateur') {
                navigate('/dashboard/formateur');
            } else {
                navigate('/dashboard/apprenant');
            }
        } catch (error) {
            setErreur('Email ou mot de passe incorrect.');
        } finally {
            setChargement(false);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setErreur('');
        setChargement(true);

        try {
            const data = await register(nom, emailReg, passwordReg, role);
            onFermer();

            if (data.user.role === 'formateur') {
                navigate('/dashboard/formateur');
            } else {
                navigate('/dashboard/apprenant');
            }
        } catch (error) {
            const msg = error.response?.data?.message || "Erreur lors de l'inscription.";
            setErreur(msg);
        } finally {
            setChargement(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={handleOverlayClick}>
            <div className="modal-boite">

                {/* Bouton fermer */}
                <button className="modal-fermer" onClick={onFermer}>✕</button>

                {/* Onglets login / register */}
                <div className="modal-onglets">
                    <button
                        className={`modal-onglet ${onglet === 'login' ? 'modal-onglet-actif' : ''}`}
                        onClick={() => { setOnglet('login'); setErreur(''); }}
                    >
                        Se connecter
                    </button>
                    <button
                        className={`modal-onglet ${onglet === 'register' ? 'modal-onglet-actif' : ''}`}
                        onClick={() => { setOnglet('register'); setErreur(''); }}
                    >
                        S'inscrire
                    </button>
                </div>

                {/* Message d'erreur */}
                {erreur && <p className="modal-erreur">{erreur}</p>}

                {/* Formulaire connexion */}
                {onglet === 'login' && (
                    <form onSubmit={handleLogin} className="modal-formulaire">
                        <label className="modal-label">Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="modal-input"
                            placeholder="votre@email.com"
                            required
                        />

                        <label className="modal-label">Mot de passe</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="modal-input"
                            placeholder="••••••••"
                            required
                        />

                        <Bouton
                            type="submit"
                            variante="principal"
                            taille="grand"
                            disabled={chargement}
                        >
                            {chargement ? 'Connexion...' : 'Se connecter'}
                        </Bouton>
                    </form>
                )}

                {/* Formulaire inscription */}
                {onglet === 'register' && (
                    <form onSubmit={handleRegister} className="modal-formulaire">
                        <label className="modal-label">Nom complet</label>
                        <input
                            type="text"
                            value={nom}
                            onChange={(e) => setNom(e.target.value)}
                            className="modal-input"
                            placeholder="Jean Dupont"
                            required
                        />

                        <label className="modal-label">Email</label>
                        <input
                            type="email"
                            value={emailReg}
                            onChange={(e) => setEmailReg(e.target.value)}
                            className="modal-input"
                            placeholder="votre@email.com"
                            required
                        />

                        <label className="modal-label">Mot de passe</label>
                        <input
                            type="password"
                            value={passwordReg}
                            onChange={(e) => setPasswordReg(e.target.value)}
                            className="modal-input"
                            placeholder="Minimum 6 caractères"
                            required
                        />

                        <label className="modal-label">Je suis</label>
                        <select
                            value={role}
                            onChange={(e) => setRole(e.target.value)}
                            className="modal-select"
                        >
                            <option value="apprenant">Apprenant</option>
                            <option value="formateur">Formateur</option>
                        </select>

                        <Bouton
                            type="submit"
                            variante="principal"
                            taille="grand"
                            disabled={chargement}
                        >
                            {chargement ? 'Création...' : 'Créer mon compte'}
                        </Bouton>
                    </form>
                )}

            </div>
        </div>
    );
}