---
name: mapping-between-boundaries
description: Implement or review mappings among web DTOs, application commands and results, domain models, and persistence entities in myvanitys-api. Use when changing controllers, use cases, mappers, DTOs, domain objects, entities, or adapters, and when checking mapper responsibility or boundary leakage.
---

# Mapping Between Boundaries

Keep mapping choices intentional: less mapping increases coupling, while more mapping increases complexity.

## Workflow

1. Read [the complete source guideline](references/source-guideline.md) before a boundary review, or read the relevant boundary and mapper-smell sections for a focused change.
2. Identify the operation as a command or query and trace its models across web, application, domain, and persistence boundaries.
3. Inspect the actual mapper and its callers before classifying a concern. Do not infer a defect from mapper volume or from a search match alone.
4. Keep mappers focused on transformation. Move business decisions, validation, collaborator calls, persistence, publication, and other side effects to the owning layer.
5. Preserve pragmatic mappings when they are explicit and safe; do not prescribe full mapping everywhere.

## Repository adaptation

The bundled guideline originated in an external evaluator. Treat scoring, MCP/Playbook promotion, scanner outputs, `execution-config.yml`, and formal report templates as conditional context. They do not authorize creating missing evaluator infrastructure or declaring a Playbook violation. In ordinary implementation and review work, use the boundary trade-offs and mapper checks as engineering guidance and report concrete repository evidence.

