# DEPLOYMENT ARCHITECTURE ANALYSIS — HOSTELHUB

**Project:** HostelHub (Hostel Management System)  
**Date:** August 24, 2026  
**Auditor:** Full-Stack & DevOps Engineering  

---

## 1. Current Architecture Audit

### 1.1 Diagram: Current Local-Only Topology

```
┌──────────────────────────────────────────────────────────┐
│                   PHYSICAL ANDROID DEVICE                │
│                 (Connected to Home Wi-Fi)                │
└────────────────────────────┬─────────────────────────────┘
                             │ HTTP (Unencrypted)
                             │ http://192.168.29.196:5000/api/
                             ▼
┌──────────────────────────────────────────────────────────┐
│                   DEVELOPER LAPTOP                       │
│  ┌────────────────────────────────────────────────────┐  │
│  │ Express REST API Server (Node.js)                  │  │
│  │ Listening on: 0.0.0.0:5000                         │  │
│  │ (Process dies when laptop sleeps / turns off)      │  │
│  └─────────────────────────┬──────────────────────────┘  │
│                            │ TCP (127.0.0.1:5432)        │
│                            ▼                             │
│  ┌────────────────────────────────────────────────────┐  │
│  │ PostgreSQL 15 / 16 (Local Service / Docker)        │  │
│  │ Status: Not Running Locally / Dummy Cloud URL      │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

---

## 2. Component Inspection Findings

| Component | Configuration File | Observed Setting | Operational State |
| :--- | :--- | :--- | :--- |
| **Backend Environment** | `backend/.env` | `PORT=5000`, `NODE_ENV=development` | Configured for local dev; dynamic cloud port binding required. |
| **Database Connection** | `backend/.env` | `DATABASE_URL="postgresql://neondb_owner:YOUR_PASSWORD@..."` | Contains placeholder credentials; no active connection. |
| **Local DB Config** | `database/database_config.env` | `DB_HOST=127.0.0.1`, `DB_PORT=5432`, `DB_USER=hostel_admin` | Points to localhost; local PostgreSQL service is not installed/running. |
| **Android Base URL** | `app/build.gradle.kts` | `BuildConfig.BASE_URL = "https://hostelhub-backend.onrender.com/api/"` | Defaults to cloud placeholder with LAN override. |
| **Android Runtime Prefs** | `NetworkConfig.kt` | `DEFAULT_LOCAL_URL = "http://192.168.29.196:5000/api/"` | Hardcoded LAN IP (192.168.29.196); fails across external networks. |
| **Docker Engine** | Host Machine | `docker ps` returned `CommandNotFoundException` | Docker is not installed in the Windows developer environment. |
| **Windows Service** | Windows OS | `Get-Service` port 5432 scan | No local PostgreSQL service currently active. |

---

## 3. Local Dependencies Blocking Multi-Network & Multi-User Access

1. **Private Subnet IP Addressing (`192.168.x.x` / `10.0.2.2`)**:
   - RFC 1918 private IP addresses are non-routable over the public Internet. Devices on 4G/5G mobile data, university campus networks, or external Wi-Fi access points cannot resolve or connect to `192.168.29.196`.
2. **Laptop Power & Network Dependency**:
   - Running the Express server on the developer's laptop requires the machine to remain continuously powered on, awake, and connected to the exact same Wi-Fi router. If the laptop sleeps or changes networks, all mobile users immediately experience connection failure.
3. **Local Port & Firewall Constraints**:
   - Windows Defender Firewall blocks inbound traffic on port `5000` unless explicit firewall rules are created, preventing other devices on the same subnet from connecting.
4. **Unencrypted HTTP Cleartext Traffic**:
   - Local IP connections use unencrypted `http://`, which is blocked by default on modern Android versions (API 28+ / Android 9.0+) unless cleartext traffic is explicitly permitted.
5. **Absence of Shared Persistent Cloud Database**:
   - Without a managed cloud database with SSL encryption, user accounts created by one user cannot be reliably stored, backed up, or shared with other mobile clients across different locations.

---

## 4. Target Production Cloud Architecture

```
┌──────────────────────────────────────────────┐     ┌──────────────────────────────────────────────┐
│            STUDENT MOBILE DEVICE             │     │             WARDEN / HOST DEVICE             │
│            (Any Cellular 4G/5G/Wi-Fi)        │     │            (Campus Wi-Fi / External)         │
└──────────────────────┬───────────────────────┘     └──────────────────────┬───────────────────────┘
                       │                                                    │
                       │ HTTPS (TLS 1.3 / Port 443)                         │ HTTPS (TLS 1.3 / Port 443)
                       │ https://hostelhub-backend.onrender.com/api/        │
                       ▼                                                    ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────┐
│                           MANAGED CLOUD BACKEND SERVICE (RENDER / RAILWAY)                        │
│  - Automated CI/CD from GitHub repo (`main` branch)                                               │
│  - Dynamic PORT assignment via `process.env.PORT`                                                 │
│  - Security: Helmet headers, Rate limiting (1000 req/15 min), CORS, JWT HS256/RS256               │
│  - Health endpoint: `GET /health` with real-time PostgreSQL database latency diagnostic           │
└──────────────────────────────────────────────────┬────────────────────────────────────────────────┘
                                                   │
                                                   │ Secure PostgreSQL Wire Protocol
                                                   │ (TLS Enabled: sslmode=require)
                                                   ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────┐
│                        MANAGED POSTGRESQL DATABASE (NEON / SUPABASE / RENDER)                     │
│  - High availability, automated daily backups, connection pooling (PgBouncer)                    │
│  - Relational Schema: Users, Hostels, Rooms, Beds, Allocations, Fees, Payments, Complaints         │
│  - Prisma ORM Data Layer: Zero data loss, migrations, indexes on foreign keys                      │
└───────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Architectural Benefits

| Attribute | Local Development Setup | Target Cloud Architecture |
| :--- | :--- | :--- |
| **Availability** | Only when developer laptop is awake | 24/7/365 global uptime (99.9% SLA) |
| **Network Reach** | Single Wi-Fi subnet (192.168.x.x) | Worldwide (Any 4G, 5G, Wi-Fi, Ethernet) |
| **Security** | Cleartext HTTP, exposed ports | HTTPS TLS 1.3 encryption, secure headers |
| **Data Persistence** | Local disk / fragile | Managed PostgreSQL with automatic backups |
| **Multi-User Isolation** | Prone to local conflicts | Complete role-based security & tenant isolation |
| **Scalability** | Single machine limits | Horizontal & vertical auto-scaling |
