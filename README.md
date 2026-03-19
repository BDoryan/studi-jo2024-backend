# STUDI | JO 2024 – Ticketing Platform

> Spring Boot backend for booking, payment, and ticket control for the 2024 Olympic Games.

## Quick Access

* Public website: [https://jo2024.doryanbessiere.fr/](https://jo2024.doryanbessiere.fr/)
* Admin interface: [http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo](http://jo2024.doryanbessiere.fr/admin/vG3EGPqaJo)
* API documentation: [https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html](https://jo2024-api.doryanbessiere.fr/swagger-ui/index.html)
* Technical documentation: docs/documentation-technical.md
* Test report: [https://bdoryan.github.io/studi-jo2024-backend/report/test/index.html](https://bdoryan.github.io/studi-jo2024-backend/report/test/index.html)
* User manual: docs/manuel-d-utillisation.pdf

## Docker Deployment

The full project (backend, frontend, database) is containerized with Docker.
The Docker repository is available here: [https://github.com/BDoryan/studi-jo2024-docker](https://github.com/BDoryan/studi-jo2024-docker)

## About

This repository hosts the **Spring Boot 3 / Java 21 backend** of the JO 2024 project developed as part of the Studi ECF.
It provides a secure REST API consumed by a public website, an admin back-office, and a ticket control application.

### Main Features

* Browse ticketing offers (solo, duo, family…)
* Customer authentication (account creation, login, password reset)
* Admin management and inspection roles
* Stripe Checkout integration and e-ticket generation (QR code)
* Ticket validation on the day of the event

## Quick Start

1. **Prerequisites**: JDK 21, MySQL 8+, Stripe/SMTP/JWT environment variables

2. **Configuration**: duplicate `application.properties` or override it via environment variables (`spring.datasource.*`, `app.jwt.*`, `stripe.*`, etc.)

3. **Run the API locally**:

   ```bash
   ./gradlew bootRun
   ```

4. **Build the artifact**:

   ```bash
   ./gradlew build
   ```

## Tests

```bash
./gradlew test
```

The HTML report is generated in `docs/report/test` after execution.

## Additional Resources

* Detailed technical documentation (architecture, security, business flows): docs/documentation-technical.md
* Stripe CLI command to listen to webhooks:

  ```bash
  stripe listen --forward-to localhost:8080/stripe/webhook
  ```
