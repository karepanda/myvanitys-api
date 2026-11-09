---
name: domain-port-ownership
description: Review output-port ownership in hexagonal myvanitys-api code when application services resolve a domain concept and pass it into a domain factory or constructor. Use for application/domain port changes and dependency-inversion reviews; exclude persistence writes, operational configuration, and projects without a domain layer.
---

# Domain Port Ownership

Use domain-owned ports only when the dependency represents a domain concept the domain must resolve; keep persistence lifecycle and operational configuration in the application or infrastructure flow that owns them.

## Workflow

1. Confirm that `domain`, `application`, and `infrastructure` layers exist and that the case uses Clean Architecture-style hexagonal layering.
2. Read [the complete source guideline](references/source-guideline.md) before classifying a port-ownership concern.
3. Trace the application service, injected output port, returned value, and domain factory or constructor receiving that value.
4. Inspect each candidate before classifying it. Exclude write or saver ports called after domain mutation and operational configuration such as feature flags or tunable properties.
5. Avoid double-reporting cases whose real issue is business decision logic in the application layer; cite concrete class, method, port, and domain-construction evidence.

## Repository adaptation

The source guideline originated in an external evaluator and assumes standards, scanners, score mappings, and report artifacts that may not exist here. Treat those as conditional provenance. Do not create evaluator infrastructure, claim MCP/Playbook backing, or score findings unless the user explicitly requests a formal evaluation and the exact standard is available. If the architectural preconditions do not hold, mark the skill not applicable instead of forcing the pattern.

