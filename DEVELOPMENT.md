# Development

Notes for contributors and maintainers of the **WSL Macros** plugin.

## Requirements

- A JDK for running Gradle — CI uses **JDK 21** (`actions/setup-java`, Zulu).
- On Windows, run the wrapper from Git Bash (`./gradlew`) or use `gradlew.bat` in
  cmd/PowerShell.

## Common commands

| Task | Command |
|---|---|
| Build the plugin ZIP | `./gradlew buildPlugin` → `build/distributions/` |
| Run unit tests | `./gradlew test` |
| Full check (tests + more) | `./gradlew check` |
| Verify IDE compatibility | `./gradlew verifyPlugin` |
| Run a dev IDE with the plugin | `./gradlew runIde` |

Predefined IntelliJ run/debug configurations are in `.run/`.

## Project layout

```
├── .github/workflows/   CI: build.yml (push/PR), release.yml (published releases)
├── .run/                Predefined IntelliJ run/debug configurations
├── scripts/
│   └── generate-certs.sh   Self-signed signing-certificate generator
├── src/
│   ├── main/
│   │   ├── kotlin/ro/pieceofcode/jetbrains/plugin/wslmacros/
│   │   │   ├── WslMacro.kt       Macro base classes + WSL→UNC path conversion
│   │   │   └── WslPathMacros.kt  The five macro implementations
│   │   └── resources/META-INF/plugin.xml   Manifest + <macro> registrations
│   └── test/kotlin/.../WslPathMacroTest.kt  Unit tests for the path conversion
```

## Compatibility

The plugin targets `sinceBuild = 261` (IntelliJ **2026.1+**); `untilBuild` is
intentionally unset so it stays open-ended for 2026.*+ releases. It compiles
against IDEA 2025.3.5 — all used platform APIs are stable well before 2026.1.

## Publishing

Releases are driven by GitHub Actions (manual upload to
[plugins.jetbrains.com/plugin/upload](https://plugins.jetbrains.com/plugin/upload)
is also possible).

### Signing certificate

Releasing from CI signs the plugin with a self-signed certificate.

1. Generate the certificate locally:

   ```bash
   bash scripts/generate-certs.sh
   ```

   This writes `build/certificates/` — `private.pem`, `chain.crt`, plus
   `PRIVATE_KEY.b64` and `CERTIFICATE_CHAIN.b64`. Default validity is **5 years**;
   override the day count and subject with arguments:
   `bash scripts/generate-certs.sh 3650 "/CN=Your Name"`. See
   [plugin signing docs](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html).

   > `build/` is wiped by `./gradlew clean` — the durable copy of the signing
   > material is the GitHub secrets, not these files.

2. Store the values as **organization-level** GitHub secrets (inherited by this
   repository — make sure no repo-level copy shadows them):

   | Secret | Value |
   |---|---|
   | `JETBRAINS_PLUGINS_PUBLISH_TOKEN` | Marketplace token ([generate](https://plugins.jetbrains.com/author/me/tokens)) |
   | `JETBRAINS_PLUGINS_CERTIFICATE_CHAIN` | contents of `build/certificates/CERTIFICATE_CHAIN.b64` |
   | `JETBRAINS_PLUGINS_PRIVATE_KEY` | contents of `build/certificates/PRIVATE_KEY.b64` |
   | `JETBRAINS_PLUGINS_PRIVATE_KEY_PASSWORD` | the passphrase chosen during generation |

   The workflow maps these secrets into the env var names IPGP's `signPlugin` /
   `publishPlugin` read (`CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`,
   `PUBLISH_TOKEN`).

### Release flow

1. Push to `main` → `.github/workflows/build.yml` builds, tests, verifies, and
   creates a **draft GitHub release** for the version in `gradle.properties`.
2. Review the draft and **Publish release** → `.github/workflows/release.yml` signs
   and uploads the plugin to JetBrains Marketplace, attaches the ZIP to the release,
   and opens a PR that patches `CHANGELOG.md` for the released version.

Bump the version in `gradle.properties` and add a `CHANGELOG.md` entry before
cutting a release.

## Git notes for Windows contributors

Committing from Windows does not record the executable bit, so shell scripts
(`gradlew`, `scripts/generate-certs.sh`) would otherwise land on Linux runners as
non-executable. Mark them in the index when adding or touching them:

```bash
git update-index --chmod=+x gradlew scripts/generate-certs.sh
```
