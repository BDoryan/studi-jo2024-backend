# STUDI | JO 2024 – Plateforme de billetterie

> Application Spring Boot pour la vente, le paiement et la validation de billets des Jeux Olympiques 2024.

* Application publique : [https://jo2024.doryanbessiere.fr/](https://jo2024.doryanbessiere.fr/)
* Interface administrateur : [http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo](http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo)
* Documentation API : [https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html](https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html)

---

## Présentation du projet

Le projet **JO 2024** est une plateforme complète de billetterie permettant d’acheter des offres, d’effectuer un paiement sécurisé via Stripe, puis de générer et valider les billets à l’entrée.
Il a été développé dans le cadre de l’ECF Studi et se compose :

* d’un **backend Spring Boot 3 / Java 21** (ce dépôt) ;
* d’un **frontend public** et d’un **backoffice administrateur** séparés ;
* d’intégrations externes comme **Stripe** pour les paiements et **SMTP** pour l’envoi d’e-mails.

---

## Architecture du projet

L’application suit une architecture claire, organisée par domaines métiers.

Schéma global :
[https://excalidraw.com/#json=kW1RE2LWFbD9jQYJmal7C,Iiudqv7kNZxKyqhmhEGPwA](https://excalidraw.com/#json=kW1RE2LWFbD9jQYJmal7C,Iiudqv7kNZxKyqhmhEGPwA)

* Les **contrôleurs REST** exposent les routes de l’API.
* Les **services** contiennent la logique principale (authentification, paiement, génération de billets, etc.).
* Les **repositories JPA** gèrent l’accès à la base de données.
* Les **aspects AOP** (`@AdminOnly`, `@CustomerOnly`) assurent les contrôles d’accès.
* La **configuration** regroupe la sécurité, CORS, Stripe et l’initialisation des comptes par défaut.

---

## Modèle de données (MCD / classes principales)

<img src="https://github.com/BDoryan/studi-jo2024-backend/blob/master/docs/mcd_mvp1.png?raw=true" alt="MCD JO 2024" width="600"/>

### Points clés :

* Les billets sont créés **uniquement après la confirmation Stripe** (`checkout.session.completed`).
* Le lien entre un ticket et un client passe par un **identifiant interne non sensible** (`customerSecret`).
* Chaque transaction garde une copie des infos de l’offre (nom, prix) même si celle-ci change plus tard.
* Les validations de mot de passe et d’e-mail sont gérées via des annotations personnalisées.

---

## Endpoints principaux (Swagger)

La documentation complète est disponible sur [https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html](https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html)

## Sécurité

* **JWT stateless** : génération et vérification de tokens signés (HMAC-SHA) via `JwtService`.
* **Spring Security 6** : configuration `SecurityConfig`, sessions désactivées, Bcrypt pour les mots de passe.
* **Contrôles d’accès** : annotations `@AdminOnly` et `@CustomerOnly` vérifiant le rôle dans le JWT.
* **Gestion d’erreurs unifiée** : `GlobalExceptionHandler` pour des réponses JSON cohérentes.
* **Validation des données** : contraintes standard (force du mot de passe, e-mail unique, etc.).
* **Webhooks Stripe** : signature vérifiée via la clé secrète et gestion des doublons.
* **CORS** : origine autorisée configurable (`CORS_ALLOWED_ORIGIN`), compatible avec les headers personnalisés.

---

### Accès rapides

* Application publique : [https://jo2024.doryanbessiere.fr/](https://jo2024.doryanbessiere.fr/)
* Administration : [http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo](http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo)
* API (Swagger) : [https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html](https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html)

---

### Commande utile (Webhook Stripe)

```
stripe listen --forward-to localhost:8080/stripe/webhook
```