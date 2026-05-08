<?php

namespace Tests\Feature;

use App\Models\Formation;
use App\Models\Inscription;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;
use Tymon\JWTAuth\Facades\JWTAuth;

/**
 * Tests fonctionnels du endpoint :
 * GET /api/formations/{id}/apprenants
 */
class ListeApprenantTest extends TestCase
{
    use RefreshDatabase;

    private function creerUtilisateur(string $role, string $suffix = ''): array
    {
        $user = User::create([
            'nom'      => ucfirst($role) . $suffix,
            'email'    => $role . $suffix . '_' . uniqid() . '@apprenants.test',
            'password' => bcrypt('password123'),
            'role'     => $role,
        ]);

        return ['user' => $user, 'token' => JWTAuth::fromUser($user)];
    }

    private function creerFormation(User $formateur): Formation
    {
        return Formation::create([
            'titre'          => 'Formation Test',
            'description'    => 'Description test',
            'categorie'      => 'developpement_web',
            'niveau'         => 'debutant',
            'nombre_de_vues' => 0,
            'formateur_id'   => $formateur->id,
        ]);
    }

    private function inscrire(User $apprenant, Formation $formation, int $progression = 0): Inscription
    {
        return Inscription::create([
            'utilisateur_id' => $apprenant->id,
            'formation_id'   => $formation->id,
            'progression'    => $progression,
        ]);
    }

    private function headers(string $token): array
    {
        return ['Authorization' => 'Bearer ' . $token];
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function un_formateur_proprietaire_voit_la_liste_des_apprenants(): void
    {
        ['user' => $formateur, 'token' => $token] = $this->creerUtilisateur('formateur');
        ['user' => $apprenant1] = $this->creerUtilisateur('apprenant', '1');
        ['user' => $apprenant2] = $this->creerUtilisateur('apprenant', '2');

        $formation = $this->creerFormation($formateur);
        $this->inscrire($apprenant1, $formation, 25);
        $this->inscrire($apprenant2, $formation, 70);

        $response = $this->getJson(
            '/api/formations/' . $formation->id . '/apprenants',
            $this->headers($token),
        );

        $response->assertStatus(200)
            ->assertJsonCount(2)
            ->assertJsonStructure([
                '*' => ['id', 'nom', 'email', 'progression', 'date_inscription'],
            ]);

        $data = $response->json();
        $this->assertEqualsCanonicalizing(
            [$apprenant1->id, $apprenant2->id],
            array_column($data, 'id'),
        );
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function un_formateur_non_proprietaire_obtient_403(): void
    {
        ['user' => $formateurProprietaire] = $this->creerUtilisateur('formateur', 'A');
        ['token' => $tokenAutre]            = $this->creerUtilisateur('formateur', 'B');

        $formation = $this->creerFormation($formateurProprietaire);

        $response = $this->getJson(
            '/api/formations/' . $formation->id . '/apprenants',
            $this->headers($tokenAutre),
        );

        $response->assertStatus(403);
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function une_formation_sans_apprenant_retourne_un_tableau_vide(): void
    {
        ['user' => $formateur, 'token' => $token] = $this->creerUtilisateur('formateur');
        $formation = $this->creerFormation($formateur);

        $response = $this->getJson(
            '/api/formations/' . $formation->id . '/apprenants',
            $this->headers($token),
        );

        $response->assertStatus(200)
            ->assertExactJson([]);
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function une_requete_sans_token_jwt_retourne_401(): void
    {
        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        $formation = $this->creerFormation($formateur);

        $response = $this->getJson('/api/formations/' . $formation->id . '/apprenants');

        $response->assertStatus(401);
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function une_formation_introuvable_retourne_404(): void
    {
        ['token' => $token] = $this->creerUtilisateur('formateur');

        $response = $this->getJson(
            '/api/formations/999999/apprenants',
            $this->headers($token),
        );

        $response->assertStatus(404);
    }
}
