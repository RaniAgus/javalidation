# Release Process

## Maven `release` Profile

GPG signing and Maven Central publishing are gated behind the `release` profile in the root `pom.xml`. This keeps `mvn verify` clean for local development — no GPG key required.

| Command | What it does |
|---|---|
| `mvn verify` | Compile, test, package, attach sources and javadoc. No signing. |
| `mvn verify -P release -Dgpg.passphrase=<pass>` | Everything above plus GPG-sign all artifacts and publish to Maven Central. |
| `mvn --batch-mode deploy -P release -Dgpg.passphrase=<pass>` | Same as above but non-interactive (used by CI). |

Plugins activated by the `release` profile:
- `maven-gpg-plugin` — signs all artifacts at the `verify` phase
- `central-publishing-maven-plugin` (Sonatype) — publishes to Maven Central at the `deploy` phase

## GitHub Actions Workflow

File: `.github/workflows/publish.yml`

### Triggers

| Trigger | Behaviour |
|---|---|
| Push to `main` (touching `pom.xml`, `**/src/**`, or the workflow file) | Deploys the current `-SNAPSHOT` version |
| Tag matching `v*.*.*` | Sets the release version, deploys, then prepares the next snapshot (see below) |
| `workflow_dispatch` | Manual ad-hoc deploy from the Actions UI |

### Steps (in order)

1. **Import GPG key** — uses `crazy-max/ghaction-import-gpg` with `GPG_PRIVATE_KEY` and `GPG_PASSPHRASE` secrets.
2. **Set version** *(tag trigger only)* — strips the `v` prefix from the tag and runs `mvn versions:set` to set the release version across all modules.
3. **Set up Java + Maven Central credentials** — `actions/setup-java@v5` with `server-id: central`; credentials come from `OSSRH_USERNAME` / `OSSRH_TOKEN` secrets.
4. **Publish** — `mvn --batch-mode deploy -P release -Dgpg.passphrase=...`
5. **Prepare next iteration** *(tag trigger only)* — updates the version in `README.md`, bumps all modules to `<tag-version>-SNAPSHOT`, commits, and pushes to `main`.

### Required GitHub Secrets

| Secret | Description |
|---|---|
| `GPG_PRIVATE_KEY` | Armored GPG private key used to sign artifacts |
| `GPG_PASSPHRASE` | Passphrase for the GPG key |
| `OSSRH_USERNAME` | Sonatype Central username (user token) |
| `OSSRH_TOKEN` | Sonatype Central password (user token) |
