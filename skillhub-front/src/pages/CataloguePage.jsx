import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import formationService from '../services/formationService';
import inscriptionService from '../services/inscriptionService';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import Bouton from '../components/Bouton';
import ModalAuth from '../components/ModalAuth';
import './CataloguePage.css';

export default function CataloguePage() {
    const { estConnecte, estApprenant } = useAuth();
    const navigate = useNavigate();

    const [formations,  setFormations]  = useState([]);
    const [chargement,  setChargement]  = useState(true);
    const [modalMode,   setModalMode]   = useState(null);
    const [notification, setNotification] = useState(null);

    const [recherche, setRecherche] = useState('');
    const [categorie, setCategorie] = useState('');
    const [niveau,    setNiveau]    = useState('');

    const afficherNotification = (message, type = 'succes') => {
        setNotification({ message, type });
        setTimeout(() => setNotification(null), 3500);
    };

    const chargerFormations = async () => {
        setChargement(true);
        try {
            const filtres = {};
            if (recherche) filtres.recherche = recherche;
            if (categorie) filtres.categorie = categorie;
            if (niveau)    filtres.niveau    = niveau;
            const data = await formationService.getFormations(filtres);
            setFormations(data);
        } catch (error) {
            console.error('Erreur chargement formations :', error);
        } finally {
            setChargement(false);
        }
    };

    useEffect(() => { chargerFormations(); }, []);

    useEffect(() => {
        const delai = setTimeout(() => { chargerFormations(); }, 400);
        return () => clearTimeout(delai);
    }, [recherche, categorie, niveau]);

    const handleInscription = async (formationId) => {
        if (!estConnecte()) {
            setModalMode('login');
            return;
        }
        try {
            await inscriptionService.sInscrire(formationId);
            afficherNotification('Inscription reussie ! Retrouvez cette formation dans votre dashboard.', 'succes');
        } catch (error) {
            const msg = error.response?.data?.message || 'Erreur inscription';
            if (msg.includes('deja')) {
                afficherNotification('Vous etes deja inscrit a cette formation.', 'info');
            } else {
                afficherNotification(msg, 'erreur');
            }
        }
    };

    const reinitialiserFiltres = () => {
        setRecherche('');
        setCategorie('');
        setNiveau('');
    };

    return (
        <div className="catalogue-page">
            <Navbar />

            {/* Notification fixe en bas a droite */}
            {notification && (
                <div className={`catalogue-notification catalogue-notification-${notification.type}`}>
                    <span className="catalogue-notif-icone">
                        {notification.type === 'succes' ? '✓' : notification.type === 'info' ? 'ℹ' : '✕'}
                    </span>
                    <span>{notification.message}</span>
                    <button className="catalogue-notif-fermer" onClick={() => setNotification(null)}>✕</button>
                </div>
            )}

            <div className="catalogue-contenu">
                <h1 className="catalogue-titre">Toutes les formations</h1>

                <div className="catalogue-filtres">
                    <input
                        type="text"
                        placeholder="Rechercher une formation..."
                        value={recherche}
                        onChange={(e) => setRecherche(e.target.value)}
                        className="catalogue-input-recherche"
                    />
                    <select value={categorie} onChange={(e) => setCategorie(e.target.value)} className="catalogue-select">
                        <option value="">Toutes les categories</option>
                        <option value="developpement_web">Developpement web</option>
                        <option value="data">Data</option>
                        <option value="design">Design</option>
                        <option value="marketing">Marketing</option>
                        <option value="devops">DevOps</option>
                        <option value="autre">Autre</option>
                    </select>
                    <select value={niveau} onChange={(e) => setNiveau(e.target.value)} className="catalogue-select">
                        <option value="">Tous les niveaux</option>
                        <option value="debutant">Debutant</option>
                        <option value="intermediaire">Intermediaire</option>
                        <option value="avance">Avance</option>
                    </select>
                    {(recherche || categorie || niveau) && (
                        <Bouton variante="secondaire" taille="petit" onClick={reinitialiserFiltres}>
                            Reinitialiser
                        </Bouton>
                    )}
                </div>

                {chargement ? (
                    <p className="catalogue-chargement">Chargement...</p>
                ) : formations.length === 0 ? (
                    <p className="catalogue-vide">Aucune formation trouvee.</p>
                ) : (
                    <>
                        <p className="catalogue-compteur">
                            {formations.length} formation{formations.length > 1 ? 's' : ''} trouvee{formations.length > 1 ? 's' : ''}
                        </p>
                        <div className="catalogue-grille">
                            {formations.map((formation) => (
                                <div key={formation.id} className="catalogue-card">
                                    <div className="catalogue-card-badges">
                                        <span className="catalogue-badge-niveau">{formation.niveau}</span>
                                        <span className="catalogue-badge-categorie">{formation.categorie?.replace('_', ' ')}</span>
                                    </div>
                                    <h3 className="catalogue-card-titre">{formation.titre}</h3>
                                    <p className="catalogue-card-description">
                                        {formation.description?.slice(0, 100)}
                                        {formation.description?.length > 100 ? '...' : ''}
                                    </p>
                                    <div className="catalogue-card-meta">
                                        <span>Par {formation.formateur?.nom}</span>
                                        <span>{formation.inscriptions_count} apprenant{formation.inscriptions_count > 1 ? 's' : ''}</span>
                                        <span>{formation.nombre_de_vues} vue{formation.nombre_de_vues > 1 ? 's' : ''}</span>
                                    </div>
                                    <div className="catalogue-card-actions">
                                        <Bouton variante="fantome" taille="petit" onClick={() => navigate(`/formation/${formation.id}`)}>
                                            Voir detail
                                        </Bouton>
                                        {estApprenant() && (
                                            <Bouton variante="principal" taille="petit" onClick={() => handleInscription(formation.id)}>
                                                S'inscrire
                                            </Bouton>
                                        )}
                                        {!estConnecte() && (
                                            <Bouton variante="principal" taille="petit" onClick={() => setModalMode('login')}>
                                                Suivre la formation
                                            </Bouton>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </>
                )}
            </div>

            <Footer />

            {modalMode && (
                <ModalAuth mode={modalMode} onFermer={() => setModalMode(null)} />
            )}
        </div>
    );
}