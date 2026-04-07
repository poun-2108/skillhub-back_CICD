import { useState, useEffect, useRef } from 'react';
import './MessagerieModal.css';

/**
 * Modal de messagerie instantanée entre utilisateurs.
 */
export default function MessagerieModal({ onFermer }) {

    const [conversations,  setConversations]  = useState([]);
    const [interlocuteurs, setInterlocuteurs] = useState([]);
    const [convActive,     setConvActive]     = useState(null);
    const [messages,       setMessages]       = useState([]);
    const [contenu,        setContenu]        = useState('');
    const [vueNouveau,     setVueNouveau]     = useState(false);
    const [chargement,     setChargement]     = useState(false);

    const messagesFinRef = useRef(null);
    const pollingRef     = useRef(null);
    const token          = localStorage.getItem('token');

    // Helpers fetch
    const get = (url) =>
        fetch(`http://localhost:8000/api/${url}`, {
            headers: { Authorization: `Bearer ${token}` },
        }).then((r) => r.json());

    const post = (url, body) =>
        fetch(`http://localhost:8000/api/${url}`, {
            method:  'POST',
            headers: {
                Authorization:  `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        }).then((r) => r.json());

    // Chargement initial
    useEffect(() => {
        chargerConversations();
    }, []);

    // Polling toutes les 3s sur la conversation active
    useEffect(() => {
        if (!convActive) return;
        pollingRef.current = setInterval(() => {
            chargerMessages(convActive.interlocuteur_id);
        }, 3000);
        return () => clearInterval(pollingRef.current);
    }, [convActive]);

    // Scroll vers le dernier message
    useEffect(() => {
        messagesFinRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    // Fonctions métier
    const chargerConversations = async () => {
        const data = await get('messages/conversations');
        setConversations(data.conversations ?? []);
    };

    const chargerMessages = async (interlocuteurId) => {
        const data = await get(`messages/conversation/${interlocuteurId}`);
        setMessages(data.messages ?? []);
    };

    const ouvrirConversation = async (conv) => {
        clearInterval(pollingRef.current);
        setConvActive(conv);
        setVueNouveau(false);
        await chargerMessages(conv.interlocuteur_id);
        chargerConversations();
    };

    const ouvrirNouveauMessage = async () => {
        setVueNouveau(true);
        setConvActive(null);
        setMessages([]);
        const data = await get('messages/interlocuteurs');
        setInterlocuteurs(data.interlocuteurs ?? []);
    };

    const envoyerMessage = async (destinataireId) => {
        if (!contenu.trim()) return;
        setChargement(true);
        await post('messages/envoyer', {
            destinataire_id: destinataireId,
            contenu:         contenu.trim(),
        });
        setContenu('');
        setChargement(false);
        await chargerMessages(destinataireId);
        chargerConversations();
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

                {/* Panneau gauche — conversations */}
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
                        {conversations.length === 0 ? (
                            <p className="msg-vide">Aucune conversation</p>
                        ) : (
                            conversations.map((conv) => (
                                <div
                                    key={conv.interlocuteur_id}
                                    className={`msg-conv-item ${convActive?.interlocuteur_id === conv.interlocuteur_id ? 'msg-conv-actif' : ''}`}
                                    onClick={() => ouvrirConversation(conv)}
                                >
                                    <div className="msg-avatar">
                                        {conv.interlocuteur_nom?.slice(0, 2).toUpperCase()}
                                    </div>
                                    <div className="msg-conv-info">
                                        <span className="msg-conv-nom">{conv.interlocuteur_nom}</span>
                                        <span className="msg-conv-apercu">{conv.dernier_message}</span>
                                    </div>
                                    {conv.non_lus > 0 && (
                                        <span className="msg-badge">{conv.non_lus}</span>
                                    )}
                                </div>
                            ))
                        )}
                    </div>
                </div>

                {/* Panneau droit — chat */}
                <div className="msg-chat">
                    <button className="msg-fermer" onClick={onFermer}>✕</button>

                    {/* Vue : choisir un interlocuteur */}
                    {vueNouveau && (
                        <div className="msg-nouveau">
                            <h4>Nouveau message</h4>
                            <p className="msg-nouveau-desc">Choisissez un interlocuteur :</p>
                            {interlocuteurs.length === 0 ? (
                                <p className="msg-vide">Aucun interlocuteur disponible</p>
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
                                            <span className="msg-role">{u.role}</span>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    )}

                    {/* Vue : conversation active */}
                    {convActive && !vueNouveau && (
                        <>
                            <div className="msg-chat-header">
                                <div className="msg-avatar">
                                    {convActive.interlocuteur_nom?.slice(0, 2).toUpperCase()}
                                </div>
                                <span>{convActive.interlocuteur_nom}</span>
                            </div>

                            <div className="msg-messages">
                                {messages.map((m) => {
                                    const moi = m.expediteur_id !== convActive.interlocuteur_id;
                                    return (
                                        <div key={m.id} className={`msg-bulle-wrap ${moi ? 'msg-moi' : 'msg-autre'}`}>
                                            <div className="msg-bulle">{m.contenu}</div>
                                            <span className="msg-heure">
                                                {new Date(m.created_at).toLocaleTimeString('fr-FR', {
                                                    hour: '2-digit', minute: '2-digit'
                                                })}
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
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                        <line x1="22" y1="2" x2="11" y2="13"/>
                                        <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                                    </svg>
                                </button>
                            </div>
                        </>
                    )}

                    {/* Vue : placeholder rien sélectionné */}
                    {!convActive && !vueNouveau && (
                        <div className="msg-placeholder">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                            </svg>
                            <p>Sélectionnez une conversation<br/>ou démarrez-en une nouvelle</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}