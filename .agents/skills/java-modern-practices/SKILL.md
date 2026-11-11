---
name: java-modern-practices
description: Implement or review Java 25 code in myvanitys-api for maintainability, type safety, immutability, modern language usage, concurrency risks, caching risks, and JPA performance hazards. Use for substantive Java changes or focused Java quality reviews; treat the bundled Java 21 rules as a minimum baseline and do not penalize the mere absence of newer features.
---

# Java Modern Practices

Apply modern Java practices only where they improve clarity, correctness, maintainability, type safety, or runtime behavior.

## Workflow

1. Confirm the Java version and relevant dependencies in `pom.xml` before recommending a language or framework feature. This repository currently targets Java 25; the source guideline's Java 21 version labels are minimum-version baselines.
2. Read [the complete source guideline](references/source-guideline.md) before performing a broad review, or read the sections relevant to a focused implementation task.
3. Inspect each candidate in its architectural and framework context. Treat search or static-pattern matches as leads, not findings.
4. Prefer domain clarity and repository conventions over novelty. Do not report the absence of records, sealed types, virtual threads, pattern matching, sequenced collections, or text blocks by itself.
5. For reviews, cite concrete files and methods, state applicability and confidence, and distinguish correctness/runtime risks from optional modernization.

## Repository adaptation

The bundled guideline originated in an external evaluation framework. Its scoring rules, MCP/Playbook gates, scanner JSON, `execution-config.yml`, and prescribed report sections are conditional provenance, not required infrastructure for this repository. Do not create missing evaluator files or claim external-standard compliance unless the user explicitly requests that evaluation and the cited sources are available. For ordinary coding work, apply the relevant engineering guidance without producing the formal evaluation tables.

