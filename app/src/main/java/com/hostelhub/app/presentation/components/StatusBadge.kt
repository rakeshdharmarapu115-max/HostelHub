package com.hostelhub.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hostelhub.app.presentation.theme.PillShape
import com.hostelhub.app.presentation.theme.StatusError
import com.hostelhub.app.presentation.theme.StatusErrorBg
import com.hostelhub.app.presentation.theme.StatusInfo
import com.hostelhub.app.presentation.theme.StatusInfoBg
import com.hostelhub.app.presentation.theme.StatusSuccess
import com.hostelhub.app.presentation.theme.StatusSuccessBg
import com.hostelhub.app.presentation.theme.StatusWarning
import com.hostelhub.app.presentation.theme.StatusWarningBg

enum class BadgeStatusType {
    SUCCESS, // Paid, Resolved, Present, Available
    WARNING, // Pending, In Progress, Late
    ERROR,   // Overdue, Rejected, Absent, Full
    INFO     // Open, On Leave, Under Maintenance
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    statusType: BadgeStatusType = BadgeStatusType.INFO,
    customBgColor: Color? = null,
    customTextColor: Color? = null
) {
    val (bgColor, textColor) = when (statusType) {
        BadgeStatusType.SUCCESS -> (customBgColor ?: StatusSuccessBg) to (customTextColor ?: StatusSuccess)
        BadgeStatusType.WARNING -> (customBgColor ?: StatusWarningBg) to (customTextColor ?: StatusWarning)
        BadgeStatusType.ERROR -> (customBgColor ?: StatusErrorBg) to (customTextColor ?: StatusError)
        BadgeStatusType.INFO -> (customBgColor ?: StatusInfoBg) to (customTextColor ?: StatusInfo)
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = PillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = textColor
        )
    }
}
