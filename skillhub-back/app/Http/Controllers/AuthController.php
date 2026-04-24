<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Tymon\JWTAuth\Facades\JWTAuth;
use Tymon\JWTAuth\Exceptions\JWTException;

/**
 * Contrôleur d'authentification.
 */
class AuthController extends Controller
{
    private const USER_NOT_FOUND_MESSAGE = 'Utilisateur non trouvé';
    private const TOKEN_INVALID_OR_ABSENT_MESSAGE = 'Token invalide ou absent';

    /**
     * Inscription utilisateur.
     */
    public function register(Request $request): JsonResponse
    {
        $request->validate([
            'nom' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'password' => 'required|string|min:6|confirmed',
            'role' => 'required|in:apprenant,formateur',
        ]);

        $user = User::create([
            'nom' => $request->input('nom'),
            'email' => $request->input('email'),
            'password' => $request->input('password'),
            'role' => $request->input('role'),
        ]);

        $token = JWTAuth::fromUser($user);

        return response()->json([
            'message' => 'Utilisateur créé avec succès',
            'user' => $user,
            'token' => $token,
        ], 201);
    }

    /**
     * Connexion utilisateur.
     */
    public function login(Request $request): JsonResponse
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required|string',
        ]);

        $credentials = [
            'email' => $request->input('email'),
            'password' => $request->input('password'),
        ];

        $token = JWTAuth::attempt($credentials);

        if (! $token) {
            return response()->json([
                'message' => 'Email ou mot de passe incorrect'
            ], 401);
        }

        /**
         * Important :
         * On récupère l'utilisateur depuis le token JWT,
         * pas avec auth()->user() car le guard par défaut est "web".
         */
        $user = JWTAuth::setToken($token)->toUser();

        return response()->json([
            'message' => 'Connexion réussie',
            'token' => $token,
            'user' => $user,
        ]);
    }

    /**
     * Profil utilisateur connecté.
     */
    public function profile(): JsonResponse
    {
        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                return response()->json([
                    'message' => self::USER_NOT_FOUND_MESSAGE
                ], 404);
            }

            return response()->json([
                'user' => $user
            ]);
        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Déconnexion utilisateur.
     */
    public function logout(): JsonResponse
    {
        try {
            JWTAuth::parseToken()->invalidate();

            return response()->json([
                'message' => 'Déconnexion réussie'
            ]);
        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }

    /**
     * Upload de la photo de profil.
     * Route : POST /api/profil/photo
     */
    public function uploadPhoto(Request $request): JsonResponse
    {
        try {
            $user = JWTAuth::parseToken()->authenticate();

            if (! $user) {
                return response()->json([
                    'message' => self::USER_NOT_FOUND_MESSAGE
                ], 404);
            }

            $request->validate([
                'photo' => 'required|image|mimes:jpeg,png,jpg,gif|max:2048',
            ]);

            if ($user->photo_profil && file_exists(public_path($user->photo_profil))) {
                unlink(public_path($user->photo_profil));
            }

            $fichier = $request->file('photo');
            $nomFichier = 'profil_' . $user->id . '_' . time() . '.' . $fichier->getClientOriginalExtension();

            $fichier->move(public_path('images/profils'), $nomFichier);

            $user->photo_profil = '/images/profils/' . $nomFichier;
            $user->save();

            return response()->json([
                'message' => 'Photo mise à jour avec succès',
                'photo_profil' => $user->photo_profil,
                'user' => $user,
            ]);
        } catch (JWTException $e) {
            return response()->json([
                'message' => self::TOKEN_INVALID_OR_ABSENT_MESSAGE
            ], 401);
        }
    }
}
