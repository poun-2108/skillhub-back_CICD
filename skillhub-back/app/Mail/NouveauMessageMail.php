<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Queue\SerializesModels;

/**
 * Mail envoyé au destinataire lors du premier message d'une conversation.
 */
class NouveauMessageMail extends Mailable
{
    use Queueable, SerializesModels;

    public string $expediteur;
    public string $destinataire;
    public string $contenu;
    public string $lienPlateforme;

    public function __construct(string $expediteur, string $destinataire, string $contenu)
    {
        $this->expediteur     = $expediteur;
        $this->destinataire   = $destinataire;
        $this->contenu        = $contenu;
        $this->lienPlateforme = config('app.url');
    }

    public function build(): static
    {
        $html = "
            <h2>Nouveau message reçu sur SkillHub</h2>
            <p>Bonjour <strong>{$this->destinataire}</strong>,</p>
            <p><strong>{$this->expediteur}</strong> vous a envoyé un message :</p>
            <blockquote style='border-left:4px solid #6366f1;padding:12px;background:#f5f3ff;margin:16px 0;'>
                " . nl2br(htmlspecialchars($this->contenu)) . "
            </blockquote>
            <p>Connectez-vous sur <a href='{$this->lienPlateforme}'>{$this->lienPlateforme}</a> pour répondre.</p>
            <p style='color:#888;font-size:12px;'>— L'équipe SkillHub</p>
        ";

        return $this->subject('SkillHub — Nouveau message de ' . $this->expediteur)
                    ->html($html);
    }
}