import { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import formationService from '../services/formationService';
import authService from '../services/authService';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import Bouton from '../components/Bouton';
import ModalFormation from '../components/ModalFormation';
import ModalModules from '../components/ModalModules';
import './DashboardFormateurPage.css';

export default function DashboardFormateurPage() {
    const { utilisateur, setUtilisateur } = useAuth();
    const navigate = useNavigate();
    const inputPhotoRef = useRef(null);

    const [formations, setFormations] = useState([]);
    const [chargement, setChargement] = useState(true);
    const [modalFormationOuverte, setModalFormationOuverte] = useState(false);
    const [modalModulesOuverte, setModalModulesOuverte] = useState(false);
    const [formationModif, setFormationModif] = useState(null);
    const [formationModules, setFormationModules] = useState(null);
    const [messageOk, setMessageOk] = useState('');
    const [erreur, setErreur] = useState('');
    const [uploadingPhoto, setUploadingPhoto] = useState(false);
    const [filtreActif, setFiltreActif] = useState('tout');

    const chargerFormations = async () => {
        setChargement(true);
        try {
            const data = await formationService.getMesFormations();
            setFormations(data);
        } catch (error) {
            setErreur('Erreur lors du chargement.');
        } finally {
            setChargement(false);
        }
    };

    useEffect(() => {
        chargerFormations();
    }, []);

    const handlePhotoChange = async (e) => {
        const fichier = e.target.files[0];
        if (!fichier) return;

        setUploadingPhoto(true);

        try {
            const data = await authService.uploadPhoto(fichier);

            if (data.user) {
                setUtilisateur(data.user);
            } else {
                const utilisateurActuel = authService.getUtilisateur();
                setUtilisateur(utilisateurActuel);
            }

            setMessageOk('Photo mise a jour.');
            setTimeout(() => setMessageOk(''), 3000);
        } catch {
            setErreur("Erreur upload photo.");
        } finally {
            setUploadingPhoto(false);
        }
    };

    const handleSupprimer = async (id) => {
        if (!window.confirm('Supprimer cette formation ?')) return;

        try {
            await formationService.supprimerFormation(id);
            setMessageOk('Formation supprimee.');
            chargerFormations();
            setTimeout(() => setMessageOk(''), 3000);
        } catch {
            setErreur('Erreur suppression.');
        }
    };

    const handleSauvegarder = () => {
        setMessageOk(formationModif ? 'Formation modifiee.' : 'Formation creee.');
        setModalFormationOuverte(false);
        setFormationModif(null);
        chargerFormations();
        setTimeout(() => setMessageOk(''), 3000);
    };

    const getNiveauLabel = (n) => ({
        debutant: 'Debutant',
        intermediaire: 'Intermediaire',
        avance: 'Avance'
    }[n] || n);

    const formationsFiltrees = filtreActif === 'tout'
        ? formations
        : formations.filter(f => f.niveau === filtreActif);

    const totalVues = formations.reduce((s, f) => s + (f.nombre_de_vues || 0), 0);
    const totalApprenants = formations.reduce((s, f) => s + (f.inscriptions_count || 0), 0);

    return (
        <div className="df-page">
            <Navbar />

            <div className="df-hero">
                <div className="df-hero-contenu">
                    <span className="df-hero-label">ESPACE FORMATEUR</span>
                    <h1 className="df-hero-titre">Dashboard Formateur</h1>
                    <p className="df-hero-sous">
                        Bienvenue <strong>{utilisateur?.nom}</strong> — gerez vos formations, modules et apprenants
                    </p>

                    <div className="df-hero-tags">
                        <button
                            className="df-hero-tag"
                            onClick={() => {
                                setFormationModif(null);
                                setModalFormationOuverte(true);
                            }}
                        >
                            Creer des formations
                        </button>

                        <button
                            className="df-hero-tag"
                            onClick={() => document.querySelector('.df-grille')?.scrollIntoView({ behavior: 'smooth' })}
                        >
                            Gerer les modules
                        </button>

                        <button
                            className="df-hero-tag"
                            onClick={() => document.querySelector('.df-stats')?.scrollIntoView({ behavior: 'smooth' })}
                        >
                            Suivre les apprenants
                        </button>

                        <button
                            className="df-hero-tag"
                            onClick={() => document.querySelector('.df-stats')?.scrollIntoView({ behavior: 'smooth' })}
                        >
                            Statistiques en direct
                        </button>
                    </div>
                </div>

                <div className="df-hero-droite">
                    <div className="df-avatar-wrapper">
                        {utilisateur?.photo_profil ? (
                            <img
                                src={`http://localhost:8001${utilisateur.photo_profil}`}
                                alt="profil"
                                className="df-avatar-img"
                            />
                        ) : (
                            <div className="df-avatar-initiales">
                                {utilisateur?.nom?.slice(0, 2).toUpperCase()}
                            </div>
                        )}

                        <button
                            className="df-avatar-btn"
                            onClick={() => inputPhotoRef.current.click()}
                            title="Changer la photo"
                            disabled={uploadingPhoto}
                        >
                            {uploadingPhoto ? '...' : '📷'}
                        </button>

                        <input
                            type="file"
                            ref={inputPhotoRef}
                            accept="image/jpeg,image/png,image/gif"
                            onChange={handlePhotoChange}
                            style={{ display: 'none' }}
                        />
                    </div>
                </div>
            </div>

            <div className="df-contenu">
                <div className="df-stats">
                    <div className="df-stat-card">
                        <span className="df-stat-valeur">{formations.length}</span>
                        <span className="df-stat-label">Formations</span>
                    </div>

                    <div className="df-stat-card">
                        <span className="df-stat-valeur">{totalApprenants}</span>
                        <span className="df-stat-label">Apprenants</span>
                    </div>

                    <div className="df-stat-card">
                        <span className="df-stat-valeur">{totalVues}</span>
                        <span className="df-stat-label">Vues totales</span>
                    </div>

                    <div
                        className="df-stat-card df-stat-action"
                        onClick={() => {
                            setFormationModif(null);
                            setModalFormationOuverte(true);
                        }}
                    >
                        <span className="df-stat-plus">+</span>
                        <span className="df-stat-label">Nouvelle formation</span>
                    </div>
                </div>

                {messageOk && <p className="df-succes">{messageOk}</p>}
                {erreur && <p className="df-erreur">{erreur}</p>}

                <div className="df-filtres">
                    {['tout', 'debutant', 'intermediaire', 'avance'].map((f) => (
                        <button
                            key={f}
                            className={`df-filtre-btn ${filtreActif === f ? 'df-filtre-actif' : ''}`}
                            onClick={() => setFiltreActif(f)}
                        >
                            {f === 'tout' ? 'Toutes' : getNiveauLabel(f)}
                        </button>
                    ))}

                    <span className="df-filtres-compteur">
                        {formationsFiltrees.length} formation{formationsFiltrees.length > 1 ? 's' : ''}
                    </span>
                </div>

                {chargement ? (
                    <div className="df-chargement">
                        <div className="df-spinner" />
                        <p>Chargement de vos formations...</p>
                    </div>
                ) : formationsFiltrees.length === 0 ? (
                    <div className="df-vide">
                        <p>Aucune formation dans cette categorie.</p>
                        <Bouton variante="principal" onClick={() => setModalFormationOuverte(true)}>
                            Creer une formation
                        </Bouton>
                    </div>
                ) : (
                    <div className="df-grille">
                        {formationsFiltrees.map((formation) => (
                            <div key={formation.id} className="df-card">
                                <div className={`df-card-bandeau df-bandeau-${formation.niveau}`} />

                                <div className="df-card-body">
                                    <div className="df-card-badges">
                                        <span className="df-badge-niveau">{getNiveauLabel(formation.niveau)}</span>
                                        <span className="df-badge-categorie">{formation.categorie?.replace('_', ' ')}</span>
                                    </div>

                                    <h3 className="df-card-titre">{formation.titre}</h3>

                                    <p className="df-card-description">
                                        {formation.description?.slice(0, 90)}
                                        {formation.description?.length > 90 ? '...' : ''}
                                    </p>

                                    <div className="df-card-stats">
                                        <div className="df-stat-mini">
                                            <span className="df-stat-mini-val">{formation.nombre_de_vues}</span>
                                            <span className="df-stat-mini-label">vues</span>
                                        </div>

                                        <div className="df-stat-mini">
                                            <span className="df-stat-mini-val">{formation.inscriptions_count}</span>
                                            <span className="df-stat-mini-label">apprenants</span>
                                        </div>
                                    </div>
                                </div>

                                <div className="df-card-actions">
                                    <div className="df-card-actions-ligne">
                                        <Bouton
                                            variante="fantome"
                                            taille="petit"
                                            onClick={() => navigate(`/formation/${formation.id}`)}
                                        >
                                            Voir
                                        </Bouton>

                                        <Bouton
                                            variante="secondaire"
                                            taille="petit"
                                            onClick={() => {
                                                setFormationModif(formation);
                                                setModalFormationOuverte(true);
                                            }}
                                        >
                                            Modifier
                                        </Bouton>

                                        <Bouton
                                            variante="secondaire"
                                            taille="petit"
                                            onClick={() => {
                                                setFormationModules(formation);
                                                setModalModulesOuverte(true);
                                            }}
                                        >
                                            Modules
                                        </Bouton>
                                    </div>

                                    <div className="df-card-actions-supprimer">
                                        <Bouton
                                            variante="danger"
                                            taille="petit"
                                            onClick={() => handleSupprimer(formation.id)}
                                        >
                                            Supprimer
                                        </Bouton>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <Footer />

            {modalFormationOuverte && (
                <ModalFormation
                    formation={formationModif}
                    onFermer={() => {
                        setModalFormationOuverte(false);
                        setFormationModif(null);
                    }}
                    onSauvegarder={handleSauvegarder}
                />
            )}

            {modalModulesOuverte && (
                <ModalModules
                    formation={formationModules}
                    onFermer={() => {
                        setModalModulesOuverte(false);
                        setFormationModules(null);
                    }}
                />
            )}
        </div>
    );
}