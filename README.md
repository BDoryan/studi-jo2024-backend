# JO 2024 – Plateforme de billetterie

> Application Spring Boot permettant la vente, le paiement et la validation des billets des Jeux Olympiques 2024.

- Application publique : https://jo2024.doryanbessiere.fr/
- Interface administrateur : http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo
- Documentation API (Swagger UI) : https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html

---

## Introduction du projet

La plateforme **JO 2024** propose une expérience de bout en bout pour l’achat de billets : consultation des offres, paiement sécurisé via Stripe, génération de billets et contrôle à l’entrée.  
Le projet a été réalisé dans le cadre de l’ECF Studi et est composé :

- d’un backend **Spring Boot 3 / Java 21** (ce dépôt) ;
- d’un frontend public et d’un backoffice administrateur (applications séparées) ;
- d’intégrations externes : Stripe (paiement) et SMTP (notifications e-mail).

---

## Architecture globale de la solution

La solution suit une architecture hexagonale légère organisée par domaines métiers.

```
Clients Web / Admin
        │
        ▼
Reverse Proxy (https://jo2024-api.doryanbessiere.fr)
        │
        ▼
Spring Boot (API REST stateless)
├─ config/      (configuration applicative, sécurité, CORS, OpenAPI)
├─ services/    (couches domaine : customers, admins, offers, payments, tickets)
├─ notifications/ (envoi d’e-mails via SMTP + moteur de templates)
├─ shared/      (authentification JWT, aspects de sécurité)
└─ common/      (exceptions, DTO communs, routes, validations)
        │
        ├─ Base MySQL (production) / H2 (tests)
        └─ Stripe API + Serveurs SMTP externes
```

- **Contrôleurs REST** exposent les endpoints publics.
- **Services métiers** encapsulent la logique (authentification, paiement, génération de billets…).
- **Repositories Spring Data JPA** orchestrent la persistance.
- **Aspects AOP** (`@AdminOnly`, `@CustomerOnly`) renforcent les contrôles d’accès.
- **Configuration** centralise Stripe, CORS, sécurité et initialisation des comptes.

---

## Modélisation (MCD / diagramme de classes)

| Entité | Description | Attributs clés | Relations |
| --- | --- | --- | --- |
| `Customer` | Acheteur en front-office. | `id`, `firstName`, `lastName`, `email` (unique), `password`, `secretKey`, `expireToken`. | 1 -* `Transaction` (via `Transaction.customer`), tickets via `customerSecret`. |
| `Admin` | Utilisateur backoffice (rôles `ADMIN`/`SCANNER`). | `id`, `email` (unique), `password`, `fullName`, `role`. | Authentification dédiée, pas de relation directe aux billets. |
| `Offer` | Offre commerciale (pack de billets). | `id`, `name`, `description`, `price`, `persons`, `quantity`, `active`. | Consultée via API publique, référencée par `Transaction.offerId`. |
| `Transaction` | Session d’achat Stripe. | `id`, `stripeSessionId`, `offerId`, `offerName`, `amount`, `status`, `createdAt`. | `ManyToOne` vers `Customer`, 1 -* `Ticket`. |
| `Ticket` | Billet généré après paiement. | `id`, `secretKey`, `customerSecret`, `entriesAllowed`, `status`, `createdAt`. | `ManyToOne` vers `Transaction`. |

**Principes de modélisation :**

- Les billets sont générés **après confirmation Stripe** (`checkout.session.completed`) afin de garantir qu’une transaction `PAID` a bien été enregistrée.
- Le lien entre `Ticket` et `Customer` est indirect (via `customerSecret`) pour simplifier l’exposition d’identifiants non sensibles côté client.
- `Transaction` conserve un snapshot de l’offre (`offerName`, `offerId`, `amount`) pour historiser la commande même si l’offre change.
- Les validations de mot de passe (`ValidPassword`, `PasswordMatches`) et d’unicité e-mail (`UniqueEmailValidator`) sont mutualisées via `common.validation`.

---

## Documentation API (Swagger)

Les contrôleurs REST exposés par l’API sont décrits dans la documentation Swagger disponible à `/swagger-ui/index.html`. Récapitulatif des requêtes :

### CustomerAuthController
- `POST /auth/customer/register`
- `POST /auth/customer/login`
- `GET /auth/customer/me`
- `GET /auth/customer/me/tickets`

### AdminAuthController
- `POST /auth/admin/login`
- `GET /auth/admin/me`

### OfferController
- `GET /offers`
- `GET /offers/{id}`
- `POST /offers`
- `PUT /offers/{id}`
- `DELETE /offers/{id}`

### PaymentController
- `POST /payments/checkout`
- `GET /payments/status/{session_id}`

### StripeWebhookController
- `POST /stripe/webhook`

### TicketController
- `POST /tickets/scan`
- `POST /tickets/validate`

---

## Sécurité de l’application

- **Stateless JWT** : signatures HMAC-SHA via `JwtService`, durée de vie configurable (`app.jwt.expiration-ms`).
- **Spring Security 6** : configuration `SecurityConfig` (CORS, CSRF off, sessions stateless, encodage Bcrypt).
- **Contrôles d’accès métiers** : annotations `@AdminOnly` et `@CustomerOnly` reposant sur des aspects AOP (`shared/security`) pour vérifier le rôle extrait du JWT.
- **Gestion centralisée des erreurs** : `GlobalExceptionHandler` renvoie des réponses JSON homogènes (`status`, `message`, `errors`).
- **Validation des données** : contraintes Bean Validation (password strength, matching, unique e-mail), retours formatés (`errors.<champ>`).
- **Webhooks Stripe** : vérification de la signature (`stripe.webhook.secret`) et états idempotents côté transactions.
- **CORS** : origine autorisée configurable (`CORS_ALLOWED_ORIGIN`), support des requêtes avec cookies / headers personnalisés.

- Application : https://jo2024.doryanbessiere.fr/
- Application administrateur : http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo
- API : https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html

# Stripe for connect Webhook:
``stripe listen --forward-to localhost:8080/stripe/webhook
``
# Stripe for connect Webhook:
``stripe listen --forward-to localhost:8080/stripe/webhook
``
