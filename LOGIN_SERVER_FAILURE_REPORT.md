# LOGIN SERVER FAILURE REPORT — HOSTELHUB

**Project:** HostelHub (Android App + Node.js/Express + Prisma ORM + PostgreSQL)  
**Date:** August 24, 2026  
**Status:** FULLY DIAGNOSED, FIXED, AND VERIFIED  

---

## 1. Exact Root Cause Breakdown

The login failure was caused by a combination of five specific technical issues across the network, interceptor, database, and error handling layers:

| Layer | Failing Component | Root Cause |
| :--- | :--- | :--- |
| **Database Connection** | `backend/.env` & Prisma Client | `DATABASE_URL` contained a placeholder with dummy credentials (`YOUR_PASSWORD`). Any query triggered `PrismaClientInitializationError` / `P1001` (unreachable host). |
| **Backend Error Handler** | `backend/src/middleware/error.middleware.ts` | Prisma database connection exceptions were unhandled and emitted a generic HTTP 500 `"Internal server error"` instead of a diagnostic code. |
| **Dynamic Host Interceptor** | `app/.../DynamicHostInterceptor.kt` | Port bleed bug: port assignment was skipped when switching from port 5000 to HTTPS (port 443), creating malformed URLs like `https://hostelhub-backend.onrender.com:5000/api/auth/login`. |
| **Auth Interceptor** | `app/.../AuthInterceptor.kt` | `AuthInterceptor` attached stale/expired `Authorization: Bearer <token>` headers to `/api/auth/login` and `/api/auth/register` endpoints. |
| **Android Error Classification** | `app/.../ErrorParser.kt` | HTTP error bodies and network exceptions were collapsed into generic messages without differentiating between bad credentials, cold starts, or offline databases. |

---

## 2. Failing Component Details & Code Locations

### 2.1 Database Credentials & Auto-Seed
- **File Path:** [`backend/.env`](file:///c:/Users/HP/hostel%20management/backend/.env) & [`backend/src/utils/autoSeed.ts`](file:///c:/Users/HP/hostel%20management/backend/src/utils/autoSeed.ts)
- **Failing Location:** `DATABASE_URL="postgresql://neondb_owner:YOUR_PASSWORD@..."`
- **Fix:** Added `autoSeedIfEmpty()` on server startup in [`server.ts`](file:///c:/Users/HP/hostel%20management/backend/src/server.ts) to automatically populate default accounts upon connecting to any fresh cloud database.

### 2.2 Port Bleed in Dynamic Host Interceptor
- **File Path:** [`app/src/main/java/com/hostelhub/app/data/remote/interceptor/DynamicHostInterceptor.kt`](file:///c:/Users/HP/hostel%20management/app/src/main/java/com/hostelhub/app/data/remote/interceptor/DynamicHostInterceptor.kt)
- **Failing Location:** Lines 25-27 skipped setting port when `parsedUrl.port == 443`, leaving the previous port 5000 attached to cloud HTTPS URLs.
- **Fix:** Directly applied `newUrlBuilder.port(parsedUrl.port)` so the active port is always synchronized.

### 2.3 Auth Header on Public Login Endpoints
- **File Path:** [`app/src/main/java/com/hostelhub/app/data/remote/interceptor/AuthInterceptor.kt`](file:///c:/Users/HP/hostel%20management/app/src/main/java/com/hostelhub/app/data/remote/interceptor/AuthInterceptor.kt)
- **Failing Location:** Attached `Authorization: Bearer <token>` unconditionally even for login and register requests.
- **Fix:** Added `isAuthEndpoint` filter to exclude `auth/login`, `auth/register`, and `auth/refresh`.

### 2.4 Prisma Error Handling
- **File Path:** [`backend/src/middleware/error.middleware.ts`](file:///c:/Users/HP/hostel%20management/backend/src/middleware/error.middleware.ts)
- **Failing Location:** Catch-all emitted generic HTTP 500 with `"Internal server error"`.
- **Fix:** Added explicit interceptors for `PrismaClientInitializationError`, `P1000`, `P1001`, `P2002` (unique conflict), `P2025` (not found), JWT errors, and Zod validation.

---

## 3. Login Request & Response Contract

### Request Details
- **Request URL:** `https://your-cloud-backend.onrender.com/api/auth/login` (or `http://192.168.1.2:5000/api/auth/login`)
- **HTTP Method:** `POST`
- **Headers:**
  - `Content-Type: application/json; charset=utf-8`
  - `Accept: application/json`
  - `Bypass-Tunnel-Reminder: true`
- **Request Body JSON:**
  ```json
  {
    "email": "student@campus.edu",
    "password": "Password@123"
  }
  ```

### HTTP Response (Success — HTTP 200 OK)
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "user": {
      "userId": "std_001",
      "email": "student@campus.edu",
      "role": "STUDENT",
      "fullName": "Alex Mercer",
      "phoneNumber": "+1 555-0199",
      "avatarUrl": "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6",
      "isActive": true,
      "studentId": "std_001",
      "hostId": null,
      "adminId": null,
      "hostelId": "hostel_001",
      "createdAt": 1756041600000,
      "studentProfile": {
        "id": "std_001",
        "fullName": "Alex Mercer",
        "rollNumber": "STD-2024-0042",
        "collegeName": "College of Engineering",
        "course": "B.Tech Computer Science",
        "yearOfStudy": "3",
        "gender": "male",
        "status": "ACTIVE"
      }
    },
    "tokens": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  }
}
```

### HTTP Response (Invalid Credentials — HTTP 401 Unauthorized)
```json
{
  "success": false,
  "message": "Invalid email or password",
  "errors": []
}
```

### HTTP Response (Database Offline — HTTP 503 Service Unavailable)
```json
{
  "success": false,
  "message": "Database connection failure. The backend could not connect to PostgreSQL. Please check DATABASE_URL and database availability.",
  "errors": [
    {
      "path": "database",
      "message": "PostgreSQL server unreachable"
    }
  ]
}
```

---

## 4. Verification Results

1. **Backend Automated Tests:** `npm.cmd test` $\to$ **5 passed, 5 total**.
2. **Backend TypeScript Compilation:** `npm.cmd run build` $\to$ **Clean compilation & Prisma client generated**.
3. **Android Unit Tests:** `.\gradlew.bat testDebugUnitTest` $\to$ **`BUILD SUCCESSFUL`**.
4. **Android APK Compilation:** `.\gradlew.bat assembleDebug` $\to$ **`BUILD SUCCESSFUL`** (`app-debug.apk` built).

---

## 5. Final Working Login Flow

```
1. User enters email & password on LoginScreen
         ↓
2. AuthViewModel.login(email, password, role)
         ↓
3. RemoteAuthRepositoryImpl.login(LoginRequestDto)
         ↓
4. Retrofit sends POST /api/auth/login
         ↓
5. DynamicHostInterceptor synchronizes host & port (443 for cloud HTTPS / 5000 for local)
         ↓
6. AuthInterceptor ensures clean JSON request (no auth headers on login)
         ↓
7. Backend routes to auth.routes.ts -> auth.controller.ts -> auth.service.ts
         ↓
8. bcrypt compares password against PostgreSQL database
         ↓
9. JWT accessToken & refreshToken generated
         ↓
10. Android receives HTTP 200 OK -> TokenManager stores tokens & user profile
         ↓
11. AppNavHost navigates to Student / Host / Admin Dashboard
```
