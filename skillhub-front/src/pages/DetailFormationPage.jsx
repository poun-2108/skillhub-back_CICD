import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import formationService from '../services/formationService';
import moduleService from '../services/moduleService';
import inscriptionService from '../services/inscriptionService';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import Bouton from '../components/Bouton';
import ModalAuth from '../components/ModalAuth';
import './DetailFormationPage.css';

export default function DetailFormationPage() {
    const { id }     = useParams();
    const navigate   = useNavigate();
    const { estConnecte, estApprenant } = useAuth();

    const [formation,    setFormation]    = useState(null);
    const [modules,      setModules]      = useState([]);
    const [chargement,   setChargement]   = useState(true);
    const [erreur,       setErreur]       = useState('');
    const [messageOk,    setMessageOk]    = useState('');
    const [modalMode,    setModalMode]    = useState(null);
    const [inscrit,      setInscrit]      = useState(false);
    const [loadingInsc,  setLoadingInsc]  = useState(false);

    useEffect(() => {
        const charger = async () => {
            try {
                // Chargement de la formation et ses modules en parallèle
                const [dataFormation, dataModules] = await Promise.all([
                    formationService.getFormation(id),
                    moduleService.getModules(id),
                ]);

                setFormation(dataFormation);
                setModules(dataModules);

                // Vérification si l'apprenant est déjà inscrit
                if (estApprenant()) {
                    const mesFormations = await inscriptionService.mesFormations();
                    const dejaInscrit   = mesFormations.some(
                        (insc) => insc.formation_id === parseInt(id)
                    );
                    setInscrit(dejaInscrit);
                }
            } catch (error) {
                setErreur('Formation introuvable.');
            } finally {
                setChargement(false);
            }
        };
        charger();
    }, [id]);

    const handleInscription = async () => {
        if (!estConnecte()) {
            setModalMode('login');
            return;
        }

        setLoadingInsc(true);
        try {
            await inscriptionService.sInscrire(id);
            setInscrit(true);
            setMessageOk('Inscription réussie ! Vous pouvez maintenant suivre cette formation.');
        } catch (error) {
            const msg = error.response?.data?.message || "Erreur lors de l'inscription.";
            setErreur(msg);
        } finally {
            setLoadingInsc(false);
        }
    };

    const getNiveauLabel = (niveau) => {
        const labels = {
            debutant:      'Débutant',
            intermediaire: 'Intermédiaire',
            avance:        'Avancé',
        };
        return labels[niveau] || niveau;
    };

    const getCategorieLabel = (categorie) => {
        const labels = {
            developpement_web: 'Développement web',
            data:              'Data',
            design:            'Design',
            marketing:         'Marketing',
            devops:            'DevOps',
            autre:             'Autre',
        };
        return labels[categorie] || categorie;
    };

    if (chargement) {
        return (
            <div className="detail-page">
                <Navbar />
                <p className="detail-chargement">Chargement...</p>
                <Footer />
            </div>
        );
    }

    if (erreur && !formation) {
        return (
            <div className="detail-page">
                <Navbar />
                <div className="detail-erreur-page">
                    <p>{erreur}</p>
                    <Bouton variante="secondaire" onClick={() => navigate('/formations')}>
                        Retour aux formations
                    </Bouton>
                </div>
                <Footer />
            </div>
        );
    }

    return (
        <div className="detail-page">
            <Navbar />

            <div className="detail-contenu">

                {/* En-tête de la formation */}
                <div className="detail-entete">
                    <div className="detail-badges">
                        <span className="detail-badge-niveau">
                            {getNiveauLabel(formation.niveau)}
                        </span>
                        <span className="detail-badge-categorie">
                            {getCategorieLabel(formation.categorie)}
                        </span>
                    </div>

                    <h1 className="detail-titre">{formation.titre}</h1>
                    <p className="detail-description">{formation.description}</p>

                    <div className="detail-meta">
                        <span>Formateur : <strong>{formation.formateur?.nom}</strong></span>
                        <span>{formation.inscriptions_count} apprenant{formation.inscriptions_count > 1 ? 's' : ''}</span>
                        <span>{formation.nombre_de_vues} vue{formation.nombre_de_vues > 1 ? 's' : ''}</span>
                    </div>

                    {/* Messages */}
                    {messageOk && (
                        <p className="detail-succes">{messageOk}</p>
                    )}
                    {erreur && (
                        <p className="detail-erreur">{erreur}</p>
                    )}

                    {/* Actions */}
                    <div className="detail-actions">
                        {inscrit ? (
                            <Bouton
                                variante="principal"
                                taille="grand"
                                onClick={() => navigate(`/apprendre/${id}`)}
                            >
                                Continuer la formation
                            </Bouton>
                        ) : (
                            <Bouton
                                variante="principal"
                                taille="grand"
                                onClick={handleInscription}
                                disabled={loadingInsc}
                            >
                                {loadingInsc ? 'Inscription...' : 'Suivre la formation'}
                            </Bouton>
                        )}

                        <Bouton
                            variante="secondaire"
                            taille="grand"
                            onClick={() => navigate('/formations')}
                        >
                            Retour aux formations
                        </Bouton>
                    </div>
                </div>

                {/* Liste des modules */}
                <div className="detail-modules">
                    <h2 className="detail-modules-titre">
                        Contenu de la formation ({modules.length} module{modules.length > 1 ? 's' : ''})
                    </h2>

                    {modules.length === 0 ? (
                        <p className="detail-modules-vide">
                            Aucun module disponible pour le moment.
                        </p>
                    ) : (
                        <div className="detail-modules-liste">
                            {modules.map((module, index) => (
                                <div key={module.id} className="detail-module-item">
                                    <div className="detail-module-numero">
                                        {index + 1}
                                    </div>
                                    <div className="detail-module-info">
                                        <h3 className="detail-module-titre">
                                            {module.titre}
                                        </h3>
                                        <p className="detail-module-apercu">
                                            {module.contenu?.slice(0, 80)}
                                            {module.contenu?.length > 80 ? '...' : ''}
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

            </div>

            <Footer />

            {modalMode && (
                <ModalAuth
                    mode={modalMode}
                    onFermer={() => setModalMode(null)}
                />
            )}
        </div>
    );
}