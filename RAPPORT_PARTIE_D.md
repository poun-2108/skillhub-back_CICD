# Partie D — Analyse des rapports SonarCloud

## D1. Tableau comparatif

Comparaison de la codebase **avant** l'ajout des fonctionnalités de la Partie B
et **après** le push des branches `feature_progression-apprenant` et
`feature_list-apprenants`.

| Indicateur                  | Avant         | Après        | Évolution                  |
| --------------------------- | ------------- | ------------ | -------------------------- |
| Nombre de bugs              | 0             | 0            | Stable (rating A maintenu) |
| Nombre de code smells       | 21            | 1            | -20 (-95 %)                |
| Nombre de vulnérabilités    | 0             | 0            | Stable (rating A maintenu) |
| Couverture de test (%)      | 88,4 %        | 82,7 %       | -5,7 pts                   |
| Duplication (%)             | 7,6 %         | 6,1 %        | -1,5 pt                    |
| Quality Gate                | Not computed  | Passed       | Amélioré                   |

> Source : tableau de bord SonarCloud du projet
> https://sonarcloud.io/project/overview?id=Nirina2108_skillhub-back_CICD
>
> - **Avant** : premier scan SonarCloud (codebase initiale avant l'ajout des
>   features Partie B). 1,2k Lines of Code, 21 Open Issues principalement de
>   type *Maintainability* (cf. graphique « Maintainability issues over time »
>   qui montre ~20 issues maintainability au démarrage).
> - **Après** : scan le plus récent sur `main` après push des branches
>   `feature_progression-apprenant` et `feature_list-apprenants` et des
>   correctifs CI (sonar.yml + JWT + MongoDB). 1k Lines of Code (nettoyage
>   de code mort lors de l'épreuve), 1 seule issue résiduelle de type
>   *Code Smell*, ratings Reliability / Security / Maintainability tous à **A**.

### Analyse de l'évolution observée

- **Bugs / Vulnérabilités (0 → 0)** : aucune régression côté Reliability ou
  Security. Les nouveaux endpoints valident systématiquement les entrées
  (`$request->validate(...)`) et l'authentification JWT, ce qui évite tout
  hotspot de sécurité supplémentaire. Les ratings A sont conservés.
- **Code smells (21 → 1)** : forte amélioration (-95 %). Le nettoyage des
  *example tests* Laravel par défaut, l'extraction de la logique dans des
  services dédiés (`RatingService`, `InscriptionService`) et le respect du
  pattern *single return* dans les contrôleurs ont permis de réduire
  drastiquement le nombre d'issues *Maintainability*. La seule issue
  restante est la duplication de la chaîne `/formations/{id}` dans
  `routes/api.php` (règle S1192) — non bloquante.
- **Couverture (88,4 % → 82,7 %)** : légère baisse de 5,7 points. C'est un
  effet mécanique : les nouveaux fichiers `RatingController`, `RatingService`,
  `InscriptionService` et le `Rating` model ajoutent ~120 lignes de code
  applicatif ; même si les 10 nouveaux tests couvrent les chemins métier,
  certaines branches mineures (réponses 401 secondaires, garde-fous) ne sont
  pas instrumentées. La couverture reste **au-dessus du seuil Quality Gate
  de 80 %**.
- **Duplication (7,6 % → 6,1 %)** : amélioration de 1,5 point. Les services
  factorisent la logique au lieu de la dupliquer dans les contrôleurs.
  L'augmentation du dénominateur (lignes de code total) joue aussi en faveur
  d'un ratio plus faible.
- **Quality Gate (Not computed → Passed)** : la première analyse n'avait pas
  de *New Code* défini, donc Sonar ne pouvait pas calculer le gate. Après
  configuration de la *New Code definition* et le respect des seuils
  (couverture ≥ 80 %, 0 issue *blocker* sur le code modifié), le gate est
  désormais **Passed** sur l'ensemble des conditions.

---

## D2. Rapport technique d'onboarding

> Document destiné à un développeur junior rejoignant l'équipe SkillHub.
> Objectif : être opérationnel sur la codebase backend en moins d'une journée.

### 1. Présentation du projet

**SkillHub** est une plateforme de formations en ligne. Le backend est une API
REST Laravel 12 (PHP 8.2) qui sert un front React. Trois rôles cohabitent :

- **Apprenant** : s'inscrit aux formations, valide les modules, peut désormais
  **noter** une formation suivie.
- **Formateur** : crée des formations + modules, peut désormais **consulter la
  liste des apprenants inscrits** à ses formations.
- **Administrateur / non authentifié** : consulte le catalogue public.

L'authentification utilise **JWT** (`tymon/jwt-auth`). MySQL stocke les données
métier (utilisateurs, formations, modules, inscriptions, ratings) et MongoDB
stocke les logs d'activité et la messagerie (`activity_logs`, `messages`).

### 2. Architecture MVC + Service

```
app/
├── Http/Controllers/   <- entrée HTTP, parse JWT, validation, codes de retour
├── Services/           <- logique métier réutilisable
└── Models/             <- Eloquent (MySQL) + MongoDB pour ActivityLog / Message
routes/api.php          <- toutes les routes API publiques
database/migrations/    <- schémas SQL versionnés
tests/Feature/          <- tests d'intégration HTTP (PHPUnit + SQLite mémoire)
```

Règle d'or : **un contrôleur n'effectue jamais de logique métier directement**.
Il délègue à un service (ex : `RatingService`, `InscriptionService`). Cela
facilite les tests unitaires et la réutilisation.

### 3. Démarrage en local

```bash
# Cloner et installer
git clone <repo> && cd skillhub-back
composer install
cp .env.example .env
php artisan key:generate
php artisan jwt:secret

# Base de données
php artisan migrate

# Lancer
php artisan serve
```

L'API écoute sur `http://localhost:8000/api`. Endpoint de santé : `GET /up`.

### 4. Fonctionnalités ajoutées durant l'épreuve (Partie B)

#### Branche `feature_progression-apprenant` — Système de notation

| Élément       | Détail                                                                |
| ------------- | --------------------------------------------------------------------- |
| Endpoint POST | `POST /api/formations/{id}/noter`                                     |
| Endpoint GET  | `GET /api/formations/{id}` enrichi avec `note_moyenne` + `nombre_avis`|
| Modèle        | `Rating(id, user_id, formation_id, note 1-5, commentaire, timestamps)`|
| Service       | `App\Services\RatingService`                                          |
| Règle métier  | Un apprenant inscrit ne peut noter qu'**une seule fois** une formation|
| Codes de retour | 201, 400 (note hors 1-5 ou doublon), 401 (sans JWT), 403 (non inscrit) |

#### Branche `feature_list-apprenants` — Liste des apprenants inscrits

| Élément       | Détail                                                                |
| ------------- | --------------------------------------------------------------------- |
| Endpoint      | `GET /api/formations/{id}/apprenants`                                 |
| Service       | `App\Services\InscriptionService::listerApprenants()`                 |
| Accès         | Formateur **propriétaire** uniquement                                 |
| Réponse 200   | `[{id, nom, email, progression, date_inscription}, ...]` (`[]` si vide) |
| Codes de retour | 200, 401 (sans JWT), 403 (non propriétaire), 404 (formation inconnue)  |

### 5. Tests

```bash
# Tous les tests (SQLite en mémoire)
php artisan test

# Tests d'une feature uniquement
php artisan test --filter RatingControllerTest
php artisan test --filter ListeApprenantTest

# Avec rapport de couverture (requiert pcov ou xdebug)
php artisan test --coverage-clover coverage.xml
```

Les tests utilisent **SQLite en mémoire** (`phpunit.xml`). MongoDB n'est pas
requis pour les tests des nouvelles features — les endpoints concernés ne
journalisent pas via `ActivityLogService`.

Les helpers privés (`creerUtilisateur`, `creerFormation`, `inscrire`, `headers`)
sont à reprendre tels quels lors de l'écriture de nouveaux tests.

### 6. CI/CD (Partie C)

Deux workflows GitHub Actions :

- **`.github/workflows/ci.yml`** : pipeline complet (tests + build Docker + push
  vers Docker Hub sur `main`).
- **`.github/workflows/sonar.yml`** : analyse SonarCloud sur **chaque push**
  de toute branche. Les 4 étapes : checkout (`fetch-depth: 0`), install Composer,
  `php artisan test --coverage-clover coverage.xml`, scan SonarCloud.

Configuration : `sonar-project.properties` à la racine. Secret requis :
`SONAR_TOKEN` (récupéré depuis SonarCloud → My Account → Security).

### 7. Conventions à respecter

1. **Branche par feature**, nommage `feature_<nom>` ; pas de commit direct sur
   `main`.
2. **Commits explicites** : `feat(rating): ...`, `fix(...)`, `chore(tests): ...`.
3. **MVC strict** : Controller → Service → Model. Pas de requête Eloquent dans
   un contrôleur.
4. **Validation systématique** des données entrantes via `$request->validate()`.
5. **Un test PHPUnit par cas métier** (succès + chaque code d'erreur attendu).
6. Ne pas modifier le front React — le backend est l'unique périmètre.

### 8. Ressources utiles

- Tableau de bord SonarCloud : `Nirina2108_skillhub-back_CICD`
- Doc Laravel 12 : https://laravel.com/docs/12.x
- Doc `tymon/jwt-auth` : https://jwt-auth.readthedocs.io
- Postman / Bruno : importer les routes depuis `routes/api.php`

### 9. Points d'attention connus

- Les tests existants qui dépendent de **MongoDB** (messagerie, ActivityLog)
  échouent en local sans instance Mongo. C'est attendu — ils passent en CI où
  un service `mongo:6` est démarré (cf. `ci.yml`).
- La duplication `/formations/{id}` dans `routes/api.php` est signalée par
  Sonar (S1192) : non bloquant, à factoriser éventuellement avec
  `Route::prefix('formations/{id}')`.
- Aucun rate-limit n'est posé sur les endpoints de notation : à ajouter avec
  `throttle:60,1` si abus en production.
