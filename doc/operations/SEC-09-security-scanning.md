# SEC-09 Backend security scanning

## Goal

Same-repository pull requests against `main` and a weekly scheduled run gate three kinds of backend findings:

- **Secrets** — new hardcoded credentials, tokens, and API keys.
- **Dependencies** — high and critical vulnerabilities in third-party libraries.
- **Images** — high and critical vulnerabilities in the published container image.

Any finding that is not covered by a recorded exception fails the pipeline. Approved and time-limited exceptions are recorded **privately** (outside this public repository) and are applied by the scanners at run time.

## Scope

The gate applies to pull requests opened from branches in this repository and to the weekly schedule. It does **not** fully gate fork pull requests: the dependency and image scans must build the private `com.myvanitys:myvanitys-api-spec` artifact, which requires `PACKAGES_TOKEN`, and GitHub withholds repository secrets from fork workflows. Fork PRs run only the secret scan; the build-dependent scans fail closed. See [Fork contributions](#fork-contributions).

## Scan matrix

| Scan | Tool | Trigger | Gate |
| --- | --- | --- | --- |
| Secret | Gitleaks 8.30.1 | same-repo PR (new commits only) and weekly schedule (full history) | exit code 1 on any unignored finding |
| Dependency | Trivy (`fs`, `scanners: vuln`) | same-repo PR and weekly schedule | exit code 1 on `CRITICAL,HIGH` library findings |
| Image | Trivy (`image`) | same-repo PR and weekly schedule | exit code 1 on `CRITICAL,HIGH` OS and library findings |

The workflow lives in `.github/workflows/security-scan.yml`. The weekly schedule is `0 6 * * 1` (Monday 06:00 UTC); `workflow_dispatch` is available for manual runs.

Secret scanning on a PR scans only the commit range `base.sha..head.sha`, so an existing finding merged before this gate is not re-blocked. The scheduled run scans the full git history, so a secret leaked outside the PR range is still caught on cadence.

## Gating behavior

- **Secret scan** downloads a pinned Gitleaks release and verifies its SHA-256 before running. Findings are redacted in logs (`--redact`) and the step fails (`--exit-code 1`) when a leak is present.
- **Dependency scan** builds the application (`mvn package -DskipTests`) so Trivy resolves the real dependency tree, then scans the repository with `--scanners vuln --severity CRITICAL,HIGH`.
- **Image scan** builds `DockerfileProduction` into a local image (not pushed) and scans it with `--severity CRITICAL,HIGH`, mirroring the release-time scan in `cd.yml`.

Both Trivy gates report **all** high and critical findings, including those without a published fix; `--ignore-unfixed` is intentionally not set, so an unfixed critical is never silently waived.

Each Trivy invocation passes `trivy-config` pointing at a trusted, intentionally empty `trivy.yaml` generated in the runner's temporary directory. Trivy otherwise auto-loads a `trivy.yaml` from the checked-out code, which would let a pull request add skip rules or other filtering to weaken the gate. The generated config keeps scan behavior driven by workflow inputs only, never by files under review.

The Trivy ignore list is written to a file in the runner's temporary directory and referenced through the `TRIVY_IGNOREFILE` environment variable. It is **not** passed through the action's `trivyignores` input, which echoes the ignore file into the public workflow log; the exception registry therefore never appears in build output.

## Private exception registry

Because this repository is public, exception lists are **not committed here**. They are stored as GitHub repository **secrets**, which are encrypted at rest, masked in logs, and unavailable to the public and to forks:

| Secret | Content | Consumed by |
| --- | --- | --- |
| `SECURITY_GITLEAKS_ALLOWLIST` | Gitleaks allowlist TOML (one or more `[[allowlists]]` tables) | secret scan |
| `SECURITY_GITLEAKS_IGNORE` | Gitleaks finding fingerprints, with approval comments, in `.gitleaksignore` format | secret scan |
| `SECURITY_TRIVY_IGNORE` | `.trivyignore` lines (one finding per line) | dependency and image scans |

The workflow materializes these secrets into temporary files outside the checkout just before scanning. It removes any checkout-controlled `.gitleaksignore` first because Gitleaks auto-loads that file even when an explicit private ignore path is supplied; only `SECURITY_GITLEAKS_IGNORE` can therefore suppress a finding. When a secret is unset, the corresponding scan runs with no exceptions and every matching finding fails the gate.

### Time-limited exceptions

Trivy exceptions support a native expiry. Add an `exp:YYYY-MM-DD` column to make an exception time-limited:

```text
# Approved by @karepanda 2026-11-24. Fixed upstream in next patch; remove after upgrade.
CVE-2026-12345  exp:2026-12-31
```

Trivy ignores that finding until the expiry date, then the finding re-enters the gate automatically. An exception without `exp:` is approved indefinitely.

Gitleaks does not support per-entry expiry. Secret findings are expected to be either false positives (record a permanent allowlist entry) or real leaks (rotate the secret and remediate the exact historical finding by fingerprint, below). Record the approver and rationale in a `description` field so each false-positive entry is auditable:

```toml
[[allowlists]]
description = "Approved by @karepanda 2026-11-24: sample token in unit-test fixture"
paths = ['''src/test/resources/sample-token\.txt''']
```

The workflow always prepends `[extend]\nuseDefault = true` to the allowlist secret, so an allowlist entry never disables the default Gitleaks rules.

## Approval process

1. A scan fails with a finding. Confirm whether it is a real secret or a false positive.
2. If it is a **real secret**, rotate it immediately and remove the historical finding so the scheduled full-history scan can pass. Rotation alone does not remove the finding from git history. Use one of:
   - **History rewrite (preferred):** purge the secret from git history (for example with `git filter-repo` or BFG) and force-push the rewritten history. After rotation plus rewrite, no finding remains and no exception is needed.
   - **Finding-scoped exception (when rewrite is not feasible):** copy the exact `Fingerprint` from Gitleaks' redacted finding output and add it to `SECURITY_GITLEAKS_IGNORE`, with the rotation date and approver in a preceding comment:

     ```text
     # Rotated on 2026-11-24 by @karepanda; secret revoked and replaced.
     <commit-sha>:<path>:<rule-id>:<line>
     ```

     Gitleaks matches this value to one finding only. Other secrets in the same commit remain gated. Revalidate stored fingerprints whenever the pinned Gitleaks version is upgraded because `.gitleaksignore` fingerprint support is experimental upstream.
3. If it is a **false positive**, update the matching repository secret with the allowlist/ignore entry, including the approver, date, and (for Trivy) an `exp:` date when the exception should be revisited.
4. Re-run the workflow. The finding is suppressed and the gate passes.
5. On the scheduled run, review any Trivy exceptions whose `exp:` date has passed (they will fail again) and either extend them with a new `exp:` date or remove them once remediated.

## Fork contributions

Fork pull requests run only the secret scan. The dependency and image scans fail closed because they cannot build `myvanitys-api-spec` without `PACKAGES_TOKEN`, which is never exposed to fork code. `PACKAGES_TOKEN` must be a least-privilege, read-only package credential. To run a fork contribution through all three gates, a maintainer:

1. Reviews the fork diff **before promotion**, treating it as untrusted code. Pay particular attention to `.github/workflows`, `pom.xml` build plugins and annotation processors, `.mvn`, Dockerfiles, build scripts, and dependency changes because they can execute while package credentials are present.
2. Promotes the commits only after that security review, by pushing them to a branch in this repository or cherry-picking them.
3. Opens a pull request from that same-repository branch; all three scans run with the read-only package credential available.
4. Merges the validated branch.

## Verification

The workflow itself is the verification for the scanning behavior. To validate a change to the workflow or to the exception format without opening a PR, trigger a run with `workflow_dispatch` and inspect each job.

## Remaining gaps

- Trivy's Maven dependency resolution runs against Maven Central; the private `com.myvanitys:myvanitys-api-spec` artifact is not in the public vulnerability feeds, so its direct coordinate is not scanned by the dependency step. Its transitive libraries are still covered because they are also dependencies of the Spring Boot tree and the packaged image.
- Fork PRs are not fully gated in place; they are fail-closed and require a maintainer to promote the change to a same-repository branch (see [Fork contributions](#fork-contributions)).
- Exception secrets are unavailable to fork PRs, so a fork PR is always scanned with no exceptions applied.
