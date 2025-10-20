# STUDI | JO 2024 – Plateforme de billetterie

> Backend Spring Boot pour la réservation, le paiement et le contrôle de billets des Jeux Olympiques 2024.

## Accès rapides

- Site public : [https://jo2024.doryanbessiere.fr/](https://jo2024.doryanbessiere.fr/)
- Interface
  administrateur : [http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo](http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo)
- Documentation
  API : [https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html](https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html)
- Documentation technique : [docs/documentation-technique.md](docs/documentation-technical.md)
- Rapport de
  tests : [https://bdoryan.github.io/studi-jo2024-backend/report/test/index.html](https://bdoryan.github.io/studi-jo2024-backend/report/test/index.html)

## À propos

Ce dépôt héberge le **backend Spring Boot 3 / Java 21** du projet JO 2024 développé dans le cadre de l’ECF Studi.  
Il fournit une API REST sécurisée consommée par un site public, un back-office et une application de contrôle des
billets.

### Fonctionnalités principales

- Consultation des offres de billetterie (solo, duo, famille…)
- Authentification client (création de compte, connexion, réinitialisation de mot de passe)
- Gestion des administrateurs et rôles d’inspection
- Intégration Stripe Checkout et génération de e-billets (QR code)
- Validation des billets le jour de l’événement

## Démarrage rapide

1. **Prérequis** : JDK 21, MySQL 8+, variables d’environnement Stripe/SMTP/JWT.
2. **Configuration** : dupliquez `application.properties` ou surchargez-le via des variables (`spring.datasource.*`,
   `app.jwt.*`, `stripe.*`…).
3. **Lancer l’API en local** :

   ```bash
   ./gradlew bootRun
   ```

4. **Construire l’artefact** :

   ```bash
   ./gradlew build
   ```

## Tests

```bash
./gradlew test
```

Le rapport HTML est copié dans `docs/report/test` après exécution.

## Ressources complémentaires

- Documentation technique détaillée (architecture, sécurité, flux
  métier) : [docs/documentation-technique.md](docs/documentation-technical.md)
- Commande Stripe CLI pour écouter les webhooks :

  ```bash
  stripe listen --forward-to localhost:8080/stripe/webhook
  ```

---

Projet maintenu par Doryan Bessière & José - ESN InfoEvent (Studi). 
