# HostelHub — Comprehensive Backend Migration & Architectural Analysis

## 1. Current Architecture Overview

HostelHub is an Android application for managing student hostels. Its current layers are:
- **UI (Jetpack Compose)**:
  - `presentation/auth/`: Splash, RoleSelection, Login, StudentRegistration, HostRegistration
  - `presentation/student/`: Dashboard, MyRoom, Complaints, ComplaintDetails, NewComplaint, FeePayments, Attendance, FoodMenu, HostelDiscovery, HostelDetails, Notifications, Profile, Settings
  - `presentation/host/`: Dashboard, RoomManagement, RoomDetail, StudentManagement, ComplaintsManagement, FeeManagement, FoodMenuAdmin, Attendance, Announcements, Profile
  - `presentation/admin/`: Dashboard, HostelList, UserManagement, Announcements, Analytics, Profile
  - `presentation/navigation/`: `AppNavHost` and `Screen` routes
  - `presentation/components/`: Reusable Compose components (buttons, text fields, cards, bottom nav bars)
- **State Management (ViewModels)**:
  - `AuthViewModel`: Manages authentication state (`loginState`, `registerState`, `currentUser`).
  - `StudentViewModel`: Holds student data (`studentProfile`, `dashboardStats`, `roomDetails`, `fees`, `payments`, `complaints`, `attendance`, `foodMenu`, `hostels`, `notifications`).
  - `HostViewModel`: Holds host data (`dashboardStats`, `hostelInfo`, `rooms`, `residents`, `fees`, `complaints`, `todayAttendance`, `announcements`, `foodMenu`).
  - `AdminViewModel`: Holds admin data (`dashboardStats`, `hostels`, `users`, `announcements`, `complaints`, `fees`).
- **Domain Layer**:
  - Domain models: `User`, `UserRole`, `Student`, `Host`, `Admin`, `Hostel`, `Room`, `Bed`, `Fee`, `Payment`, `Complaint`, `AttendanceRecord`, `FoodMenu`, `Announcement`, `AppNotification`, `DashboardStats`.
  - Repository interfaces in `Repositories.kt`: `AuthRepository`, `StudentRepository`, `HostelRepository`, `RoomRepository`, `FeePaymentRepository`, `ComplaintRepository`, `AttendanceRepository`, `FoodMenuRepository`, `AnnouncementRepository`, `NotificationRepository`.
- **Local Data Layer**:
  - `HostelDatabaseHelper.kt`: SQLite database helper (`hostelhub.db`, version 1) that creates 24 SQLite tables and seeds initial demo records.
  - `DatabaseDaos.kt`: Raw SQL query implementations using Android's `SQLiteDatabase`.
  - `DatabaseRepositories.kt`: Implementations of domain repository interfaces wrapping `DatabaseDaos`.
  - `AppModule.kt`: Dagger Hilt module binding `Database...RepositoryImpl` to domain interfaces.

---

## 2. Detailed Audit of Mock, Demo, Hardcoded & Fallback Data

| Location | Hardcoded Value / Demo Pattern | Issue Description | Mitigation Plan |
|---|---|---|---|
| `DatabaseRepositories.kt` (lines 25–26) | `daos.getUserById("std_001")` in `DatabaseAuthRepositoryImpl.init` | Forces initial user session to always be student `std_001` upon app start. | Replace with `TokenManager.getCurrentUser()` or null flow from `RemoteAuthRepositoryImpl`. |
| `DatabaseRepositories.kt` (lines 33–45) | Fallback to `"std_001"`, `"host_001"`, `"admin_001"` or synthetic `User(fullName = "Alex Mercer" / "Robert Vance" / "Dean Henderson")` | When login fails or email is not found, it silently succeeds with fake users. | Remove all fallback branches. Return genuine 401/404 `Resource.Error("Invalid credentials")`. |
| `StudentViewModel.kt` (line 30) | `_currentStudentId = MutableStateFlow("std_001")` | Initializes student view model to fixed demo student `std_001`. | Initialize dynamically from `AuthRepository.getCurrentUser()` session. |
| `StudentViewModel.kt` (lines 75, 80) | `loadRoom("room_204")`, `loadFoodMenu("hostel_001")` | Hardcodes specific room and hostel during student data load. | Derive `roomId` and `hostelId` dynamically from student profile. |
| `StudentViewModel.kt` (lines 215–218) | `hostelId = "hostel_001"`, `studentName = "Alex Mercer"`, `roomNumber = "A-204"` in `submitComplaint` | Hardcodes complaint metadata to demo student. | Send complaint with student session token; backend resolves student ID, name, room, and hostel automatically. |
| `StudentViewModel.kt` (line 243) | `hostelId = "hostel_001"` in `payFee` | Hardcodes hostel ID for payment. | Fetch fee details from backend; fee already knows its associated `hostelId`. |
| `StudentViewModel.kt` (lines 262–265) | `hostelId = "hostel_001"`, `studentName = "Alex Mercer"`, `roomNumber = "A-204"` in `markSelfAttendance` | Hardcodes attendance metadata. | Backend derives student name, room, and hostel from authenticated JWT context. |
| `HostViewModel.kt` (line 30) | `_currentHostelId = MutableStateFlow("hostel_001")` | Initializes host view model to fixed demo hostel `hostel_001`. | Dynamically load hostel(s) owned by authenticated Host. |
| `AdminViewModel.kt` (lines 147–149) | `senderId = "admin_001"`, `hostelId = "GLOBAL_CAMPUS"` in `broadcastAnnouncement` | Hardcodes admin sender ID. | Backend derives sender ID and admin role from authenticated JWT. |
| `domain/model/DashboardStats.kt` (lines 4–6) | Default parameters `"A-204"`, `"Bed-A"`, `"Green Valley Residencies"` | Default fallback strings if fields are null. | Replace with empty strings or nullable defaults; populated from real API stats. |
| `database/database_config.env` | Contains local development credentials (`DB_PASSWORD=SecurePassword_2026!`, `JWT_SECRET=super_secret_...`) | Potential secret leak if committed to public repositories. | Create `backend/.env.example` with clear placeholders; never commit live production secrets. |

---

## 3. Authentication & Security Problems in Existing App

1. **No Real Password Verification**: The current local database has dummy hashes without cryptographic salts or bcrypt comparison during login.
2. **Missing Token Management**: There are no JWT access tokens or refresh tokens. The app had no HTTP interceptor to securely transmit credentials.
3. **Client-Side Trust**: Roles could be selected on the client and passed to functions without cryptographic server verification.
4. **No Concurrency / Transaction Control**: Bed allocation in SQLite simply performed separate update statements without atomic rollback if a bed was simultaneously assigned to another student.

---

## 4. Database Schema Comparison (SQLite vs. PostgreSQL Prisma)

All 24 relational tables from `database/schema.sql` and the Android SQLite DAO will be properly mapped into PostgreSQL with Prisma ORM:

| Entity | SQLite Table | PostgreSQL Prisma Model | Key Fields & Constraints |
|---|---|---|---|
| Role | `roles` | `Role` (Enum / Table) | `ADMIN`, `HOST`, `STUDENT`, `STAFF` |
| User | `users` | `User` | `id`, `email` (unique), `passwordHash`, `role`, `fullName`, `phoneNumber`, `isActive`, `fcmToken`, timestamps |
| Host | `hosts` | `Host` | `id`, `userId` (unique FK), `businessName`, `contactPhone`, `contactEmail`, `verifiedStatus` |
| Admin | `admins` | `Admin` | `id`, `userId` (unique FK), `associationName`, `designation`, `permissions`, `contactPhone` |
| Student | `students` | `Student` | `id`, `userId` (unique FK), `rollNumber` (unique), `collegeName`, `course`, `yearOfStudy`, `gender`, `hostelId` (FK), `roomId` (FK), `bedNumber`, `status` |
| Hostel | `hostels` | `Hostel` | `id`, `hostId` (FK), `name`, `address`, `city`, `genderType`, `amenities`, `rules`, `images`, `totalRooms`, `totalBeds`, `occupiedBeds`, `baseMonthlyRent`, `cautionDeposit`, `rating` |
| Block | `blocks` | `Block` | `id`, `hostelId` (FK), `blockName`, `totalFloors`, unique(`hostelId`, `blockName`) |
| Floor | `floors` | `Floor` | `id`, `blockId` (FK), `hostelId` (FK), `floorNumber`, `totalRooms` |
| Room | `rooms` | `Room` | `id`, `hostelId` (FK), `blockId` (FK), `floorId` (FK), `roomNumber`, `floor`, `block`, `roomType`, `totalCapacity`, `occupiedCount`, `monthlyRent`, `status` |
| Bed | `beds` | `Bed` | `id`, `roomId` (FK), `bedNumber`, `isOccupied`, unique(`roomId`, `bedNumber`) |
| Room Allocation | `room_allocations` | `RoomAllocation` | `id`, `bedId` (FK), `roomId` (FK), `hostelId` (FK), `studentId` (FK), `allocationDate`, `checkInDate`, `checkOutDate`, `status`, `allocatedBy` (FK) |
| Staff | `staff` | `Staff` | `id`, `userId` (FK), `hostelId` (FK), `fullName`, `roleTitle`, `phone`, `email`, `isAvailable` |
| Fee Type | `fee_types` | `FeeType` | `id`, `hostelId` (FK), `name`, `defaultAmount`, `billingCycle` |
| Fee | `fees` | `Fee` | `id`, `hostelId` (FK), `studentId` (FK), `roomId` (FK), `title`, `feeType`, `amount`, `amountPaid`, `dueDate`, `billingMonth`, `billingYear`, `status` |
| Payment | `payments` | `Payment` | `id`, `feeId` (FK), `studentId` (FK), `hostelId` (FK), `amountPaid`, `paymentMethod`, `transactionReference` (unique), `paymentDate`, `receiptUrl`, `status` |
| Complaint | `complaints` | `Complaint` | `id`, `hostelId` (FK), `studentId` (FK), `studentName`, `roomNumber`, `category`, `title`, `description`, `urgency`, `status`, `assignedStaffName`, `hostNotes`, `resolutionSummary` |
| Maintenance Log | `maintenance_logs` | `MaintenanceLog` | `id`, `complaintId` (FK), `hostelId` (FK), `roomId` (FK), `performedByStaffId` (FK), `issueType`, `actionTaken`, `cost` |
| Leave Request | `leave_requests` | `LeaveRequest` | `id`, `studentId` (FK), `hostelId` (FK), `startDate`, `endDate`, `reason`, `status`, `approvedBy` (FK) |
| Attendance Record | `attendance_records` | `AttendanceRecord` | `id`, `hostelId` (FK), `studentId` (FK), `studentName`, `roomNumber`, `date`, `status`, `checkInTime`, `markedBy`, unique(`studentId`, `date`) |
| Visitor | `visitors` | `Visitor` | `id`, `hostelId` (FK), `studentId` (FK), `visitorName`, `relationship`, `phone`, `purpose`, `checkInTime`, `checkOutTime`, `status` |
| Food Menu | `food_menus` | `FoodMenu` | `id`, `hostelId` (FK), `weekStartDate`, `scheduleJson`, `specialNotice`, `isPublished`, unique(`hostelId`, `weekStartDate`) |
| Announcement | `announcements` | `Announcement` | `id`, `hostelId`, `senderId` (FK), `senderRole`, `senderName`, `title`, `message`, `priority`, `targetAudience`, `attachmentUrls` |
| Notification | `notifications` | `Notification` | `id`, `recipientUserId` (FK), `title`, `body`, `type`, `relatedEntityId`, `isRead` |
| Audit Log | `audit_logs` | `AuditLog` | `id`, `userId` (FK), `action`, `entityType`, `entityId`, `details`, `ipAddress`, `timestamp` |
| Refresh Token | *new* | `RefreshToken` | `id`, `userId` (FK), `token` (unique), `expiresAt`, `revoked`, `createdAt` |

---

## 5. Required Backend API Suite

1. **Authentication (`/api/auth`)**:
   - `POST /api/auth/register/student`
   - `POST /api/auth/register/host`
   - `POST /api/auth/login`
   - `POST /api/auth/refresh`
   - `POST /api/auth/logout`
   - `GET  /api/auth/me`
2. **Users & Admin (`/api/users`)**:
   - `GET /api/users`, `GET /api/users/:id`, `PATCH /api/users/:id`, `PATCH /api/users/:id/status`
3. **Students (`/api/students`)**:
   - `GET /api/students`, `GET /api/students/:id`, `PATCH /api/students/:id`, `DELETE /api/students/:id`, `GET /api/students/hostel/:hostelId`
4. **Hosts (`/api/hosts`)**:
   - `GET /api/hosts`, `GET /api/hosts/:id`, `PATCH /api/hosts/:id/verify`
5. **Hostels (`/api/hostels`)**:
   - `GET /api/hostels`, `POST /api/hostels`, `GET /api/hostels/:id`, `PATCH /api/hostels/:id`, `DELETE /api/hostels/:id`
6. **Rooms & Beds (`/api/rooms`, `/api/beds`)**:
   - `GET /api/hostels/:hostelId/rooms`, `POST /api/hostels/:hostelId/rooms`, `GET /api/rooms/:id`, `PATCH /api/rooms/:id`, `DELETE /api/rooms/:id`
   - `GET /api/rooms/:roomId/beds`, `POST /api/rooms/:roomId/beds`, `PATCH /api/beds/:id`, `DELETE /api/beds/:id`
7. **Allocations (`/api/allocations`)**:
   - `POST /api/allocations` (Atomic transaction for bed assignment)
   - `PATCH /api/allocations/:id/checkout` (Atomic vacate transaction)
   - `GET /api/allocations/student/:studentId`
8. **Attendance (`/api/attendance`)**:
   - `POST /api/attendance`, `POST /api/attendance/batch`, `GET /api/attendance/student/:studentId`, `GET /api/attendance/hostel/:hostelId`
9. **Fees & Payments (`/api/fees`, `/api/payments`)**:
   - `GET /api/fees/student/:studentId`, `GET /api/fees/hostel/:hostelId`, `GET /api/fees`, `POST /api/fees`, `PATCH /api/fees/:id`
   - `GET /api/payments/student/:studentId`, `POST /api/payments`, `GET /api/payments/:id`
10. **Complaints (`/api/complaints`)**:
    - `POST /api/complaints`, `GET /api/complaints/student/:studentId`, `GET /api/complaints/hostel/:hostelId`, `GET /api/complaints`, `GET /api/complaints/:id`, `PATCH /api/complaints/:id`, `DELETE /api/complaints/:id`
11. **Leave Requests (`/api/leave-requests`)**:
    - `POST /api/leave-requests`, `GET /api/leave-requests/student/:studentId`, `GET /api/leave-requests/hostel/:hostelId`, `PATCH /api/leave-requests/:id/status`
12. **Visitors (`/api/visitors`)**:
    - `POST /api/visitors`, `GET /api/visitors/hostel/:hostelId`, `PATCH /api/visitors/:id/checkout`
13. **Food Menu (`/api/food-menu`)**:
    - `GET /api/food-menu`, `POST /api/food-menu`, `PATCH /api/food-menu/:id`
14. **Announcements (`/api/announcements`)**:
    - `GET /api/announcements`, `POST /api/announcements`, `GET /api/announcements/:id`, `DELETE /api/announcements/:id`
15. **Notifications (`/api/notifications`)**:
    - `GET /api/notifications`, `PATCH /api/notifications/:id/read`, `PATCH /api/notifications/read-all`
16. **Dashboards (`/api/dashboard`)**:
    - `GET /api/dashboard/student`, `GET /api/dashboard/host`, `GET /api/dashboard/admin`

---

## 6. Android Integration Plan & Impact Matrix

### Files That Need Modification
1. `gradle/libs.versions.toml`: Add Retrofit, OkHttp, Logging Interceptor, Moshi / Gson converter.
2. `app/build.gradle.kts`: Add networking dependencies.
3. `app/src/main/java/com/hostelhub/app/di/AppModule.kt`: Bind remote repository implementations instead of local database repositories.
4. `app/src/main/java/com/hostelhub/app/presentation/student/StudentViewModel.kt`: Remove `"std_001"` hardcoded default and listen to authenticated user session.
5. `app/src/main/java/com/hostelhub/app/presentation/host/HostViewModel.kt`: Remove `"hostel_001"` hardcoded default and load host's assigned hostel.
6. `app/src/main/java/com/hostelhub/app/presentation/admin/AdminViewModel.kt`: Load dynamic admin user ID from auth context.

### New Android Files To Create
1. `data/remote/api/*`: `AuthApi.kt`, `StudentApi.kt`, `HostelApi.kt`, `RoomApi.kt`, `FeePaymentApi.kt`, `ComplaintApi.kt`, `AttendanceApi.kt`, `FoodMenuApi.kt`, `AnnouncementApi.kt`, `NotificationApi.kt`, `DashboardApi.kt`.
2. `data/remote/dto/*`: Type-safe request and response DTOs matching backend JSON structures.
3. `data/remote/mapper/*`: Extension mappers from DTO to domain model.
4. `data/remote/interceptor/AuthInterceptor.kt` & `TokenAuthenticator.kt`.
5. `data/remote/datasource/TokenManager.kt`.
6. `data/remote/repository/*`: 10 Remote Repository implementations implementing domain interfaces.

### Files That Can Remain Unchanged
- All Jetpack Compose UI Screens (`presentation/student/*`, `presentation/host/*`, `presentation/admin/*`, `presentation/components/*`, `presentation/theme/*`).
- Domain models (`domain/model/*`).
- Repository interfaces (`domain/repository/Repositories.kt`).
- `MainActivity.kt` and `HostelApp.kt`.
- `DatabaseDaos.kt` and `HostelDatabaseHelper.kt` (kept as legacy local database reference).
