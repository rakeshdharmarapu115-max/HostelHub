package com.hostelhub.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hostelhub.app.presentation.theme.ButtonShape
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryTeal
import com.hostelhub.app.presentation.theme.StatusError

enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINED,
    GHOST,
    DANGER
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isFullWidth: Boolean = true
) {
    val buttonModifier = if (isFullWidth) modifier.fillMaxWidth().height(48.dp) else modifier.height(48.dp)

    when (variant) {
        ButtonVariant.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNavy,
                    contentColor = Color.White,
                    disabledContainerColor = PrimaryNavy.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonContent(text, isLoading, leadingIcon, trailingIcon, Color.White)
            }
        }
        ButtonVariant.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryTeal,
                    contentColor = Color.White,
                    disabledContainerColor = SecondaryTeal.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonContent(text, isLoading, leadingIcon, trailingIcon, Color.White)
            }
        }
        ButtonVariant.OUTLINED, ButtonVariant.GHOST -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = ButtonShape,
                border = BorderStroke(1.dp, if (enabled) PrimaryNavy else PrimaryNavy.copy(alpha = 0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryNavy,
                    disabledContentColor = PrimaryNavy.copy(alpha = 0.4f)
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonContent(text, isLoading, leadingIcon, trailingIcon, PrimaryNavy)
            }
        }
        ButtonVariant.DANGER -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusError,
                    contentColor = Color.White,
                    disabledContainerColor = StatusError.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonContent(text, isLoading, leadingIcon, trailingIcon, Color.White)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    contentColor: Color
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = contentColor,
            strokeWidth = 2.dp
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                androidx.compose.material3.Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            }
        }
    }
}
