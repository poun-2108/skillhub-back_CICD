<?php

namespace App\Http\Controllers;

use App\Models\Formation;
use App\Models\FormationVue;
use App\Services\ActivityLogService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Tymon\JWTAuth\Facades\JWTAuth;
use Tymon\JWTAuth\Exceptions\JWTException;

/**
 * Contrôleur de gestion des formations.
 */
class FormationController extends Controller
{
    private const USER_NOT_FOUND_MESSAGE = 'Utilisateur non trouvé';
    private const TOKEN_INVALID_OR_ABSENT_MESSAGE = 'Token invalide ou absent';
    private const FORMATION_NOT_FOUND_MESSAGE = 'Formation introuvable';

    /**
     * Liste des formations du formateur connecté (endpoint dédié).
     * Route : GET /formateur/mes-formations
     */
    public function mesFormations(): JsonResponse
    {
        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                return response()->json(['message' => self::USER_NOT_FOUND_MESSAGE], 404);
            }

            if ($user->role !== 'formateur') {
                return response()->json(['message' => 'Accès réservé aux formateurs'], 403);
            }

            $formations = Formation::with('formateur:id,nom,email')
                ->withCount('inscriptions')
                ->where('formateur_id', $user->id)
                ->get();

            return response()->json($formations);

        } catch (JWTException $e) {
            return response()->json(['message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE], 401);
        }
    }

    /**
     * Liste des formations avec filtres optionnels.
     * Route : GET /formations
     */
    public function index(Request $request): JsonResponse
    {
        $query = Formation::with('formateur:id,nom,email')
            ->withCount('inscriptions');

        if ($request->filled('recherche')) {
            $motCle = $request->input('recherche');

            $query->where(function ($q) use ($motCle) {
                $q->where('titre', 'like', '%' . $motCle . '%')
                    ->orWhere('description', 'like', '%' . $motCle . '%');
            });
        }

        if ($request->filled('categorie')) {
            $query->where('categorie', $request->input('categorie'));
        }

        if ($request->filled('niveau')) {
            $query->where('niveau', $request->input('niveau'));
        }

        return response()->json($query->get());
    }

    /**
     * Afficher une formation et incrémenter ses vues de façon unique.
     * Route : GET /formations/{id}
     */
    public function show(Request $request, $id): JsonResponse
    {
        $formation = Formation::with('formateur:id,nom,email')
            ->withCount('inscriptions')
            ->find($id);

        if (! $formation) {
            return response()->json([
                'message' => self::FORMATION_NOT_FOUND_MESSAGE
            ], 404);
        }

        $utilisateurId = null;

        try {
            $user = JWTAuth::parseToken()->authenticate();
            if ($user) {
                $utilisateurId = $user->id;
            }
        } catch (JWTException $e) {
            // Utilisateur non connecté
        }

        if ($utilisateurId) {
            // Utilisateur connecté — vue unique par utilisateur
            try {
                $created = FormationVue::firstOrCreate(
                    [
                        'formation_id'   => $formation->id,
                        'utilisateur_id' => $utilisateurId,
                    ],
                    ['ip' => $request->ip()]
                );

                // N'incrémente que si la vue vient d'être créée (pas un doublon)
                if ($created->wasRecentlyCreated) {
                    $formation->increment('nombre_de_vues');
                }
            } catch (\Throwable $e) {
                // Unique constraint violation = vue déjà comptée, on ignore
            }
        } else {
            // Visiteur anonyme — vue unique par IP
            try {
                $created = FormationVue::firstOrCreate(
                    [
                        'formation_id'   => $formation->id,
                        'utilisateur_id' => null,
                        'ip'             => $request->ip(),
                    ]
                );

                if ($created->wasRecentlyCreated) {
                    $formation->increment('nombre_de_vues');
                }
            } catch (\Throwable $e) {
                // Ignore les doublons
            }
        }

        // Log de consultation (MongoDB)
        try {
            ActivityLogService::consultationFormation($formation->id, $utilisateurId);
        } catch (\Throwable $e) {
            \Log::warning('ActivityLog indisponible (show): ' . $e->getMessage());
        }

        // Rafraîchit le modèle pour renvoyer le nombre_de_vues à jour
        $formation->refresh();
        $formation->load('formateur:id,nom,email');
        $formation->loadCount('inscriptions');

        return response()->json($formation);
    }

    /**
     * Créer une nouvelle formation.
     * Route : POST /formations
     */
    public function store(Request $request): JsonResponse
    {
        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                return response()->json([
                    'message' => self::USER_NOT_FOUND_MESSAGE
                ], 404);
            }

            if ($user->role !== 'formateur') {
                return response()->json([
                    'message' => 'Seul un formateur peut créer une formation'
                ], 403);
            }

            $request->validate([
                'titre' => 'required|string|max:255',
                'description' => 'required|string',
                'categorie' => 'required|in:developpement_web,data,design,marketing,devops,autre',
                'niveau' => 'required|in:debutant,intermediaire,avance',
                'prix' => 'nullable|numeric|min:0',
                'duree_heures' => 'nullable|integer|min:0',
            ]);

            $formation = Formation::create([
                'titre' => $request->input('titre'),
                'description' => $request->input('description'),
                'categorie' => $request->input('categorie'),
                'niveau' => $request->input('niveau'),
                'prix' => $request->input('prix', 0),
                'duree_heures' => $request->input('duree_heures', 0),
                'nombre_de_vues' => 0,
                'formateur_id' => $user->id,
            ]);

            try {
                ActivityLogService::creationFormation($formation->id, $user->id);
            } catch (\Throwable $e) {
                \Log::warning('ActivityLog indisponible (store): ' . $e->getMessage());
            }

            return response()->json([
                'message' => 'Formation créée avec succès',
                'formation' => $formation
            ], 201);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Mettre à jour une formation.
     * Route : PUT /formations/{id}
     */
    public function update(Request $request, $id): JsonResponse
    {
        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                return response()->json([
                    'message' => self::USER_NOT_FOUND_MESSAGE
                ], 404);
            }

            $formation = Formation::find($id);

            if (! $formation) {
                return response()->json([
                    'message' => self::FORMATION_NOT_FOUND_MESSAGE
                ], 404);
            }

            if ($user->role !== 'formateur' || $formation->formateur_id !== $user->id) {
                return response()->json([
                    'message' => 'Action non autorisée'
                ], 403);
            }

            $request->validate([
                'titre' => 'required|string|max:255',
                'description' => 'required|string',
                'categorie' => 'required|in:developpement_web,data,design,marketing,devops,autre',
                'niveau' => 'required|in:debutant,intermediaire,avance',
                'prix' => 'nullable|numeric|min:0',
                'duree_heures' => 'nullable|integer|min:0',
            ]);

            $oldValues = $formation->getOriginal();

            $formation->update([
                'titre' => $request->input('titre'),
                'description' => $request->input('description'),
                'categorie' => $request->input('categorie'),
                'niveau' => $request->input('niveau'),
                'prix' => $request->input('prix', $formation->prix),
                'duree_heures' => $request->input('duree_heures', $formation->duree_heures),
            ]);

            try {
                ActivityLogService::modificationFormation($formation->id, $user->id, $oldValues, $formation->getChanges());
            } catch (\Throwable $e) {
                \Log::warning('ActivityLog indisponible (update): ' . $e->getMessage());
            }

            return response()->json([
                'message' => 'Formation mise à jour avec succès',
                'formation' => $formation
            ]);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Supprimer une formation.
     * Route : DELETE /formations/{id}
     */
    public function destroy($id): JsonResponse
    {
        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                return response()->json([
                    'message' => self::USER_NOT_FOUND_MESSAGE
                ], 404);
            }

            $formation = Formation::find($id);

            if (! $formation) {
                return response()->json([
                    'message' => self::FORMATION_NOT_FOUND_MESSAGE
                ], 404);
            }

            if ($user->role !== 'formateur' || $formation->formateur_id !== $user->id) {
                return response()->json([
                    'message' => 'Action non autorisée'
                ], 403);
            }

            try {
                ActivityLogService::suppressionFormation($formation->id, $user->id);
            } catch (\Throwable $e) {
                \Log::warning('ActivityLog indisponible (destroy): ' . $e->getMessage());
            }

            $formation->delete();

            return response()->json([
                'message' => 'Formation supprimée avec succès'
            ]);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }
}
