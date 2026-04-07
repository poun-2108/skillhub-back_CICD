import './Bouton.css';

export default function Bouton({
    variante = 'principal',
    taille   = 'moyen',
    onClick,
    disabled = false,
    type     = 'button',
    children,
}) {
    const classes = [
        'bouton',
        `bouton-${variante}`,
        `bouton-${taille}`,
        disabled ? 'bouton-desactive' : '',
    ].join(' ').trim();

    return (
        <button
            type={type}
            className={classes}
            onClick={onClick}
            disabled={disabled}
        >
            {children}
        </button>
    );
}