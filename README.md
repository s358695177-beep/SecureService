# SecureService

SecureService is a **security-oriented backend template** built to demonstrate **correct authentication/authorization architecture** (more than feature quantity), with a focus on being *production-style* and easy to explain in interviews for Japan-oriented backend roles.

> Keywords: Spring Boot 3.x · JWT · RBAC · TokenVersion invalidation · Clean layering · Integration tests

---

## Core Highlights

- BCrypt password hashing (no plaintext storage)
- JWT-based stateless authentication
- **TokenVersion forced invalidation** (global logout without token blacklist)
- **Clear 401 vs 403 behavior** with unified JSON error response
- Clean layered structure (Controller → Service → Repository)
- RBAC foundation via explicit join entity (User ↔ Role)
- Integration-tested security chain (happy path + failure cases)

---

## Architecture at a Glance

**Layers**

- **Controller**: HTTP boundary, request/response DTOs
- **Service**: business use-cases (login, me, invalidate)
- **Repository**: persistence operations
- **Security / Filters**: JWT parsing + authentication injection + exception handling

**Why this design**
- Easy to swap implementation details (e.g., storage) without rewriting business flow
- Easy to test the security chain end-to-end

---

## Authentication & Authorization Overview

SecureService uses JWT with an additional **TokenVersion mechanism** to support *forced logout* without maintaining token blacklists.

### JWT contains

- `userId`
- `roles`
- `issuer`
- `expiration`
- `tokenVersion`

### Request flow (happy path)

1. Client logs in with username/password
2. Server verifies with BCrypt
3. Server issues JWT (includes tokenVersion)
4. Client sends `Authorization: Bearer <token>`
5. Server validates:
    - Signature
    - Expiration
    - **tokenVersion consistency** (compare JWT tokenVersion with DB value)
6. If valid → inject `Authentication` into `SecurityContext`
7. Controller executes normally

---

## 401 vs 403: When Each Happens

**401 Unauthorized** (authentication failed / missing)
- No token / malformed token
- Signature invalid / expired token
- tokenVersion mismatch (forced invalidation triggered)
- Invalid credentials during login

**403 Forbidden** (authentication succeeded, but not allowed)
- Token is valid, but role/permission check fails (e.g., missing `ADMIN`)
- Method-level authorization denied (`@PreAuthorize(...)`)

> Practical interview explanation:
> **401 = who are you?** (identity not established)  
> **403 = you are identified, but you don't have permission**

---

## Filter vs EntryPoint: Responsibilities

**JWT Filter**
- Extracts token from header
- Validates token (signature/expiry/tokenVersion)
- If valid: builds Authentication and puts it into `SecurityContext`

**AuthenticationEntryPoint**
- Runs when Spring Security determines the request is **unauthenticated** for a protected resource
- Responsible for writing the **unified 401 JSON response**

(Optional) **AccessDeniedHandler**
- Runs when request is authenticated but not authorized
- Responsible for writing the **unified 403 JSON response**

---

## TokenVersion Forced Invalidation

- Each user has a `token_version` column in DB
- JWT includes the tokenVersion at issuance
- Each request compares `JWT.tokenVersion` with `DB.token_version`

**Invalidate operation**

```sql
UPDATE users
SET token_version = token_version + 1
WHERE id = ?
```

**Effect**
- All previously issued tokens become invalid immediately
- No token blacklist required
- Minimal state complexity
- Supports global logout / revoke-on-password-change patterns

---

## Demonstration Flow

1. Login → receive JWT
2. Access `/me` → 200
3. Call invalidate → token_version++
4. Access `/me` again → 401
5. Login again → 200

---

## Testing

This project emphasizes **integration tests** for the security chain:

- Login success/failure
- Protected endpoint with/without token
- Expired/invalid token → 401
- tokenVersion mismatch → 401
- Role denied → 403

---

## Design Philosophy

This project emphasizes:

- Security correctness
- Architectural clarity
- Controlled invalidation capability
- Professional engineering expression
- Extensibility toward production-grade backend templates
