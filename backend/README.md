# HostelHub Backend Server

Production-style REST API backend for the **HostelHub** Hostel Management Application, built with Node.js, TypeScript, Express, Prisma ORM, JWT authentication, and Zod validation.

---

## 🌍 Worldwide Access (All Networks, Wi-Fi, 4G/5G & All Countries)

You can run the backend and access it from any phone or device anywhere in the world using either of the two methods below:

### Method 1: Instant 1-Click Worldwide HTTPS Tunnel (Recommended for Development)
Run your local server and immediately make it accessible worldwide on any 4G/5G cellular network or any Wi-Fi:

1. **Option A (One-click batch script)**:
   Double-click `start-global-tunnel.bat` in the project root.
2. **Option B (Command Line)**:
   ```bash
   cd backend
   npm run dev:tunnel
   ```
3. A public HTTPS URL will be generated (e.g. `https://quiet-tree-12.loca.lt`).
4. In the **HostelHub Android App**:
   - Tap the **Server Connection** button on the Login screen.
   - Paste the public URL and tap **Save & Apply**.

---

### Method 2: Permanent 24/7 Free Cloud Deployment (Render.com / Railway)
To keep the backend running 24/7 without needing your PC to stay turned on:

#### Deploy to Render (Free Web Service)
1. Push your repository to GitHub / GitLab.
2. Go to [Render.com Dashboard](https://dashboard.render.com/) and click **New +** -> **Web Service**.
3. Select your repository.
4. Render automatically detects `render.yaml` or set:
   - **Environment**: `Node`
   - **Build Command**: `npm install && npx prisma generate && npm run build`
   - **Start Command**: `npm start`
5. Click **Deploy**.
6. Render gives you a permanent worldwide HTTPS URL, e.g.:
   `https://hostelhub-backend-xxxx.onrender.com`
7. In the Android App, set Server URL to: `https://hostelhub-backend-xxxx.onrender.com` (or configure in `build.gradle.kts`).

---

## 🛠️ Architecture & Tech Stack

- **Runtime**: Node.js (v20+ / v22+ / v24+)
- **Language**: TypeScript 5.x
- **Framework**: Express.js
- **Database**: SQLite (`dev.db`) / PostgreSQL
- **ORM**: Prisma ORM
- **Authentication**: JWT Access Token (7d) & Refresh Token (30d), bcrypt password hashing (12 rounds)
- **Validation**: Zod schema middleware
- **API Documentation**: Swagger / OpenAPI 3.0 at `/api/docs`
- **Security**: Helmet, CORS, Trust Proxy, Rate Limiting, RBAC authorization, SQL injection protection via Prisma
- **Global Tunneling**: Integrated Localtunnel & Reverse Proxy support

---

## 🚀 Local Quick Start (LAN / Emulator)

### Local Development:
1. **Install Dependencies**:
   ```bash
   cd backend
   npm install
   ```
2. **Run Prisma Migrations & Seed**:
   ```bash
   npm run prisma:generate
   npm run prisma:seed
   ```
3. **Start Local Server**:
   ```bash
   npm run dev
   ```
   Server will start at `http://localhost:5000`.

---

## 📱 Android Connection Modes

| Mode | Base URL Format | Reachability |
|---|---|---|
| **🌐 Cloud Deployment** | `https://hostelhub-backend.onrender.com/api/` | **Worldwide (All Networks, 4G/5G, Any Wi-Fi)** |
| **🌍 Global Tunnel** | `https://your-subdomain.loca.lt/api/` | **Worldwide (All Networks, 4G/5G, Any Wi-Fi)** |
| **📶 Local Wi-Fi** | `http://192.168.29.196:5000/api/` | Same Wi-Fi Router Only |
| **💻 Android Emulator** | `http://10.0.2.2:5000/api/` | PC Emulator Loopback Only |

---

## 📖 API Documentation (Swagger)

Interactive Swagger UI documentation is available at:
👉 **`http://localhost:5000/api/docs`** (or your public cloud URL + `/api/docs`)

---

## 🔐 Default Development Accounts (Password: `Password@123`)

| Role | Email | Password | Role Description |
|---|---|---|---|
| **Admin** | `admin@campus.edu` | `Password@123` | Campus Housing Administrator |
| **Host** | `warden@greenvalley.edu` | `Password@123` | Green Valley Residencies Warden |
| **Host** | `warden@stjude.edu` | `Password@123` | St. Jude Student Suites Warden |
| **Student** | `student@campus.edu` | `Password@123` | Alex Mercer (Enrolled Student) |
| **Student** | `david.miller@campus.edu` | `Password@123` | David Miller (Enrolled Student) |
| **Student** | `jordan.reed@campus.edu` | `Password@123` | Jordan Reed (Enrolled Student) |

---

## 🧪 Testing

Run automated tests:
```bash
npm test
```
