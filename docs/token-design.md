# Token Authentication Design

## 1. Background

The SecureService system uses **JWT-based stateless authentication** to protect API access.

To balance **security, scalability, and usability**, the system adopts a **dual-token mechanism**:

- **Access Token** — short-lived token used to access APIs
- **Refresh Token** — long-lived token used to obtain new Access Tokens

This design avoids frequent logins while maintaining strong control over token validity.

---

## 2. Token Architecture

The authentication system uses the following token structure:

```text
Client
   │
   │ login
   ▼
Auth Server
   │
   ├── Access Token (short-lived)
   └── Refresh Token (long-lived)
```

### Access Flow

```text
Client ──AccessToken──▶ API Server
```

### Refresh Flow

```text
Client ──RefreshToken──▶ /auth/refresh
                       │
                       ▼
                 new AccessToken
```

Key properties:

| Token | Storage | Lifetime | Purpose |
|-----|-----|-----|-----|
| AccessToken | Client memory / Authorization header | Short | Access APIs |
| RefreshToken | HttpOnly Cookie + Database | Long | Renew AccessToken |

---

## 3. Access Token Design

The Access Token is a **stateless JWT**.

It contains the minimal identity information required for authorization.

### Payload Structure

```json
{
  "sub": "user_id",
  "roles": ["USER"],
  "tokenType": "access",
  "iat": 1710000000,
  "exp": 1710000900
}
```

### Field Description

| Field | Meaning |
|-----|-----|
| sub | User ID (token subject) |
| roles | User roles for authorization |
| tokenType | Identifies token type |
| iat | Issued time |
| exp | Expiration time |

### Lifetime

Recommended:

```text
AccessToken expiration = 15 minutes
```

Short expiration limits the impact of token leakage.

### Storage Recommendation

The Access Token should be stored in **frontend memory** and sent through the `Authorization` header:

```http
Authorization: Bearer <access_token>
```

This reduces traditional CSRF risk because browsers do not automatically attach the `Authorization` header in cross-site requests.

---

## 4. Refresh Token Design

The Refresh Token is also a JWT but **must be tracked by the server**.

Unlike Access Tokens, Refresh Tokens can be **revoked**.

### Payload Structure

```json
{
  "sub": "user_id",
  "tokenId": "uuid",
  "tokenType": "refresh",
  "iat": 1710000000,
  "exp": 1710600000
}
```

### Field Description

| Field | Meaning |
|-----|-----|
| sub | User ID |
| tokenId | Unique identifier for token |
| tokenType | Token category |
| iat | Issued time |
| exp | Expiration time |

### Lifetime

Recommended:

```text
RefreshToken expiration = 7 days
```

### Storage Recommendation

The Refresh Token should be stored in an **HttpOnly, Secure, SameSite cookie** rather than in localStorage.

Example:

```http
Set-Cookie: refreshToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/auth/refresh
```

Reasoning:

- it is a long-lived credential
- it should not be readable by client-side JavaScript
- HttpOnly reduces XSS-based token theft

However, because cookies are automatically attached by browsers, the refresh endpoint must still consider CSRF protection.

---

## 5. Refresh Token Database Design

Refresh Tokens must be persisted in the database to support:

- token revocation
- token expiration management
- future device/session management

### Table: `refresh_tokens`

| Column | Type | Description |
|------|------|------|
| id | UUID | Primary key |
| user_id | BIGINT | Owner of token |
| token_id | VARCHAR | Unique token identifier |
| expires_at | TIMESTAMP | Token expiration time |
| revoked | BOOLEAN | Revocation flag |
| created_at | TIMESTAMP | Creation time |

### Suggested Indexes

```text
index(token_id)
index(user_id)
```

---

## 6. Token Refresh Flow

### Login

```http
POST /auth/login
```

Response:

```json
{
  "accessToken": "..."
}
```

At the same time, the server sets the Refresh Token cookie:

```http
Set-Cookie: refreshToken=<token>; HttpOnly; Secure; SameSite=Strict; Path=/auth/refresh
```

---

### Refresh Access Token

```http
POST /auth/refresh
```

Request:

- no refresh token is sent in the JSON body
- the browser automatically attaches the refresh token cookie

Processing steps:

1. Read Refresh Token from cookie
2. Verify Refresh Token signature
3. Parse `sub` and `tokenId`
4. Query database for token record
5. Check revoked status
6. Check expiration time
7. Generate new Access Token

Response:

```json
{
  "accessToken": "..."
}
```

---

## 7. Security Considerations

### 1. Minimal Payload

JWT payload must **not contain sensitive data**.

Avoid:

- password
- phone number
- personal identity information

---

### 2. Short Access Token Lifetime

Short expiration reduces the risk of token theft.

```text
AccessToken ≈ 15 minutes
```

---

### 3. Refresh Token Revocation

Refresh Tokens must support revocation via database records.

Example scenarios:

- user logout
- password change
- account suspension

---

### 4. Signature Validation

All tokens must be verified using the server secret before processing.

---

### 5. CSRF Protection for Refresh Endpoint

Because the Refresh Token is stored in a cookie, the `/auth/refresh` endpoint must be protected against CSRF-related abuse.

Recommended measures:

- use `SameSite=Strict` if deployment allows it
- allow only `POST /auth/refresh`
- configure strict CORS rules
- avoid returning refresh tokens in frontend-readable storage

Important note:

A CSRF attacker may be able to trigger a refresh request if protections are weak, but under proper same-origin and CORS rules, the attacker should not be able to read the response body containing the new Access Token.

---

## 8. Future Extensions

The following improvements can be added in later versions:

### Token Rotation

Each refresh operation issues a new Refresh Token.

---

### Device-based Sessions

Track tokens per device.

---

### Token Blacklist

Immediate token invalidation for compromised tokens.

---

## 9. Scope of Current Implementation

The current phase includes:

- AccessToken design
- RefreshToken structure
- storage strategy
- database schema
- refresh API definition

The following features are **not implemented in this phase**:

- refresh token rotation
- device session management
- token blacklist
- full cookie writing logic in controller/filter layer

These will be implemented in future iterations.
