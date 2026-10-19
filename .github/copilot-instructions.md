# Crop Advisory Portal Guidelines

## Project
- Java 17, Spring Boot 3.5, packaged as a WAR; Thymeleaf UI, Spring Security, Spring Data JPA, H2, and Maven 3.9.
- Java packages use `com.cropadvisory`.
- Roles are `FARMER`, `EXPERT`, and `ADMIN`. The advisory workflow statuses are `Submitted`, `Under Review`, `Advised`, and `Closed`.
- Preserve existing application behavior and project patterns. Keep changes minimal, and explain every file changed in the completion summary.

## Build and run
- Follow the setup and commands in [README.md](../README.md). Build and test with `mvn clean package`; run locally with `mvn spring-boot:run`.
- Configuration uses `APP_PORT` (development default `8083`), `APP_ENV`, and `DB_PATH`.
- `/actuator/health` is public; preserve its unauthenticated access when changing security rules.

## Ports and Windows environment
- Jenkins is a Windows service on port `8080`; do not use that port for the application.
- Tomcat 10.1 uses port `8081`; the Docker host port is `8082`; local development uses `8083`.
- Jenkins runs on Windows. In Jenkins pipelines use `bat`, never `sh`. Use Windows backslash paths and PowerShell or `curl.exe`; do not use bash-only commands or syntax.

## UI and browser tests
- Do not change, remove, or repurpose existing HTML `id` attributes: Selenium tests depend on them.
- When changing UI behavior, preserve the existing selectors and verify related tests where practical.

## Branches and commits
- Follow [docs/BRANCH_POLICY.md](../docs/BRANCH_POLICY.md) for branch naming and pull request rules.
- Prefix commit messages with `feat:`, `fix:`, `docs:`, `test:`, `ci:`, or `chore:`, followed by a short present-tense summary.
