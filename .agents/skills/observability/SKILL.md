---
name: observability
description: Implement or review observability and operational robustness in myvanitys-api. Use for logging, correlation and tracing, health and metrics, outbound HTTP client resilience, and ControllerAdvice exception handling, especially when changes affect external calls or error responses.
---

# Observability

Make runtime behavior diagnosable without leaking sensitive implementation details or assuming resilience supplied by code that has not been inspected.

## Workflow

1. Read [the complete source guideline](references/source-guideline.md) before an observability review, or read the relevant resilience or exception-handling section for a focused change.
2. Inspect code and configuration together, including shared infrastructure or platform guarantees when evidence is available.
3. For outbound clients, verify the effective retry, circuit-breaker, bulkhead, rate-limit, timeout, and fallback strategy before identifying a gap.
4. For global exception handlers, distinguish application-authored specific exceptions from catch-all failures. Never expose raw catch-all messages, localized messages, stack traces, SQL details, class names, or paths in response bodies.
5. Keep readability observations, such as handler source ordering, separate from functional or security findings.

## Repository adaptation

The source document references an external scanner and scoring framework. Those files and score caps are not requirements of `myvanitys-api`. Do not create them, infer platform behavior, or claim Playbook compliance unless the user requests a formal evaluation and the standards are accessible. For ordinary work, apply the concrete observability and robustness rules with repository evidence.

