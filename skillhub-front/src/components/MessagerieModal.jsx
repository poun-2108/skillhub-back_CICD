import { useState, useEffect, useRef } from 'react';
import api from '../services/axiosConfig';
import './MessagerieModal.css';

export default function MessagerieModal({ onFermer }) {

    const [conversations,  setConversations]  = useState([]);
    const [interlocuteurs, setInterlocuteurs] = useState([]);
    const [convActive,     setConvActive]     = useState(null);
    const [messages,       setMessages]       = useState([]);
    const [contenu,        setContenu]        = useState('');
    const [vueNouveau,     setVueNouveau]     = useState(false);
    const [chargement,     setChargement]     = useState(false);
    const [chargementInit, setChargementInit] = useState(true);
    const [erreur,         setErreur]         = useState('');

    useEffect(() => {
        chargerConversations().finally(() => setChargementInit(false));
    }, []);

    useEffect(() => {
        if (!convActive) return;
        pollingRef.current = setInterval(() => chargerMessages(convActive.interlocuteur_id, true), 3000);
        return () => clearInterval(pollingRef.current);
    }, [convActive]);

    useEffect(() => {
        messagesFinRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    useEffect(() => {
        if (!erreur) return;
        const t = setTimeout(() => setErreur(''), 4000);
        return () => clearTimeout(t);
    }, [erreur]);

    const chargerConversations = async () => {
        try {
            const response = await api.get('/messages/conversations');
            setConversations(response.data.conversations ?? []);
        } catch (e) { /* silence polling */ }
    };

    const chargerMessages = async (interlocuteurId, silencieux = false) => {
        try {
            const response = await api.get(`/messages/conversation/${interlocuteurId}`);
            setMessages(response.data.messages ?? []);
        } catch (e) {
            if (!silencieux) setErreur('Impossible de charger les messages.');
        }
    };

    const ouvrirConversation = async (conv) => {
        clearInterval(pollingRef.current);
        setConvActive(conv);
        setVueNouveau(false);
        setErreur('');
        await chargerMessages(conv.interlocuteur_id);
        chargerConversations();
    };

    const ouvrirNouveauMessage = async () => {
        setVueNouveau(true);
        setConvActive(null);
        setMessages([]);
        setErreur('');
        try {
            const response = await api.get('/messages/interlocuteurs');
            setInterlocuteurs(response.data.interlocuteurs ?? []);
        } catch (e) {
            setErreur('Impossible de charger les contacts.');
        }
    };

    const envoyerMessage = async (destinataireId) => {
        if (!contenu.trim() || chargement) return;
        setChargement(true);
        setErreur('');
        try {
            await api.post('/messages/envoyer', {
                destinataire_id: destinataireId,
                contenu:         contenu.trim(),
            });
            setContenu('');
            await chargerMessages(destinataireId);
            chargerConversations();
        } catch (e) {
            setErreur(e.response?.data?.message || "Erreur lors de l'envoi.");
        } finally {
            setChargement(false);
        }
    };

    const gererToucheEntree = (e, destinataireId) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            envoyerMessage(destinataireId);
        }
    };

    return (
        <div className="msg-overlay" onClick={(e) => e.target === e.currentTarget && onFermer()}>
            <div className="msg-modal">

                <div className="msg-sidebar">
                    <div className="msg-sidebar-header">
                        <h3>Messages</h3>
                        <button className="msg-btn-nouveau" onClick={ouvrirNouveauMessage} title="Nouveau message">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                            </svg>
                        </button>
                    </div>

                    <div className="msg-conv-liste">
                        {chargementInit ? (
                            <p className="msg-vide">Chargement...</p>
                        ) : conversations.length === 0 ? (
                            <p className="msg-vide">Aucune conversation — cliquez ✏️ pour commencer</p>
                        ) : (
                            conversations.map((conv) => (
                                <div
                                    key={conv.interlocuteur_id}
                                    className={`msg-conv-item ${convActive?.interlocuteur_id === conv.interlocuteur_id ? 'msg-conv-actif' : ''}`}
                                    onClick={() => ouvrirConversation(conv)}
                                >
                                    <div className="msg-avatar">{conv.interlocuteur_nom?.slice(0, 2).toUpperCase()}</div>
                                    <div className="msg-conv-info">
                                        <span className="msg-conv-nom">{conv.interlocuteur_nom}</span>
                                        <span className="msg-conv-apercu">{conv.dernier_message}</span>
                                    </div>
                                    {conv.non_lus > 0 && <span className="msg-badge">{conv.non_lus}</span>}
                                </div>
                            ))
                        )}
                    </div>
                </div>

                <div className="msg-chat">
                    <button className="msg-fermer" onClick={onFermer}>✕</button>

                    {erreur && <div className="msg-erreur-bandeau">{erreur}</div>}

                    {vueNouveau && (
                        <div className="msg-nouveau">
                            <h4>Nouveau message</h4>
                            <p className="msg-nouveau-desc">Choisissez un destinataire :</p>
                            {interlocuteurs.length === 0 ? (
                                <p className="msg-vide">Aucun utilisateur disponible.</p>
                            ) : (
                                interlocuteurs.map((u) => (
                                    <div
                                        key={u.id}
                                        className="msg-interlocuteur"
                                        onClick={() => ouvrirConversation({
                                            interlocuteur_id:  u.id,
                                            interlocuteur_nom: u.nom,
                                            non_lus:           0,
                                        })}
                                    >
                                        <div className="msg-avatar">{u.nom?.slice(0, 2).toUpperCase()}</div>
                                        <div>
                                            <span className="msg-conv-nom">{u.nom}</span>
                                            <span className="msg-role"> — {u.role}</span>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    )}

                    {convActive && !vueNouveau && (
                        <>
                            <div className="msg-chat-header">
                                <div className="msg-avatar">{convActive.interlocuteur_nom?.slice(0, 2).toUpperCase()}</div>
                                <span>{convActive.interlocuteur_nom}</span>
                            </div>

                            <div className="msg-messages">
                                {messages.length === 0 && (
                                    <p className="msg-vide" style={{ textAlign: 'center', marginTop: '2rem' }}>
                                        Commencez la conversation !
                                    </p>
                                )}
                                {messages.map((m) => {
                                    const moi = m.expediteur_id !== convActive.interlocuteur_id;
                                    return (
                                        <div key={m.id} className={`msg-bulle-wrap ${moi ? 'msg-moi' : 'msg-autre'}`}>
                                            <div className="msg-bulle">{m.contenu}</div>
                                            <span className="msg-heure">
                                                {new Date(m.created_at).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}
                                            </span>
                                        </div>
                                    );
                                })}
                                <div ref={messagesFinRef} />
                            </div>

                            <div className="msg-saisie">
                                <textarea
                                    value={contenu}
                                    onChange={(e) => setContenu(e.target.value)}
                                    onKeyDown={(e) => gererToucheEntree(e, convActive.interlocuteur_id)}
                                    placeholder="Écrivez un message... (Entrée pour envoyer)"
                                    rows={2}
                                    disabled={chargement}
                                />
                                <button
                                    onClick={() => envoyerMessage(convActive.interlocuteur_id)}
                                    disabled={chargement || !contenu.trim()}
                                    className="msg-btn-envoyer"
                                >
                                    {chargement ? '...' : (
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                            <line x1="22" y1="2" x2="11" y2="13"/>
                                            <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                                        </svg>
                                    )}
                                </button>
                            </div>
                        </>
                    )}

                    {!convActive && !vueNouveau && (
                        <div className="msg-placeholder">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                            </svg>
                            <p>Sélectionnez une conversation<br/>ou cliquez ✏️ pour en démarrer une</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
