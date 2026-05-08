<?php

namespace App\Http\Controllers;

use App\Models\Formation;
use App\Services\RatingService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;
use Tymon\JWTAuth\Exceptions\JWTException;
use Tymon\JWTAuth\Facades\JWTAuth;

/**
 * Controleur de gestion des notations de formations.
 */
class RatingController extends Controller
{
    private const MSG_TOKEN_INVALIDE  = 'Token invalide ou absent';
    private const MSG_USER_NON_TROUVE = 'Utilisateur non trouvé';
    private const MSG_FORMATION_INTRO = 'Formation introuvable';

    public function __construct(private readonly RatingService $ratingService)
    {
    }

    /**
     * Noter une formation.
     * Route : POST /api/formations/{id}/noter
     */
    public function noter(Request $request, $formationId): JsonResponse
    {
        $reponse = response()->json(['message' => self::MSG_TOKEN_INVALIDE], 401);

        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                $reponse = response()->json(['message' => self::MSG_USER_NON_TROUVE], 404);
            } else {
                $reponse = $this->traiterNote($request, $user, (int) $formationId);
            }
        } catch (JWTException $e) {
            // reponse 401 deja definie
        }

        return $reponse;
    }

    /**
     * Logique de creation d une note pour un utilisateur authentifie.
     */
    private function traiterNote(Request $request, $user, int $formationId): JsonResponse
    {
        $formation = Formation::find($formationId);

        if (! $formation) {
            return response()->json(['message' => self::MSG_FORMATION_INTRO], 404);
        }

        if ($user->role !== 'apprenant'
            || ! $this->ratingService->estInscrit($user->id, $formation->id)) {
            return response()->json([
                'message' => 'Vous devez être inscrit à la formation pour la noter',
            ], 403);
        }

        try {
            $donnees = $request->validate([
                'note'        => 'required|integer|min:1|max:5',
                'commentaire' => 'nullable|string|max:1000',
            ]);
        } catch (ValidationException $e) {
            return response()->json([
                'message' => 'Note hors de l\'intervalle [1-5]',
                'errors'  => $e->errors(),
            ], 400);
        }

        if ($this->ratingService->aDejaNote($user->id, $formation->id)) {
            return response()->json([
                'message' => 'Vous avez déjà noté cette formation',
            ], 400);
        }

        $rating = $this->ratingService->creerNote(
            $user->id,
            $formation->id,
            (int) $donnees['note'],
            $donnees['commentaire'] ?? null,
        );

        return response()->json($rating, 201);
    }
}
