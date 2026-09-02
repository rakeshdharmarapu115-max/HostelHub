# ☁️ HOSTELHUB CLOUD SERVICES SETUP & INTEGRATION GUIDE

This comprehensive guide details how to connect the **HostelHub Full-Stack Platform** to 100% free-tier cloud services.

```
┌───────────────────────────────────────────────────────────────────┐
│                       HostelHub Android App                       │
│  (Retrofit REST Client • Coil Cloud CDN • FCM Push Receiver)      │
└─────────────────┬───────────────────────────────┬─────────────────┘
                  │ HTTPS                         │ Cloud Assets
                  ▼                               ▼
┌───────────────────────────────────┐   ┌───────────────────────────┐
│   Cloud Backend API (Render)      │   │  Cloudinary / AWS S3      │
│  • Express REST API               │   │  • Hostel Photos          │
│  • Multi-Stage Docker Container   │   │  • Payment Receipts       │
│  • JWT Authentication             │   │  • Profile Avatars        │
└──────┬────────────┬─────────────┬─┘   └───────────────────────────┘
       │            │             │
       ▼            ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌───────────────────────────────────┐
│ Cloud DB    │ │ FCM Cloud   │ │ Cloud Email (Resend/SMTP)         │
│ (Neon/      │ │ Messaging   │ │ • Fee Receipts                    │
│ Supabase/   │ │ • Push      │ │ • Leave Approvals                 │
│ PostgreSQL) │ │   Alerts    │ │ • Password Resets                 │
└─────────────┘ └─────────────┘ └───────────────────────────────────┘
```

---

## 🚀 Cloud Providers Summary (Free Tier)

| Service | Provider | Free Tier Benefits | Setup Time |
| :--- | :--- | :--- | :--- |
| **Backend Web Hosting** | [Render.com](https://render.com) | Free Web Service with automatic SSL & continuous Git deployment | 3 mins |
| **Managed Cloud Database** | [Neon.tech](https://neon.tech) or [Supabase](https://supabase.com) | Serverless PostgreSQL with auto-scaling & backups | 2 mins |
| **Cloud Media & CDN** | [Cloudinary](https://cloudinary.com) | 25GB free storage, auto-WebP compression, responsive crops | 2 mins |
| **Cloud Push Alerts** | [Firebase (FCM)](https://firebase.google.com) | Unlimited free push notifications to Android devices | 5 mins |
| **Cloud Email** | [Resend](https://resend.com) / [SendGrid](https://sendgrid.com) | 3,000 free transactional emails / month | 2 mins |

---

## 🛠️ Step 1: Managed Cloud PostgreSQL Database

### Option A: Neon.tech (Recommended - Instant)
1. Sign up at [neon.tech](https://neon.tech) (no credit card required).
2. Click **Create Project** -> Name it `hostelhub-db`.
3. Copy the **Connection String** (PostgreSQL URI):
   ```env
   DATABASE_URL="postgresql://neondb_owner:YOUR_PASS@ep-cool-lake-123456.us-east-2.aws.neon.tech/neondb?sslmode=require"
   ```
4. Push your schema and seed initial data:
   ```bash
   cd backend
   npx prisma db push
   npm run prisma:seed
   ```

### Option B: Supabase
1. Sign up at [supabase.com](https://supabase.com).
2. Create project `hostelhub`.
3. Go to **Settings** -> **Database** -> Copy **URI (Transaction Pooler / Direct)**.

---

## 🌐 Step 2: Deploy Backend to Render (1-Click Blueprint)

The repository includes a ready-to-deploy [`render.yaml`](file:///c:/Users/HP/hostel%20management/backend/render.yaml) blueprint.

1. Push your repository to GitHub:
   ```bash
   git add .
   git commit -m "Configure cloud services"
   git push origin main
   ```
2. Log in to [Render.com](https://render.com).
3. Click **New +** -> **Blueprint**.
4. Select your GitHub repository.
5. Render will automatically detect `render.yaml` and provision:
   - **`hostelhub-backend`**: Node.js web service.
   - **`hostelhub-db`**: Free PostgreSQL database (if using Render DB).
6. Click **Apply**.
7. Once deployed, Render will provide a public URL like:
   `https://hostelhub-yp73.onrender.com`

---

## 📸 Step 3: Cloud Media Storage (Cloudinary)

To enable cloud storage for hostel gallery photos, profile avatars, and payment receipts:

1. Sign up at [cloudinary.com](https://cloudinary.com).
2. From the Cloudinary Dashboard, copy:
   - **Cloud Name**
   - **API Key**
   - **API Secret**
3. In your Render / Railway environment variables, add:
   ```env
   STORAGE_DRIVER=cloudinary
   CLOUDINARY_CLOUD_NAME=your_cloud_name
   CLOUDINARY_API_KEY=your_api_key
   CLOUDINARY_API_SECRET=your_api_secret
   CLOUDINARY_FOLDER=hostelhub_prod
   ```

### Upload Endpoints Available:
- `POST /api/storage/upload` (General file/document upload)
- `POST /api/storage/avatar` (Auto face-cropped square avatar)
- `POST /api/storage/receipt` (Payment proof slip upload)
- `POST /api/storage/hostel-images` (Batch hostel room gallery images)
- `GET /api/storage/status` (Healthcheck & cloud provider status)

---

## 🔔 Step 4: Firebase Cloud Messaging (FCM Push Alerts)

1. Open [Firebase Console](https://console.firebase.google.com).
2. Create project `HostelHub`.
3. Go to **Project Settings** -> **Service accounts** -> Click **Generate new private key**.
4. Set the following environment variables on Render:
   ```env
   FIREBASE_PROJECT_ID=hostelhub-prod
   FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxx@hostelhub-prod.iam.gserviceaccount.com
   FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
   ```

---

## 📱 Step 5: Connect Android App to Cloud

1. Open the **HostelHub Android App**.
2. Tap the **Server IP / Network** icon at the top-right of the Login screen (or go to **Settings** -> **Network Configuration**).
3. Paste your public Render URL (e.g. `https://hostelhub-yp73.onrender.com/api/`).
4. Tap **Save & Apply**.
5. The Android client will instantly communicate with your live cloud backend and database from any Wi-Fi, 4G, or 5G connection globally!
