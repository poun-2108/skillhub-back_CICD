<?php

use App\Http\Controllers\AuthController;
use App\Http\Controllers\FormationController;
use App\Http\Controllers\InscriptionController;
use App\Http\Controllers\MessageController;
use App\Http\Controllers\ModuleController;
use Illuminate\Support\Facades\Route;

if (! defined('ROUTE_FORMATION_BY_ID')) {
    define('ROUTE_FORMATION_BY_ID', '/formations/{id}');
}

if (! defined('ROUTE_FORMATIONS')) {
    define('ROUTE_FORMATIONS', '/formations');
}

if (! defined('ROUTE_FORMATION_MODULES')) {
    define('ROUTE_FORMATION_MODULES', '/formations/{id}/modules');
}

if (! defined('ROUTE_FORMATION_MODULES_TERMINES')) {
    define('ROUTE_FORMATION_MODULES_TERMINES', '/formations/{id}/modules-termines');
}

if (! defined('ROUTE_FORMATION_INSCRIPTION')) {
    define('ROUTE_FORMATION_INSCRIPTION', '/formations/{id}/inscription');
}

if (! defined('ROUTE_MODULE_BY_ID')) {
    define('ROUTE_MODULE_BY_ID', '/modules/{id}');
}

if (! defined('ROUTE_MODULE_TERMINER')) {
    define('ROUTE_MODULE_TERMINER', '/modules/{id}/terminer');
}

if (! defined('ROUTE_MESSAGES_NON_LUS')) {
    define('ROUTE_MESSAGES_NON_LUS', '/messages/non-lus');
}

if (! defined('ROUTE_MESSAGES_CONVERSATIONS')) {
    define('ROUTE_MESSAGES_CONVERSATIONS', '/messages/conversations');
}

if (! defined('ROUTE_MESSAGES_CONVERSATION')) {
    define('ROUTE_MESSAGES_CONVERSATION', '/messages/conversation/{interlocuteurId}');
}

if (! defined('ROUTE_MESSAGES_ENVOYER')) {
    define('ROUTE_MESSAGES_ENVOYER', '/messages/envoyer');
}

if (! defined('ROUTE_MESSAGES_INTERLOCUTEURS')) {
    define('ROUTE_MESSAGES_INTERLOCUTEURS', '/messages/interlocuteurs');
}

if (! defined('ROUTE_FORMATEUR_MES_FORMATIONS')) {
    define('ROUTE_FORMATEUR_MES_FORMATIONS', '/formateur/mes-formations');
}

if (! defined('ROUTE_APPRENANT_FORMATIONS')) {
    define('ROUTE_APPRENANT_FORMATIONS', '/apprenant/formations');
}

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
    Route::post(ROUTE_FORMATIONS, [FormationController::class, 'store']);
    Route::put(ROUTE_FORMATION_BY_ID, [FormationController::class, 'update']);
    Route::delete(ROUTE_FORMATION_BY_ID, [FormationController::class, 'destroy']);
    Route::get(ROUTE_FORMATEUR_MES_FORMATIONS, [FormationController::class, 'mesFormations']);

    // ─── Modules protégés ────────────────────────────────────
    Route::post(ROUTE_FORMATION_MODULES, [ModuleController::class, 'store']);
    Route::put(ROUTE_MODULE_BY_ID, [ModuleController::class, 'update']);
    Route::delete(ROUTE_MODULE_BY_ID, [ModuleController::class, 'destroy']);
    Route::post(ROUTE_MODULE_TERMINER, [ModuleController::class, 'terminer']);
    Route::get(ROUTE_FORMATION_MODULES_TERMINES, [ModuleController::class, 'mesModulesTermines']);

    // ─── Inscriptions protégées ──────────────────────────────
    Route::post(ROUTE_FORMATION_INSCRIPTION, [InscriptionController::class, 'store']);
    Route::delete(ROUTE_FORMATION_INSCRIPTION, [InscriptionController::class, 'destroy']);
    Route::get(ROUTE_APPRENANT_FORMATIONS, [InscriptionController::class, 'mesFormations']);

    // ─── Messagerie protégée ─────────────────────────────────
    Route::get(ROUTE_MESSAGES_NON_LUS, [MessageController::class, 'nonLus']);
    Route::get(ROUTE_MESSAGES_CONVERSATIONS, [MessageController::class, 'conversations']);
    Route::get(ROUTE_MESSAGES_CONVERSATION, [MessageController::class, 'messagerie']);
    Route::post(ROUTE_MESSAGES_ENVOYER, [MessageController::class, 'envoyer']);
    Route::get(ROUTE_MESSAGES_INTERLOCUTEURS, [MessageController::class, 'interlocuteurs']);
});

// ─── Formations publiques ─────────────────────────────────────
Route::get(ROUTE_FORMATIONS, [FormationController::class, 'index']);
Route::get(ROUTE_FORMATION_BY_ID, [FormationController::class, 'show']);

// ─── Modules publics ──────────────────────────────────────────
Route::get(ROUTE_FORMATION_MODULES, [ModuleController::class, 'index']);

// ─── Gestion des requêtes preflight CORS ──────────────────────
Route::options('/{any}', function () {
    return response('', 200);
})->where('any', '.*');
