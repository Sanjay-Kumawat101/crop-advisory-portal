# Branch Policy

| Branch | Purpose | Rules |
|---|---|---|
| `main` | Stable, release-ready code | No direct commits. Only merged from `develop` or `release/*` by pull request. Tagged for releases (v1.0.0). |
| `develop` | Integration branch | Receives merged feature branches by pull request. Jenkins builds this branch. |
| `feature/<issue-no>-<short-name>` | New features | Branch from `develop`. Example: `feature/3-create-record`. |
| `bugfix/<issue-no>-<short-name>` | Defect fixes | Branch from `develop`. Example: `bugfix/15-status-validation`. |
| `hotfix/<short-name>` | Urgent fix on a release | Branch from `main`, merge into both `main` and `develop`. |
| `release/<version>` | Release preparation | Optional. Example: `release/1.0.0`. |

## Rules
- Use lowercase letters and hyphens only in branch names.
- One branch per issue. Keep branches short-lived.
- Every merge into `develop` or `main` goes through a pull request with at least a self-review and a passing build.
- Delete a feature branch after it is merged.
- Commit messages use a prefix: `feat:`, `fix:`, `docs:`, `test:`, `ci:`, `chore:`, followed by a short present-tense summary (for example `feat: add record creation form`).
- Enable "Require a pull request before merging" on `main` if GitHub offers branch protection for the repository.
