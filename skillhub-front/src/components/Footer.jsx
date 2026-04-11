import { Link } from 'react-router-dom';
import './Footer.css';

export default function Footer() {
    const annee = new Date().getFullYear();

    return (
        <footer className="footer">

            {/* Bandeau supérieur */}
            <div className="footer-bandeau">
                <div className="footer-brand">
                    <span className="footer-brand-nom">SKILLHUB</span>
                    <span className="footer-brand-tag">SKILLS FOR THE FUTURE</span>
                </div>

                <div className="footer-newsletter">
                    <span className="footer-news-titre">NEWSLETTER</span>
                    <span className="footer-news-sub">Inscrivez-vous pour ne rien manquer</span>
                    <button className="footer-news-btn">+ Je m'inscris</button>
                </div>

                <a href="#top" className="footer-top">⤴</a>
            </div>

            {/* Contenu principal */}
            <div className="footer-principal">
                <div className="footer-col">
                    <h4 className="footer-col-titre">Adresse</h4>
                    <p className="footer-col-texte">
                        57 Avenue Ollier<br />
                        Quatre Bornes, Maurice
                    </p>
                    <h4 className="footer-col-titre footer-mt">Téléphone</h4>
                    <a className="footer-lien-rouge" href="tel:+23055525559">+230 555 25 59</a>
                </div>

                <div className="footer-col">
                    <h4 className="footer-col-titre">Navigation</h4>
                    <Link to="/"           className="footer-lien">Accueil</Link>
                    <Link to="/formations" className="footer-lien">Formations</Link>
                    <Link to="/"           className="footer-lien">À propos</Link>
                    <Link to="/"           className="footer-lien">Contact</Link>
                </div>

                <div className="footer-col">
                    <h4 className="footer-col-titre">Informations</h4>
                    <a href="#" className="footer-lien">Mentions légales</a>
                    <a href="#" className="footer-lien">Conditions générales</a>
                    <a href="#" className="footer-lien">Protection des données</a>
                    <a href="#" className="footer-lien">Gestion des cookies</a>
                </div>

                <div className="footer-col footer-col-droite">
                    <div className="footer-social">
                        <a href="#" className="footer-social-btn" aria-label="Facebook">f</a>
                        <a href="#" className="footer-social-btn" aria-label="Instagram">◎</a>
                        <a href="#" className="footer-social-btn" aria-label="YouTube">▶</a>
                    </div>
                    <p className="footer-credit">
                        WEBSITE BY<br />
                        <span>SKILLHUB TEAM</span>
                    </p>
                </div>
            </div>

            {/* Bas de page */}
            <div className="footer-bas">
                <p>© {annee} Skillhub | All Rights Reserved</p>
                <div className="footer-bas-liens">
                    <a href="#">Privacy Policy</a>
                    <span>•</span>
                    <a href="#">Terms & Conditions</a>
                </div>
            </div>

        </footer>
    );
}