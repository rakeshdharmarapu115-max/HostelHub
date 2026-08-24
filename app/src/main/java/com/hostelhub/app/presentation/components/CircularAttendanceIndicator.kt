package com.hostelhub.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hostelhub.app.presentation.theme.SecondaryContainer
import com.hostelhub.app.presentation.theme.SecondaryTeal

@Composable
fun CircularAttendanceIndicator(
    percentage: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 8.dp,
    activeColor: Color = SecondaryTeal,
    trackColor: Color = SecondaryContainer.copy(alpha = 0.5f),
    label: String? = null
) {
    val displayPercent = (percentage * 100).toInt().coerceIn(0, 100)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            // Background track
            drawCircle(
                color = trackColor,
                style = stroke
            )
            // Progress arc
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = 360f * percentage.coerceIn(0f, 1f),
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$displayPercent%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
