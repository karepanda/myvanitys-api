---
name: myvanitys-change-verification
description: Select and run the smallest sufficient verification stack for myvanitys-api changes, then report evidence. Use after changing Java behavior, API-spec consumption, persistence, migrations, authentication, configuration, or architectural boundaries. Skip documentation-only edits.
---

# MyVanitys Change Verification

Verify completed backend work with the smallest sufficient checks first, then broaden verification in proportion to risk.

## Workflow

1. Read `.agents/AGENTS.md`, the working-tree diff, and the tests/configuration associated with the changed behavior.
2. Separate in-scope changes from unrelated user changes. Never rewrite, revert, or format unrelated files.
3. Read [references/verification-matrix.md](references/verification-matrix.md) and classify the change by affected layers.
4. Run the smallest focused check that can fail meaningfully.
5. Diagnose failures and distinguish product defects, test defects, environment failures, missing credentials, and unavailable infrastructure.
6. Expand to the required unit, architecture, integration, coverage, or mutation checks.
7. Stop after repeated failures when further progress needs user input or unavailable infrastructure; do not claim an unrun check passed.

## Boundaries

- This skill selects and runs checks; it does not design unit tests. Use the unit-testing workflow when tests must be created or substantially rewritten.
- Documentation-only changes do not require Maven unless they make executable or generated-behavior claims that need confirmation.
- Unit tests are `*Test` and run under Surefire. Integration tests are `*IT` and run under Failsafe.
- Run `ArchitectureTest` when packages, ports, adapters, generated API dependencies, or provider/storage SDKs change.
- Run full `clean verify` when a change crosses layers, affects persistence, changes security, or consumes a new API artifact.
- Run PIT deliberately for important domain/application behavior or when the task explicitly requires mutation confidence.
- Do not publish, deploy, start external services, or change credentials merely to complete verification without authorization.

## Result format

Report:

```text
Behavior verified:
Focused checks:
Broader checks:
Result:
Skipped checks and reason:
Remaining risk:
```

Include exact commands and distinguish passing, failing, and not-run checks.
