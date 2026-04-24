<?php

namespace Tests\Feature;

use App\Models\Formation;
use App\Models\Module;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Http\UploadedFile;
use PHPUnit\Framework\Attributes\Test;
use Tests\TestCase;
use Tymon\JWTAuth\Facades\JWTAuth;

class ControllerCoverageTest extends TestCase
{
    use RefreshDatabase;

    private function headers(string $token): array
    {
        return ['Authorization' => 'Bearer ' . $token];
    }

    private function creerUtilisateur(string $role): array
    {
        $user = User::create([
            'nom' => 'Test ' . ucfirst($role),
            'email' => $role . '_' . uniqid() . '@test.com',
            'password' => bcrypt('password123'),
            'role' => $role,
        ]);

        return ['user' => $user, 'token' => JWTAuth::fromUser($user)];
    }

    private function creerFormation(User $formateur): Formation
    {
        return Formation::create([
            'titre' => 'Formation Test',
            'description' => 'Description de test',
            'categorie' => 'developpement_web',
            'niveau' => 'debutant',
            'nombre_de_vues' => 0,
            'formateur_id' => $formateur->id,
        ]);
    }

    private function creerModule(Formation $formation, int $ordre = 1): Module
    {
        return Module::create([
            'titre' => 'Module ' . $ordre,
            'contenu' => 'Contenu du module ' . $ordre,
            'ordre' => $ordre,
            'formation_id' => $formation->id,
        ]);
    }

    #[Test]
    public function auth_couvre_les_cas_absents(): void
    {
        $this->withoutMiddleware();

        $this->postJson('/api/logout', [])
            ->assertStatus(401);

        $this->getJson('/api/profile')
            ->assertStatus(401);

        $file = UploadedFile::fake()->create('photo.jpg', 10, 'image/jpeg');
        $this->post('/api/profil/photo', ['photo' => $file])
            ->assertStatus(401);
    }

    #[Test]
    public function formations_couvrent_les_cas_absents_et_sans_token(): void
    {
        $this->withoutMiddleware();

        $this->getJson('/api/formateur/mes-formations')
            ->assertStatus(401);

        ['user' => $formateur, 'token' => $token] = $this->creerUtilisateur('formateur');
        $formation = $this->creerFormation($formateur);

        $this->getJson('/api/formations/9999', $this->headers($token))
            ->assertStatus(404);

        $this->postJson('/api/formations', [
            'titre' => 'Sans token',
        ])
            ->assertStatus(401);

        $this->postJson('/api/formations/9999/modules', [
            'titre' => 'Module',
            'contenu' => 'Contenu',
            'ordre' => 1,
        ], $this->headers($token))
            ->assertStatus(404);

        $this->putJson('/api/formations/9999', [
            'titre' => 'Titre',
            'description' => 'Desc',
            'categorie' => 'developpement_web',
            'niveau' => 'debutant',
        ], $this->headers($token))
            ->assertStatus(404);

        $this->deleteJson('/api/formations/9999', [], $this->headers($token))
            ->assertStatus(404);

        $this->getJson('/api/formateur/mes-formations', $this->headers($token))
            ->assertStatus(200);
    }

    #[Test]
    public function inscriptions_couvrent_les_cas_absents_et_sans_token(): void
    {
        $this->withoutMiddleware();

        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        $formation = $this->creerFormation($formateur);

        $this->postJson('/api/formations/' . $formation->id . '/inscription', [])
            ->assertStatus(401);

        ['user' => $apprenant, 'token' => $token] = $this->creerUtilisateur('apprenant');

        $this->postJson('/api/formations/9999/inscription', [], $this->headers($token))
            ->assertStatus(404);

        $this->deleteJson('/api/formations/' . $formation->id . '/inscription')
            ->assertStatus(401);

        $this->deleteJson('/api/formations/9999/inscription', [], $this->headers($token))
            ->assertStatus(404);

        ['user' => $formateur2, 'token' => $token2] = $this->creerUtilisateur('formateur');
        $this->getJson('/api/apprenant/formations', $this->headers($token2))
            ->assertStatus(403);
    }

    #[Test]
    public function modules_couvrent_les_cas_absents_et_sans_token(): void
    {
        $this->withoutMiddleware();

        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        $formation = $this->creerFormation($formateur);
        $module = $this->creerModule($formation);

        $this->postJson('/api/formations/' . $formation->id . '/modules', [])
            ->assertStatus(401);

        ['user' => $formateur2, 'token' => $token2] = $this->creerUtilisateur('formateur');

        $this->postJson('/api/formations/9999/modules', [
            'titre' => 'Module',
            'contenu' => 'Contenu',
            'ordre' => 1,
        ], $this->headers($token2))
            ->assertStatus(404);

        $this->putJson('/api/modules/' . $module->id, [
            'titre' => 'Module modifié',
            'contenu' => 'Contenu modifié',
            'ordre' => 1,
        ], $this->headers($token2))
            ->assertStatus(403);

        $this->deleteJson('/api/modules/' . $module->id)
            ->assertStatus(401);

        $this->deleteJson('/api/modules/9999', [], $this->headers($token2))
            ->assertStatus(404);

        ['user' => $apprenant, 'token' => $tokenApprenant] = $this->creerUtilisateur('apprenant');

        $this->getJson('/api/formations/' . $formation->id . '/modules-termines', $this->headers($tokenApprenant))
            ->assertStatus(200)
            ->assertJsonStructure(['modules_termines']);

        $this->getJson('/api/formations/' . $formation->id . '/modules-termines', $this->headers($token2))
            ->assertStatus(403);

        $this->postJson('/api/modules/' . $module->id . '/terminer', [], $this->headers($token2))
            ->assertStatus(403);

        $this->postJson('/api/modules/' . $module->id . '/terminer', [], $this->headers($tokenApprenant))
            ->assertStatus(403);

        $this->postJson('/api/modules/9999/terminer', [], $this->headers($tokenApprenant))
            ->assertStatus(404);

        $this->postJson('/api/modules/' . $module->id . '/terminer', [])
            ->assertStatus(401);
    }
}

