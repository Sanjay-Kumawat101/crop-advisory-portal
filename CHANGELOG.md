# Changelog

## [1.0.0] - 2026-10-18

First complete release candidate for the Crop Advisory Portal and its demonstration delivery workflow.

### Added
- Farmer query creation, viewing, and search with the Submitted, Under Review, Advised, and Closed lifecycle.
- Role-based FARMER, EXPERT, and ADMIN access, including an ADMIN-only summary dashboard.
- Selenium/JUnit 5 browser journeys and failure screenshots, run through Maven Failsafe only with the `ui-tests` profile.
- Windows Jenkins pipeline with unit-test and Selenium gates, commit-tagged Docker image build/push, and Docker container replacement.
- Docker image and persistent named-volume deployment guidance for port 8082.
- Ubuntu WSL2 Ansible provisioning on port 8084 with immutable versioned WAR releases, health verification, automatic rollback, manual rollback, and retention.
- Architecture, operations troubleshooting, limitations, future enhancements, and demo/viva documentation.

### Operational notes
- Demo identities/passwords are sample-only and must not be used in a public deployment.
- The Jenkins pipeline's local registry and Docker deployment are demonstrated separately from the Ansible post-pipeline deployment.
- This changelog records the intended release contents; deployment evidence and final publication state are tracked by the release commit, tag, and GitHub Release.
