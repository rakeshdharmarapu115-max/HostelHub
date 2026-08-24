package com.hostelhub.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hostelhub.app.presentation.theme.BorderSubtle
import com.hostelhub.app.presentation.theme.PrimaryContainer
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SurfaceWhite

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val StudentBottomNavItems = listOf(
    BottomNavItem("student_dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("student_my_room", "My Room", Icons.Filled.MeetingRoom, Icons.Outlined.MeetingRoom),
    BottomNavItem("student_payments", "Payments", Icons.Filled.Payment, Icons.Outlined.Payment),
    BottomNavItem("student_complaints", "Complaints", Icons.Filled.Assignment, Icons.Outlined.Assignment),
    BottomNavItem("student_profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

val HostBottomNavItems = listOf(
    BottomNavItem("host_dashboard", "Dashboard", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("host_rooms", "Rooms", Icons.Filled.MeetingRoom, Icons.Outlined.MeetingRoom),
    BottomNavItem("host_students", "Students", Icons.Filled.Group, Icons.Outlined.Group),
    BottomNavItem("host_complaints", "Complaints", Icons.Filled.Assignment, Icons.Outlined.Assignment),
    BottomNavItem("host_fees", "Fees", Icons.Filled.Payment, Icons.Outlined.Payment)
)

val AdminBottomNavItems = listOf(
    BottomNavItem("admin_dashboard", "Overview", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("admin_hostels", "Hostels", Icons.Filled.Business, Icons.Outlined.Business),
    BottomNavItem("admin_analytics", "Analytics", Icons.Filled.Assignment, Icons.Outlined.Assignment),
    BottomNavItem("admin_users", "Users", Icons.Filled.Group, Icons.Outlined.Group)
)

@Composable
fun AppBottomNavigation(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = PrimaryNavy,
    indicatorColor: Color = PrimaryContainer
) {
    NavigationBar(
        modifier = modifier,
        containerColor = SurfaceWhite,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = indicatorColor
                )
            )
        }
    }
}
