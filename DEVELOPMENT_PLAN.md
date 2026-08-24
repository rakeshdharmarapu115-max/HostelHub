# Hostel Management System (HostelHub / Hostel Nexus) - Development Plan

## Project Overview
A production-ready Native Android application built with Kotlin, Jetpack Compose, Material 3, Clean Architecture, MVVM, Navigation Compose, and Firebase (Authentication, Firestore, Storage, Cloud Messaging).

---

## 1. Clean Architecture & Project Directory Structure

```text
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/hostelhub/app/
│   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   │   ├── UserDto.kt
│   │   │   │   │   ├── StudentDto.kt
│   │   │   │   │   ├── HostDto.kt
│   │   │   │   │   ├── AdminDto.kt
│   │   │   │   │   ├── HostelDto.kt
│   │   │   │   │   ├── RoomDto.kt
│   │   │   │   │   ├── RoomAssignmentDto.kt
│   │   │   │   │   ├── FeeDto.kt
│   │   │   │   │   ├── PaymentDto.kt
│   │   │   │   │   ├── ComplaintDto.kt
│   │   │   │   │   ├── AttendanceDto.kt
│   │   │   │   │   ├── FoodMenuDto.kt
│   │   │   │   │   ├── AnnouncementDto.kt
│   │   │   │   │   └── NotificationDto.kt
│   │   │   │   ├── mapper/
│   │   │   │   │   ├── UserMapper.kt
│   │   │   │   │   ├── StudentMapper.kt
│   │   │   │   │   ├── HostelMapper.kt
│   │   │   │   │   ├── RoomMapper.kt
│   │   │   │   │   ├── FeePaymentMapper.kt
│   │   │   │   │   ├── ComplaintMapper.kt
│   │   │   │   │   └── AttendanceMenuMapper.kt
│   │   │   │   ├── remote/
│   │   │   │   │   ├── FirebaseAuthSource.kt
│   │   │   │   │   ├── FirestoreDataSource.kt
│   │   │   │   │   └── FirebaseStorageSource.kt
│   │   │   │   └── repository/
│   │   │   │       ├── AuthRepositoryImpl.kt
│   │   │   │       ├── StudentRepositoryImpl.kt
│   │   │   │       ├── HostRepositoryImpl.kt
│   │   │   │       ├── AdminRepositoryImpl.kt
│   │   │   │       ├── HostelRepositoryImpl.kt
│   │   │   │       ├── RoomRepositoryImpl.kt
│   │   │   │       ├── FeePaymentRepositoryImpl.kt
│   │   │   │       ├── ComplaintRepositoryImpl.kt
│   │   │   │       ├── AttendanceRepositoryImpl.kt
│   │   │   │       ├── FoodMenuRepositoryImpl.kt
│   │   │   │       ├── AnnouncementRepositoryImpl.kt
│   │   │   │       └── NotificationRepositoryImpl.kt
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.kt
│   │   │   │   │   ├── UserRole.kt
│   │   │   │   │   ├── Student.kt
│   │   │   │   │   ├── Host.kt
│   │   │   │   │   ├── Admin.kt
│   │   │   │   │   ├── Hostel.kt
│   │   │   │   │   ├── Room.kt
│   │   │   │   │   ├── Bed.kt
│   │   │   │   │   ├── RoomAssignment.kt
│   │   │   │   │   ├── Fee.kt
│   │   │   │   │   ├── Payment.kt
│   │   │   │   │   ├── Complaint.kt
│   │   │   │   │   ├── AttendanceRecord.kt
│   │   │   │   │   ├── FoodMenu.kt
│   │   │   │   │   ├── Announcement.kt
│   │   │   │   │   └── AppNotification.kt
│   │   │   │   ├── repository/
│   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   ├── StudentRepository.kt
│   │   │   │   │   ├── HostRepository.kt
│   │   │   │   │   ├── AdminRepository.kt
│   │   │   │   │   ├── HostelRepository.kt
│   │   │   │   │   ├── RoomRepository.kt
│   │   │   │   │   ├── FeePaymentRepository.kt
│   │   │   │   │   ├── ComplaintRepository.kt
│   │   │   │   │   ├── AttendanceRepository.kt
│   │   │   │   │   ├── FoodMenuRepository.kt
│   │   │   │   │   ├── AnnouncementRepository.kt
│   │   │   │   │   └── NotificationRepository.kt
│   │   │   │   └── usecase/
│   │   │   │       ├── auth/
│   │   │   │       │   ├── LoginUseCase.kt
│   │   │   │       │   ├── RegisterStudentUseCase.kt
│   │   │   │       │   ├── RegisterHostUseCase.kt
│   │   │   │       │   ├── GetCurrentUserUseCase.kt
│   │   │   │       │   └── LogoutUseCase.kt
│   │   │   │       ├── student/
│   │   │   │       │   ├── GetStudentDashboardUseCase.kt
│   │   │   │       │   ├── GetStudentRoomDetailsUseCase.kt
│   │   │   │       │   ├── GetStudentFeesUseCase.kt
│   │   │   │       │   ├── SubmitComplaintUseCase.kt
│   │   │   │       │   ├── GetStudentComplaintsUseCase.kt
│   │   │   │       │   ├── GetStudentAttendanceUseCase.kt
│   │   │   │       │   ├── GetWeeklyMenuUseCase.kt
│   │   │   │       │   └── DiscoverHostelsUseCase.kt
│   │   │   │       ├── host/
│   │   │   │       │   ├── GetHostDashboardStatsUseCase.kt
│   │   │   │       │   ├── ManageRoomsUseCase.kt
│   │   │   │       │   ├── ManageResidentsUseCase.kt
│   │   │   │       │   ├── AssignBedUseCase.kt
│   │   │   │       │   ├── ProcessComplaintUseCase.kt
│   │   │   │       │   ├── RecordOfflinePaymentUseCase.kt
│   │   │   │       │   ├── MarkAttendanceBatchUseCase.kt
│   │   │   │       │   └── UpdateFoodMenuUseCase.kt
│   │   │   │       └── admin/
│   │   │   │           ├── GetCampusOverviewUseCase.kt
│   │   │   │           ├── GetAllHostelsUseCase.kt
│   │   │   │           ├── GetCampusComplaintsAnalyticsUseCase.kt
│   │   │   │           └── BroadcastGlobalNoticeUseCase.kt
│   │   │   ├── presentation/
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── Screen.kt
│   │   │   │   │   ├── AppNavHost.kt
│   │   │   │   │   ├── AuthNavGraph.kt
│   │   │   │   │   ├── StudentNavGraph.kt
│   │   │   │   │   ├── HostNavGraph.kt
│   │   │   │   │   ├── AdminNavGraph.kt
│   │   │   │   │   └── NavigationUtils.kt
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Type.kt
│   │   │   │   │   ├── Shape.kt
│   │   │   │   │   └── Theme.kt
│   │   │   │   ├── components/
│   │   │   │   │   ├── AppButton.kt
│   │   │   │   │   ├── AppTextField.kt
│   │   │   │   │   ├── AppCard.kt
│   │   │   │   │   ├── StatusBadge.kt
│   │   │   │   │   ├── MetricStatCard.kt
│   │   │   │   │   ├── CircularAttendanceIndicator.kt
│   │   │   │   │   ├── FilterChipRow.kt
│   │   │   │   │   ├── EmptyStateView.kt
│   │   │   │   │   ├── LoadingStateView.kt
│   │   │   │   │   ├── ErrorStateView.kt
│   │   │   │   │   ├── AppTopBar.kt
│   │   │   │   │   └── AppBottomNavigation.kt
│   │   │   │   ├── auth/
│   │   │   │   │   ├── splash/
│   │   │   │   │   ├── role_selection/
│   │   │   │   │   ├── login/
│   │   │   │   │   ├── student_register/
│   │   │   │   │   └── host_register/
│   │   │   │   ├── student/
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   ├── room/
│   │   │   │   │   ├── payments/
│   │   │   │   │   ├── complaints/
│   │   │   │   │   ├── attendance/
│   │   │   │   │   ├── food_menu/
│   │   │   │   │   ├── discovery/
│   │   │   │   │   ├── profile/
│   │   │   │   │   └── notifications/
│   │   │   │   ├── host/
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   ├── rooms/
│   │   │   │   │   ├── students/
│   │   │   │   │   ├── complaints/
│   │   │   │   │   ├── fees/
│   │   │   │   │   ├── food_menu/
│   │   │   │   │   ├── attendance/
│   │   │   │   │   └── announcements/
│   │   │   │   ├── admin/
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   ├── hostels/
│   │   │   │   │   ├── analytics/
│   │   │   │   │   ├── announcements/
│   │   │   │   │   └── users/
│   │   │   └── utils/
│   │   │       ├── Resource.kt
│   │   │       ├── UiState.kt
│   │   │       ├── FormValidators.kt
│   │   │       ├── DateFormatter.kt
│   │   │       ├── CurrencyFormatter.kt
│   │   │       └── Constants.kt
│   │   └── res/
│   │       ├── drawable/
│   │       ├── values/
│   │       └── mipmap/
│   └── test/
│   └── androidTest/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 2. Firebase Firestore Schema & Relationships

### Collection: `users`
*Purpose*: Root authentication and profile baseline for all users.
*Document ID*: `userId` (Firebase Auth UID)
```json
{
  "userId": "string (PK, matches auth.uid)",
  "email": "string (e.g. user@campus.edu)",
  "role": "string ('STUDENT' | 'HOST' | 'ADMIN')",
  "fullName": "string",
  "phoneNumber": "string",
  "avatarUrl": "string (nullable)",
  "isActive": "boolean",
  "fcmToken": "string (device push token)",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### Collection: `students`
*Purpose*: Detailed student profiles, academic enrollment, and hostel boarding records.
*Document ID*: `studentId` (matches `userId`)
```json
{
  "studentId": "string (PK, FK -> users.userId)",
  "userId": "string",
  "fullName": "string",
  "rollNumber": "string (e.g. STD-2024-001)",
  "collegeName": "string",
  "course": "string (e.g. B.Tech Computer Science)",
  "yearOfStudy": "string ('1' | '2' | '3' | '4' | 'pg')",
  "gender": "string ('male' | 'female' | 'other')",
  "permanentAddress": "string",
  "emergencyContactName": "string",
  "emergencyContactPhone": "string",
  "hostelId": "string (nullable, FK -> hostels.hostelId)",
  "roomId": "string (nullable, FK -> rooms.roomId)",
  "bedNumber": "string (nullable, e.g. 'Bed-A')",
  "admissionDate": "timestamp (nullable)",
  "status": "string ('ACTIVE' | 'VACATED' | 'PENDING_APPROVAL')",
  "createdAt": "timestamp"
}
```

### Collection: `hosts`
*Purpose*: Hostel owner / property host profile.
*Document ID*: `hostId` (matches `userId`)
```json
{
  "hostId": "string (PK, FK -> users.userId)",
  "userId": "string",
  "fullName": "string",
  "businessName": "string (e.g. St. Jude Residencies)",
  "contactPhone": "string",
  "contactEmail": "string",
  "hostelIds": ["array of string (FK -> hostels.hostelId)"],
  "verifiedStatus": "boolean",
  "createdAt": "timestamp"
}
```

### Collection: `admins`
*Purpose*: Association heads / campus housing administrators.
*Document ID*: `adminId` (matches `userId`)
```json
{
  "adminId": "string (PK, FK -> users.userId)",
  "userId": "string",
  "fullName": "string",
  "associationName": "string (e.g. University Housing Council)",
  "designation": "string",
  "permissions": ["array of string ('ALL', 'ANALYTICS_VIEW', 'POLICY_EDIT', 'HOSTEL_VERIFY')"],
  "contactPhone": "string"
}
```

### Collection: `hostels`
*Purpose*: Hostel properties managed in the system.
*Document ID*: `hostelId`
```json
{
  "hostelId": "string (PK)",
  "hostId": "string (FK -> hosts.hostId)",
  "name": "string (e.g. Green Valley Hostel)",
  "address": "string",
  "city": "string",
  "state": "string",
  "postalCode": "string",
  "geoPoint": "geopoint (latitude, longitude)",
  "description": "string",
  "genderType": "string ('BOYS' | 'GIRLS' | 'COED')",
  "amenities": ["array of string ('Wi-Fi', '24/7 Security', 'Mess', 'Laundry', 'Gym')"],
  "rules": ["array of string ('Curfew 10 PM', 'No loud music after 11 PM')"],
  "images": ["array of string (Storage URLs)"],
  "totalRooms": "number",
  "totalBeds": "number",
  "occupiedBeds": "number",
  "baseMonthlyRent": "number",
  "cautionDeposit": "number",
  "rating": "number (0.0 to 5.0)",
  "ratingCount": "number",
  "contactEmail": "string",
  "contactPhone": "string",
  "createdAt": "timestamp"
}
```

### Collection: `rooms`
*Purpose*: Individual rooms within each hostel.
*Document ID*: `roomId`
```json
{
  "roomId": "string (PK)",
  "hostelId": "string (FK -> hostels.hostelId)",
  "roomNumber": "string (e.g. 'A-204')",
  "floor": "number",
  "block": "string",
  "roomType": "string ('SINGLE' | 'DOUBLE' | 'TRIPLE' | 'DORMITORY')",
  "totalCapacity": "number",
  "occupiedCount": "number",
  "monthlyRent": "number",
  "amenities": ["array of string ('AC', 'Attached Bath', 'Study Desk', 'Balcony')"],
  "beds": [
    {
      "bedId": "string",
      "bedNumber": "string (e.g. 'A')",
      "studentId": "string (nullable, FK -> students.studentId)",
      "isOccupied": "boolean"
    }
  ],
  "status": "string ('AVAILABLE' | 'FULL' | 'MAINTENANCE')",
  "createdAt": "timestamp"
}
```

### Collection: `room_assignments`
*Purpose*: Historical and active room occupancy ledger.
*Document ID*: `assignmentId`
```json
{
  "assignmentId": "string (PK)",
  "hostelId": "string (FK -> hostels.hostelId)",
  "roomId": "string (FK -> rooms.roomId)",
  "studentId": "string (FK -> students.studentId)",
  "bedNumber": "string",
  "startDate": "timestamp",
  "endDate": "timestamp (nullable)",
  "status": "string ('ACTIVE' | 'TRANSFERRED' | 'VACATED')",
  "assignedByHostId": "string (FK -> hosts.hostId)",
  "createdAt": "timestamp"
}
```

### Collection: `fees`
*Purpose*: Recurring and one-time fee invoices generated for students.
*Document ID*: `feeId`
```json
{
  "feeId": "string (PK)",
  "hostelId": "string (FK -> hostels.hostelId)",
  "studentId": "string (FK -> students.studentId)",
  "roomId": "string (FK -> rooms.roomId)",
  "title": "string (e.g. 'October 2026 Hostel Rent & Mess')",
  "feeType": "string ('RENT' | 'MESS' | 'CAUTION_DEPOSIT' | 'ELECTRICITY' | 'FINE')",
  "amount": "number",
  "amountPaid": "number",
  "dueDate": "timestamp",
  "billingMonth": "number (1-12)",
  "billingYear": "number",
  "status": "string ('PAID' | 'PARTIALLY_PAID' | 'PENDING' | 'OVERDUE')",
  "createdAt": "timestamp"
}
```

### Collection: `payments`
*Purpose*: Payment records, transactions, receipts, and offline settlement proofs.
*Document ID*: `paymentId`
```json
{
  "paymentId": "string (PK)",
  "feeId": "string (FK -> fees.feeId)",
  "studentId": "string (FK -> students.studentId)",
  "hostelId": "string (FK -> hostels.hostelId)",
  "amountPaid": "number",
  "paymentMethod": "string ('ONLINE' | 'UPI' | 'CARD' | 'CASH' | 'BANK_TRANSFER')",
  "transactionReference": "string",
  "paymentDate": "timestamp",
  "receiptUrl": "string (Storage URL, nullable)",
  "status": "string ('SUCCESS' | 'PENDING' | 'FAILED')",
  "verifiedByHostId": "string (nullable, FK -> hosts.hostId)",
  "remarks": "string (nullable)",
  "createdAt": "timestamp"
}
```

### Collection: `complaints`
*Purpose*: Maintenance requests, issues, triage status, and resolution timelines.
*Document ID*: `complaintId`
```json
{
  "complaintId": "string (PK)",
  "hostelId": "string (FK -> hostels.hostelId)",
  "studentId": "string (FK -> students.studentId)",
  "studentName": "string",
  "roomNumber": "string",
  "category": "string ('ELECTRICAL' | 'PLUMBING' | 'WIFI' | 'CLEANING' | 'FOOD' | 'FURNITURE' | 'OTHER')",
  "title": "string",
  "description": "string",
  "attachments": ["array of string (Storage image URLs)"],
  "urgency": "string ('LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL')",
  "status": "string ('OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED')",
  "assignedStaffName": "string (nullable)",
  "hostNotes": "string (nullable)",
  "resolutionSummary": "string (nullable)",
  "createdAt": "timestamp",
  "resolvedAt": "timestamp (nullable)"
}
```

### Collection: `attendance`
*Purpose*: Daily attendance logs for hostel residents.
*Document ID*: `attendanceId` (`{hostelId}_{studentId}_{YYYYMMDD}`)
```json
{
  "attendanceId": "string (PK)",
  "hostelId": "string (FK -> hostels.hostelId)",
  "studentId": "string (FK -> students.studentId)",
  "date": "string (format: 'YYYY-MM-DD')",
  "status": "string ('PRESENT' | 'ABSENT' | 'ON_LEAVE' | 'LATE')",
  "checkInTime": "timestamp (nullable)",
  "remarks": "string (nullable)",
  "markedBy": "string ('STUDENT_SELF' | 'HOST_ADMIN')",
  "leaveRequestId": "string (nullable)",
  "createdAt": "timestamp"
}
```

### Collection: `food_menus`
*Purpose*: Weekly scheduled meal plans.
*Document ID*: `menuId` (`{hostelId}_{weekStartDate}`)
```json
{
  "menuId": "string (PK)",
  "hostelId": "string (FK -> hostels.hostelId)",
  "weekStartDate": "string (format: 'YYYY-MM-DD')",
  "schedule": {
    "monday": {
      "breakfast": ["Poha", "Boiled Eggs", "Tea/Coffee"],
      "lunch": ["Rice", "Dal Tadka", "Paneer Butter Masala", "Curd"],
      "snacks": ["Samosa", "Tea"],
      "dinner": ["Roti", "Mixed Veg", "Jeera Rice", "Gulab Jamun"]
    },
    "tuesday": { "breakfast": [], "lunch": [], "snacks": [], "dinner": [] },
    "wednesday": { "breakfast": [], "lunch": [], "snacks": [], "dinner": [] },
    "thursday": { "breakfast": [], "lunch": [], "snacks": [], "dinner": [] },
    "friday": { "breakfast": [], "lunch": [], "snacks": [], "dinner": [] },
    "saturday": { "breakfast": [], "lunch": [], "snacks": [], "dinner": [] },
    "sunday": { "breakfast": [], "lunch": [], "snacks": [], "dinner": [] }
  },
  "specialNotice": "string (nullable)",
  "isPublished": "boolean",
  "updatedAt": "timestamp"
}
```

### Collection: `announcements`
*Purpose*: Host and Association-wide broadcast notices.
*Document ID*: `announcementId`
```json
{
  "announcementId": "string (PK)",
  "hostelId": "string ('GLOBAL_CAMPUS' or FK -> hostels.hostelId)",
  "senderId": "string (FK -> users.userId)",
  "senderRole": "string ('HOST' | 'ADMIN')",
  "senderName": "string",
  "title": "string",
  "message": "string",
  "priority": "string ('NORMAL' | 'IMPORTANT' | 'URGENT')",
  "targetAudience": "string ('ALL' | 'STUDENTS_ONLY' | 'HOSTS_ONLY' | 'SPECIFIC_BLOCK')",
  "attachmentUrls": ["array of string"],
  "createdAt": "timestamp",
  "expiresAt": "timestamp (nullable)"
}
```

### Collection: `notifications`
*Purpose*: User-specific actionable push alerts and event updates.
*Document ID*: `notificationId`
```json
{
  "notificationId": "string (PK)",
  "recipientUserId": "string (FK -> users.userId)",
  "title": "string",
  "body": "string",
  "type": "string ('PAYMENT_DUE' | 'PAYMENT_CONFIRMED' | 'COMPLAINT_UPDATE' | 'ATTENDANCE_ALERT' | 'ANNOUNCEMENT')",
  "relatedEntityId": "string (e.g. feeId, complaintId, announcementId)",
  "isRead": "boolean",
  "createdAt": "timestamp"
}
```

---

## 3. Screen Inventory & Flow by User Role

### A. Authentication & Onboarding
1. **SplashScreen**: App branding with Stitch Indigo `#1A237E` theme, checks auth token and cached role to auto-route.
2. **RoleSelectionScreen**: 3 interactive role cards (Student, Hostel Owner / Host, Association Head) with accent hover/press states and descriptions.
3. **LoginScreen**: Email/Password authentication, role validation guard, forgot password bottom sheet.
4. **StudentRegistrationScreen**: Multi-field form: Name, Student ID, Mobile, Academic Email, College, Course, Year, Gender, Address, Password with live validation.
5. **HostRegistrationScreen**: Property Host onboarding form: Full Name, Business Name, Mobile, Email, Property details.

### B. Student Role Screens
1. **StudentDashboardScreen**:
   - Header with greeting, hostel name & room badge.
   - Quick Stat Cards: Room Info (`A-204`), Pending Fees (`$0.00` / `$450.00`), Attendance (`92% Present`), Today's Food Menu preview.
   - Quick Action buttons: File Complaint, Pay Rent, Leave Application.
   - Recent Announcements horizontal carousel.
2. **HostelDiscoveryScreen**:
   - Search bar with filter chips (Boys/Girls, AC/Non-AC, Price Range).
   - Hostel cards with thumbnail, rating badge, address, amenities, and rent starting price.
3. **HostelDetailScreen**:
   - Image carousel, full description, amenities grid, house rules, host contact details, "Book Room" call-to-action.
4. **MyRoomScreen**:
   - Room card: Room Number, Floor, Block, Room Type.
   - Roommates list with avatar, name, and course.
   - Bed allocation details and room inventory checklist.
5. **StudentAttendanceScreen**:
   - Monthly calendar view with color-coded day markers (Present - Green, Absent - Red, Leave - Amber).
   - Circular progress indicator showing overall attendance percentage.
   - "Apply for Leave" modal with date range and reason.
6. **StudentFoodMenuScreen**:
   - Day selector (Mon - Sun tabs).
   - 4 meal cards (Breakfast, Lunch, Evening Snacks, Dinner) with vegetarian/non-vegetarian indicators.
   - Notice banner for special weekend meals.
7. **StudentComplaintsScreen**:
   - Filter chips: `All`, `Open`, `In Progress`, `Resolved`.
   - List of complaint cards with urgency badge (`High`, `Medium`, `Low`), category icon, and date.
   - Floating Action Button (FAB) to create a new complaint.
8. **NewComplaintScreen**:
   - Category selector dropdown (Plumbing, Electrical, Wi-Fi, Cleaning, Food, Other).
   - Urgency radio selection (Low, Medium, High, Critical).
   - Title & Detailed description inputs.
   - Photo attachment picker.
9. **ComplaintDetailsScreen**:
   - Step-by-step progress timeline (`Submitted` -> `In Review` -> `Technician Assigned` -> `Resolved`).
   - Assigned staff details and host comments.
10. **StudentFeePaymentScreen**:
    - Outstanding Balance card with due date alert.
    - Fee breakdown items (Rent, Mess, Utilities, Caution Deposit).
    - Payment History list with download receipt action.
    - "Pay Now" bottom sheet.
11. **StudentProfileScreen & SettingsScreen**:
    - Profile details, emergency contacts, edit profile, push notification toggles, theme toggle, logout.
12. **NotificationsScreen**:
    - List of unread/read alerts with swipe-to-dismiss.

### C. Hostel Owner / Host Role Screens
1. **HostDashboardScreen**:
   - Metric summary cards: Total Occupancy (e.g. `88% - 44/50 Beds`), Total Revenue Collected vs Outstanding, Active Complaints count, Today's Attendance count.
   - Pending action alerts (Unassigned beds, pending maintenance triage).
   - Recent resident activities.
2. **HostRoomManagementScreen**:
   - Filter by floor/block and status (Available, Occupied, Maintenance).
   - Room grid cards with capacity progress bar (`2/3 Beds Filled`).
   - "Add New Room" FAB and bottom sheet form.
3. **HostRoomDetailScreen**:
   - Room specifications, assigned student cards with bed numbers, room maintenance log.
   - Action to assign a student to a vacant bed.
4. **HostStudentManagementScreen**:
   - Searchable resident directory with filter by room, fee status, and college.
   - Student profile view, contact triggers (Call/SMS/Email), room reassignment, vacate student action.
5. **HostFeeManagementScreen**:
   - Total collections vs Pending dues dashboard widget.
   - Student list with payment status badges (`Paid`, `Pending`, `Overdue`).
   - Action to "Record Offline Payment" (Cash/Bank transfer receipt) and send automated payment reminder.
6. **HostComplaintsManagementScreen**:
   - Triage view for all incoming hostel complaints.
   - Filters by urgency (`Critical`, `High`, etc.) and category.
   - Update complaint status modal: Assign staff name, add progress notes, mark as resolved.
7. **HostFoodMenuAdminScreen**:
   - Weekly menu editor. Select day and edit menu items for Breakfast, Lunch, Snacks, Dinner.
   - "Publish Menu" trigger with instant notification to residents.
8. **HostAttendanceScreen**:
   - Daily attendance roster.
   - Quick "Mark All Present" button with individual toggle for Absentees.
   - Resident leave requests review and approval.
9. **HostAnnouncementsScreen**:
   - Compose announcement dialog (Title, message, priority tag, target floor/all).
   - Broadcast history list.

### D. Association Head / Admin Role Screens
1. **AdminDashboardScreen**:
   - Campus-wide aggregate metrics: Total registered hostels, Total student capacity, Campus occupancy %, Overall complaint resolution rate.
   - Hostel performance ranking table (Occupancy, Safety compliance, Resolution speed).
2. **AdminHostelListScreen**:
   - Directory of all affiliated hostels with host details, room counts, and verification badges.
   - Inspect hostel profile and audit history.
3. **AdminAnalyticsScreen**:
   - Visual charts: Monthly occupancy trends, Category breakdown of student complaints campus-wide, Fee compliance rates.
4. **AdminAnnouncementsScreen**:
   - Publish university/association-wide notices, policies, and emergency alerts.
5. **AdminUserManagementScreen**:
   - Host verification queue (Approve/Reject new property owners).

---

## 4. Reusable UI Components Spec (Stitch Fidelity)

| Component | Description | Properties / States |
|---|---|---|
| `AppButton` | Primary Navy (`#1A237E`) or Secondary Teal (`#00897B`), 12dp rounded corners | Primary, Secondary, Ghost, Danger; `isLoading`, `isEnabled` |
| `AppTextField` | Outlined text input with 8dp rounded corner, label-lg header, outline `#757684` | Default, Focused (Indigo ring), Error (Red border + message), Disabled |
| `AppCard` | White `#FFFFFF` surface card with 16dp rounded corner, subtle 1dp border `#E4E7EC`, soft elevation shadow | Normal, Clickable, Selected |
| `StatusBadge` | Soft tonal pill badge (100dp radius) with 10% opacity background and high-contrast text | `Paid` (Green), `Pending` (Amber), `Overdue`/`Critical` (Red), `Open` (Blue), `Resolved` (Teal) |
| `MetricStatCard` | Dashboard KPI card with circular icon container, large bold metric value, trend subtitle | Icon, Title, Value, Subtitle, Container Color |
| `CircularAttendanceIndicator`| Circular ring progress bar using Secondary Teal `#00897B` | Float percentage (0.0 - 1.0), Center label |
| `FilterChipRow` | Horizontal scrollable row of pill-shaped filter chips | Chips list, selectedChip, onChipSelected |
| `EmptyStateView` | Centered vector icon, headline, supportive text, and CTA button | IconRes, Title, Message, ActionButtonText, onAction |
| `LoadingStateView` | Skeleton shimmer effect mirroring card and list layouts | Item count, Layout type |
| `ErrorStateView` | Error illustration, message, and "Try Again" primary button | ErrorMessage, onRetry |

---

## 5. Form Validation Rules

1. **Student Registration Form**:
   - *Full Name*: Required, minimum 3 characters, alphabetical and spaces only.
   - *Student ID*: Required, format `[A-Z0-9-]{4,15}`.
   - *Mobile Number*: Required, valid E.164 phone format (10-15 digits).
   - *Email Address*: Required, valid academic email pattern (`^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`).
   - *College & Course*: Non-empty selection from defined catalog.
   - *Year of Study*: Non-empty selection (`1st Year`, `2nd Year`, `3rd Year`, `4th Year`, `PG`).
   - *Password*: Minimum 8 characters, at least 1 uppercase letter, 1 number, and 1 special character (`[!@#$%^&*]`).
2. **New Complaint Form**:
   - *Category*: Mandatory single selection.
   - *Urgency*: Mandatory single selection.
   - *Title*: Required, 5 to 100 characters.
   - *Description*: Required, 10 to 500 characters.
   - *Attachments*: Maximum 3 images, JPEG/PNG up to 5MB each.
3. **Room Creation Form**:
   - *Room Number*: Required, unique within hostel.
   - *Floor & Block*: Valid positive integer and non-empty string.
   - *Room Type & Capacity*: Single (1), Double (2), Triple (3), Dormitory (4-10).
   - *Monthly Rent*: Positive numeric value > 0.
4. **Fee Record Form**:
   - *Amount*: Positive numeric value > 0.
   - *Due Date*: Must be current date or future date.
   - *Fee Type & Student*: Valid active student selection.

---

## 6. Development Phases & Implementation Order

### Phase 1: Foundation, Build System & Core Architecture
- Set up Gradle build configuration with Kotlin 2.0+, Jetpack Compose, Material 3, Navigation Compose, Hilt / Koin dependency injection, Coroutines, and Firebase BoM.
- Implement Design System tokens in `theme/`: Colors (`#1A237E`, `#00897B`, `#F5F7F8`, `#FFFFFF`), Typography (Inter scale), Shapes (8dp, 12dp, 16dp, 100dp pill).
- Build core reusable components (`AppButton`, `AppTextField`, `AppCard`, `StatusBadge`, `MetricStatCard`, `EmptyStateView`, `LoadingStateView`, `ErrorStateView`).
- Setup Firebase configurations (`google-services.json`, Firebase Auth, Firestore offline settings).

### Phase 2: Domain Layer & Repository Abstractions
- Create domain entities (`User`, `Student`, `Host`, `Admin`, `Hostel`, `Room`, `Fee`, `Payment`, `Complaint`, `Attendance`, `FoodMenu`, `Announcement`).
- Implement repository interfaces and UseCase business interactors.
- Write DTOs, Mappers, and remote Firebase data source implementations.

### Phase 3: Authentication & Role Selection Flow
- Implement `SplashScreen` with persistent auth session check.
- Build `RoleSelectionScreen` with Stitch interactive cards.
- Implement `LoginScreen`, `StudentRegistrationScreen`, and `HostRegistrationScreen`.
- Wire up `AuthNavGraph` with role-based routing to Student, Host, or Admin destinations.

### Phase 4: Student Experience Implementation
- Implement `StudentDashboardScreen` with live stats and quick actions.
- Build `MyRoomScreen` (roommate cards, inventory checklist).
- Build `StudentAttendanceScreen` with circular progress and leave application.
- Build `StudentFoodMenuScreen` with day tabs and meal cards.
- Implement `StudentComplaintsScreen`, `NewComplaintScreen`, and `ComplaintDetailsScreen` with live timeline.
- Implement `StudentFeePaymentScreen` with invoice breakdown and payment receipt history.
- Implement `HostelDiscoveryScreen` and `HostelDetailScreen`.
- Implement `StudentProfileScreen` and `NotificationsScreen`.

### Phase 5: Hostel Owner / Host Experience Implementation
- Implement `HostDashboardScreen` with aggregate occupancy, revenue, and triage alerts.
- Build `HostRoomManagementScreen`, `HostRoomDetailScreen`, and `AddRoomDialog`.
- Build `HostStudentManagementScreen` with resident directory and room assignment.
- Build `HostFeeManagementScreen` with offline payment recording and payment reminders.
- Build `HostComplaintsManagementScreen` with staff assignment and status workflow.
- Build `HostFoodMenuAdminScreen` with weekly menu editor.
- Build `HostAttendanceScreen` with batch marking and leave approval.
- Build `HostAnnouncementsScreen` with notification broadcast.

### Phase 6: Association Head / Admin Experience Implementation
- Implement `AdminDashboardScreen` with campus-wide analytics and hostel rankings.
- Build `AdminHostelListScreen` for compliance auditing.
- Build `AdminAnalyticsScreen` with visual metric charts.
- Build `AdminAnnouncementsScreen` for global policy distribution.
- Build `AdminUserManagementScreen` for host approval.

### Phase 7: Polish, Offline Resilience, Push Notifications & Verification
- Integrate Firebase Cloud Messaging (FCM) for push notifications (fees, complaints, menu updates).
- Add Firestore offline persistence and optimistic UI updates.
- Conduct UI fidelity verification against Stitch screens.
- Run unit tests for ViewModels, UseCases, and Mappers.

---

## 7. Risks & Missing Requirements Analysis

1. **Payment Gateway Integration**:
   - *Risk*: Stitch design shows fee settlement, but actual online payments require third-party SDKs (e.g. Razorpay, Stripe) with webhook verification.
   - *Mitigation*: Architecture defines a clean `PaymentGatewayService` interface with mock implementation for development and ready drop-in for production SDKs.
2. **Attendance Verification Mechanism**:
   - *Risk*: Student self-check-in can lead to proxy attendance if not restricted by geolocation or QR code scanning.
   - *Mitigation*: Schema supports `markedBy` (`STUDENT_SELF` vs `HOST_ADMIN`) and can integrate geofencing (Hostel `geoPoint` verification) or QR code generation in Phase 7.
3. **Role Elevation & Host Verification Security**:
   - *Risk*: Anyone registering as Host could gain access to create fake hostels.
   - *Mitigation*: Added `verifiedStatus: Boolean` in Host entity. Host accounts require Admin approval before hostels become discoverable to students.
4. **Offline Sync Conflicts**:
   - *Risk*: Multiple hosts or offline updates modifying room capacity simultaneously.
   - *Mitigation*: Firestore Transactions used for all bed allocations and room assignment modifications to guarantee consistency.
