import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import authService from '../services/authService';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import Bouton from '../components/Bouton';
import './ProfilPage.css';

export default function ProfilPage() {
    const { utilisateur, setUtilisateur } = useAuth();
    const navigate = useNavigate();

    const inputRef = useRef(null);

    const [apercu,      setApercu]      = useState(null);
    const [fichier,     setFichier]     = useState(null);
    const [chargement,  setChargement]  = useState(false);
    const [messageOk,   setMessageOk]   = useState('');
    const [erreur,      setErreur]      = useState('');

    const handleFichierChange = (e) => {
        const f = e.target.files[0];
        if (!f) return;
        setFichier(f);
        setApercu(URL.createObjectURL(f));
    };

    const handleUpload = async () => {
        if (!fichier) return;
        setChargement(true);
        setErreur('');
        setMessageOk('');

        try {
            const data = await authService.uploadPhoto(fichier);
            setMessageOk('Photo mise a jour avec succes.');
            setFichier(null);
            // Mise a jour du contexte
            setUtilisateur(data.user);
        } catch (error) {
            setErreur("Erreur lors de l'upload. Verifiez le format (jpg, png) et la taille (max 2MB).");
        } finally {
            setChargement(false);
        }
    };

    const photoActuelle = apercu || utilisateur?.photo_profil
        ? (apercu || `http://localhost:8000${utilisateur?.photo_profil}`)
        : null;

    return (
        <div className="profil-page">
            <Navbar />

            <div className="profil-contenu">
                <h1 className="profil-titre">Mon profil</h1>

                {messageOk && <p className="profil-succes">{messageOk}</p>}
                {erreur    && <p className="profil-erreur">{erreur}</p>}

                <div className="profil-carte">

                    {/* Photo de profil */}
                    <div className="profil-photo-section">
                        <div className="profil-avatar-wrapper">
                            {photoActuelle ? (
                                <img
                                    src={photoActuelle}
                                    alt="Photo de profil"
                                    className="profil-avatar-img"
                                />
                            ) : (
                                <div className="profil-avatar-initiales">
                                    {utilisateur?.nom?.slice(0, 2).toUpperCase()}
                                </div>
                            )}

                            {/* Bouton camera par dessus la photo */}
                            <button
                                className="profil-avatar-btn"
                                onClick={() => inputRef.current.click()}
                                title="Changer la photo"
                            >
                                📷
                            </button>
                        </div>

                        <input
                            type="file"
                            ref={inputRef}
                            accept="image/jpeg,image/png,image/gif"
                            onChange={handleFichierChange}
                            style={{ display: 'none' }}
                        />

                        <p className="profil-photo-hint">
                            JPG, PNG ou GIF — max 2 MB
                        </p>

                        {fichier && (
                            <Bouton
                                variante="principal"
                                taille="moyen"
                                onClick={handleUpload}
                                disabled={chargement}
                            >
                                {chargement ? 'Upload...' : 'Sauvegarder la photo'}
                            </Bouton>
                        )}
                    </div>

                    {/* Infos utilisateur */}
                    <div className="profil-infos">
                        <div className="profil-info-ligne">
                            <span className="profil-info-label">Nom</span>
                            <span className="profil-info-valeur">{utilisateur?.nom}</span>
                        </div>
                        <div className="profil-info-ligne">
                            <span className="profil-info-label">Email</span>
                            <span className="profil-info-valeur">{utilisateur?.email}</span>
                        </div>
                        <div className="profil-info-ligne">
                            <span className="profil-info-label">Role</span>
                            <span className="profil-badge-role">
                                {utilisateur?.role === 'formateur' ? 'Formateur' : 'Apprenant'}
                            </span>
                        </div>
                    </div>

                </div>

                <div className="profil-retour">
                    <Bouton
                        variante="secondaire"
                        onClick={() => navigate(
                            utilisateur?.role === 'formateur'
                                ? '/dashboard/formateur'
                                : '/dashboard/apprenant'
                        )}
                    >
                        Retour au dashboard
                    </Bouton>
                </div>
            </div>

            <Footer />
        </div>
    );
}