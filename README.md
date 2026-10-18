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

## Docker Desktop (Windows, Linux containers)
Build the WAR before building the image. From the repository root in PowerShell:

```powershell
mvn clean package
docker build -t crop-advisory:0.2.0 .
docker images crop-advisory
docker volume create crop-advisory-data
docker run -d --name crop-advisory -p 8082:8080 -v crop-advisory-data:/data crop-advisory:0.2.0
docker ps
```

The portal is available at http://localhost:8082/login. Port 8082 is used on the host because Jenkins already occupies host port 8080; Docker forwards host port 8082 to the application’s container port 8080. The application stores its H2 database in `/data`, persisted in the named `crop-advisory-data` volume.

Useful container lifecycle commands:

```powershell
# Follow application output; press Ctrl+C to stop following (the container keeps running).
docker logs -f crop-advisory

# Open a shell in the running container; type exit to leave.
docker exec -it crop-advisory bash

# Show the configured image, ports, environment, mount and health status.
docker inspect crop-advisory --format 'Image={{.Config.Image}} Ports={{json .NetworkSettings.Ports}} Env={{json .Config.Env}} Mounts={{json .Mounts}} Health={{json .State.Health}}'

docker stop crop-advisory
docker ps
docker ps -a
docker start crop-advisory
docker ps
docker restart crop-advisory
docker ps
docker stop crop-advisory
docker rm crop-advisory
docker ps -a
docker rmi crop-advisory:0.2.0
```

`docker ps` lists running containers only; after `docker stop`, use `docker ps -a` to see the stopped container. Remove the named data volume only when its database should also be deleted: `docker volume rm crop-advisory-data`.

## Jenkins CI (Windows)
The Freestyle job `crop-advisory-ci` builds the `develop` branch from GitHub. Configure Jenkins to poll SCM with `H/5 * * * *`; polling is used because a Jenkins instance running locally is not reachable by GitHub webhooks.

## Jenkins Pipeline and Docker deployment (Windows)

Keep the existing Freestyle job `crop-advisory-ci` and its build history. Create a separate Jenkins **Pipeline** job (for example, `crop-advisory-pipeline`) using **Pipeline script from SCM**:

1. Set **SCM** to Git, enter the repository URL, and set **Branches to build** to `*/feature/12-jenkins-docker-cd` to validate this branch, then `*/develop` after merge.
2. Set **Script Path** to `Jenkinsfile` and enable **Poll SCM** with `H/5 * * * *` if webhooks cannot reach the Jenkins instance.
3. Save and run the job. It builds and tests, runs Selenium, builds and pushes commit-tagged Docker images, then deploys only after all earlier stages pass.

The UI Tests stage runs Chrome headlessly as the Jenkins service account. Install Chrome for that account and create three Jenkins **Username with password** credentials with IDs `ui-test-farmer`, `ui-test-expert`, and `ui-test-admin`, each for the corresponding local test account. These values are injected only during the test stage. The Maven `ui-tests` profile starts the application in `pre-integration-test` on port 8085 and stops it in `post-integration-test`; `DB_PATH` points to a temporary H2 file under the workspace. The post action publishes Surefire and Failsafe XML reports and archives any `target/screenshots` failure images.

In **Manage Jenkins > Tools**, configure JDK 17 (or a compatible newer JDK) and Maven 3.9. Ensure Git, Maven, and Docker CLI are on the system `Path` and accessible to the Jenkins Windows service account.

### Local registry setup

This pipeline uses a local registry at `localhost:5000`, so it does not require Docker Hub credentials. Start it once on the Docker host before the first pipeline run:

```powershell
docker run -d --name crop-registry --restart unless-stopped -p 5000:5000 registry:2
docker ps --filter name=crop-registry
curl.exe http://localhost:5000/v2/_catalog
```

The image is tagged `localhost:5000/crop-advisory:<BUILD_NUMBER>-<7-character-GIT_COMMIT>` and `latest`; Docker Deploy uses the unique commit tag. Docker Hub can be used instead by adding a Jenkins Username with password credential with ID `dockerhub`, logging in with `docker login --password-stdin`, and using `<DOCKERHUB_USER>/crop-advisory` as the image repository.

### Windows Jenkins service Docker access

Docker commands execute under the Jenkins Windows service identity. Configure Jenkins to run as a dedicated Windows account supported by Docker Desktop, add that same account to the local `docker-users` group from an elevated PowerShell prompt, then restart Jenkins:

```powershell
Add-LocalGroupMember -Group 'docker-users' -Member '.\jenkins'
Restart-Service Jenkins
```

Replace `.\jenkins` with the account shown under **Services > Jenkins > Properties > Log On**. If changing the service identity, grant it **Log on as a service** and the minimum necessary workspace access. Ensure Docker Desktop is running Linux containers. The `docker-users` group grants access to Docker Desktop's privileged named pipe; it does not start Docker Desktop. For a Jenkins service running as `LocalSystem`, use a service account supported by the Docker Desktop installation rather than blindly adding `LocalSystem` to the group.

Docker deployment replaces the Tomcat deploy stage in this pipeline: deploying the same application to both targets would be redundant and require extra Tomcat credentials and service permissions. Tomcat 10.1 on port 8081 remains available for separate manual deployments; Docker uses host port 8082 because Jenkins already occupies 8080.

### Legacy Task 8 Tomcat Jenkins deployment

The earlier Task 8 pipeline used Tomcat Manager. To continue using that separate deployment flow, create a Jenkins Username with password credential with ID `tomcat-manager`. Do not put its username or password in the Jenkinsfile, repository, or command-line parameters.

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

Do not expose the Manager text API to remote hosts. Restart Tomcat after changing its Manager configuration. The Task 8 deploy script deploys `target\crop-advisory.war` through `http://localhost:8081/manager/text/` and performs a bounded health check against `http://localhost:8081/crop-advisory/actuator/health`.

For Task 8 delivery evidence, capture the Jenkins Pipeline job's SCM/script-path configuration and a successful Tomcat deployment/health-check result. Redact usernames, credentials, tokens, and other secrets from screenshots or logs; never include the actual `tomcat-users.xml` secret.

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
