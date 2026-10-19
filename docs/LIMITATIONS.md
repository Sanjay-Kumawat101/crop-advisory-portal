# Release Limitations

This release is a development and demonstration application. It is not a production-ready agricultural advisory service without additional operational and security work.

- **Persistence:** H2 file storage is used for local, Docker, and Ansible deployments. It is not a managed, highly available database; backups, restore drills, schema migration governance, and concurrent production load have not been established.
- **Credentials:** the repository documents seeded demo accounts with known passwords. They are for local demonstration only. Replace them with managed identities and strong secret provisioning before any shared or public deployment.
- **Transport security:** the documented local deployment endpoints use HTTP. TLS termination, certificate lifecycle, secure cookies behind a proxy, and external ingress hardening are not configured.
- **Pipeline scope:** Jenkins builds, executes unit tests and Selenium UI tests, and deploys the Docker image to a local registry/container. The Ansible deployment is a separate post-pipeline invocation; the Windows Jenkinsfile does not currently orchestrate WSL Ansible.
- **Deployment targets:** Docker and Ansible examples are single-host demonstrations. There is no multi-node scheduler, rolling/blue-green traffic management, or production-grade disaster recovery.
- **Monitoring and alerting:** `/actuator/health` is exposed for readiness checks. Centralized metrics, log aggregation, alerting, SLOs, and on-call procedures are not configured.
- **Test environment:** Selenium tests require Chrome, credentials configured in Jenkins/local environment, and a reachable application. UI tests are excluded from plain `mvn clean package` so that the standard package build does not require a browser.
- **Network assumptions:** the pipeline uses a local unauthenticated Docker registry for the lab. This configuration is not appropriate for an untrusted network; use a secured private registry for shared environments.
- **Business scope:** workflow, roles, dashboard, and seed data are intentionally small. The portal does not integrate with external weather, soil, identity, or notification services.

Use [docs/TROUBLESHOOTING.md](TROUBLESHOOTING.md) for environment-specific failure diagnosis and [docs/FUTURE_ENHANCEMENTS.md](FUTURE_ENHANCEMENTS.md) for candidate work.
