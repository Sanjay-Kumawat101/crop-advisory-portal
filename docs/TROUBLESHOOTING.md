# Troubleshooting

## Windows Jenkins

- The Jenkins service account, not the interactive desktop user, runs pipeline commands. Confirm the service identity under **Services > Jenkins > Properties > Log On**.
- Use Jenkins `bat` steps on Windows; the pipeline is not a POSIX shell. Confirm `git`, `mvn`, `java`, `docker`, and Chrome resolve for the service account.
- Verify Java and Maven with Jenkins-managed tools or `java -version` and `mvn -version` in a build step. The compiler target is Java 17; Maven 3.9 is the documented build baseline.
- If Chrome cannot start headlessly, install Chrome for all users or the service account and check its profile permissions. The Selenium UI tests use Selenium Manager for the matching driver; allow driver resolution/network access or pre-provision the driver.
- Add the Jenkins service identity to Docker Desktop's `docker-users` group, then restart the Jenkins service. Docker Desktop must be running in Linux-container mode. Do not expose or print credential values in console logs.
- UI credentials must exist in Jenkins with IDs `ui-test-farmer`, `ui-test-expert`, and `ui-test-admin`. The pipeline injects them as environment variables; it does not store passwords in source.
- UI test app data uses a build-workspace path and port 8085. Check for another process already using 8085 and stale Maven processes if startup fails.
- The local registry must be running on port 5000. Check `docker ps --filter name=crop-registry` and `curl.exe http://localhost:5000/v2/_catalog`.
- Poll SCM can replace GitHub webhooks for a local-only Jenkins instance. Check the job branch specifier points at the intended release branch and inspect the **Checkout** stage commit before diagnosing a stale build.

## Tomcat 10.1

- Confirm Tomcat is running on 8081 and that `target/crop-advisory.war` is copied as `webapps/crop-advisory.war`. The context path is `/crop-advisory`, not `/`.
- Check `%CATALINA_BASE%\logs\catalina*.log` and the application log for Java version, deployment, or database path errors.
- Verify Java 17 is available to the Tomcat service. The WAR packages embedded Tomcat for executable use while marking Tomcat provided for an external container.
- For the Task 8 Tomcat Manager deploy script, confirm Manager text access is limited to loopback and that the Jenkins credential ID is configured. Never remove the loopback restriction to work around a remote 403.
- Probe `http://localhost:8081/crop-advisory/actuator/health`. A 404 commonly indicates an incorrect context path or failed WAR deployment.

## Docker Desktop

- Run `docker info` (not only `docker --version`) to verify that the Docker daemon is responding. Ensure Docker Desktop uses Linux containers.
- The registry pipeline requires the local registry on 5000. A port conflict or a stopped registry causes the Push stage to fail before deployment.
- Port 8080 belongs to Jenkins on this Windows host. Publish the app as `-p 8082:8080`; do not bind the application to host port 8080.
- Check `docker ps -a`, `docker logs crop-advisory`, `docker inspect crop-advisory`, and `docker volume inspect crop-data` for container, health, and persistence issues.
- The Docker image health check probes the container's port 8080. The external health probe is `http://localhost:8082/actuator/health`.
- If Docker says a name is already in use, confirm the existing container before removing it. The Jenkins pipeline replaces the named container after the test gates pass; the H2 database volume is preserved.

## WSL2 Ansible

- Run Ansible from Ubuntu WSL2, not Windows PowerShell. Keep the project and Ansible control files in the Linux filesystem when practical; `/mnt/c` world-writable mount options may cause Ansible to ignore `ansible.cfg`.
- Confirm systemd is enabled in `/etc/wsl.conf`, restart WSL after changing it, and verify `systemctl is-system-running`.
- WSL target IPs can change after a shutdown. Refresh `ansible/inventory/hosts.ini` from `hostname -I` on the target, then run `ansible all -m ping`.
- Verify SSH service/key authentication, target SSH user, and privilege escalation before running the playbook. Do not commit SSH keys or use lab-only passwordless sudo on a production machine.
- `war_path` is read on the Ansible control node. Translate a Windows workspace path to its WSL mount path, such as `/mnt/c/.../target/crop-advisory.war`.
- If deployment fails its health gate, inspect `systemctl status crop-advisory`, `journalctl -u crop-advisory`, and `readlink -f /opt/crop-advisory/current`. The playbook requires HTTP 200 with JSON status `UP`; it intentionally does not follow redirects. A failed candidate should restore the previous release.
- Use a unique `app_version` for changed WAR bytes; active version directories are immutable. The playbook retains the active release and up to two previous releases.
