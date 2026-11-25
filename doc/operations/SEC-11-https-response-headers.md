# SEC-11 HTTPS and API response headers

> **Release status:** application implementation is complete; production verification is pending.
> Keep SEC-11 open until the deployed black-box and edge-sanitization checks below have passed and
> their observed results have been recorded in this document.

## Goal

Every API response carries a consistent set of security headers, HSTS is emitted only on genuinely secure connections, and HTTPS enforcement is handled at the edge without introducing an open-redirect surface in the application.

## What the application does

`common/config/SecurityHeadersFilter` (registered right after the highest-precedence filters) sets, before the rest of the chain runs, so they are present on success **and** error responses — including the JWT filter's 401 short-circuit and `GlobalExceptionHandler` bodies:

| Header | Value |
| --- | --- |
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `DENY` |
| `Referrer-Policy` | `no-referrer` |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` — only when the request is secure |

HSTS is configurable via `security.headers.*` (`hsts`, `hsts-max-age-seconds`, `hsts-include-sub-domains`, `hsts-preload`) and defaults on, but is only ever emitted on secure requests.

## HTTPS redirection — accepted edge behavior

The application deliberately does **not** implement its own HTTP-to-HTTPS redirect. The managed public edge (currently Cloudflare in front of Railway for `api.myvanitys.com`) terminates public TLS and answers plain port-80 traffic with a redirect. Railway forwards HTTPS requests to the container with `X-Forwarded-Proto: https`. An application-level redirect would therefore be unreachable for public production traffic and would only add a request-derived `Location` target that a forged host header could turn into an open redirect.

Consequence to accept: the public edge redirect is a `301` (permanent), which browsers may downgrade from `POST` to `GET`. This platform-level behavior is outside the application's control. If method-preserving (`307`/`308`) redirects are required, they must be configured at the edge (Cloudflare/Railway), not in this service.

## Trusted proxy handling

Because the service sits behind Railway and needs to know whether the client is on HTTPS, `SecurityHeadersFilter` treats a request as secure when:

- `request.isSecure()` is `true` (direct TLS), or
- the single `X-Forwarded-Proto` header equals `https` (case-insensitive).

This selects exactly the one forwarded header the decision needs and **ignores** everything else — `X-Forwarded-Host`, `X-Forwarded-Port`, `X-Forwarded-Prefix`, and the RFC 7239 `Forwarded` header. Forwarded host, port, and prefix values therefore cannot select a response host or create a redirect. The forwarded scheme intentionally affects only whether HSTS is emitted. `server.forward-headers-strategy` is intentionally left unset (the default `none`), so Spring does not install a `ForwardedHeaderFilter` that would trust the wider forwarded-header family.

**Prerequisite on the platform:** Railway's edge must overwrite a client-supplied `X-Forwarded-Proto` with its own value. Railway documents that the header always indicates `https`, but the repository cannot prove how duplicate or client-supplied values are normalized. The deployed edge-sanitization check below verifies this assumption.

## Verification

### In-repository (automated)

`SecurityHeadersFilterTest` covers the static headers on success and error, HSTS on direct TLS, HSTS on trusted `X-Forwarded-Proto: https`, suppression on `http`, ignoring the `Forwarded` header, the `hsts=false` and `preload` toggles, and error responses. Run:

```bash
mvn -Dtest=SecurityHeadersFilterTest test
mvn test
```

### Deployed black-box test (requires Railway access)

Run against the deployed public URL (substitute the real domain):

```bash
# 1. Plain HTTP is redirected to HTTPS by the edge (expect 3xx with Location: https://...).
curl -sS -o /dev/null -D - http://<railway-domain>/myvanitys/actuator/health

# 2. HTTPS serves HSTS + the static security headers on a success response.
curl -sS -o /dev/null -D - https://<railway-domain>/myvanitys/actuator/health

# 3. The same headers appear on an error response (401 for an unauthenticated resource).
curl -sS -o /dev/null -D - https://<railway-domain>/myvanitys/api/v1/products
```

Expected:

- Step 1 returns `301`/`3xx` with a `Location` starting `https://`.
- Steps 2 and 3 include `strict-transport-security: max-age=31536000; includeSubDomains`, `x-content-type-options: nosniff`, `x-frame-Options: DENY`, and `referrer-policy: no-referrer`.

### Production verification record

| Check | Date | Target | Observed result | Status |
| --- | --- | --- | --- | --- |
| HTTP-to-HTTPS redirect | 2026-11-25 | `http://api.myvanitys.com/myvanitys/actuator/health` | `301 Moved Permanently`; `Location: https://api.myvanitys.com/myvanitys/actuator/health` | Pass |
| Headers on HTTPS success | — | `https://api.myvanitys.com/myvanitys/actuator/health` | Awaiting deployment of this revision | Pending |
| Headers on HTTPS error | — | `https://api.myvanitys.com/myvanitys/api/v1/products` | Awaiting deployment of this revision | Pending |
| Conflicting `X-Forwarded-Proto` | — | HTTPS health endpoint with client value `http` | Awaiting deployment of this revision | Pending |

The redirect observation was made against the existing production deployment. Its response identified Cloudflare as the public server. The three header checks must be repeated only after this SEC-11 revision is deployed; the pre-SEC-11 deployment does not emit these new headers and cannot validate the filter or sanitization assumption.

### Edge sanitization check

After deploying this revision, send a conflicting client value over HTTPS:

```bash
curl -sS -o /dev/null -D - \
  -H 'X-Forwarded-Proto: http' \
  https://<railway-domain>/myvanitys/actuator/health
```

The response must still include:

```text
strict-transport-security: max-age=31536000; includeSubDomains
```

That result demonstrates that Railway replaced the client value with `https` before forwarding the request. If HSTS is absent, Railway preserved or merged the conflicting value in a form this filter does not accept; do not close SEC-11 until the trust-boundary design is revised. A spoofed `https` on a genuinely plain-HTTP response would only add HSTS where browsers ignore it, while a preserved `http` value on an HTTPS request could suppress HSTS. In either case the influence is limited to the HSTS decision because the application does not construct redirects from forwarded data.

## Remaining gaps

- The public redirect check has passed. The HTTPS success/error header checks and edge sanitization check require a deployment of this revision. Until those three checks pass and their results are recorded above, the implementation is ready for deployment but SEC-11 is not verified or complete.
- Method-preserving (`307`/`308`) HTTPS redirects are not provided; the edge performs a `301`. This is accepted platform behavior unless the requirement changes.
- Global `Cache-Control: no-store` and a `Content-Security-Policy` were intentionally omitted: a global CSP could break the springdoc Swagger UI, and a broad `Cache-Control` would interfere with static assets.
