# Crop Advisory Portal configuration specification

The Ansible playbook manages an Ubuntu 22.04 or 24.04 host with systemd. The host may be a second Ubuntu WSL2 distro or a Linux VM reachable over SSH. The Ansible control node runs in Ubuntu WSL2.

| Category | Resource | Configuration |
|---|---|---|
| Package | `openjdk-17-jre-headless` | Installed from apt; runs the executable WAR. |
| Package | `curl` | Installed from apt; supports target-side health checks. |
| User and group | `cropapp` | System account and group; user has no home directory and uses `/usr/sbin/nologin`. |
| User | `ansible` (host-specific) | SSH management account configured outside this playbook; must have privilege escalation rights on the target. The disposable local WSL target uses an SSH public key and passwordless sudo for this account only. |
| Folder | `/opt/crop-advisory` | Root-owned application root. |
| Folder | `/opt/crop-advisory/releases` | `cropapp:cropapp`, mode `0755`; contains versioned releases. |
| Folder | `/opt/crop-advisory/releases/<app_version>` | `cropapp:cropapp`, mode `0755`; contains `app.war`. |
| Symlink | `/opt/crop-advisory/current` | Root-owned link to the selected `cropapp:cropapp` versioned release directory; the service runs `current/app.war`. |
| Folder | `/var/lib/crop-advisory/data` | `cropapp:cropapp`, mode `0750`; persistent H2 database directory. |
| Folder | `/etc/crop-advisory` | `root:cropapp`, mode `0750`; application configuration. |
| File | `/etc/crop-advisory/crop-advisory.env` | `root:cropapp`, mode `0640`; defines `APP_PORT=8084`, `APP_ENV=production`, and `DB_PATH=/var/lib/crop-advisory/data/cropdb`. |
| File | `/etc/systemd/system/crop-advisory.service` | Root-owned systemd unit launching `/usr/bin/java -jar /opt/crop-advisory/current/app.war` as `cropapp`. |
| Port | TCP `8084` | Application HTTP port; allowed through UFW only if UFW is installed and active. SSH port 22 is used by Ansible to manage the target. |
| Service | `crop-advisory.service` | Enabled at boot and started by Ansible; restarts on failure. |
| Service | `ssh` | Must be enabled and reachable on the target for remote Ansible management (TCP 22). |
| Service | `systemd` | Required on the target; for WSL2, enable `systemd=true` in `/etc/wsl.conf` and restart WSL. |
