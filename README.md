# SkillHub — Bloc 03 · Cloud, DevOps et Architecture

Plateforme collaborative de mise en relation entre formateurs et apprenants.
Ce dépôt couvre la **Phase 3 – Industrialisation** : conteneurisation Docker, pipelines CI/CD GitHub Actions, analyse de qualité SonarCloud et hébergement cloud AWS.

[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=poun-2108_skillhub-back_CICD&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=poun-2108_skillhub-back_CICD)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=poun-2108_skillhub-back_CICD&metric=coverage)](https://sonarcloud.io/summary/new_code?id=poun-2108_skillhub-back_CICD)
[![Duplications](https://sonarcloud.io/api/project_badges/measure?project=poun-2108_skillhub-back_CICD&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=poun-2108_skillhub-back_CICD)
[![PHP](https://img.shields.io/badge/PHP-8.2-777bb4?logo=php&logoColor=white)](https://www.php.net)
[![Java](https://img.shields.io/badge/Java-17-ed8b00?logo=openjdk&logoColor=white)](https://adoptium.net)
[![React](https://img.shields.io/badge/React-18-61dafb?logo=react&logoColor=black)](https://react.dev)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ed?logo=docker&logoColor=white)](https://www.docker.com)

---

## Sommaire

1. [Description du projet](#description-du-projet)
2. [Architecture microservices](#architecture-microservices)
3. [Stack technique](#stack-technique)
4. [Prérequis](#prérequis)
5. [Démarrage rapide](#démarrage-rapide)
6. [Système d'authentification SSO](#système-dauthentification-sso)
7. [Règle métier implémentée : limite d'inscriptions](#règle-métier-implémentée--limite-dinscriptions)
8. [Commandes de test](#commandes-de-test)
9. [Structure du dépôt](#structure-du-dépôt)
10. [Fonctionnement des composants](#fonctionnement-des-composants)
11. [Variables d'environnement](#variables-denvironnement)
12. [Stratégie de branches](#stratégie-de-branches)
13. [Analyse SonarCloud et plan d'action](#analyse-sonarcloud-et-plan-daction)
14. [Utilisation de l'IA](#utilisation-de-lia)

---

## Description du projet

SkillHub est une plateforme de formation en ligne où des **formateurs** publient des formations structurées en modules, et où des **apprenants** s'inscrivent, suivent les modules à leur rythme et échangent avec les formateurs via une messagerie interne. La plateforme gère le suivi de progression, les notifications par email et le comptage des vues par formation.

L'application est bâtie sur une **architecture microservices** composée de trois services indépendants qui communiquent par HTTP. Chaque service a son propre cycle de vie, son propre dépôt de code (dans des dossiers séparés), son propre Dockerfile et son propre pipeline CI/CD, ce qui permet de les déployer, scaler et faire évoluer séparément.

Les principes clés du projet sont :

- **Séparation des responsabilités** : authentification isolée dans un service dédié, logique métier centralisée dans Laravel, présentation gérée côté React.
- **Stateless** : aucun service ne stocke de session, l'état d'authentification est porté par le token JWT.
- **Observabilité** : logs d'activité persistés dans MongoDB, métriques de qualité suivies dans SonarCloud, builds tracés dans GitHub Actions.
- **Reproductibilité** : toute la stack se lance d'une commande, sans prérequis de runtime sur la machine hôte.

---

## Architecture microservices

Le système est découpé en **trois services applicatifs** et **trois bases de données**, tous conteneurisés et orchestrés par Docker Compose sur un réseau interne partagé.

```
                      Navigateur utilisateur
                              |
                              v
                  +-----------------------+
                  |  Frontend React       |
                  |  Nginx · port 5173    |
                  +-----------------------+
                    |                   |
       Auth calls   |                   |  Business calls
    (login, profil) |                   |  (formations, modules,
                    v                   v   inscriptions, messagerie)
        +----------------------+   +----------------------+
        |  Auth Service        |   |  Backend Laravel     |
        |  Spring Boot 3       |   |  Laravel 11 · PHP 8.2|
        |  port 8000           |   |  port 8001           |
        +----------------------+   +----------------------+
                    |                   |        |
                    v                   v        v
           +-----------------+   +-----------+  +-----------+
           | MySQL auth-db   |   |  MySQL    |  | MongoDB   |
           | port 3307       |   |  port 3306|  | port 27017|
           +-----------------+   +-----------+  +-----------+
```

**Flux des requêtes :**

| Type de requête | Destination | Exemple |
|---|---|---|
| Authentification | Spring Boot (8000) | `POST /auth/login`, `POST /auth/register`, `GET /auth/me` |
| Formations et modules | Laravel (8001) | `GET /api/formations`, `POST /api/modules` |
| Inscriptions et progression | Laravel (8001) | `POST /api/formations/{id}/inscription` |
| Messagerie | Laravel (8001) | `POST /api/messages`, `GET /api/conversations` |
| Logs et historique | Laravel écrit dans MongoDB | journalisation asynchrone |

Le frontend ne communique **jamais** avec MySQL ou MongoDB directement. Les deux backends sont la seule porte d'entrée vers les données.

---

## Stack technique

| Couche | Technologie |
|---|---|
| Frontend | React 18 + Vite, servi par Nginx |
| Auth Service | Spring Boot 3 · Java 17 · JWT (protocole SCRAM-like) |
| Backend | Laravel 11 · PHP 8.2 · API REST |
| Base principale | MySQL 8.0 (Laravel) |
| Base auth | MySQL 8.0 instance séparée (Spring Boot) |
| Logs / Messaging | MongoDB 7.0 |
| Conteneurisation | Docker · Docker Compose |
| CI/CD | GitHub Actions |
| Qualité de code | SonarCloud (couverture instructions >= 96 % pour Spring Boot) |
| Cloud cible | AWS (ECS Fargate + RDS MySQL + DocumentDB) |

---

## Prérequis

- **Docker Desktop** >= 24
- **Git** >= 2.40
- Pour le développement local uniquement : **Node.js 22**, **PHP 8.2**, **Java 17**, **Maven 3.9**

---

## Démarrage rapide

```bash
# Cloner le dépôt
git clone https://github.com/poun-2108/skillhub_CICD_exam.git
cd skillhub_CICD_exam

# Copier le fichier de variables d'environnement et renseigner les valeurs
cp .env.example .env

# Construire les images et démarrer tous les conteneurs
docker compose up --build
```

> **Premier démarrage** : compter 3 à 5 minutes pour le téléchargement des images de base et le build. Les lancements suivants prennent une dizaine de secondes grâce au cache Docker.

Les services sont disponibles sur :

| Service | URL locale |
|---|---|
| Frontend React | http://localhost:5173 |
| Auth Service Spring Boot | http://localhost:8000 |
| Backend Laravel | http://localhost:8001 |
| MySQL principal | localhost:3306 |
| MySQL auth | localhost:3307 |
| MongoDB | localhost:27017 |

**Commandes utiles :**

```bash
# Voir les logs d'un service en temps réel
docker compose logs -f skillhub-back

# Arrêter la stack (préserve les données)
docker compose down

# Remise à zéro complète (supprime aussi les volumes et les données)
docker compose down -v

# Rebuilder un seul service sans toucher aux autres
docker compose up --build auth-service
```

---

## Système d'authentification SSO

L'authentification de la plateforme repose sur un modèle **Single Sign-On** centralisé dans le service Spring Boot. Le principe est qu'**un seul service émet les tokens**, mais **tous les services les valident** grâce à un secret partagé.

### Flux de connexion

1. L'utilisateur saisit ses identifiants dans le frontend React.
2. Le frontend envoie les identifiants uniquement à **Spring Boot** (`POST /auth/login`).
3. Spring Boot applique un protocole de type **SCRAM** : le mot de passe n'est jamais envoyé en clair, un challenge-response prouve la connaissance du secret sans le divulguer.
4. Côté serveur, le hash BCrypt stocké en base est comparé au hash dérivé du challenge.
5. Si la vérification réussit, Spring Boot génère un **token JWT signé** contenant l'`id` utilisateur, son `rôle` (formateur ou apprenant) et une date d'expiration.
6. Le token est renvoyé au frontend qui le stocke dans le `localStorage`.

### Flux des requêtes authentifiées

Toutes les requêtes suivantes (qu'elles aillent vers Laravel ou Spring Boot) incluent le token dans l'en-tête :

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Le middleware JWT du service destinataire :

1. **Extrait** le token de l'en-tête.
2. **Vérifie la signature** avec le secret partagé `JWT_SECRET`.
3. **Contrôle l'expiration**.
4. **Extrait** l'`id` utilisateur et le `rôle` pour autoriser l'accès aux ressources.

### Pourquoi ce modèle

Le point critique du SSO est que **Laravel et Spring Boot partagent exactement le même `JWT_SECRET`**. Cela permet à Laravel de valider un token émis par Spring Boot **sans faire un appel réseau** à chaque requête, ce qui élimine la latence et le couplage fort entre les services.

| Avantage | Bénéfice |
|---|---|
| Services stateless | Chaque service peut être scalé horizontalement sans sticky sessions |
| Pas de cache de sessions | Pas d'infrastructure Redis ou Memcached supplémentaire |
| Validation locale | Pas d'appel réseau à Spring Boot pour chaque requête Laravel |
| Responsabilité claire | Seul Spring Boot touche aux mots de passe |

> **Règle d'or** : si `JWT_SECRET` change, il doit être mis à jour **simultanément des deux côtés**. Tout désalignement fait échouer la validation et déconnecte tous les utilisateurs.

### Expiration et déconnexion

- **Durée de vie** d'un token : 24 heures par défaut (configurable via `JWT_EXPIRATION`).
- **Déconnexion** côté client : suppression du token du `localStorage`. Le serveur n'a rien à invalider puisqu'il est stateless.
- **Révocation globale** : rotation du `JWT_SECRET` (déconnecte tous les utilisateurs en une opération).

---

## Règle métier implémentée : limite d'inscriptions

### Énoncé

> Un apprenant ne peut pas être inscrit à **plus de cinq formations actives** en même temps.

L'objectif pédagogique est d'éviter qu'un apprenant s'éparpille sur trop de formations et abandonne sans en terminer aucune. La plateforme encourage ainsi un engagement progressif et mesurable.

### Définition d'une inscription active

Une inscription est considérée **active** tant que la progression est **strictement inférieure à 100 %**. Les formations terminées (progression = 100 %) sont exclues du compteur, ce qui permet à l'apprenant de continuer à s'inscrire à mesure qu'il progresse.

### Endpoint modifié

```
POST /api/formations/{id}/inscription
```

Contrôleur : `App\Http\Controllers\InscriptionController@store`

### Logique de validation

La méthode vérifie dans cet ordre :

| Ordre | Condition | Code HTTP en cas d'échec |
|---|---|---|
| 1 | L'utilisateur authentifié est un **apprenant** (pas un formateur) | `403 Forbidden` |
| 2 | L'apprenant n'est **pas déjà inscrit** à cette formation | `409 Conflict` |
| 3 | L'apprenant a **moins de 5 inscriptions actives** | `422 Unprocessable Entity` |
| 4 | La formation existe | `404 Not Found` |

### Extrait simplifié de la règle

```php
$inscriptionsActives = Inscription::where('utilisateur_id', $user->id)
    ->where('progression', '<', 100)
    ->count();

if ($inscriptionsActives >= 5) {
    return response()->json([
        'message' => 'Vous avez atteint la limite de 5 inscriptions actives.',
    ], 422);
}
```

### Tests couvrant la règle

- `un_apprenant_peut_sinscrire_a_une_formation` — cas nominal
- `un_apprenant_ne_peut_pas_sinscrire_a_plus_de_cinq_formations` — cas limite
- `linscription_en_double_retourne_409` — idempotence
- `un_formateur_ne_peut_pas_sinscrire_a_une_formation` — contrôle de rôle
- `linscription_a_une_formation_inexistante_retourne_404` — robustesse

Voir `skillhub-back/tests/Feature/InscriptionLimitTest.php`.

---

## Commandes de test

### Spring Boot — JUnit + JaCoCo (couverture >= 96 %)

```bash
cd springboot
./mvnw clean verify
```

> `clean verify` compile le projet, exécute tous les tests JUnit et génère le rapport de couverture JaCoCo dans `target/site/jacoco/jacoco.xml`.

### Laravel — PHPUnit

```bash
cd skillhub-back
composer install
cp .env.example .env
php artisan key:generate
./vendor/bin/phpunit --coverage-clover coverage.xml
```

> Les tests utilisent une base **SQLite en mémoire** (configurée dans `.env.example`) pour ne pas dépendre d'un serveur MySQL au moment du CI.

### React — Vitest

```bash
cd skillhub-front
npm ci
npm run test:coverage
```

> `test:coverage` lance Vitest en mode coverage et génère `coverage/lcov.info` lu ensuite par SonarCloud.

---

## Structure du dépôt

```
skillhub_CICD_exam/
├── skillhub-front/              # Application React (Vite)
│   ├── Dockerfile               # Build multi-stage : Node 22 puis nginx:alpine
│   ├── sonar-project.properties # Config analyse SonarCloud front
│   └── .github/workflows/ci.yml # Pipeline lint + test + build + push Docker
├── skillhub-back/               # API REST Laravel 11 (PHP 8.2)
│   ├── Dockerfile               # Image PHP avec MongoDB et Composer
│   ├── sonar-project.properties # Config analyse SonarCloud back
│   └── .github/workflows/ci.yml # Pipeline PHPUnit + Sonar + push Docker
├── springboot/                  # Service d'authentification Spring Boot 3
│   ├── Dockerfile               # Build Maven puis exécution du JAR
│   ├── sonar-project.properties # Config analyse SonarCloud auth
│   └── .github/workflows/ci.yml # Pipeline JUnit + JaCoCo >= 96 % + Sonar + push Docker
├── docker-compose.yml           # Orchestre tous les services ensemble
├── .env.example                 # Template des variables d'environnement
├── .gitignore                   # Exclut vendor/, target/, node_modules/, .env
├── CONTRIBUTING.md              # Règles Git, branches, commits, PR
└── README.md
```

---

## Fonctionnement des composants

### docker-compose.yml

Ce fichier orchestre **six services Docker** qui communiquent tous sur un réseau interne nommé `skillhub_network`.

| Service | Rôle | Dépendances |
|---|---|---|
| `mysql` | Base MySQL pour Laravel, données persistées dans un volume nommé | — |
| `auth-db` | Base MySQL séparée pour Spring Boot, port 3307 exposé | — |
| `mongodb` | Base MongoDB pour les logs d'activité et la messagerie Laravel | — |
| `auth-service` | Conteneur Spring Boot | `auth-db` healthy |
| `skillhub-back` | Conteneur Laravel | `mysql` et `mongodb` healthy |
| `frontend` | Conteneur Nginx qui sert les fichiers React buildés | — |

Chaque service a un `healthcheck` qui vérifie que le service est réellement opérationnel avant d'autoriser les dépendants à démarrer (`depends_on: condition: service_healthy`). Des **limites mémoire** sont définies sur chaque service pour simuler les contraintes de production.

### Dockerfile Laravel (skillhub-back)

Image en une seule étape basée sur `php:8.2-cli`. Elle installe les extensions PHP nécessaires : `pdo_mysql` pour MySQL, `mongodb` via PECL pour la connexion MongoDB, et `xdebug` pour la génération du rapport de couverture PHPUnit. Composer installe les dépendances PHP, puis l'application démarre avec `php artisan serve`.

### Dockerfile React (skillhub-front)

**Build multi-étapes :**

1. **Étape `builder`** — utilise `node:22-alpine` pour exécuter `npm run build` et générer les fichiers statiques dans `dist/`.
2. **Étape finale** — copie uniquement ce dossier `dist/` dans une image `nginx:alpine` ultra-légère.

> Cela réduit la taille finale de l'image car les outils Node ne sont pas inclus dans le conteneur de production.

### Dockerfile Spring Boot (springboot)

Image basée sur `eclipse-temurin:17-jdk`. Maven compile le projet et génère le JAR dans `target/`. Le conteneur exécute ensuite ce JAR directement avec `java -jar`. Les variables de connexion à la base de données et au JWT sont injectées via les variables d'environnement de docker-compose.

### Pipelines CI/CD GitHub Actions

Chaque composant a son propre fichier `.github/workflows/ci.yml` qui se déclenche sur :

- **push** vers `main` ou `dev`
- **Pull Request** vers `main`

Le pipeline se déroule en deux jobs :

**Job `test`** — install des dépendances, lint du code, exécution des tests avec génération du rapport de couverture, envoi du rapport à SonarCloud pour analyse de qualité statique.

**Job `docker`** — se lance uniquement après la réussite du job `test` et uniquement sur `push` (pas sur PR). Il construit l'image Docker et la pousse vers Docker Hub avec deux tags : `latest` ou `dev` selon la branche, et le SHA du commit Git pour la traçabilité.

#### Déclencheurs

| Événement | Action | Tag Docker produit |
|---|---|---|
| push sur `main` | Déploiement du code stable | `latest` + SHA |
| push sur `dev` | Intégration continue | `dev` + SHA |
| Pull Request vers `main` | Validation avant merge | aucun (pas de push Docker) |

#### Pipeline Spring Boot (auth-service)

Fichier `springboot/.github/workflows/ci.yml`.

Le **job test** suit ces étapes dans l'ordre :

1. `actions/checkout@v4` avec `fetch-depth: 0` récupère tout l'historique Git pour que SonarCloud puisse calculer les métriques de blame.
2. `actions/setup-java@v4` installe le JDK 17 Temurin avec cache Maven activé.
3. `chmod +x mvnw` rend le wrapper Maven exécutable sous Linux.
4. `./mvnw clean verify -B` compile le projet, exécute les 187 tests JUnit et génère le rapport JaCoCo dans `target/site/jacoco/jacoco.xml`.
5. Un script Python lit le rapport JaCoCo, calcule le pourcentage d'instructions couvertes et fait échouer le pipeline si la couverture est inférieure à 96 %.
6. `./mvnw sonar:sonar` envoie le code et le rapport JaCoCo à SonarCloud avec les exclusions adéquates (DTOs, repositories, exceptions, config).

Le **job docker** ne se lance que sur push et après la réussite du job test :

1. `docker/setup-buildx-action@v3` active le builder Buildx avec cache GitHub Actions.
2. `docker/login-action@v3` authentifie le runner sur Docker Hub via les secrets `DOCKER_USERNAME` et `DOCKER_PASSWORD`.
3. Le tag est `latest` sur `main`, `dev` sur `dev`. Le SHA du commit est toujours ajouté comme second tag.
4. `docker/build-push-action@v5` construit l'image depuis le Dockerfile et pousse les deux tags vers Docker Hub avec cache GHA en lecture et écriture.

#### Pipeline Laravel (skillhub-back)

Fichier `skillhub-back/.github/workflows/ci.yml`.

Le **job test** :

1. Checkout du code avec historique complet.
2. `shivammathur/setup-php@v2` installe PHP 8.2 avec les extensions `pdo_mysql`, `pdo_sqlite`, `mbstring`, `zip`, `mongodb` et le driver de couverture `pcov` (plus rapide que Xdebug en CI).
3. `actions/cache@v4` cache le dossier `vendor/` en utilisant le hash de `composer.lock` comme clé.
4. `composer install` installe toutes les dépendances PHP.
5. L'environnement Laravel est préparé : copie de `.env.example` vers `.env`, génération de la clé applicative et nettoyage du cache. Les tests utilisent SQLite en mémoire donc aucun serveur MySQL n'est nécessaire en CI.
6. `./vendor/bin/phpunit --coverage-clover coverage.xml` lance les 88 tests PHPUnit et génère le rapport Clover.
7. `sonarsource/sonarcloud-github-action@v3` envoie code et couverture à SonarCloud. Les dossiers `vendor`, `storage`, `public`, `resources/views` sont exclus de l'analyse.

Le **job docker** est identique au pipeline Spring Boot : login Docker Hub, détermination du tag, build et push avec le SHA.

#### Pipeline React (skillhub-front)

Fichier `skillhub-front/.github/workflows/ci.yml`.

Le **job build** :

1. Checkout avec historique complet.
2. `actions/setup-node@v4` installe Node.js 22 avec cache npm basé sur `package-lock.json`.
3. `npm ci` installation stricte qui respecte exactement `package-lock.json`.
4. `npm run lint` vérification ESLint, le pipeline échoue si des erreurs sont trouvées.
5. `npm run test:coverage` lance les 46 tests Vitest et génère `coverage/lcov.info`.
6. Analyse SonarCloud avec le rapport lcov. Composants, pages, contextes, `main.jsx` et `App.jsx` sont exclus du calcul de couverture car trop liés au DOM.
7. `npm run build` génère les fichiers statiques optimisés dans `dist/`.

Le **job docker** construit l'image multi-stage et la pousse vers Docker Hub.

#### Secrets GitHub utilisés

Les trois pipelines partagent les mêmes secrets stockés dans `Settings → Secrets and variables → Actions` du dépôt :

| Secret | Rôle |
|---|---|
| `DOCKER_USERNAME` | Nom d'utilisateur Docker Hub |
| `DOCKER_PASSWORD` | Token d'accès Docker Hub (pas le mot de passe en clair) |
| `SONAR_TOKEN` | Token d'authentification SonarCloud |
| `GITHUB_TOKEN` | Fourni automatiquement par GitHub Actions pour la décoration des PR |

> Aucun de ces secrets n'apparaît en clair dans les fichiers `.yml`.

#### Traçabilité et cache

Chaque image Docker est taguée avec le **SHA complet du commit Git**, ce qui garantit qu'on peut retrouver précisément quelle version du code correspond à une image déployée. Le cache Buildx `type=gha,mode=max` réutilise les couches Docker précédentes et réduit drastiquement le temps de build.

| Type de build | Durée moyenne |
|---|---|
| Build à froid (premier run) | ~4 min |
| Rebuild incrémental (cache chaud) | ~30 s |

#### Cas d'échec du pipeline

Le pipeline affiche une croix rouge sur GitHub dans les cas suivants :

- un test unitaire tombe en échec
- la couverture Spring Boot descend sous **96 %**
- ESLint trouve une erreur dans le front-end
- la Quality Gate SonarCloud n'est pas passée (bugs bloquants, vulnérabilités critiques, duplications > 0,5 %)
- le build Docker échoue (Dockerfile cassé, image de base indisponible)
- le push Docker Hub échoue (credentials invalides ou rate limit atteint)

### sonar-project.properties

Configure l'analyse SonarCloud pour chaque projet : organisation, clé du projet, chemins des sources et des tests, chemin du rapport de couverture, fichiers exclus de l'analyse et fichiers exclus du calcul de duplication (`sonar.cpd.exclusions`). Les fichiers comme les migrations, DTOs et tests en sont exclus car leur répétition structurelle est intentionnelle.

---

## Variables d'environnement

Copier `.env.example` en `.env` et renseigner les valeurs :

| Variable | Description | Exemple |
|---|---|---|
| `APP_KEY` | Clé Laravel générée par `php artisan key:generate` | `base64:xxxx...` |
| `DB_PASSWORD` | Mot de passe root MySQL principal | `changeme` |
| `MYSQL_ROOT_PASSWORD` | Mot de passe root MySQL (conteneur) | `changeme` |
| `JWT_SECRET` | Clé secrète JWT, **partagée Laravel + Spring Boot** | >= 32 caractères |
| `APP_SMK` | Clé SCRAM utilisée par le service Spring Boot | 32 caractères aléatoires |
| `MONGO_DATABASE` | Nom de la base MongoDB | `skillhub_logs` |
| `MAIL_*` | Configuration SMTP pour les notifications | — |
| `CORS_ALLOWED_ORIGINS` | Liste blanche des origines autorisées | `http://localhost:5173` |

> **Critique** : `JWT_SECRET` doit être **identique** entre Laravel et Spring Boot. Tout désalignement fait échouer la validation des tokens et déconnecte tous les utilisateurs. En production, cette valeur doit vivre dans un gestionnaire de secrets (AWS Secrets Manager, GitHub Secrets, Azure Key Vault).

---

## Stratégie de branches

| Branche | Rôle | Règle |
|---|---|---|
| `main` | Production, code stable uniquement | Aucun commit direct, uniquement via PR depuis `dev` |
| `dev` | Intégration, accumule les features validées | Merge depuis les branches `feature/*` et `hotfix/*` |
| `feature/<nom>` | Développement d'une fonctionnalité isolée | Merge vers `dev` via PR |
| `hotfix/<nom>` | Correctif urgent | Merge vers `main` **et** `dev` |

Voir [CONTRIBUTING.md](CONTRIBUTING.md) pour la procédure complète.

---

## Analyse SonarCloud et plan d'action

### État actuel

Après l'ajout de la fonctionnalité de limitation des inscriptions, le scan SonarCloud a été relancé. Résultats :

| Métrique | Valeur | Seuil Quality Gate |
|---|---|---|
| **Quality Gate** | Passed | — |
| **Coverage (Overall)** | 92,2 % | >= 80 % |
| **Duplications** | 6,1 % | < 0,5 % sur new code |
| **Open Issues** | 1 081 | — |
| **Security Hotspots** | 1 | à revoir |

La Quality Gate est verte car elle se concentre sur le **new code** (code nouveau ou modifié). Les 1 081 issues constituent une dette technique accumulée sur l'historique du projet et n'empêchent pas la livraison.

### Plan d'action — sept chantiers classés effort / impact

Les chantiers sont ordonnés par ratio **gain de qualité / temps investi**. Les premiers sont des quick wins, les derniers du nettoyage de confort.

#### Chantier 1 — Conventions de formatage (quick win)

Environ **30 fichiers** n'ont pas de saut de ligne final, principalement les fichiers de configuration Laravel (`config/*.php`) et les migrations.

**Action** :

- Créer un `.editorconfig` à la racine du projet avec `end_of_line = lf` et `insert_final_newline = true`.
- Ajouter un hook Git `pre-commit` (via Husky ou équivalent) qui refuse les fichiers non conformes.
- Faire une passe unique avec `sed` ou un script pour normaliser l'existant.

**Impact estimé** : -30 issues en une heure de travail.

#### Chantier 2 — Refactorisation des contrôleurs Laravel (impact structurel)

Les contrôleurs `FormationController`, `ModuleController` et `InscriptionController` contiennent plusieurs méthodes avec **trop d'instructions return** (entre 4 et 7 alors que la règle en autorise 3). Ce problème vient d'une suite de vérifications qui retournent une `Response` à chaque cas d'erreur.

**Action** :

- Extraire les validations dans des **Form Requests Laravel** (`StoreInscriptionRequest`, `UpdateFormationRequest`...).
- Déplacer la logique métier dans des **Services** dédiés (`InscriptionService`, `FormationService`).
- Les contrôleurs deviennent de simples orchestrateurs : une seule méthode, un seul return.

**Impact estimé** : -15 issues, gain de maintenabilité significatif.

#### Chantier 3 — Complexité cognitive de `ModuleController::store` (priorité haute)

Cette méthode atteint une complexité cognitive de **21** alors que le seuil autorisé est **15**. C'est un signal fort que la méthode fait trop de choses.

**Action** :

- Décomposer en plusieurs méthodes privées ou extraire dans un `ModuleService`.
- Isoler la validation, la création du module, la mise à jour des inscriptions associées et la notification dans des étapes distinctes.
- Écrire des tests unitaires pour chaque étape.

**Impact estimé** : -1 issue critique, gain de testabilité majeur.

#### Chantier 4 — Extraction de constantes (maintenabilité)

Plusieurs chaînes littérales sont dupliquées jusqu'à **6 fois** dans le même fichier : `127.0.0.1` dans `config/database.php`, `/formations/{id}` dans `routes/api.php`, `/api/modules/` dans les tests.

**Action** :

- Créer des constantes de classe ou des constantes de configuration (`config('skillhub.routes.formations')`).
- Remplacer toutes les occurrences.
- Bénéfice : une seule modification en cas de changement de route ou de valeur.

**Impact estimé** : -8 issues critiques.

#### Chantier 5 — Security Hotspot CORS (priorité production)

SonarCloud signale que la politique CORS actuelle (`allowed_origins: ['*']` en développement) est **permissive**. Cette configuration a été relâchée pour faciliter le développement local mais doit être verrouillée avant tout déploiement en production.

**Action** :

- Limiter `CORS_ALLOWED_ORIGINS` à la seule URL du frontend déployé (ex: `https://skillhub.example.com`).
- Rendre la variable obligatoire via un check dans `AppServiceProvider` en environnement `production`.
- Documenter la configuration dans un fichier `docs/deployment.md`.

**Impact estimé** : fermeture du Security Hotspot, conformité OWASP.

#### Chantier 6 — Nettoyage du code mort

Plusieurs fichiers de tests et de seeders contiennent du **code commenté** ou des **variables locales jamais utilisées** (`$apprenant`, `$expediteur`, `$formateur2`...), héritées de phases de développement antérieures.

**Action** :

- Activer PHPStan ou Psalm dans le pipeline CI pour détecter automatiquement ces cas.
- Passer une fois sur les fichiers listés par Sonar pour supprimer.

**Impact estimé** : -15 issues, meilleure lisibilité.

#### Chantier 7 — Bug accessibilité email

Le template `resources/views/emails/nouveau_message.blade.php` ne contient pas de balise `<title>`, ce qui dégrade l'accessibilité et le rendu dans certains clients mail.

**Action** :

- Ajouter `<title>Nouveau message SkillHub</title>` dans le `<head>` du template.
- Ajouter un test visuel du rendu via un client comme Mailtrap.

**Impact estimé** : -1 bug majeur, meilleure délivrabilité.

### Synthèse prévisionnelle

| Itération | Chantiers | Réduction d'issues |
|---|---|---|
| Sprint 1 | 1, 7 | ~30 issues |
| Sprint 2 | 4, 6 | ~25 issues |
| Sprint 3 | 2, 3 | ~15 issues structurelles |
| Avant prod | 5 | 1 Security Hotspot |

**Cible à 3 mois** : passer de 1 081 issues à moins de 100, avec un Security Hotspot à zéro.

---

## Utilisation de l'IA

Conformément aux règles de l'épreuve **EC06**, l'usage d'IA est déclaré ci-dessous.

**Outil utilisé** : Claude Code (`claude-sonnet-4-6`) — Anthropic

**Parties concernées** :

- Vérification de conformité au cahier des charges Bloc 03
- Création du `README.md` et du `CONTRIBUTING.md`
- Complétion des fichiers `sonar-project.properties`
- Complétion du `.env.example` racine et du `.gitignore`
- Ajout de `sonar.cpd.exclusions` pour maintenir la duplication sous 0,5 %
- Ajout de commentaires explicatifs dans les fichiers de configuration
- Diagnostic et correction des pipelines CI/CD (encodage UTF-8, clés Sonar, workflows mal placés)
- Refactorisation du `Dockerfile` Spring Boot en multi-stage avec utilisateur non-root

**Commandes Claude Code utilisées dans le terminal** :

```bash
# Lancer Claude Code dans le dossier projet
claude

# Analyser l'ensemble du projet
# (effectué via l'outil Explore en sous-agent)

# Lire un fichier spécifique
# (effectué via l'outil Read)

# Créer ou modifier un fichier
# (effectué via les outils Write et Edit)
```

**Principe de déclaration** : toute contribution générée par IA a été relue, testée et validée avant d'être commitée. Aucune portion de code n'a été intégrée sans compréhension préalable de son fonctionnement.