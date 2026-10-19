# Future Enhancements

Potential follow-up work, ordered by operational value and scope:

1. **PostgreSQL:** migrate from H2 to PostgreSQL for managed backups, stronger production concurrency, and a path to managed database hosting. Add schema migrations and test against PostgreSQL in CI.
2. **HTTPS:** terminate TLS at a managed reverse proxy or ingress, configure secure cookie/proxy behavior, and automate certificate renewal.
3. **Kubernetes:** package the application as a container deployment with readiness/liveness probes, secret management, persistent storage, and environment-specific manifests. Validate operational complexity before replacing the simpler Docker/Ansible deployments.
4. **SonarQube:** add static analysis and quality gates to Jenkins, define an initial baseline, and gate only on agreed new-code conditions.
5. **Notifications:** notify farmers and experts on workflow transitions through an email/SMS provider, with delivery status, retries, consent, and secret management.
6. **Monitoring:** export application metrics, centralize structured logs, configure dashboards and actionable alerts, and define service-level objectives.

Each enhancement should include threat modeling, data migration and rollback strategy where applicable, automated tests, and updated operational documentation.
