# TP5 — Évolution Password Change & Déploiement Docker

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?style=flat-square&logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue?style=flat-square&logo=docker)
![SonarCloud](https://img.shields.io/badge/SonarCloud-Quality%20Gate-brightgreen?style=flat-square&logo=sonarcloud)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-black?style=flat-square&logo=githubactions)
![License](https://img.shields.io/badge/License-Academic-lightgrey?style=flat-square)

---

## Table des matières

- [Présentation](#présentation)
- [Architecture du projet](#architecture-du-projet)
- [Nouvelles fonctionnalités TP5](#nouvelles-fonctionnalités-tp5)
- [Sécurité](#sécurité)
- [API REST](#api-rest)
- [Lancer le projet](#lancer-le-projet)
- [Docker](#docker)
- [Pipeline CI/CD](#pipeline-cicd)
- [Tests JUnit](#tests-junit)
- [Analyse qualité SonarCloud](#analyse-qualité-sonarcloud)
- [Interface JavaFX](#interface-javafx)
- [Auteur](#auteur)

---

## Présentation

Le **TP5** est l'évolution du serveur d'authentification sécurisé initié au TP4. Il introduit une nouvelle fonctionnalité de **changement de mot de passe** et la **conteneurisation Docker** de l'application Spring Boot, intégrée dans un pipeline CI/CD complet via GitHub Actions.

### Progression des TPs

| TP | Thème | Apport principal |
|----|-------|-----------------|
| TP1 | Authentification dangereuse | Base REST + Spring Boot |
| TP2 | Authentification fragile | Hachage des mots de passe |
| TP3 | Authentification forte | HMAC + Nonce + Timestamp |
| TP4 | Industrialisation | Master Key + GitHub Actions |
| **TP5** | **Évolution + Docker** | **Change Password + Conteneurisation** |

---

## Architecture du projet

```
TP_5_Docker/
│
├── src/
│   ├── main/
│   │   ├── java/com/example/auth/
│   │   │   ├── controller/         # AuthController, HomeController
│   │   │   ├── service/            # AuthService, PasswordCryptoService,
│   │   │   │                       # HmacService, ClientProofService
│   │   │   ├── entity/             # User, AuthNonce
│   │   │   ├── dto/                # RegisterRequest, LoginRequest,
│   │   │   │                       # ChangePasswordRequest, ClientProofRequest/Response
│   │   │   ├── repository/         # UserRepository, AuthNonceRepository
│   │   │   ├── validator/          # PasswordPolicyValidator, PasswordStrengthUtil
│   │   │   ├── exception/          # GlobalExceptionHandler
│   │   │   ├── client/             # AuthClientUI (JavaFX)
│   │   │   └── ui/                 # AuthUiApplication, AuthUiController
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       ├── java/com/example/auth/  # Tests JUnit
│       └── resources/
│           └── application.properties  # Config H2 pour les tests
│
├── .github/
│   └── workflows/
│       └── ci.yml                  # Pipeline GitHub Actions
│
├── Dockerfile                      # Conteneurisation Docker
├── pom.xml                         # Dépendances Maven + JaCoCo + SonarCloud
└── README.md
```

---

## Nouvelles fonctionnalités TP5

### Changement de mot de passe — `PUT /api/auth/change-password`

Le serveur effectue les validations suivantes dans l'ordre :

1. Vérification que l'utilisateur est **authentifié** (token Bearer valide)
2. Vérification que l'**ancien mot de passe** est correct
3. Vérification que `newPassword` et `confirmPassword` sont **identiques**
4. Vérification de la **force du nouveau mot de passe** (politique de sécurité)
5. **Chiffrement** du nouveau mot de passe avec la Master Key (AES-GCM)
6. **Mise à jour** en base de données
7. Levée d'exception si une règle n'est pas respectée

---

## Sécurité

### Politique de mot de passe

| Règle | Valeur |
|-------|--------|
| Longueur minimale | 12 caractères |
| Majuscule | Au moins 1 |
| Minuscule | Au moins 1 |
| Chiffre | Au moins 1 |
| Caractère spécial | Au moins 1 |

### Chiffrement AES-GCM

- Transformation : `AES/GCM/NoPadding`
- Tag d'authentification : 128 bits
- IV aléatoire : 12 bytes (généré à chaque chiffrement)
- Clé maître serveur (SMK) : injectée via variable d'environnement

### Protocole HMAC

- Authentification basée sur HMAC-SHA256
- Protection contre les attaques de rejeu (Nonce + Timestamp)
- Tokens Bearer pour les endpoints protégés

---

## API REST

### Endpoints disponibles

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| `POST` | `/api/auth/register` | ❌ | Inscription |
| `POST` | `/api/auth/client-proof` | ❌ | Génération HMAC |
| `POST` | `/api/auth/login` | ❌ | Connexion HMAC |
| `GET` | `/api/auth/me` | ✅ Bearer | Profil utilisateur |
| `POST` | `/api/auth/logout` | ✅ Bearer | Déconnexion |
| `PUT` | `/api/auth/change-password` | ✅ Bearer | Changement de mot de passe |

### Exemple — Changement de mot de passe

**Requête :**
```http
PUT /api/auth/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "oldPassword": "AncienMotDePasse123!",
  "newPassword": "NouveauMotDePasse456@",
  "confirmPassword": "NouveauMotDePasse456@"
}
```

**Réponse succès :**
```json
{
  "message": "Mot de passe changé avec succès"
}
```

**Réponse erreur :**
```json
{
  "error": "Ancien mot de passe incorrect"
}
```

---

## Lancer le projet

### Prérequis

- Java 17+
- Maven 3.8+
- MySQL 8+
- Docker (optionnel)

### Configuration

Créez un fichier `application.properties` ou définissez les variables d'environnement :

```properties
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/tp5_auth_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
APP_SECURITY_SMK=votre_master_key_secrete
```

### Lancement en local

```bash
# Cloner le projet
git clone https://github.com/Nirina2108/TP_5_Docker.git
cd TP_5_Docker

# Build et lancement
./mvnw clean package
java -jar target/TP_5_Docker-*.jar
```

L'application est accessible sur : **http://localhost:8000**

---

## Docker

### Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Build et run en local

```bash
# Build de l'image
docker build -t nirina2108/tp_5_docker:latest .

# Lancement du conteneur
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/tp5_auth_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e APP_SECURITY_SMK=votre_master_key \
  nirina2108/tp_5_docker:latest
```

### Récupérer l'image depuis Docker Hub

```bash
docker pull nirina2108/tp_5_docker:latest
```

> Image disponible sur : [hub.docker.com/r/nirina2108/tp_5_docker](https://hub.docker.com/r/nirina2108/tp_5_docker)

---

## Pipeline CI/CD

Le pipeline GitHub Actions se déclenche à chaque push sur `main` ou `tp5-dev`.

### Étapes du pipeline

```
┌─────────────────────────────────────────────────────────┐
│                   GitHub Actions Pipeline                │
│                                                         │
│  1. Checkout du projet    →  actions/checkout@v4        │
│  2. Installer Java 17     →  actions/setup-java@v4      │
│  3. Droits mvnw           →  chmod +x mvnw              │
│  4. Build + Tests Maven   →  ./mvnw clean verify        │
│  5. Analyse SonarCloud    →  ./mvnw sonar:sonar         │
│  6. Build image Docker    →  docker build               │
│  7. Login Docker Hub      →  docker/login-action@v3     │
│  8. Push Docker Hub       →  docker push                │
└─────────────────────────────────────────────────────────┘
```

### Secrets GitHub requis

| Secret | Description |
|--------|-------------|
| `SONAR_TOKEN` | Token d'analyse SonarCloud |
| `DOCKER_USERNAME` | Login Docker Hub (`nirina2108`) |
| `DOCKER_PASSWORD` | Access Token Docker Hub |

### Fichier `.github/workflows/ci.yml`

```yaml
name: TP5 CI/CD

on:
  push:
    branches: [main, tp5-dev]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
          cache: maven
      - run: chmod +x mvnw
      - run: ./mvnw clean verify
      - name: Analyse SonarCloud
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: ./mvnw sonar:sonar -Dsonar.token=${{ secrets.SONAR_TOKEN }}
      - run: docker build -t nirina2108/tp_5_docker:latest .
      - uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      - run: docker push nirina2108/tp_5_docker:latest
```

---

## Tests JUnit

### Cas de test couverts

| Cas de test | Statut |
|-------------|--------|
| Changement de mot de passe réussi | ✅ |
| Ancien mot de passe incorrect | ✅ |
| Confirmation différente | ✅ |
| Nouveau mot de passe trop faible | ✅ |
| Utilisateur inexistant | ✅ |
| Chiffrement/déchiffrement AES-GCM | ✅ |
| Validation politique de mot de passe | ✅ |
| Login HMAC valide | ✅ |
| Login HMAC invalide | ✅ |

### Lancer les tests

```bash
./mvnw test
```

### Configuration H2 pour les tests

Les tests utilisent une base de données **H2 en mémoire** (pas de MySQL requis) :

```properties
# src/test/resources/application.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
app.security.smk=test_master_key_1234
```

---

## Analyse qualité SonarCloud

### Configuration

```xml
<!-- pom.xml -->
<sonar.organization>nirina2108</sonar.organization>
<sonar.projectKey>Nirina2108_TP_5_Docker</sonar.projectKey>
<sonar.host.url>https://sonarcloud.io</sonar.host.url>
<sonar.coverage.jacoco.xmlReportPaths>
    target/site/jacoco/jacoco.xml
</sonar.coverage.jacoco.xmlReportPaths>
```

### Exclusions du coverage

Les classes UI et exceptions sont exclues de l'analyse :

```xml
<sonar.coverage.exclusions>
    **/client/**,
    **/ui/**,
    **/*Application.java,
    **/exception/**
</sonar.coverage.exclusions>
```

### Corrections SonarCloud appliquées

| Règle | Description | Correction |
|-------|-------------|------------|
| `java:S2119` | `SecureRandom` recréé à chaque appel | `private static final SecureRandom` |
| `java:S2142` | `InterruptedException` ignorée | `Thread.currentThread().interrupt()` |

---

## Interface JavaFX

L'interface graphique **AuthClientUI** permet de tester tous les endpoints de l'API sans outil externe.

### Fonctionnalités

- Inscription utilisateur avec indicateur de force du mot de passe
- Génération de la preuve client HMAC
- Login sécurisé avec récupération automatique du token
- Consultation du profil `/me`
- Déconnexion
- **Changement de mot de passe** avec confirmation en temps réel
- Affichage JSON indenté des réponses

### Lancement de l'interface

```bash
./mvnw javafx:run
```

---

## Auteur

**Poun** — Étudiant en développement logiciel sécurisé

> Projet réalisé dans le cadre des TPs de Sécurité des Applications — D. Samfat