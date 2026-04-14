<?php

namespace App\Http\Controllers;

use App\Models\Formation;
use App\Models\Inscription;
use App\Services\ActivityLogService;
use Illuminate\Http\JsonResponse;
use Tymon\JWTAuth\Facades\JWTAuth;
use Tymon\JWTAuth\Exceptions\JWTException;

/**
 * Contrôleur de gestion des inscriptions.
 */
class InscriptionController extends Controller
{
    private const USER_NOT_FOUND_MESSAGE = 'Utilisateur non trouvé';
    private const TOKEN_INVALID_OR_ABSENT_MESSAGE = 'Token invalide ou absent';
    private const FORMATION_NOT_FOUND_MESSAGE = 'Formation introuvable';

    /**
     * Inscrire un apprenant à une formation.
     * Route : POST /formations/{id}/inscription
     */
    public function store($formationId): JsonResponse
    {
        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                return response()->json([
                    'message' => self::USER_NOT_FOUND_MESSAGE
                ], 404);
            }

            if ($user->role !== 'apprenant') {
                return response()->json([
                    'message' => "Seul un apprenant peut s'inscrire à une formation"
                ], 403);
            }

            $formation = Formation::find($formationId);

            if (! $formation) {
                return response()->json([
                    'message' => self::FORMATION_NOT_FOUND_MESSAGE
                ], 404);
            }

            $dejaInscrit = Inscription::where('utilisateur_id', $user->id)
                ->where('formation_id', $formation->id)
                ->first();

            if ($dejaInscrit) {
                return response()->json([
                    'message' => 'Vous êtes déjà inscrit à cette formation'
                ], 409);
            }

            $inscription = Inscription::create([
                'utilisateur_id' => $user->id,
                'formation_id' => $formation->id,
                'progression' => 0,
            ]);

            try {
                ActivityLogService::inscriptionFormation($formation->id, $user->id);
            } catch (\Throwable $e) {
                \Log::warning('ActivityLog indisponible (inscription): ' . $e->getMessage());
            }

            return response()->json([
                'message' => 'Inscription réussie',
                'inscription' => $inscription
            ], 201);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Désinscrire un apprenant d'une formation.
     * Route : DELETE /formations/{id}/inscription
     */
    public function destroy($formationId): JsonResponse
    {
        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                return response()->json([
                    'message' => self::USER_NOT_FOUND_MESSAGE
                ], 404);
            }

            if ($user->role !== 'apprenant') {
                return response()->json([
                    'message' => 'Seul un apprenant peut se désinscrire'
                ], 403);
            }

            $inscription = Inscription::where('utilisateur_id', $user->id)
                ->where('formation_id', $formationId)
                ->first();

            if (! $inscription) {
                return response()->json([
                    'message' => 'Inscription introuvable'
                ], 404);
            }

            $inscription->delete();

            return response()->json([
                'message' => 'Désinscription réussie'
            ]);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Liste des formations suivies par l'apprenant connecté.
     * Route : GET /apprenant/formations
     */
    public function mesFormations(): JsonResponse
    {
        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                return response()->json([
                    'message' => self::USER_NOT_FOUND_MESSAGE
                ], 404);
            }

            if ($user->role !== 'apprenant') {
                return response()->json([
                    'message' => 'Seul un apprenant peut voir ses formations'
                ], 403);
            }

            $inscriptions = Inscription::with('formation.formateur:id,nom,email')
                ->where('utilisateur_id', $user->id)
                ->get();

            return response()->json([
                'message' => 'Liste des formations récupérée avec succès',
                'inscriptions' => $inscriptions
            ]);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }
}
