<?php

use App\Http\Controllers\AuthController;
use App\Http\Controllers\FormationController;
use App\Http\Controllers\InscriptionController;
use App\Http\Controllers\MessageController;
use App\Http\Controllers\ModuleController;
use Illuminate\Support\Facades\Route;

// ─── Test API ─────────────────────────────────────────────────
Route::get('/test', function () {
    return response()->json(['message' => 'API SkillHub OK']);
});

// ─── Authentification publique ────────────────────────────────
Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);

// ─── Authentification protégée ────────────────────────────────
Route::middleware('auth:api')->group(function () {
    Route::get('/profile', [AuthController::class, 'profile']);
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::post('/profil/photo', [AuthController::class, 'uploadPhoto']);

    // ─── Formations protégées ────────────────────────────────
    Route::post('/formations', [FormationController::class, 'store']);
    Route::put('/formations/{id}', [FormationController::class, 'update']);
    Route::delete('/formations/{id}', [FormationController::class, 'destroy']);
    Route::get('/formateur/mes-formations', [FormationController::class, 'mesFormations']);

    // ─── Modules protégés ────────────────────────────────────
    Route::post('/formations/{id}/modules', [ModuleController::class, 'store']);
    Route::put('/modules/{id}', [ModuleController::class, 'update']);
    Route::delete('/modules/{id}', [ModuleController::class, 'destroy']);
    Route::post('/modules/{id}/terminer', [ModuleController::class, 'terminer']);
    Route::get('/formations/{id}/modules-termines', [ModuleController::class, 'mesModulesTermines']);

    // ─── Inscriptions protégées ──────────────────────────────
    Route::post('/formations/{id}/inscription', [InscriptionController::class, 'store']);
    Route::delete('/formations/{id}/inscription', [InscriptionController::class, 'destroy']);
    Route::get('/apprenant/formations', [InscriptionController::class, 'mesFormations']);

    // ─── Messagerie protégée ─────────────────────────────────
    Route::get('/messages/non-lus', [MessageController::class, 'nonLus']);
    Route::get('/messages/conversations', [MessageController::class, 'conversations']);
    Route::get('/messages/conversation/{interlocuteurId}', [MessageController::class, 'messagerie']);
    Route::post('/messages/envoyer', [MessageController::class, 'envoyer']);
    Route::get('/messages/interlocuteurs', [MessageController::class, 'interlocuteurs']);
});

// ─── Formations publiques ─────────────────────────────────────
Route::get('/formations', [FormationController::class, 'index']);
Route::get('/formations/{id}', [FormationController::class, 'show']);

// ─── Modules publics ──────────────────────────────────────────
Route::get('/formations/{id}/modules', [ModuleController::class, 'index']);

// ─── Gestion des requêtes preflight CORS ──────────────────────
Route::options('/{any}', function () {
    return response('', 200);
})->where('any', '.*');