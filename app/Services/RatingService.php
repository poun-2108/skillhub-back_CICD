<?php

namespace App\Services;

use App\Models\Inscription;
use App\Models\Rating;

/**
 * Service de gestion des notations de formations.
 */
class RatingService
{
    /**
     * Verifier si un apprenant est inscrit a une formation.
     */
    public function estInscrit(int $userId, int $formationId): bool
    {
        return Inscription::where('utilisateur_id', $userId)
            ->where('formation_id', $formationId)
            ->exists();
    }

    /**
     * Verifier si un apprenant a deja note une formation.
     */
    public function aDejaNote(int $userId, int $formationId): bool
    {
        return Rating::where('user_id', $userId)
            ->where('formation_id', $formationId)
            ->exists();
    }

    /**
     * Creer une note pour une formation.
     */
    public function creerNote(int $userId, int $formationId, int $note, ?string $commentaire): Rating
    {
        return Rating::create([
            'user_id'      => $userId,
            'formation_id' => $formationId,
            'note'         => $note,
            'commentaire'  => $commentaire,
        ]);
    }

    /**
     * Calculer la note moyenne d une formation.
     */
    public function noteMoyenne(int $formationId): ?float
    {
        $moyenne = Rating::where('formation_id', $formationId)->avg('note');

        if ($moyenne === null) {
            return null;
        }

        return round((float) $moyenne, 2);
    }

    /**
     * Compter le nombre d avis pour une formation.
     */
    public function nombreAvis(int $formationId): int
    {
        return Rating::where('formation_id', $formationId)->count();
    }
}
