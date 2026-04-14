<?php

namespace Tests\Feature;

use App\Models\Formation;
use App\Models\Inscription;
use App\Models\Module;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
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
        ['token' => $token] = $this->creerUtilisateur('apprenant');
        $user = User::findOrFail(JWTAuth::setToken($token)->authenticate()->id);
        $user->delete();

        $this->getJson('/api/profile', $this->headers($token))
            ->assertStatus(404);

        $this->postJson('/api/logout', [])
            ->assertStatus(401);

        $file = \Illuminate\Http\UploadedFile::fake()->image('photo.jpg');
        $this->post('/api/profil/photo', ['photo' => $file], $this->headers($token))
            ->assertStatus(404);
    }

    #[Test]
    public function formations_couvrent_les_cas_absents_et_sans_token(): void
    {
        $this->getJson('/api/formateur/mes-formations')
            ->assertStatus(401);

        ['user' => $formateur, 'token' => $token] = $this->creerUtilisateur('formateur');
        $formateur->delete();
        $this->getJson('/api/formateur/mes-formations', $this->headers($token))
            ->assertStatus(404);

        ['user' => $formateur2, 'token' => $token2] = $this->creerUtilisateur('formateur');
        $this->postJson('/api/formations', [
            'titre' => 'Sans token',
        ])
            ->assertStatus(401);

        $this->postJson('/api/formations', [
            'titre' => 'Titre',
            'description' => 'Desc',
            'categorie' => 'developpement_web',
            'niveau' => 'debutant',
        ], $this->headers($token2))
            ->assertStatus(201);

        $formation = Formation::first();
        $formateur2->delete();
        $this->postJson('/api/formations', [
            'titre' => 'Titre 2',
            'description' => 'Desc 2',
            'categorie' => 'developpement_web',
            'niveau' => 'debutant',
        ], $this->headers($token2))
            ->assertStatus(404);

        ['user' => $formateur3, 'token' => $token3] = $this->creerUtilisateur('formateur');
        $formation3 = $this->creerFormation($formateur3);
        $this->putJson('/api/formations/' . $formation3->id, [
            'titre' => 'Titre',
            'description' => 'Desc',
            'categorie' => 'developpement_web',
            'niveau' => 'debutant',
        ])
            ->assertStatus(401);

        $formateur3->delete();
        $this->putJson('/api/formations/' . $formation3->id, [
            'titre' => 'Titre',
            'description' => 'Desc',
            'categorie' => 'developpement_web',
            'niveau' => 'debutant',
        ], $this->headers($token3))
            ->assertStatus(404);

        ['user' => $formateur4, 'token' => $token4] = $this->creerUtilisateur('formateur');
        $formation4 = $this->creerFormation($formateur4);
        $this->deleteJson('/api/formations/' . $formation4->id)
            ->assertStatus(401);

        $formateur4->delete();
        $this->deleteJson('/api/formations/' . $formation4->id, [], $this->headers($token4))
            ->assertStatus(404);
    }

    #[Test]
    public function inscriptions_couvrent_les_cas_absents_et_sans_token(): void
    {
        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        $formation = $this->creerFormation($formateur);

        $this->postJson('/api/formations/' . $formation->id . '/inscription', [])
            ->assertStatus(401);

        ['user' => $apprenant, 'token' => $token] = $this->creerUtilisateur('apprenant');
        $apprenant->delete();
        $this->postJson('/api/formations/' . $formation->id . '/inscription', [], $this->headers($token))
            ->assertStatus(404);

        ['user' => $apprenant2, 'token' => $token2] = $this->creerUtilisateur('apprenant');
        $this->postJson('/api/formations/9999/inscription', [], $this->headers($token2))
            ->assertStatus(404);

        $this->deleteJson('/api/formations/' . $formation->id . '/inscription')
            ->assertStatus(401);

        $apprenant2->delete();
        $this->deleteJson('/api/formations/' . $formation->id . '/inscription', [], $this->headers($token2))
            ->assertStatus(404);

        ['user' => $apprenant3, 'token' => $token3] = $this->creerUtilisateur('apprenant');
        $this->getJson('/api/apprenant/formations')
            ->assertStatus(401);

        $apprenant3->delete();
        $this->getJson('/api/apprenant/formations', $this->headers($token3))
            ->assertStatus(404);
    }

    #[Test]
    public function modules_couvrent_les_cas_absents_et_sans_token(): void
    {
        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        $formation = $this->creerFormation($formateur);
        $module = $this->creerModule($formation);

        $this->postJson('/api/formations/' . $formation->id . '/modules', [])
            ->assertStatus(401);

        ['user' => $formateur2, 'token' => $token2] = $this->creerUtilisateur('formateur');
        $formateur2->delete();
        $this->postJson('/api/formations/' . $formation->id . '/modules', [
            'titre' => 'Module',
            'contenu' => 'Contenu',
            'ordre' => 1,
        ], $this->headers($token2))
            ->assertStatus(404);

        $this->postJson('/api/formations/9999/modules', [
            'titre' => 'Module',
            'contenu' => 'Contenu',
            'ordre' => 1,
        ], $this->headers(JWTAuth::fromUser($formateur)))
            ->assertStatus(404);

        ['user' => $formateur3, 'token' => $token3] = $this->creerUtilisateur('formateur');
        $formation3 = $this->creerFormation($formateur3);
        $this->putJson('/api/modules/' . $module->id, [
            'titre' => 'Module modifié',
            'contenu' => 'Contenu modifié',
            'ordre' => 1,
        ])
            ->assertStatus(401);

        $formateur3->delete();
        $this->putJson('/api/modules/' . $module->id, [
            'titre' => 'Module modifié',
            'contenu' => 'Contenu modifié',
            'ordre' => 1,
        ], $this->headers($token3))
            ->assertStatus(404);

        $this->putJson('/api/modules/9999', [
            'titre' => 'Module modifié',
            'contenu' => 'Contenu modifié',
            'ordre' => 1,
        ], $this->headers(JWTAuth::fromUser($formateur3)))
            ->assertStatus(404);

        $this->deleteJson('/api/modules/' . $module->id)
            ->assertStatus(401);

        $this->deleteJson('/api/modules/9999', [], $this->headers(JWTAuth::fromUser($formateur3)))
            ->assertStatus(404);

        ['user' => $apprenant, 'token' => $tokenApprenant] = $this->creerUtilisateur('apprenant');
        $apprenant->delete();
        $this->getJson('/api/formations/' . $formation3->id . '/modules-termines', $this->headers($tokenApprenant))
            ->assertStatus(404);

        $this->getJson('/api/formations/' . $formation3->id . '/modules-termines')
            ->assertStatus(401);

        $this->postJson('/api/modules/' . $module->id . '/terminer', [], $this->headers(JWTAuth::fromUser($formateur3)))
            ->assertStatus(403);

        $this->postJson('/api/modules/9999/terminer', [], $this->headers($tokenApprenant))
            ->assertStatus(404);

        $this->postJson('/api/modules/' . $module->id . '/terminer', [])
            ->assertStatus(401);
    }
}

