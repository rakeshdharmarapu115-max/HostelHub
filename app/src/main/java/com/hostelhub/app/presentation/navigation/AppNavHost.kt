package com.hostelhub.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.presentation.admin.*
import com.hostelhub.app.presentation.auth.*
import com.hostelhub.app.presentation.components.AdminBottomNavItems
import com.hostelhub.app.presentation.components.AppBottomNavigation
import com.hostelhub.app.presentation.components.HostBottomNavItems
import com.hostelhub.app.presentation.components.StudentBottomNavItems
import com.hostelhub.app.presentation.host.*
import com.hostelhub.app.presentation.student.*
import com.hostelhub.app.presentation.theme.*

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authViewModel: AuthViewModel = hiltViewModel()
    val studentViewModel: StudentViewModel = hiltViewModel()
    val hostViewModel: HostViewModel = hiltViewModel()
    val adminViewModel: AdminViewModel = hiltViewModel()

    val context = androidx.compose.ui.platform.LocalContext.current

    // Global listener for automatic deallocation logout
    LaunchedEffect(Unit) {
        authViewModel.deallocationNoticeFlow.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val isStudentBottomNavRoute = currentRoute in listOf(
        Screen.StudentDashboard.route,
        Screen.MyRoom.route,
        Screen.StudentPayments.route,
        Screen.StudentComplaints.route,
        Screen.StudentProfile.route
    )

    val isHostBottomNavRoute = currentRoute in listOf(
        Screen.HostDashboard.route,
        Screen.HostRooms.route,
        Screen.HostStudents.route,
        Screen.HostComplaints.route,
        Screen.HostFees.route
    )

    val isAdminBottomNavRoute = currentRoute in listOf(
        Screen.AdminDashboard.route,
        Screen.AdminHostels.route,
        Screen.AdminAnalytics.route,
        Screen.AdminUsers.route
    )

    Scaffold(
        bottomBar = {
            when {
                isStudentBottomNavRoute -> {
                    AppBottomNavigation(
                        items = StudentBottomNavItems,
                        currentRoute = currentRoute,
                        activeColor = StudentOnAccentContainer,
                        indicatorColor = StudentAccentContainer,
                        onItemClick = { route ->
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(Screen.StudentDashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
                isHostBottomNavRoute -> {
                    AppBottomNavigation(
                        items = HostBottomNavItems,
                        currentRoute = currentRoute,
                        activeColor = HostOnAccentContainer,
                        indicatorColor = HostAccentContainer,
                        onItemClick = { route ->
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(Screen.HostDashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
                isAdminBottomNavRoute -> {
                    AppBottomNavigation(
                        items = AdminBottomNavItems,
                        currentRoute = currentRoute,
                        activeColor = AdminOnAccentContainer,
                        indicatorColor = AdminAccentContainer,
                        onItemClick = { route ->
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(Screen.AdminDashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                // Auth Flow
                composable(Screen.Splash.route) {
                    SplashScreen(
                        authViewModel = authViewModel,
                        onNavigateToDashboard = { role ->
                            val targetRoute = when (role) {
                                UserRole.STUDENT -> Screen.StudentDashboard.route
                                UserRole.HOST -> Screen.HostDashboard.route
                                UserRole.ADMIN -> Screen.AdminDashboard.route
                            }
                            navController.navigate(targetRoute) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToRoleSelection = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.RoleSelection.route) {
                    RoleSelectionScreen(
                        onRoleSelected = { role ->
                            selectedRole = role
                            when (role) {
                                UserRole.STUDENT -> navController.navigate(Screen.StudentRegister.route)
                                UserRole.HOST -> navController.navigate(Screen.HostRegister.route)
                                UserRole.ADMIN -> navController.navigate(Screen.AdminRegister.route)
                            }
                        },
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHostelDiscovery = {
                            navController.navigate(Screen.HostelDiscovery.route)
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        selectedRole = selectedRole,
                        authViewModel = authViewModel,
                        onLoginSuccess = { role ->
                            val targetRoute = when (role) {
                                UserRole.STUDENT -> Screen.StudentDashboard.route
                                UserRole.HOST -> Screen.HostDashboard.route
                                UserRole.ADMIN -> Screen.AdminDashboard.route
                            }
                            navController.navigate(targetRoute) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { _ ->
                            navController.navigate(Screen.RoleSelection.route)
                        },
                        onNavigateToDiscovery = {
                            navController.navigate(Screen.HostelDiscovery.route)
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.StudentRegister.route) {
                    StudentRegistrationScreen(
                        authViewModel = authViewModel,
                        onRegistrationSuccess = {
                            navController.navigate(Screen.StudentDashboard.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.HostRegister.route) {
                    HostRegistrationScreen(
                        authViewModel = authViewModel,
                        onRegistrationSuccess = {
                            navController.navigate(Screen.HostDashboard.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminRegister.route) {
                    AdminRegistrationScreen(
                        authViewModel = authViewModel,
                        onRegistrationSuccess = {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Student Routes
                composable(Screen.StudentDashboard.route) {
                    StudentDashboardScreen(
                        studentViewModel = studentViewModel,
                        onNavigateToRoom = { navController.navigate(Screen.MyRoom.route) },
                        onNavigateToAttendance = { navController.navigate(Screen.StudentAttendance.route) },
                        onNavigateToMenu = { navController.navigate(Screen.StudentFoodMenu.route) },
                        onNavigateToComplaints = { navController.navigate(Screen.StudentComplaints.route) },
                        onNavigateToPayments = { navController.navigate(Screen.StudentPayments.route) },
                        onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                        onNavigateToHostelDiscovery = { navController.navigate(Screen.HostelDiscovery.route) },
                        onNavigateToProfile = { navController.navigate(Screen.StudentProfile.route) }
                    )
                }

                composable(Screen.MyRoom.route) {
                    MyRoomScreen(
                        studentViewModel = studentViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.StudentAttendance.route) {
                    StudentAttendanceScreen(
                        studentViewModel = studentViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.StudentFoodMenu.route) {
                    StudentFoodMenuScreen(
                        studentViewModel = studentViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.StudentComplaints.route) {
                    StudentComplaintsScreen(
                        studentViewModel = studentViewModel,
                        onNavigateToNewComplaint = { navController.navigate(Screen.NewComplaint.route) },
                        onNavigateToDetails = { id -> navController.navigate(Screen.ComplaintDetails.createRoute(id)) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.NewComplaint.route) {
                    NewComplaintScreen(
                        studentViewModel = studentViewModel,
                        onSubmitSuccess = { navController.popBackStack() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.ComplaintDetails.route,
                    arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
                    ComplaintDetailsScreen(
                        complaintId = complaintId,
                        studentViewModel = studentViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.StudentPayments.route) {
                    StudentFeePaymentScreen(
                        studentViewModel = studentViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.HostelDiscovery.route) {
                    HostelDiscoveryScreen(
                        studentViewModel = studentViewModel,
                        onNavigateToDetails = { id -> navController.navigate(Screen.HostelDetails.createRoute(id)) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.HostelDetails.route,
                    arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                    HostelDetailScreen(
                        hostelId = hostelId,
                        studentViewModel = studentViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.StudentProfile.route) {
                    StudentProfileScreen(
                        studentViewModel = studentViewModel,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onLogout = {
                            authViewModel.logout {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    val currentUser by authViewModel.currentUser.collectAsState()
                    SettingsScreen(
                        currentUser = currentUser,
                        studentViewModel = studentViewModel,
                        onNavigateToProfile = {
                            when (currentUser?.role) {
                                UserRole.HOST -> navController.navigate(Screen.HostProfile.route)
                                UserRole.ADMIN -> navController.navigate(Screen.AdminProfile.route)
                                else -> navController.navigate(Screen.StudentProfile.route)
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Notifications.route) {
                    NotificationsScreen(
                        studentViewModel = studentViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Host Routes
                composable(Screen.HostDashboard.route) {
                    HostDashboardScreen(
                        hostViewModel = hostViewModel,
                        onNavigateToRooms = { navController.navigate(Screen.HostRooms.route) },
                        onNavigateToStudents = { navController.navigate(Screen.HostStudents.route) },
                        onNavigateToComplaints = { navController.navigate(Screen.HostComplaints.route) },
                        onNavigateToFees = { navController.navigate(Screen.HostFees.route) },
                        onNavigateToMenu = { navController.navigate(Screen.HostFoodMenuAdmin.route) },
                        onNavigateToAttendance = { navController.navigate(Screen.HostAttendance.route) },
                        onNavigateToAnnouncements = { navController.navigate(Screen.HostAnnouncements.route) },
                        onNavigateToProfile = { navController.navigate(Screen.HostProfile.route) }
                    )
                }

                composable(Screen.HostRooms.route) {
                    HostRoomManagementScreen(
                        hostViewModel = hostViewModel,
                        onNavigateToRoomDetail = { id -> navController.navigate(Screen.HostRoomDetail.createRoute(id)) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.HostRoomDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                    HostRoomDetailScreen(
                        roomId = roomId,
                        hostViewModel = hostViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.HostStudents.route) {
                    HostStudentManagementScreen(
                        hostViewModel = hostViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.HostFees.route) {
                    HostFeeManagementScreen(
                        hostViewModel = hostViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.HostComplaints.route) {
                    HostComplaintsManagementScreen(
                        hostViewModel = hostViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.HostFoodMenuAdmin.route) {
                    HostFoodMenuAdminScreen(
                        hostViewModel = hostViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.HostAttendance.route) {
                    HostAttendanceScreen(
                        hostViewModel = hostViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.HostAnnouncements.route) {
                    HostAnnouncementsScreen(
                        hostViewModel = hostViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.HostProfile.route) {
                    HostProfileScreen(
                        hostViewModel = hostViewModel,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onLogout = {
                            authViewModel.logout {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Admin Routes
                composable(Screen.AdminDashboard.route) {
                    AdminDashboardScreen(
                        adminViewModel = adminViewModel,
                        onNavigateToHostels = { navController.navigate(Screen.AdminHostels.route) },
                        onNavigateToAnalytics = { navController.navigate(Screen.AdminAnalytics.route) },
                        onNavigateToAnnouncements = { navController.navigate(Screen.AdminAnnouncements.route) },
                        onNavigateToUsers = { navController.navigate(Screen.AdminUsers.route) },
                        onNavigateToProfile = { navController.navigate(Screen.AdminProfile.route) }
                    )
                }

                composable(Screen.AdminHostels.route) {
                    AdminHostelListScreen(
                        adminViewModel = adminViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminAnalytics.route) {
                    AdminAnalyticsScreen(
                        adminViewModel = adminViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminAnnouncements.route) {
                    AdminAnnouncementsScreen(
                        adminViewModel = adminViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminUsers.route) {
                    AdminUserManagementScreen(
                        adminViewModel = adminViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminProfile.route) {
                    AdminProfileScreen(
                        adminViewModel = adminViewModel,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onLogout = {
                            authViewModel.logout {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
