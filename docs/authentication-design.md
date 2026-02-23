# Authentication & TokenVersion Design

## 1. Design Motivation

JWT is inherently stateless. Once issued, it remains valid until
expiration. However, real-world systems require controlled invalidation
capability, such as:

-   Admin-forced logout
-   Password change
-   Account suspension

Instead of maintaining server-side token blacklists, this system
introduces a TokenVersion mechanism.

------------------------------------------------------------------------

## 2. Login Process

1.  User submits username and password
2.  BCrypt verifies hashed password
3.  System loads user roles
4.  JWT issued containing:
    -   userId
    -   roles
    -   issuer
    -   issued time
    -   expiration
    -   tokenVersion

JWT is returned to client and sent in Authorization header.

------------------------------------------------------------------------

## 3. Request Authentication Flow

For each protected request:

1.  Extract JWT from Authorization header
2.  Verify signature
3.  Verify expiration
4.  Load user from database
5.  Compare JWT.tokenVersion with database token_version
6.  If match → Authentication built
7.  If mismatch → 401 Unauthorized

Security principle: fail-closed.

------------------------------------------------------------------------

## 4. TokenVersion Invalidation Mechanism

Each user record includes:

    token_version INT NOT NULL DEFAULT 0

Invalidate operation:

    UPDATE users
    SET token_version = token_version + 1
    WHERE id = ?

Impact:

-   All previously issued JWTs become invalid
-   No blacklist storage required
-   Stateless architecture largely preserved
-   Immediate forced logout capability

------------------------------------------------------------------------

## 5. Failure Handling Strategy

If database is unavailable:

-   tokenVersion cannot be validated
-   Authentication fails safely (fail-closed)
-   System should return 503 (service dependency unavailable)
-   Once DB recovers, valid tokens resume normal function if not expired
    and version unchanged

------------------------------------------------------------------------

## 6. Design Trade-offs

Advantages:

-   Simple implementation
-   Low state complexity
-   Immediate global logout
-   Horizontal scalability maintained

Trade-offs:

-   Requires DB lookup per request
-   Not fully stateless (lightweight state dependency)

This design balances security control and architectural simplicity.
