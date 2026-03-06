# Authentication & TokenVersion Design

## 1. Design Motivation

JWT is inherently stateless. Once issued, it remains valid until expiration.
However, real-world backend systems require **controlled invalidation capability**, such as:

- Admin-forced logout
- Password change
- Account suspension
- Security incident response

Instead of maintaining a server-side token blacklist (which increases state complexity),
this system introduces a lightweight **TokenVersion mechanism**.

Design goal:
- Keep architecture mostly stateless
- Enable immediate global invalidation
- Preserve horizontal scalability
- Fail safely under uncertainty

---

## 2. Login Process

1. User submits username and password
2. BCrypt verifies the hashed password
3. System loads user roles
4. JWT is issued containing:

    - userId
    - roles
    - issuer
    - issuedAt
    - expiration
    - tokenVersion

JWT is returned to the client and must be sent in the `Authorization: Bearer <token>` header.

---

## 3. Request Authentication Flow

For each protected request:

1. Extract JWT from Authorization header
2. Verify signature
3. Verify expiration
4. Load current `token_version` from database
5. Compare `JWT.tokenVersion` with database `token_version`
6. If match → build Authentication object
7. If mismatch → return **401 Unauthorized**

Security principle: **fail-closed**  
If validation cannot be confidently completed, the request must not proceed.

---

## 4. TokenVersion Invalidation Mechanism

Each user record includes:

    token_version INT NOT NULL DEFAULT 0

Invalidate operation:

    UPDATE users
    SET token_version = token_version + 1
    WHERE id = ?

Impact:

- All previously issued JWTs become invalid immediately
- No blacklist storage required
- Minimal additional state
- Immediate forced logout capability
- Supports revoke-on-password-change pattern

---

## 5. Failure Handling Strategy

### Case 1: Token invalid (signature/expiry/version mismatch)
→ Return **401 Unauthorized**

### Case 2: Authenticated but insufficient role
→ Return **403 Forbidden**

### Case 3: Database unavailable
If `token_version` cannot be validated:

- Authentication cannot be trusted
- System fails safely
- Return **503 Service Unavailable**
- Do NOT incorrectly return 401 (identity may be valid but dependency failed)

Rationale:
- 401 indicates authentication failure
- 503 indicates infrastructure dependency failure
- This distinction improves observability and production diagnostics

---

## 6. Design Trade-offs

Advantages:

- Simple implementation
- Low state complexity
- Immediate global logout
- Horizontal scalability maintained
- No token blacklist storage

Trade-offs:

- Requires DB lookup per request
- Not fully stateless (lightweight state dependency)
- Slight increase in per-request latency

This design balances security control with architectural simplicity.

---

## 7. Interview Explanation (Concise Version)

"JWT is stateless, but production systems require controlled invalidation.
Instead of introducing a blacklist, we embed a tokenVersion in the JWT and compare it with the database value on each request. If the version changes, all old tokens are invalidated immediately. The system follows a fail-closed security model and clearly distinguishes between 401, 403, and 503."

---

## 8. Security Philosophy

- Identity must be verifiable
- Authorization must be explicit
- Failure must be safe
- Error semantics must be precise
- Invalidation must be controllable

The goal is not maximum stateless purity,
but practical, production-ready security architecture.
