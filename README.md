# Crop Advisory Portal

A web portal where farmers raise crop queries, agricultural experts give advice, and admins monitor progress. The project is also a demonstration of a full DevOps lifecycle.

## Features (MVP)
- Create, view, update and search advisory records
- Role-based status workflow: Submitted, Under Review, Advised, Closed
- Summary dashboard (totals, status-wise and crop-wise counts)
- Login with roles: Farmer, Expert, Admin

## Tech stack
Java 17, Spring Boot 3.5 (WAR), Thymeleaf, Spring Security, Spring Data JPA, H2, Maven, Tomcat 10.1

## Ports
| Service | Port |
|---|---|
| Jenkins | 8080 |
| Tomcat | 8081 |
| Docker host port | 8082 |
| Dev run (mvn spring-boot:run) | 8083 |

## Getting started
1. Install JDK 17, Maven 3.9 and Git.
2. Clone the repository and open the project folder.
3. Build and test: `mvn clean package`
4. Run: `mvn spring-boot:run`
5. Open http://localhost:8083/login
6. Health check: http://localhost:8083/actuator/health

## Jenkins CI (Windows)
The Freestyle job `crop-advisory-ci` builds the `develop` branch from GitHub. Configure Jenkins to poll SCM with `H/5 * * * *`; polling is used because a Jenkins instance running locally is not reachable by GitHub webhooks.

In **Manage Jenkins > Tools**, configure a JDK installation and Maven 3.9. The project compiles for Java 17; a newer JDK such as 21 can also run the build while preserving that target. Ensure Git and Maven are available to the Jenkins Windows service account and on the system `Path`.

Configure the job with:
- **Source Code Management:** Git repository URL and branch `*/develop`.
- **Build trigger:** Poll SCM schedule `H/5 * * * *`.
- **Build step:** Execute Windows batch command `mvn clean package`.
- **Archive artifacts:** `target/*.war`.
- **Publish JUnit test results:** `target/surefire-reports/*.xml`.

The packaged WAR is `target/crop-advisory.war`. Jenkins must have permission to access the JDK, Maven, Git, job workspace, and Maven local repository.

## Deploy on Tomcat 10.1 (port 8081)
Copy `target/crop-advisory.war` into `<TOMCAT_HOME>/webapps/` and open http://localhost:8081/crop-advisory/login

## Configuration (environment variables)
| Variable | Default | Purpose |
|---|---|---|
| APP_PORT | 8083 | Port for `mvn spring-boot:run` |
| APP_ENV | dev | Environment label shown on the home page |
| DB_PATH | ./data/cropdb | H2 database file location |

## Branching
See docs/BRANCH_POLICY.md. Work happens on feature branches, merged into develop through pull requests. The main branch holds tagged, release-ready code.

## Demo users (dummy data only)
| Username | Password | Role |
|---|---|---|
| farmer1 | Farmer@123 | Farmer |
| expert1 | Expert@123 | Expert |
| admin1 | Admin@123 | Admin |

## Project status
Work is tracked through GitHub Issues and milestones.
