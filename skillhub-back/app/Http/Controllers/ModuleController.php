<?php

namespace App\Http\Controllers;

use App\Models\Formation;
use App\Models\Inscription;
use App\Models\Module;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Tymon\JWTAuth\Facades\JWTAuth;
use Tymon\JWTAuth\Exceptions\JWTException;

/**
 * Contrôleur de gestion des modules.
 */
class ModuleController extends Controller
{
    private const USER_NOT_FOUND_MESSAGE = 'Utilisateur non trouvé';
    private const TOKEN_INVALID_OR_ABSENT_MESSAGE = 'Token invalide ou absent';
    private const MODULE_NOT_FOUND_MESSAGE = 'Module introuvable';

    /**
     * Lister les modules d'une formation.
     * Route : GET /formations/{id}/modules
     */
    public function index($formationId): JsonResponse
    {
        $modules = Module::where('formation_id', $formationId)
            ->orderBy('ordre')
            ->get();

        return response()->json($modules);
    }

    /**
     * Créer un module.
     * Route : POST /formations/{id}/modules
     */
    public function store(Request $request, $formationId): JsonResponse
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
                    'message' => 'Seul un formateur peut créer un module'
                ], 403);
            }

            $formation = Formation::find($formationId);

            if (! $formation) {
                return response()->json([
                    'message' => self::MODULE_NOT_FOUND_MESSAGE
                ], 404);
            }

            if ($formation->formateur_id !== $user->id) {
                return response()->json([
                    'message' => 'Vous ne pouvez pas modifier une formation qui ne vous appartient pas'
                ], 403);
            }

            $data = $request->validate([
                'titre' => 'required|string|max:255',
                'contenu' => 'required|string',
                'ordre' => 'required|integer|min:1',
            ]);

            $module = Module::create([
                'titre' => $data['titre'],
                'contenu' => $data['contenu'],
                'ordre' => $data['ordre'],
                'formation_id' => $formationId,
            ]);

            return response()->json([
                'message' => 'Module créé avec succès',
                'module' => $module
            ], 201);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Mettre à jour un module.
     * Route : PUT /modules/{id}
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

            if ($user->role !== 'formateur') {
                return response()->json([
                    'message' => 'Seul un formateur peut modifier un module'
                ], 403);
            }

            $module = Module::find($id);

            if (! $module) {
                return response()->json([
                    'message' => self::MODULE_NOT_FOUND_MESSAGE
                ], 404);
            }

            $formation = Formation::find($module->formation_id);

            if (! $formation || $formation->formateur_id !== $user->id) {
                return response()->json([
                    'message' => 'Action non autorisée'
                ], 403);
            }

            $data = $request->validate([
                'titre' => 'required|string|max:255',
                'contenu' => 'required|string',
                'ordre' => 'required|integer|min:1',
            ]);

            $module->update([
                'titre' => $data['titre'],
                'contenu' => $data['contenu'],
                'ordre' => $data['ordre'],
            ]);

            return response()->json([
                'message' => 'Module mis à jour avec succès',
                'module' => $module
            ]);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Supprimer un module.
     * Route : DELETE /modules/{id}
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

            if ($user->role !== 'formateur') {
                return response()->json([
                    'message' => 'Seul un formateur peut supprimer un module'
                ], 403);
            }

            $module = Module::find($id);

            if (! $module) {
                return response()->json([
                    'message' => self::MODULE_NOT_FOUND_MESSAGE
                ], 404);
            }

            $formation = Formation::find($module->formation_id);

            if (! $formation || $formation->formateur_id !== $user->id) {
                return response()->json([
                    'message' => 'Action non autorisée'
                ], 403);
            }

            $module->delete();

            return response()->json([
                'message' => 'Module supprimé avec succès'
            ]);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Retourne les IDs des modules terminés par l'apprenant pour une formation.
     * Route : GET /formations/{id}/modules-termines
     */
    public function mesModulesTermines($formationId): JsonResponse
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
                    'message' => 'Seul un apprenant peut consulter ses modules terminés'
                ], 403);
            }

            $ids = $user->modulesTermines()
                ->where('formation_id', $formationId)
                ->pluck('modules.id');

            return response()->json([
                'modules_termines' => $ids
            ]);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Marquer un module comme terminé.
     * Route : POST /modules/{id}/terminer
     */
    public function terminer($id): JsonResponse
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
                    'message' => 'Seul un apprenant peut terminer un module'
                ], 403);
            }

            $module = Module::find($id);

            if (! $module) {
                return response()->json([
                    'message' => self::MODULE_NOT_FOUND_MESSAGE
                ], 404);
            }

            $inscription = Inscription::where('utilisateur_id', $user->id)
                ->where('formation_id', $module->formation_id)
                ->first();

            if (! $inscription) {
                return response()->json([
                    'message' => "Vous n'êtes pas inscrit à cette formation"
                ], 403);
            }

            $dejaTermine = $user->modulesTermines()
                ->where('modules.id', $module->id)
                ->exists();

            if ($dejaTermine) {
                return response()->json([
                    'message' => 'Ce module est déjà terminé',
                    'progression' => $inscription->progression
                ]);
            }

            /**
             * syncWithoutDetaching évite les doublons.
             */
            $user->modulesTermines()->syncWithoutDetaching([
                $module->id => ['termine' => true]
            ]);

            $totalModules = Module::where('formation_id', $module->formation_id)->count();

            $modulesTermines = $user->modulesTermines()
                ->where('formation_id', $module->formation_id)
                ->count();

            $progression = $totalModules > 0
                ? round(($modulesTermines / $totalModules) * 100)
                : 0;

            $inscription->progression = $progression;
            $inscription->save();

            return response()->json([
                'message' => 'Module terminé avec succès',
                'progression' => $inscription->progression
            ]);

        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }
}
