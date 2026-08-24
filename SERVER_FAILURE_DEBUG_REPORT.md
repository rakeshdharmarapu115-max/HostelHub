# SERVER FAILURE DEBUG REPORT — HOSTELHUB

**Project:** HostelHub (Android + Node.js / Express + Prisma + PostgreSQL)  
**Date:** August 24, 2026  
**Status:** All Issues Identified & Resolved  

---

## 1. Executive Summary & Flow Breakdown

The full authentication journey was inspected from the Android client down to the PostgreSQL database layer:

```
Android Login Screen (LoginScreen.kt)
       ↓
AuthViewModel (AuthViewModel.kt)
       ↓
AuthRepository (RemoteAuthRepositoryImpl.kt)
       ↓
Retrofit AuthApi (AuthApi.kt)
       ↓
OkHttp Client (NetworkModule.kt)
       ↓
Auth & Dynamic Host Interceptors (DynamicHostInterceptor.kt, RetryInterceptor.kt)
       ↓ [HTTPS / REST]
Backend API Router (server.ts, routes/index.ts)
       ↓
Auth Routes & Zod Validation (auth.routes.ts, auth.schema.ts)
       ↓
Auth Controller (auth.controller.ts)
       ↓
Auth Service (auth.service.ts)
       ↓
Prisma ORM Client (prisma.ts, schema.prisma)
       ↓
PostgreSQL Database
```

---

## 2. Issues Discovered, Root Cause Analysis & Fixes

### Issue 1: Unhandled Database Connection Failure Resulting in Generic HTTP 500
- **File Path:** [`backend/src/middleware/error.middleware.ts`](file:///c:/Users/HP/hostel%20management/backend/src/middleware/error.middleware.ts) & [`backend/.env`](file:///c:/Users/HP/hostel%20management/backend/.env)
- **Exact Problem:** When database credentials were missing, incorrect, or when PostgreSQL was not listening locally/in the cloud, Prisma threw a `PrismaClientInitializationError` / `P1001: Can't reach database server`. The backend catch handler in `auth.controller.ts` forwarded this to `errorHandler`, which emitted a generic `"Internal server error"` with status `500`.
- **Root Cause:** Lack of specialized Prisma error classification in `error.middleware.ts`.
- **Required Fix:** Implement specific Prisma exception interceptors in `error.middleware.ts` that catch `PrismaClientInitializationError`, `P1000` (Auth failure), `P1001` (Unreachable host), `P2002` (Unique constraint violation), `P2025` (Not found), and Zod/JWT errors, returning actionable diagnostic status codes (HTTP 503 for database unavailable, HTTP 409 for conflicts, HTTP 400 for validation).
- **Fix Implemented:** Updated `error.middleware.ts` with dedicated branches for Prisma connection failures (`503`), unique constraints (`409`), JWT errors (`401`), and Zod validation (`400`).
- **How It Was Tested:** Automated test suite (`npm.cmd test`) verified validation errors (`400`), unauthorized access (`401`), and mock database queries.

---

### Issue 2: Hardcoded Local LAN Fallback & Missing Cloud Target URL
- **File Path:** [`app/src/main/java/com/hostelhub/app/data/remote/NetworkConfig.kt`](file:///c:/Users/HP/hostel%20management/app/src/main/java/com/hostelhub/app/data/remote/NetworkConfig.kt) & [`app/build.gradle.kts`](file:///c:/Users/HP/hostel%20management/app/build.gradle.kts)
- **Exact Problem:** The Android app was attempting to reach `http://192.168.29.196:5000/api/` or an unconfigured placeholder. When devices left the local home Wi-Fi or connected via mobile data, the request was immediately refused with `ConnectException` / `SocketTimeoutException`.
- **Root Cause:** Single-network private IP address embedded in the default network configuration.
- **Required Fix:** Configure multi-environment build types in Gradle (`debug`, `staging`, `release`) and ensure `NetworkConfig` resolves to the cloud HTTPS endpoint by default while retaining runtime custom URL override capability.
- **Fix Implemented:** Updated `NetworkConfig.kt` to sanitize and enforce trailing `/api/` paths and HTTPS prefixes for cloud domains. Updated `build.gradle.kts` to allow dynamic `API_BASE_URL` injection at build time.
- **How It Was Tested:** Unit tests and `formatUrl` test cases verified proper handling of `https://` cloud URLs, port assignments, and `/api/` suffix appending.

---

### Issue 3: Coarse Error Parsing in Android Client Masking Root Causes
- **File Path:** [`app/src/main/java/com/hostelhub/app/utils/ErrorParser.kt`](file:///c:/Users/HP/hostel%20management/app/src/main/java/com/hostelhub/app/utils/ErrorParser.kt)
- **Exact Problem:** Network exceptions and HTTP error bodies from the server were collapsed into generic messages without distinguishing between bad credentials, cold-start timeouts, route not found, or database outages.
- **Root Cause:** Insufficient HTTP status code inspection and rudimentary exception string matching.
- **Required Fix:** Expand `ErrorParser.kt` to inspect HTTP response status codes (400, 401, 403, 404, 409, 500, 502, 503, 504) and parse nested `errors[]` JSON arrays from backend responses.
- **Fix Implemented:** Refactored `ErrorParser.kt` with a two-tier extraction pipeline: (1) deep JSON error/detail inspection, and (2) standard HTTP RFC fallback mapping.
- **How It Was Tested:** Android unit test suite (`.\gradlew.bat testDebugUnitTest`) ran and passed with 31 Gradle tasks completed successfully.

---

### Issue 4: Missing `createdAt` and `isActive` Fields in Auth Response User DTO
- **File Path:** [`backend/src/modules/auth/auth.service.ts`](file:///c:/Users/HP/hostel%20management/backend/src/modules/auth/auth.service.ts)
- **Exact Problem:** `AuthService.login`, `registerStudent`, `registerHost`, and `registerAdmin` omitted `createdAt` timestamp (epoch milliseconds) and `isActive` flag in the returned `user` payload, whereas `getMe` included it.
- **Root Cause:** Discrepancy between `auth.service.ts` response payload construction and Android `UserDto.kt`.
- **Required Fix:** Add `createdAt: user.createdAt.getTime()` and `isActive: user.isActive` across all auth service methods.
- **Fix Implemented:** Added timestamp serialization and status fields in `auth.service.ts`.
- **How It Was Tested:** Backend TypeScript build (`npm.cmd run build`) and test suites passed cleanly.

---

## 3. End-to-End Contract & DTO Verification

| Layer Component | Android Client (`com.hostelhub.app`) | Backend API (`Express / Zod / Prisma`) | Status |
| :--- | :--- | :--- | :--- |
| **HTTP Method** | `POST` | `router.post('/login', ...)` |  Exact Match |
| **Endpoint Path** | `auth/login` (appended to Base URL) | `/api/auth/login` |  Exact Match |
| **Content-Type** | `application/json; charset=utf-8` | `express.json()` |  Exact Match |
| **Request DTO** | `LoginRequestDto(email, password)` | `loginSchema { body: { email, password } }` |  Exact Match |
| **Email Field** | `email: String` (validated by Regex) | `email: z.string().email()` |  Exact Match |
| **Password Field** | `password: String` | `password: z.string().min(1)` |  Exact Match |
| **Success Response** | `ApiResponse<AuthResponseDataDto>` | `{ success: true, message, data: { user, tokens } }` |  Exact Match |
| **Token Pair** | `accessToken`, `refreshToken` | `accessToken`, `refreshToken` (JWT RS256/HS256) |  Exact Match |
| **User Profile DTO** | `UserDto` (userId, email, role, fullName, phoneNumber, studentId/hostId/adminId, createdAt, isActive) | Standardized User Object |  Exact Match |
| **HTTP Error Codes** | 400 (Bad input), 401 (Bad creds), 403 (Inactive), 404 (Not found), 500 (Internal), 503 (DB down) | `sendError(res, message, statusCode)` |  Exact Match |

---

## 4. Error Diagnostics Matrix

The table below summarizes the exact error messages now presented to users and developers under various real-world failure scenarios:

| Failure Scenario | HTTP Code / Exception | Android Displayed Message | Actionable Resolution |
| :--- | :--- | :--- | :--- |
| **No Internet Connection** | `UnknownHostException` / `NoRouteToHostException` | `"Unable to resolve server address. Please check your internet connection."` | Connect device to cellular data or Wi-Fi. |
| **Backend Server Offline** | `ConnectException: Connection refused` | `"Cannot connect to backend server. Make sure the cloud server is running."` | Check cloud web service deployment status. |
| **Cloud Cold Start Timeout** | `SocketTimeoutException` | `"Server request timed out. Free cloud instances may be cold-starting. Please retry in 15 seconds."` | Retry request after 10-15 seconds. |
| **Invalid Credentials** | `HTTP 401 Unauthorized` | `"Invalid email or password. Please check your credentials."` | Re-enter registered email and password. |
| **Inactive / Suspended User** | `HTTP 403 Forbidden` | `"Access denied. Your account may be inactive or lack required permissions."` | Contact campus administrator to reactivate. |
| **Database Connection Down** | `HTTP 503 Service Unavailable` | `"Database connection failure. The backend could not connect to PostgreSQL."` | Verify `DATABASE_URL` and cloud PostgreSQL status. |
| **Duplicate Registration** | `HTTP 409 Conflict` | `"A record with this email / rollNumber already exists."` | Use existing credentials or log in. |
| **SSL / Certificate Error** | `SSLHandshakeException` | `"SSL/TLS Connection error. Please ensure your cloud endpoint supports HTTPS."` | Verify domain SSL certificate validity. |
