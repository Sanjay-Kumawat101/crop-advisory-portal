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
| Ansible-managed Ubuntu service | 8084 |

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

## Ansible deployment (WSL2 control node)

Ansible's control node requires Linux; run it from Ubuntu on WSL2, not from Windows. Install Ubuntu 24.04 (or 22.04) from Windows PowerShell if it is not already available:

```powershell
wsl.exe --install -d Ubuntu-24.04
```

The managed host must be a second Ubuntu 22.04/24.04 WSL2 distro or a Linux VM with SSH and systemd. Enable systemd in `/etc/wsl.conf` on each WSL distro that needs it:

```ini
[boot]
systemd=true
```

After editing the file, run `wsl.exe --shutdown` in Windows PowerShell and reopen the distros. Install Ansible and the build prerequisites in the control distro:

```bash
sudo apt update
sudo apt install -y ansible openjdk-17-jdk maven
```

Check `mvn -version` before packaging: Ubuntu's repository may provide a Maven release older than the project's Maven 3.9 requirement. Use Maven 3.9 for the WAR build.

On the target, install and enable SSH, and ensure the SSH account can use `sudo`:

```bash
sudo apt update
sudo apt install -y openssh-server
sudo systemctl enable --now ssh
hostname -I
```

Configure SSH key authentication or use Ansible's password prompts. Do not commit private keys or passwords. Clone the project inside the control distro's Linux filesystem, build and test the WAR, and enter the Ansible directory:

```bash
mvn -B clean package
cd ansible
```

Set the target host address and SSH user in `inventory/hosts.ini`. This branch's entry points to the local WSL test target; WSL addresses may change, so refresh it with `hostname -I` on the target after WSL restarts. A VM with a stable address is preferable. The inventory group is `crop_servers`.

```bash
ansible --version
ansible-inventory --list
ansible all -m ping
ansible-playbook site.yml --syntax-check
ansible-playbook site.yml --check --diff
ansible-playbook site.yml
```

The local disposable WSL target uses key-only SSH and passwordless sudo for the `ansible` account. For a regular target, configure SSH and sudo securely, and add `--ask-become-pass` to check and deployment runs when sudo requires a password. Do not use passwordless sudo as a general production recommendation.

The playbook installs Java 17 and curl, installs the WAR under `/opt/crop-advisory/releases/<version>/`, and starts `crop-advisory.service` on port 8084. It persists H2 data in `/var/lib/crop-advisory/data`. The UFW rule is added only when UFW is installed and active. Once deployed, verify on the target with `systemctl status crop-advisory` and `curl http://localhost:8084/actuator/health`. See [docs/CONFIG_SPEC.md](docs/CONFIG_SPEC.md) for the managed configuration inventory.

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
