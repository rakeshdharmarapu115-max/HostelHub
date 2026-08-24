package com.hostelhub.app.presentation.navigation

sealed class Screen(val route: String) {
    // Auth & Onboarding
    data object Splash : Screen("splash")
    data object RoleSelection : Screen("role_selection")
    data object Login : Screen("login")
    data object StudentRegister : Screen("student_register")
    data object HostRegister : Screen("host_register")
    data object AdminRegister : Screen("admin_register")

    // Student Flow
    data object StudentDashboard : Screen("student_dashboard")
    data object HostelDiscovery : Screen("student_discovery")
    data object HostelDetails : Screen("student_hostel_details/{hostelId}") {
        fun createRoute(hostelId: String) = "student_hostel_details/$hostelId"
    }
    data object MyRoom : Screen("student_my_room")
    data object StudentAttendance : Screen("student_attendance")
    data object StudentFoodMenu : Screen("student_food_menu")
    data object StudentComplaints : Screen("student_complaints")
    data object NewComplaint : Screen("student_new_complaint")
    data object ComplaintDetails : Screen("student_complaint_details/{complaintId}") {
        fun createRoute(complaintId: String) = "student_complaint_details/$complaintId"
    }
    data object StudentPayments : Screen("student_payments")
    data object StudentProfile : Screen("student_profile")
    data object Settings : Screen("settings")
    data object Notifications : Screen("notifications")

    // Host Flow
    data object HostDashboard : Screen("host_dashboard")
    data object HostRooms : Screen("host_rooms")
    data object HostRoomDetail : Screen("host_room_detail/{roomId}") {
        fun createRoute(roomId: String) = "host_room_detail/$roomId"
    }
    data object HostStudents : Screen("host_students")
    data object HostFees : Screen("host_fees")
    data object HostComplaints : Screen("host_complaints")
    data object HostFoodMenuAdmin : Screen("host_food_menu_admin")
    data object HostAttendance : Screen("host_attendance")
    data object HostAnnouncements : Screen("host_announcements")
    data object HostProfile : Screen("host_profile")

    // Admin Flow
    data object AdminDashboard : Screen("admin_dashboard")
    data object AdminHostels : Screen("admin_hostels")
    data object AdminAnalytics : Screen("admin_analytics")
    data object AdminAnnouncements : Screen("admin_announcements")
    data object AdminUsers : Screen("admin_users")
    data object AdminProfile : Screen("admin_profile")
}
