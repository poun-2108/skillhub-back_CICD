<?php

namespace Tests\Feature;

use App\Models\Formation;
use App\Models\Inscription;
use App\Models\Rating;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;
use Tymon\JWTAuth\Facades\JWTAuth;

/**
 * Tests fonctionnels du systeme de notation des formations.
 * Endpoint : POST /api/formations/{id}/noter
 */
class RatingControllerTest extends TestCase
{
    use RefreshDatabase;

    private function creerUtilisateur(string $role, string $suffix = ''): array
    {
        $user = User::create([
            'nom'      => ucfirst($role) . $suffix,
            'email'    => $role . $suffix . '_' . uniqid() . '@rating.test',
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

    private function inscrire(User $apprenant, Formation $formation): Inscription
    {
        return Inscription::create([
            'utilisateur_id' => $apprenant->id,
            'formation_id'   => $formation->id,
            'progression'    => 0,
        ]);
    }

    private function headers(string $token): array
    {
        return ['Authorization' => 'Bearer ' . $token];
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function un_apprenant_inscrit_peut_noter_une_formation(): void
    {
        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        ['user' => $apprenant, 'token' => $token] = $this->creerUtilisateur('apprenant');
        $formation = $this->creerFormation($formateur);
        $this->inscrire($apprenant, $formation);

        $response = $this->postJson(
            '/api/formations/' . $formation->id . '/noter',
            ['note' => 4, 'commentaire' => 'Très bonne formation'],
            $this->headers($token),
        );

        $response->assertStatus(201)
            ->assertJsonFragment([
                'note'         => 4,
                'commentaire'  => 'Très bonne formation',
                'formation_id' => $formation->id,
                'user_id'      => $apprenant->id,
            ]);

        $this->assertDatabaseHas('ratings', [
            'user_id'      => $apprenant->id,
            'formation_id' => $formation->id,
            'note'         => 4,
            'commentaire'  => 'Très bonne formation',
        ]);
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function un_apprenant_ne_peut_pas_noter_deux_fois_la_meme_formation(): void
    {
        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        ['user' => $apprenant, 'token' => $token] = $this->creerUtilisateur('apprenant');
        $formation = $this->creerFormation($formateur);
        $this->inscrire($apprenant, $formation);

        $this->postJson(
            '/api/formations/' . $formation->id . '/noter',
            ['note' => 5, 'commentaire' => 'Première note'],
            $this->headers($token),
        )->assertStatus(201);

        $response = $this->postJson(
            '/api/formations/' . $formation->id . '/noter',
            ['note' => 3, 'commentaire' => 'Deuxième note'],
            $this->headers($token),
        );

        $response->assertStatus(400);

        $this->assertEquals(1, Rating::where('user_id', $apprenant->id)
            ->where('formation_id', $formation->id)
            ->count());
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function une_note_hors_intervalle_retourne_400(): void
    {
        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        ['user' => $apprenant, 'token' => $token] = $this->creerUtilisateur('apprenant');
        $formation = $this->creerFormation($formateur);
        $this->inscrire($apprenant, $formation);

        $response = $this->postJson(
            '/api/formations/' . $formation->id . '/noter',
            ['note' => 6, 'commentaire' => 'Hors intervalle'],
            $this->headers($token),
        );

        $response->assertStatus(400);
        $this->assertDatabaseMissing('ratings', [
            'user_id'      => $apprenant->id,
            'formation_id' => $formation->id,
        ]);
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function un_apprenant_non_inscrit_ne_peut_pas_noter(): void
    {
        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        ['user' => $apprenant, 'token' => $token] = $this->creerUtilisateur('apprenant');
        $formation = $this->creerFormation($formateur);

        $response = $this->postJson(
            '/api/formations/' . $formation->id . '/noter',
            ['note' => 4, 'commentaire' => 'Sans inscription'],
            $this->headers($token),
        );

        $response->assertStatus(403);
        $this->assertDatabaseMissing('ratings', [
            'user_id'      => $apprenant->id,
            'formation_id' => $formation->id,
        ]);
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function une_requete_sans_token_jwt_retourne_401(): void
    {
        ['user' => $formateur] = $this->creerUtilisateur('formateur');
        $formation = $this->creerFormation($formateur);

        $response = $this->postJson(
            '/api/formations/' . $formation->id . '/noter',
            ['note' => 4, 'commentaire' => 'Sans token'],
        );

        $response->assertStatus(401);
    }
}
