# 10-Minute Demo and Viva Preparation

## Demo setup

Before presenting, verify that the Docker container and Ansible target are healthy, the Jenkins job has a recent successful build, and the browser can reach each configured port. Demo accounts are seeded sample data; do not use real farmer information.

## 10-minute demo script

| Time | Demo action | Talk track |
|---|---|---|
| 0:00-0:45 | Introduce the portal and show the README architecture diagram. | “Farmers submit crop queries, experts review and advise, and administrators monitor the workflow. This release also demonstrates build, test, container, and configuration-management stages.” |
| 0:45-1:30 | Show the ports table, stack, and running deployment URLs. | “Jenkins is on 8080; application targets are separated across Tomcat 8081, Docker 8082, development 8083, and Ansible Ubuntu 8084.” |
| 1:30-2:30 | Sign in as `farmer1` at `http://localhost:8082/login`; create a query. | “A farmer can create and follow a query. The identifiers used by Selenium are preserved as stable UI selectors.” |
| 2:30-3:15 | Search for the new query as farmer. | “The record is persisted and searchable; the UI flow verifies that a newly created record can be found.” |
| 3:15-4:15 | Sign out; sign in as `expert1`; move the submitted query to Under Review and then Advised with advice. | “Experts advance the workflow through the permitted transitions and attach advice.” |
| 4:15-5:00 | Sign out; sign in as `admin1`; show the dashboard. Briefly try `/dashboard` as farmer if time permits. | “The dashboard is ADMIN-only; the UI suite asserts a farmer receives 403 and an admin sees the summary.” |
| 5:00-6:30 | Show Jenkins build stages and Surefire/Failsafe report for a passing run. | “Build and unit tests run first. Selenium is a gate; Docker image build, push, and replacement happen only after it passes.” |
| 6:30-7:30 | Show Docker image tag, container port mapping, logs, and health. | “The pipeline tags the image with build number and commit, pushes to the local registry, and replaces the named container while retaining its data volume.” |
| 7:30-8:45 | Show Ansible syntax/idempotency evidence and the active release/current symlink on Ubuntu. | “Ansible runs from Ubuntu WSL2 against a separate Ubuntu systemd target. It installs a versioned WAR, probes health, and automatically restores the old release on failure.” |
| 8:45-9:30 | Show release branch commit, tag/release page if available, and changelog. | “The release branch is prepared for review; the final main-branch merge and GitHub release happen only after the project’s PR checks and approval.” |
| 9:30-10:00 | Close with limitations and future work. | “H2 and local HTTP are demo choices. PostgreSQL, HTTPS, centralized monitoring, notifications, and production orchestration are follow-up work.” |

Demo passwords are listed in the README and are intentionally predictable sample credentials. Do not show or reuse Jenkins secrets during the presentation.

## 20 likely viva questions

1. **What problem does the portal solve?** It lets farmers submit crop issues, experts provide advice, and administrators monitor query status.
2. **Why Spring Boot?** It provides the web, security, persistence, actuator, and executable-WAR integration used by this compact Java application.
3. **Why package as a WAR?** The same artifact can run with embedded Spring Boot Tomcat or be deployed to external Tomcat 10.1.
4. **What are the user roles?** FARMER, EXPERT, and ADMIN, enforced through Spring Security.
5. **What is the workflow?** Submitted, Under Review, Advised, and Closed.
6. **How is the dashboard protected?** The security filter restricts `/dashboard/**` to ADMIN; tests verify farmer denial and admin access.
7. **Why H2?** It keeps local/demo setup simple and file-backed; PostgreSQL is a future enhancement for production durability and concurrency.
8. **How does the UI suite run?** JUnit 5 and Selenium run `*IT.java` through Maven Failsafe under the `ui-tests` profile, using headless Chrome.
9. **Why not launch a browser for every package build?** The profile keeps `mvn clean package` usable on build agents without a browser; UI tests are an explicit gate.
10. **How are UI test credentials handled in Jenkins?** They are Jenkins credentials injected only into the test stage, not stored in source.
11. **Why is Jenkins on port 8080 while Docker uses 8082?** Host port 8080 is reserved by the Jenkins service; Docker maps 8082 on the host to 8080 in its container.
12. **What gates deployment?** Build/unit tests and Selenium must succeed before Docker image push/deployment stages proceed.
13. **How is an image identified?** Jenkins uses build number plus a short Git commit hash, and also updates a `latest` tag.
14. **Why use a volume for Docker?** It preserves the H2 database across container replacement.
15. **Why run Ansible from WSL2?** Ansible control tooling is run in Linux; WSL2 provides that control environment from the Windows development host.
16. **What makes the Ansible play idempotent?** Declarative package, account, file, template, symlink, and service tasks converge on the desired state and avoid restarting when inputs are unchanged.
17. **How does automatic rollback work?** It records the prior `current` target, switches to the candidate release, requires HTTP 200 and JSON status `UP`, then restores and verifies the prior release if health fails.
18. **Why keep immutable versioned releases?** A failed deployment can return to known prior bytes instead of overwriting the rollback artifact.
19. **What security limitations remain?** Demo credentials, local HTTP, local registry, and lack of production secret/identity integration must be addressed before public use.
20. **What would you improve next?** PostgreSQL with migrations, HTTPS, monitoring/alerts, notifications, SonarQube quality gates, then evaluate Kubernetes where scale requires it.
