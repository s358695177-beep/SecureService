# SecureService

SecureService is a **security-oriented backend template** built to demonstrate **correct authentication/authorization architecture**, now extended with **refresh token rotation** and **stateful token control**.

> Keywords: Spring Boot 3.x · JWT · RBAC · TokenVersion invalidation · Refresh Token Rotation · HttpOnly Cookie · Integration tests

---

## Core Highlights

- BCrypt password hashing (no plaintext storage)
- JWT-based stateless authentication (Access Token)
- **Refresh Token with DB persistence**
- **Refresh Token Rotation (one-time use)**
- **TokenVersion forced invalidation**
- Clear 401 vs 403 behavior with unified JSON response
- Clean layered structure (Controller → Service → Repository)
- Integration-tested security chain

---

## Authentication Model (Updated)

### Dual Token Strategy

| Token Type | Storage | Lifetime | Purpose |
|----------|--------|----------|--------|
| Access Token | Header (Bearer) | Short (e.g. 15min) | API authentication |
| Refresh Token | HttpOnly Cookie | Long (e.g. 7d) | Token renewal |

---

## Refresh Token Rotation (NEW)

### Goal

Prevent replay attacks and token reuse.

### Mechanism

Each refresh request:

1. Validate refresh token
2. Mark **old token as revoked**
3. Generate **new refresh token**
4. Store new token in DB
5. Return:
   - new access token (body)
   - new refresh token (cookie)

---

### Flow

```text
Login
  ↓
issue access + refresh token
  ↓
Client calls /auth/refresh
  ↓
validate refresh token
  ↓
revoke old token
  ↓
issue new access + refresh token
```

---

### Security Properties

- Refresh token is **one-time usable**
- Stolen refresh token becomes useless after first use
- Server has full control (revocation supported)
- No JWT parsing needed for refresh token (UUID-based)

---

## Token Validation Rules

### Access Token

- Signature valid
- Not expired
- tokenVersion matches DB

### Refresh Token

- Exists in DB
- Not revoked
- Not expired
- Format valid (rt_ + 32 hex)

---

## Example Refresh Response

```json
{
  "code": 0,
  "message": "success",
  "data": "<access_token>"
}
```

Cookie:
```
Set-Cookie: refreshToken=rt_xxx; HttpOnly; SameSite=Strict
```

---

## Testing Coverage (Updated)

- Refresh success (rotation)
- Old refresh token rejected
- New refresh token valid
- Missing cookie → 401
- Invalid format → 401
- DB state validation (revoked flags)

---

## Design Philosophy

This project now demonstrates:

- Stateless + Stateful hybrid auth design
- Secure token lifecycle management
- Replay attack prevention
- Backend-controlled session invalidation
- Production-grade auth modeling

---

## Next Steps (Planned)

- Logout endpoint
- Refresh token hashing (DB security)
- Multi-device session control
- Redis-based token management
- Rate limiting / abuse protection
