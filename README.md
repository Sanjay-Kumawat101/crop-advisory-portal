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

## Selenium UI tests

Install Google Chrome before running the browser tests. In one terminal, start the app with `mvn spring-boot:run`. In another PowerShell terminal, set environment variables to the local UI test account values (do not put credentials in source control), then run the Selenium/JUnit journeys:

```powershell
$env:UI_FARMER_USERNAME = "<farmer username>"
$env:UI_FARMER_PASSWORD = "<farmer password>"
$env:UI_EXPERT_USERNAME = "<expert username>"
$env:UI_EXPERT_PASSWORD = "<expert password>"
$env:UI_ADMIN_USERNAME = "<admin username>"
$env:UI_ADMIN_PASSWORD = "<admin password>"
mvn verify -Pui-tests "-Dbase.url=http://localhost:8083"
```

Selenium is included with test scope, and the `ui-tests` profile runs `*IT.java` tests through Maven Failsafe. The default `mvn clean package` build does not launch a browser. Chrome runs headlessly, and Selenium Manager resolves the matching WebDriver. If a UI test fails, its browser screenshot is saved under `target/screenshots`; Failsafe reports are written to `target/failsafe-reports`.

## Jenkins CI (Windows)
The Freestyle job `crop-advisory-ci` builds the `develop` branch from GitHub. Configure Jenkins to poll SCM with `H/5 * * * *`; polling is used because a Jenkins instance running locally is not reachable by GitHub webhooks.

## Jenkins Pipeline and Tomcat deployment (Windows)

Keep the existing Freestyle job `crop-advisory-ci` and its build history. Create a separate Jenkins **Pipeline** job (for example, `crop-advisory-pipeline`) using **Pipeline script from SCM**:

1. Set **SCM** to Git, enter the repository URL, and set **Branches to build** to `*/develop` after the feature is merged (or the intended deployment branch for testing).
2. Set **Script Path** to `Jenkinsfile` and enable **Poll SCM** with `H/5 * * * *` if webhooks cannot reach the Jenkins instance.
3. Save and run the job. Its `ENVIRONMENT` choice parameter selects `dev`, `test`, or `prod`; the pipeline builds and archives the WAR before deploying it.

In **Manage Jenkins > Tools**, configure JDK 17 (or a compatible newer JDK) and Maven 3.9. Ensure Git and Maven are on the system `Path` and accessible to the Jenkins Windows service account.

### Jenkins credentials and Windows service permissions

Create a Jenkins credential of kind **Username with password**, with ID `tomcat-manager`. Use the Tomcat Manager account described below. The pipeline binds the credential only for deployment; do not put the username or password in the Jenkinsfile, repository, or command-line job parameters.

Set `CATALINA_BASE` in the Jenkins agent/service environment to the Tomcat base directory. The configured Tomcat 10.1.61 installation is `C:\apache-tomcat-10.1.61`; the deploy script uses this path and the Windows service name `Tomcat10` as defaults when they are not overridden. Configure its HTTP connector to use port 8081 (Jenkins uses 8080), then install the Windows service from an elevated command prompt with `C:\apache-tomcat-10.1.61\bin\service.bat install Tomcat10`. The Jenkins Windows service account needs access to the JDK, Maven, Git, workspace and Maven cache, permission to write those Tomcat/data paths, and permission to stop and start the Tomcat Windows service. Grant only the necessary rights; an administrator may need to install/configure the service and grant service-control permissions.

**Windows service caveat:** `setenv.bat` is normally consumed by Tomcat's startup scripts, but a Tomcat Windows service installed through the service wrapper (Procrun) may not read it. Verify how the service starts Tomcat. If it does not consume `setenv.bat`, configure `APP_ENV` and `DB_PATH` as Java options/environment for that service using its supported service configuration, and ensure the Jenkins account can update the selected environment settings. The script's file creation alone does not guarantee that the running service will inherit them.

### Tomcat Manager setup

In Tomcat's `%CATALINA_BASE%\conf\tomcat-users.xml`, define a dedicated deployment user with only the `manager-script` role. Replace both placeholder values with a unique username and a strong, non-reused secret; the values below are examples, not working credentials:

```xml
<role rolename="manager-script"/>
<user username="jenkins-deploy" password="REPLACE_WITH_A_STRONG_SECRET" roles="manager-script"/>
```

In the Manager application's `META-INF\context.xml` (commonly `%CATALINA_BASE%\webapps\manager\META-INF\context.xml`), retain a `RemoteAddrValve` that permits loopback addresses only so the text API remains local to the Jenkins/Tomcat host:

```xml
<Valve className="org.apache.catalina.valves.RemoteAddrValve"
       allow="127\.\d+\.\d+\.\d+|::1|0:0:0:0:0:0:0:1" />
```

Do not expose the Manager text API to remote hosts. Restart Tomcat after changing its Manager configuration. The pipeline deploys `target\crop-advisory.war` through `http://localhost:8081/manager/text/` and performs a bounded health check against `http://localhost:8081/crop-advisory/actuator/health`.

For delivery evidence, capture the Jenkins Pipeline job's SCM/script-path configuration and a successful build/Deploy console result, plus the local health-check success. Redact usernames, credentials, tokens, and other secrets from screenshots or logs; never include the actual `tomcat-users.xml` secret.

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
