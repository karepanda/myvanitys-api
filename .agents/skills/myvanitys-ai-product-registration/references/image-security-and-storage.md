# Image Security and Storage

Read this reference when the task accepts, stores, promotes, serves, replaces, or removes product images.

## Ingestion boundary

Validate on the server even when the web client already resized or validated the image.

1. Enforce the configured request and file-size limit before expensive decoding.
2. Allow only contract-approved media types.
3. Verify file signatures/magic bytes; do not trust `Content-Type` or the original filename.
4. Decode with limits for dimensions, pixel count, frame count, and decompression expansion.
5. Reject malformed, unsupported, animated, or dangerous inputs according to the contract.
6. Strip EXIF and other metadata, normalize orientation, and re-encode to an approved format.
7. Generate server-side object identifiers. Never embed a client filename in a storage key.

Do not invent missing numeric limits. Use the approved configuration or stop for a product/security decision.

## Storage layout and access

Use these semantic prefixes:

| Prefix | Purpose | Access | Lifetime |
|---|---|---|---|
| `tmp/{userId}/{uuid}` | Sanitized image awaiting confirmation | Private | 24-hour lifecycle |
| `users/{userId}/{uuid}` | User-specific product image | Private, presigned GET | Until replaced or removed |
| `refs/{productId}/{uuid}` | Catalog reference image | Public read | Permanent |

The application works with opaque references and storage ports. Only the storage adapter knows provider SDK request/response types, bucket configuration, and URL construction.

## Temporary references

A temporary reference must resolve to metadata that proves:

- Owning authenticated user.
- Temporary key.
- Creation/expiry time.
- Sanitized content type and relevant dimensions.
- Intended workflow or purpose when reuse could cross a security boundary.

Before promotion, verify ownership, expiry, prefix, and object existence. Do not accept an arbitrary R2 key or URL from the client.

## Promotion and consent

On create or add-to-vanity:

1. Validate the temporary reference before database mutation.
2. Copy the sanitized object to a new `users/{userId}/{uuid}` key.
3. Persist the user association and private image key.
4. Only when explicit consent is true and the product lacks a reference image, copy to a new `refs/{productId}/{uuid}` key and persist it as the product reference.
5. Remove or allow lifecycle expiration of the temporary object according to the chosen compensation design.

Consent is specific to public reference use. Saving a private custom image does not imply consent.

## Partial-failure policy

Object storage and PostgreSQL do not share a transaction. Make the chosen ordering and compensation explicit.

- When an object write succeeds and the database write fails, delete the unreferenced new object or record a safe cleanup/retry state.
- When the database succeeds and a required object operation fails, roll back the database transaction when possible; otherwise preserve a detectable retry state rather than a broken key.
- Make retries idempotent. Reusing a request must not create duplicate associations or promote the same image repeatedly.
- Log object identifiers and correlation IDs, never image bytes, presigned query strings, credentials, or extracted personal text.

## Effective image resolution

For an authenticated user and product:

1. If the product-user association has `image_key`, return a newly presigned URL with source `USER`.
2. Otherwise, if the product has `reference_image_key`, return its public URL with source `REFERENCE`.
3. Otherwise, return the category placeholder with source `PLACEHOLDER`.

Do not persist presigned URLs. Persist only keys and derive URLs at response time.

## Replace and remove

For replacement, verify that the product belongs to the authenticated user's vanity, sanitize and store a new object, update the association, and delete the old object only after the new key is safely persisted. A failure deleting the old object is a cleanup concern and must not erase the new valid reference.

For removal, clear the authenticated user's association key and delete only that user's prior object. The response falls back to reference or placeholder resolution. Never delete a public reference image as a side effect of removing a private custom image.
