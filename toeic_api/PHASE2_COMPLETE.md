> Tài liệu lịch sử trước refactor. Mô hình/API hiện tại: [BACKEND_ARCHITECTURE.md](../BACKEND_ARCHITECTURE.md).

# 🔐 Phase 2: JWT Authentication & Authorization - Hoàn Thành

**Ngày:** 2026-08-15  
**Status:** ✅ COMPLETE

---

## 📋 Những gì được thêm

### 1. **Dependency Management** ✅

**pom.xml - Uncommented:**

```xml
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>9.30.1</version>
</dependency>
```

**Giải thích:**

- Nimbus JOSE + JWT: Thư viện JWT hàng đầu để tạo, ký, và xác minh JWT tokens
- Tương thích với Spring Security OAuth2 Resource Server
- Hỗ trợ HS512 algorithm (HMAC-SHA512)

---

### 2. **Configuration Files** ✅

#### application.yml - JWT Config

```yaml
jwt:
  signer-key: "eb31b964f2424daefebb00e74c35ab42e4778a2f7c00e714515683d1b9a64a8d"
  valid-duration: 20 # Token hợp lệ 20 giờ
  refreshable-duration: 120 # Có thể refresh trong 120 giờ
```

**Giải thích:**

- `signer-key`: Secret key để ký/verify JWT (phải giữ bí mật!)
- `valid-duration`: Thời gian token còn sử dụng được
- `refreshable-duration`: Thời gian có thể refresh token

#### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Cho phép @PreAuthorize
public class SecurityConfig {
    // PUBLIC_ENDPOINTS: /api/v1/users, /api/v1/auth/login, /api/v1/auth/introspect
    // Tất cả endpoint khác yêu cầu Bearer Token
}
```

**Giải thích:**

- `@EnableWebSecurity`: Bật Spring Security
- `@EnableMethodSecurity`: Cho phép `@PreAuthorize` trên methods
- `filterChain()`: Cấu hình security rules
- `jwtAuthenticationConverter()`: Convert JWT claims thành Spring authorities

#### CustomJwtDecoder.java

```java
@Component
public class CustomJwtDecoder implements JwtDecoder {
    // Decode JWT token
    // Verify signature bằng HS512
    // Check token expiration
}
```

**Giải thích:**

- Implement Spring's `JwtDecoder` interface
- Decode token + verify chữ ký
- Throw `JwtException` nếu token không hợp lệ

#### JwtAuthenticationEntryPoint.java

```java
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // Handle authentication errors
    // Return ApiResponse with ErrorCode.UNAUTHENTICATED
}
```

**Giải thích:**

- Xử lý khi user không được xác thực
- Trả về JSON response thay vì redirect

---

### 3. **Authentication Service** ✅

#### AuthenticationService.java

```java
public AuthenticationResponse authenticate(AuthenticationRequest request) {
    // 1. Tìm user by username
    // 2. Verify password (BCrypt)
    // 3. Generate JWT token
    // 4. Return token + authenticated=true
}

public IntrospectResponse introspect(IntrospectRequest request) {
    // Verify token hợp lệ
    // Return valid=true/false
}

private String generateToken(User user) {
    // JWTClaimsSet: subject, issueTime, expirationTime, scope
    // Sign bằng HS512
    // Return token string
}

private void verifyToken(String token) {
    // JWSObject.parse(token)
    // Verify signature
    // Check expiration
}
```

**Giải thích:**

- `authenticate()`: Đăng nhập user, trả JWT token
- `introspect()`: Kiểm tra token còn hợp lệ không
- `generateToken()`: Tạo JWT với thông tin user
- `verifyToken()`: Xác minh token (chữ ký + hạn)

---

### 4. **Request/Response DTOs** ✅

#### AuthenticationRequest

```java
{
  "username": "john_doe",
  "password": "SecurePass123"
}
```

#### AuthenticationResponse

```java
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "authenticated": true
}
```

#### IntrospectRequest

```java
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### IntrospectResponse

```java
{
  "valid": true
}
```

---

### 5. **Controller** ✅

#### AuthenticationController

```java
@PostMapping("/login")
public ApiResponse<AuthenticationResponse> authenticate(
    @RequestBody AuthenticationRequest request)

@PostMapping("/introspect")
public ApiResponse<IntrospectResponse> introspect(
    @RequestBody IntrospectRequest request)
```

**Endpoints:**

- `POST /api/v1/auth/login` - Đăng nhập
- `POST /api/v1/auth/introspect` - Kiểm tra token

---

### 6. **Authorization** ✅

#### Role-Based Access Control

**Roles:**

- `ADMIN`: Full access (xem users, quản lý content)
- `USER`: Limited access (làm bài, xem kết quả riêng)

**Implementation:**

```java
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<List<UserResponse>> getAllUsers() { }

@PreAuthorize("isAuthenticated()")
public ApiResponse<UserResponse> getProfile() { }

// No annotation = Public
public ApiResponse<UserResponse> createUser() { }
```

**Spring Authority Format:**

- Roles được lưu trong JWT claim "scope"
- Automatically prefix với "ROLE\_" (e.g., "ROLE_USER", "ROLE_ADMIN")

---

## 🔄 Authentication Flow

### 1. Đăng ký User

```
POST /api/v1/users
- Password được hash bằng BCrypt(10 rounds)
- Role mặc định = USER
- Tự động tạo vocabulary collection
```

### 2. Đăng nhập

```
POST /api/v1/auth/login
- Verify password
- Generate JWT token (valid 20 hours)
- Token contains: userId (subject), role (scope), timestamps
```

### 3. Gửi Request

```
Authorization: Bearer {token}
- Spring Security tự động extract token từ header
- CustomJwtDecoder verify signature + expiration
- JwtAuthenticationConverter convert claims thành authorities
```

### 4. Authorization

```
@PreAuthorize("hasRole('ADMIN')")
- Check nếu user có ADMIN role
- Nếu không → throw AccessDeniedException
```

---

## 📊 Public vs Protected Endpoints

### Public (No Token Needed)

```
POST /api/v1/users                    # Register
POST /api/v1/auth/login               # Login
POST /api/v1/auth/introspect          # Check token
GET  /api/v1/vocabulary/categories    # View categories
```

### Protected (Require Bearer Token)

```
GET  /api/v1/users/{userId}           # Authenticated users
GET  /api/v1/users                    # ADMIN ONLY
GET  /api/v1/listening/parts          # Authenticated users
POST /api/v1/listening/answer         # Authenticated users
... (tất cả endpoints khác)
```

---

## 🔐 Security Features

✅ **Password Encryption**

- BCrypt with 10 rounds
- Never store plain-text passwords

✅ **JWT Token**

- Signed with HS512 (HMAC-SHA512)
- 20-hour expiration
- Contains user ID + role

✅ **Token Verification**

- Signature check
- Expiration check
- Custom JwtDecoder implementation

✅ **Role-Based Authorization**

- @PreAuthorize annotations
- Method-level security
- Automatic role extraction from JWT

✅ **Error Handling**

- JwtAuthenticationEntryPoint
- Returns ApiResponse with ErrorCode
- Consistent error format

---

## 📝 Testing Guide

### 1. Register User

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "fullName": "John Doe"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123"
  }'
```

Response: `{"token": "eyJhbGciOiJIUzUxMiJ9...", "authenticated": true}`

### 3. Use Token (Copy from response)

```bash
curl -X GET http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### 4. Try Admin Endpoint (Will fail with USER role)

```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer {token}"
```

Response: `{"code": 1008, "message": "Bạn không có quyền truy cập"}`

---

## 🔧 Code Architecture

```
configuration/
├── SecurityConfig.java              # Main security configuration
├── CustomJwtDecoder.java            # JWT token decoder
├── JwtAuthenticationEntryPoint.java # Error handling
└── ApplicationConfig.java           # PasswordEncoder bean

service/
├── AuthenticationService.java       # JWT generation + verification
├── UserService.java                 # User management
└── [Others...]

controller/
├── AuthenticationController.java    # Login endpoints
└── UserController.java              # @PreAuthorize examples

dto/
├── request/AuthenticationRequest.java
├── response/AuthenticationResponse.java
├── request/IntrospectRequest.java
└── response/IntrospectResponse.java
```

---

## ⚙️ How It Works

### 1. Token Generation (Login)

```
User submits credentials
    ↓
AuthenticationService.authenticate()
    ↓
Verify password (BCrypt)
    ↓
Create JWTClaimsSet (subject=userId, scope=role)
    ↓
Sign with HS512 algorithm
    ↓
Return token string
```

### 2. Token Verification (Protected Request)

```
Client sends: Authorization: Bearer {token}
    ↓
Spring extracts token from header
    ↓
CustomJwtDecoder.decode(token)
    ↓
Parse + Verify signature (HS512)
    ↓
Check expiration
    ↓
JwtAuthenticationConverter extracts authorities
    ↓
Build Authentication object
    ↓
@PreAuthorize checks role
    ↓
Allow or Deny request
```

---

## 📚 Database Structure

User entity được update để work với authentication:

- `password` field: Stores BCrypt hash
- `role` field: UserRole enum (USER, ADMIN)
- `createdAt`, `updatedAt`: Auto-managed timestamps

Password NEVER stored in plain text!

---

## ⚠️ Important Notes

1. **Giữ secret key bí mật**
   - `jwt.signer-key` không được public
   - Sử dụng environment variables trên production

2. **Token expiration**
   - Token hết hạn sau 20 giờ
   - User phải đăng nhập lại
   - Có thể thêm refresh token sau

3. **Always use HTTPS**
   - Token được gửi trong Authorization header
   - Cần HTTPS để bảo vệ token

4. **Role assignment**
   - Default role = USER
   - Admin phải assign role ADMIN thủ công
   - Xem HELP.md để biết thêm

---

## 🎯 Next Features

- [ ] Refresh Token endpoint (tái cấp token mà không cần đăng nhập lại)
- [ ] Logout endpoint (blacklist token)
- [ ] Forgot Password endpoint
- [ ] Change Password endpoint
- [ ] Admin Panel (assign roles)
- [ ] Audit Logging (track authentication)
- [ ] Rate Limiting (prevent brute force)
- [ ] Swagger/OpenAPI Documentation
- [ ] Unit Tests
- [ ] Integration Tests

---

## 📖 Full Documentation

Xem chi tiết tại: **[HELP.md](./HELP.md)**

Nó bao gồm:

- Setup guide
- Curl examples
- Error codes
- Troubleshooting
- Code implementation details

---

**Tạo bởi:** Your Team  
**Ngày:** 2026-08-15  
**Version:** 1.0
