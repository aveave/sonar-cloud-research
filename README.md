# sonar-cloud-research

Spring Boot project for SonarCloud testing with:
- minimal Employee CRUD API
- JPA + PostgreSQL
- integration test with Testcontainers (Docker Postgres)

## Requirements
- Java 21+
- Docker Desktop (for integration tests)

## Run tests
./mvnw test

## Run app locally
Set DB env vars (or run local postgres on `localhost:5432`):
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Then run:
./mvnw spring-boot:run

## API
Base URL: `/api/v1/employees`
- `GET /api/v1/employees`
- `GET /api/v1/employees/{id}`
- `POST /api/v1/employees`
- `PUT /api/v1/employees/{id}`
- `DELETE /api/v1/employees/{id}`

## SonarCloud
Before running scanner, set:
- `SONAR_TOKEN`

Example:
sonar-scanner
