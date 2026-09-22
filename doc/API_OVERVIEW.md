# MyVanitys API — Overview for API Consumers

Audience: client developers (mobile, web, backend) integrating with the MyVanitys HTTP API. You never need to read this repository's source code.

## What this API does

MyVanitys is a beauty-product catalog with a personal collection feature. A client can: browse the product catalog and search it; find public products; create products; manage a private "vanity" (a user's personal product collection); leave reviews; and authenticate users with Google. Authentication is Google OAuth, exchanged for a JWT that authorizes every non-auth endpoint.

## Authentication

1. Client sends the user to Google's consent screen.
2. Google redirects back with an authorization `code`.
3. Client sends that code to the API; the API exchanges it with Google and returns a JWT.
4. Client sends the JWT on every protected request: `Authorization: Bearer <token>`.

- Token lifetime: **3600 seconds (1 hour)** in the base/docker/prod configuration, **86400 seconds (24 hours)** in local development. The token is an opaque string to the client; do not parse it.
- Missing, invalid, or expired token: **HTTP 401** with a `ProblemDetail` body (`type`, `title`, `status`, `detail`, `instance`).
- Public endpoints (no token required): `POST /auth/google`, `POST /auth/register`, and the actuator health endpoint.

### Two auth endpoints — and why the status codes are asymmetric

| Endpoint | Purpose | If the Google account... | HTTP |
|---|---|---|---|
| `POST /auth/register` | Register a new user | **is already registered** | **409** |
| `POST /auth/google` | Log in an existing user | **is NOT registered** | **401** |

This asymmetry is intentional. Branch your UI accordingly: on `409` from `/auth/register` show "you already have an account" (and route to login); on `401` from `/auth/google` show "please register first".

> **Note — registration does not return a usable token.** `POST /auth/register` currently returns only `userId`; the `token`, `expiresIn`, and `refreshToken` fields exist in the schema but are not populated. To obtain a JWT, call `POST /auth/google` after a successful registration.

> **Accepted decision (not yet implemented) — single-step registration.** The two-step behavior above is accepted technical debt. The accepted target is that a successful registration authenticates the user in the same operation and returns exactly `userId`, `token`, and `expiresIn` (with `refreshToken` removed from the schema until a full MyVanitys refresh-token lifecycle exists). This is **not deployed yet**: the current API still returns only `userId`, so clients must keep following the two-step behavior described above until the coordinated contract/backend/web rollout is complete. See [0012 — Registration session response contract](decisions/0012-registration-session-contract.md).

## Core concepts (as they appear in responses)

| Concept | Response fields | Notes |
|---|---|---|
| **Product** | `id`, `name`, `brand`, `category`, `colorHex`, `averageRating`, `inUserCollection`, `reviews[]`, `createdAt`, `imageUrl` | `inUserCollection` tells you whether the product is in the current user's vanity. `imageUrl` is `null` when a product has no photo. |
| **Category** | `id`, `name` | Nested inside a product (`product.category`). |
| **Review** | `id`, `rating`, `comment`, `createdAt`, `userId` | `rating` is an integer 1–5. `userId` is the author. |
| **Vanity** | represented by `inUserCollection` on a product, plus the add/remove endpoints | A user's personal product collection. Not a standalone object in responses. |
| **Search result** | `content[]` (array of Product) | Returned by `GET /products/search`. |

## Base URLs

| Environment | Base URL |
|---|---|
| Local | `http://localhost:8080/myvanitys/api/v1` |
| Docker | `http://<host>:8080/myvanitys/api/v1` |
| Production | `https://api.myvanitys.com/api/v1` |

The service uses a context path of `/myvanitys` and an API version segment of `/api/v1`. All paths below are relative to one of these base URLs.

## Endpoints

| Method | Path | Auth | Success | Purpose |
|---|---|---|---|---|
| POST | `/auth/register` | No | 200 `UserCreatedResponse` | Register a new user via Google |
| POST | `/auth/google` | No | 200 `AuthResponse` | Log in an existing user, returns JWT |
| GET | `/products` | Bearer | 200 `ProductResponse[]` | All products with collection status |
| GET | `/products/search?query=` | Bearer | 200 `ProductSearchResponse` | Search by name or brand |
| GET | `/users/{userId}/products` | Bearer | 200 `ProductResponse[]` | A user's vanity collection (must be your own id, else 403) |
| POST | `/products` | Bearer | 201 `ProductResponse` | Create a product |
| POST | `/products/{productId}/add-to-vanity` | Bearer | 200 `ProductResponse` | Add a product to your vanity |
| DELETE | `/products/{productId}` | Bearer | 204 (no body) | Remove a product from your vanity |
| POST | `/products/{productId}/reviews` | Bearer | 202 `ProductResponse` | Add a review to a product |

## Error responses

Errors use a `ProblemDetail` body:

```json
{
  "type": "https://api.myvanitys.com/problems/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Invalid input data: ...",
  "instance": "/api/v1/auth/google"
}
```

**Caveat — the body `status` is not always the HTTP status.** Some handlers set a `status` value in the body that differs from the HTTP status code actually returned.

> **Always code against the actual HTTP status of the response, not the `status` field inside the body.**

Known mismatches:

| Scenario | Actual HTTP status | Body `status` says |
|---|---|---|
| Product not found | **400** | 404 |
| Google authorization error | **400** | 404 |
| Infrastructure/database error | **400** | 500 |

Responses where HTTP status and body `status` agree:

| Scenario | HTTP status |
|---|---|
| Request validation error (bad body/headers) | 400 |
| Missing / invalid / expired token | 401 |
| Login: user not registered | 401 |
| Authentication failed | 401 |
| Register: user already exists | 409 |
| Unhandled server error | 500 |

### Special case — `POST /auth/register` can return an empty 500

If registration succeeds internally but no session/result is produced, `POST /auth/register` returns **HTTP 500 with no body** — not a `ProblemDetail`. It is the only endpoint that does not follow the ProblemDetail contract on failure, so do not assume every 5xx response has a parsable JSON body.

> Maintainer reference: `AuthController.createUser` — the `if (result == null) { return ResponseEntity.internalServerError().build(); }` branch.

## Not currently available

`POST /products/image-analysis` is still present in the currently published API contract, but it **has no working implementation** — do not integrate against it. Because no controller overrides the generated method, the route currently falls through to the generated fallback and returns **HTTP 501**, which is **not** part of the advertised response contract and carries no `ProblemDetail` body. The `imageReference` field on `POST /products` is tied to that feature, is silently ignored by the API today, and must likewise not be used.

> **Accepted decision (removal not yet deployed) — image-analysis contract.** Removal of `POST /products/image-analysis`, `ProductImageAnalysisResult`, and `CreateProductRequest.imageReference` has been accepted for the next breaking API-spec release, but **nothing has been removed yet**: the currently published contract and the deployed API still expose them. Clients must not build against them. See [0013 — Image-analysis API contract](decisions/0013-image-analysis-contract.md).

## Where to go next — sequence diagrams

- [Register a new user (Google OAuth)](sequenceDiagrams/googleAuthNewUser.mermaid)
- [Log in an existing user (Google OAuth)](sequenceDiagrams/googleAuthExistingUser.mermaid)
- [Authenticated request (JWT pattern)](sequenceDiagrams/authenticatedRequest.mermaid)
- [Create a product](sequenceDiagrams/createProduct.mermaid)
- [Add a review to a product](sequenceDiagrams/addReviewToProduct.mermaid)
- [Manage your vanity (add / remove)](sequenceDiagrams/manageVanity.mermaid)

## Full API contract

The complete OpenAPI contract is the file `openapi.yaml` in the `myvanitys-api-spec` repository — the source of truth for every endpoint, schema, and example.

- The **published Java artifact** of that repo (`myvanitys-api-spec`, GitHub Packages) is useful only to JVM consumers (another Java/Kotlin backend that wants generated types).
- For Swift, Kotlin (Android native), or a cross-platform framework, use the `openapi.yaml` file itself: run it through `openapi-generator` (or any OpenAPI-compatible generator) to produce a client in your language, or read it directly as the endpoint/schema reference.
