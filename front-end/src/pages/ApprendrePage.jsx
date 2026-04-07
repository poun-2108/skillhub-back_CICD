import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import formationService from '../services/formationService';
import moduleService from '../services/moduleService';
import inscriptionService from '../services/inscriptionService';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import Bouton from '../components/Bouton';
import './ApprendrePage.css';

export default function ApprendrePage() {
    const { id }   = useParams();
    const navigate = useNavigate();
    const { utilisateur } = useAuth();

    const [formation,    setFormation]    = useState(null);
    const [modules,      setModules]      = useState([]);
    const [inscription,  setInscription]  = useState(null);
    const [modulesTermines, setModulesTermines] = useState([]);
    const [chargement,   setChargement]   = useState(true);
    const [erreur,       setErreur]       = useState('');
    const [messageOk,    setMessageOk]    = useState('');
    const [moduleActif,  setModuleActif]  = useState(null);
    const [loadingTerminer, setLoadingTerminer] = useState(false);

    const charger = async () => {
        try {
            const [dataFormation, dataModules, mesFormations] = await Promise.all([
                formationService.getFormation(id),
                moduleService.getModules(id),
                inscriptionService.mesFormations(),
            ]);

            setFormation(dataFormation);
            setModules(dataModules);

            // Trouver l'inscription de l'apprenant pour cette formation
            const insc = mesFormations.find(
                (i) => parseInt(i.formation_id) === parseInt(id)
            );

            if (!insc) {
                navigate('/dashboard/apprenant');
                return;
            }

            setInscription(insc);

            // Premier module ouvert par défaut
            if (dataModules.length > 0) {
                setModuleActif(dataModules[0]);
            }

        } catch (error) {
            setErreur('Erreur lors du chargement de la formation.');
        } finally {
            setChargement(false);
        }
    };

    useEffect(() => {
        charger();
    }, [id]);

    const handleTerminer = async (moduleId) => {
        if (modulesTermines.includes(moduleId)) return;

        setLoadingTerminer(true);
        try {
            const data = await moduleService.terminerModule(moduleId);
            setModulesTermines([...modulesTermines, moduleId]);
            setInscription({ ...inscription, progression: data.progression });
            setMessageOk(`Module terminé. Progression : ${data.progression}%`);
            setTimeout(() => setMessageOk(''), 3000);
        } catch (error) {
            const msg = error.response?.data?.message || 'Erreur lors de la progression.';
            setErreur(msg);
            setTimeout(() => setErreur(''), 3000);
        } finally {
            setLoadingTerminer(false);
        }
    };

    const estTermine = (moduleId) => modulesTermines.includes(moduleId);

    if (chargement) {
        return (
            <div className="apprendre-page">
                <Navbar />
                <p className="apprendre-chargement">Chargement...</p>
                <Footer />
            </div>
        );
    }

    if (erreur && !formation) {
        return (
            <div className="apprendre-page">
                <Navbar />
                <div className="apprendre-erreur-page">
                    <p>{erreur}</p>
                    <Bouton variante="secondaire" onClick={() => navigate('/dashboard/apprenant')}>
                        Retour au dashboard
                    </Bouton>
                </div>
                <Footer />
            </div>
        );
    }

    return (
        <div className="apprendre-page">
            <Navbar />

            <div className="apprendre-contenu">

                {/* En-tête formation */}
                <div className="apprendre-entete">
                    <div className="apprendre-entete-info">
                        <button
                            className="apprendre-retour"
                            onClick={() => navigate('/dashboard/apprenant')}
                        >
                            ← Retour au dashboard
                        </button>
                        <h1 className="apprendre-titre">{formation?.titre}</h1>
                        <p className="apprendre-description">{formation?.description}</p>
                    </div>

                    {/* Progression globale */}
                    <div className="apprendre-progression-bloc">
                        <div className="apprendre-progression-header">
                            <span className="apprendre-progression-label">Progression globale</span>
                            <span className="apprendre-progression-valeur">
                                {inscription?.progression}%
                            </span>
                        </div>
                        <div className="apprendre-progression-barre">
                            <div
                                className="apprendre-progression-remplissage"
                                style={{ width: `${inscription?.progression}%` }}
                            />
                        </div>
                        {inscription?.progression === 100 && (
                            <p className="apprendre-felicitations">
                                Félicitations, vous avez terminé cette formation !
                            </p>
                        )}
                    </div>
                </div>

                {/* Messages */}
                {messageOk && <p className="apprendre-succes">{messageOk}</p>}
                {erreur    && <p className="apprendre-erreur">{erreur}</p>}

                <div className="apprendre-layout">

                    {/* Liste des modules — colonne gauche */}
                    <div className="apprendre-sidebar">
                        <h2 className="apprendre-sidebar-titre">Modules</h2>
                        <div className="apprendre-modules-liste">
                            {modules.map((module, index) => (
                                <button
                                    key={module.id}
                                    className={`apprendre-module-item ${moduleActif?.id === module.id ? 'apprendre-module-actif' : ''} ${estTermine(module.id) ? 'apprendre-module-termine' : ''}`}
                                    onClick={() => setModuleActif(module)}
                                >
                                    <span className="apprendre-module-numero">
                                        {estTermine(module.id) ? '✓' : index + 1}
                                    </span>
                                    <span className="apprendre-module-nom">
                                        {module.titre}
                                    </span>
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Contenu du module actif — colonne droite */}
                    <div className="apprendre-module-contenu">
                        {moduleActif ? (
                            <>
                                <div className="apprendre-module-entete">
                                    <h2 className="apprendre-module-titre">
                                        {moduleActif.titre}
                                    </h2>
                                    {estTermine(moduleActif.id) ? (
                                        <span className="apprendre-badge-termine">
                                            Terminé
                                        </span>
                                    ) : (
                                        <Bouton
                                            variante="principal"
                                            taille="petit"
                                            onClick={() => handleTerminer(moduleActif.id)}
                                            disabled={loadingTerminer}
                                        >
                                            {loadingTerminer ? 'Enregistrement...' : 'Marquer comme terminé'}
                                        </Bouton>
                                    )}
                                </div>

                                <div className="apprendre-module-texte">
                                    {moduleActif.contenu}
                                </div>

                                {/* Navigation entre modules */}
                                <div className="apprendre-navigation">
                                    <Bouton
                                        variante="secondaire"
                                        taille="petit"
                                        onClick={() => {
                                            const index = modules.findIndex(m => m.id === moduleActif.id);
                                            if (index > 0) setModuleActif(modules[index - 1]);
                                        }}
                                        disabled={modules.findIndex(m => m.id === moduleActif.id) === 0}
                                    >
                                        ← Module précédent
                                    </Bouton>
                                    <Bouton
                                        variante="secondaire"
                                        taille="petit"
                                        onClick={() => {
                                            const index = modules.findIndex(m => m.id === moduleActif.id);
                                            if (index < modules.length - 1) setModuleActif(modules[index + 1]);
                                        }}
                                        disabled={modules.findIndex(m => m.id === moduleActif.id) === modules.length - 1}
                                    >
                                        Module suivant →
                                    </Bouton>
                                </div>
                            </>
                        ) : (
                            <p className="apprendre-vide">
                                Sélectionnez un module pour commencer.
                            </p>
                        )}
                    </div>

                </div>
            </div>

            <Footer />
        </div>
    );
}