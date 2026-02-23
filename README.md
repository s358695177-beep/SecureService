# SecureService

SecureService is a security-oriented backend system designed as part of
my backend capability reconstruction journey toward becoming a
**Security-Focused Backend Engineer in Japan**.

This project focuses on authentication architecture correctness rather
than feature quantity.

------------------------------------------------------------------------

## Core Highlights

-   BCrypt password hashing (no plaintext storage)
-   JWT-based stateless authentication
-   TokenVersion forced invalidation mechanism
-   Unified 401 structured response
-   Clean layered architecture (Controller → Service → Repository)
-   RBAC foundation with explicit join entity
-   Integration-tested security chain

------------------------------------------------------------------------

## Authentication Architecture Overview

Authentication is implemented using JWT with an additional
**TokenVersion mechanism** to support controlled forced logout without
maintaining token blacklists.

Flow:

1.  User logs in with username and password
2.  BCrypt verifies credentials
3.  JWT is issued containing:
    -   userId
    -   roles
    -   issuer
    -   expiration
    -   tokenVersion
4.  Client sends JWT in `Authorization: Bearer <token>` header
5.  Server validates:
    -   Signature
    -   Expiration
    -   tokenVersion consistency (DB check)
6.  If valid → Authentication injected into SecurityContext
7.  If invalid → 401 structured response

------------------------------------------------------------------------

## TokenVersion Forced Invalidation

-   Each user has a `token_version` column in database

-   JWT contains the current tokenVersion at issuance

-   Each request compares JWT.tokenVersion with DB value

-   Admin invalidate operation performs:

    UPDATE users SET token_version = token_version + 1

Effect:

-   All previously issued tokens immediately become invalid
-   No token blacklist required
-   Minimal state complexity
-   Immediate global logout support

------------------------------------------------------------------------

## Demonstration Flow

1.  Login → receive JWT
2.  Access `/me` → 200
3.  Call invalidate → token_version++
4.  Access `/me` again → 401
5.  Login again → 200

------------------------------------------------------------------------

## Design Philosophy

This project emphasizes:

-   Security correctness
-   Architectural clarity
-   Controlled invalidation capability
-   Professional engineering expression
-   Extensibility toward production-grade backend templates
