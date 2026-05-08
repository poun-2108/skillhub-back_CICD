<?php

namespace App\Services;

use App\Models\Inscription;

/**
 * Service de gestion des inscriptions.
 */
class InscriptionService
{
    /**
     * Liste des apprenants inscrits a une formation.
     *
     * @return array<int, array{id:int, nom:string, email:string, progression:int, date_inscription:?string}>
     */
    public function listerApprenants(int $formationId): array
    {
        $inscriptions = Inscription::with('utilisateur:id,nom,email')
            ->where('formation_id', $formationId)
            ->get();

        return $inscriptions->map(function (Inscription $inscription) {
            return [
                'id'               => $inscription->utilisateur->id,
                'nom'              => $inscription->utilisateur->nom,
                'email'            => $inscription->utilisateur->email,
                'progression'      => (int) $inscription->progression,
                'date_inscription' => optional($inscription->created_at)->toIso8601String(),
            ];
        })->all();
    }
}
