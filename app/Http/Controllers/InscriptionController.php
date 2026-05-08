<?php

namespace App\Http\Controllers;

use App\Models\Formation;
use App\Models\Inscription;
use App\Services\ActivityLogService;
use App\Services\InscriptionService;
use Illuminate\Http\JsonResponse;
use Tymon\JWTAuth\Facades\JWTAuth;
use Tymon\JWTAuth\Exceptions\JWTException;

/**
 * Controleur de gestion des inscriptions.
 */
class InscriptionController extends Controller
{
    private const MSG_TOKEN_INVALIDE  = 'Token invalide ou absent';
    private const MSG_USER_NON_TROUVE = 'Utilisateur non trouvé';
    private const MSG_FORMATION_INTRO = 'Formation introuvable';

    public function __construct(private readonly InscriptionService $inscriptionService = new InscriptionService())
    {
    }

    /**
     * Inscrire un apprenant a une formation.
     * Route : POST /formations/{id}/inscription
     */
    public function store($formationId): JsonResponse
    {
        $reponse = response()->json(['message' => self::MSG_TOKEN_INVALIDE], 401);

        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                $reponse = response()->json(['message' => self::MSG_USER_NON_TROUVE], 404);
            } elseif ($user->role !== 'apprenant') {
                $reponse = response()->json([
                    'message' => "Seul un apprenant peut s'inscrire à une formation",
                ], 403);
            } else {
                $formation = Formation::find($formationId);

                if (! $formation) {
                    $reponse = response()->json(['message' => 'Formation introuvable'], 404);
                } else {
                    $dejaInscrit = Inscription::where('utilisateur_id', $user->id)
                        ->where('formation_id', $formation->id)
                        ->first();

                    if ($dejaInscrit) {
                        $reponse = response()->json([
                            'message' => 'Vous êtes déjà inscrit à cette formation',
                        ], 409);
                    } else {
                        $inscription = Inscription::create([
                            'utilisateur_id' => $user->id,
                            'formation_id'   => $formation->id,
                            'progression'    => 0,
                        ]);

                        ActivityLogService::inscriptionFormation($formation->id, $user->id);

                        $reponse = response()->json([
                            'message'     => 'Inscription réussie',
                            'inscription' => $inscription,
                        ], 201);
                    }
                }
            }
        } catch (JWTException $e) {
            // reponse 401 deja definie
        }

        return $reponse;
    }

    /**
     * Desinscrire un apprenant d une formation.
     * Route : DELETE /formations/{id}/inscription
     */
    public function destroy($formationId): JsonResponse
    {
        $reponse = response()->json(['message' => self::MSG_TOKEN_INVALIDE], 401);

        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                $reponse = response()->json(['message' => self::MSG_USER_NON_TROUVE], 404);
            } elseif ($user->role !== 'apprenant') {
                $reponse = response()->json(['message' => 'Seul un apprenant peut se désinscrire'], 403);
            } else {
                $inscription = Inscription::where('utilisateur_id', $user->id)
                    ->where('formation_id', $formationId)
                    ->first();

                if (! $inscription) {
                    $reponse = response()->json(['message' => 'Inscription introuvable'], 404);
                } else {
                    $inscription->delete();
                    $reponse = response()->json(['message' => 'Désinscription réussie']);
                }
            }
        } catch (JWTException $e) {
            // reponse 401 deja definie
        }

        return $reponse;
    }

    /**
     * Liste des apprenants inscrits a une formation.
     * Accessible uniquement par le formateur proprietaire.
     * Route : GET /api/formations/{id}/apprenants
     */
    public function apprenants($formationId): JsonResponse
    {
        $reponse = response()->json(['message' => self::MSG_TOKEN_INVALIDE], 401);

        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                $reponse = response()->json(['message' => self::MSG_USER_NON_TROUVE], 404);
            } else {
                $reponse = $this->traiterListeApprenants($user, (int) $formationId);
            }
        } catch (JWTException $e) {
            // reponse 401 deja definie
        }

        return $reponse;
    }

    /**
     * Logique de recuperation de la liste des apprenants pour un formateur authentifie.
     */
    private function traiterListeApprenants($user, int $formationId): JsonResponse
    {
        $formation = Formation::find($formationId);

        if (! $formation) {
            return response()->json(['message' => self::MSG_FORMATION_INTRO], 404);
        }

        if ($user->role !== 'formateur' || $formation->formateur_id !== $user->id) {
            return response()->json([
                'message' => 'Vous n\'êtes pas propriétaire de cette formation',
            ], 403);
        }

        return response()->json($this->inscriptionService->listerApprenants($formation->id));
    }

    /**
     * Liste des formations suivies par l apprenant connecte.
     * Route : GET /apprenant/formations
     */
    public function mesFormations(): JsonResponse
    {
        $reponse = response()->json(['message' => self::MSG_TOKEN_INVALIDE], 401);

        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                $reponse = response()->json(['message' => self::MSG_USER_NON_TROUVE], 404);
            } elseif ($user->role !== 'apprenant') {
                $reponse = response()->json([
                    'message' => 'Seul un apprenant peut voir ses formations',
                ], 403);
            } else {
                $inscriptions = Inscription::with('formation.formateur:id,nom,email')
                    ->where('utilisateur_id', $user->id)
                    ->get();

                $reponse = response()->json($inscriptions);
            }
        } catch (JWTException $e) {
            // reponse 401 deja definie
        }

        return $reponse;
    }
}
