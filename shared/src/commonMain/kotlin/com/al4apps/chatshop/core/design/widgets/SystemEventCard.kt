package com.al4apps.chatshop.core.design.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.al4apps.chatshop.core.design.model.StatusBadgeTone
import com.al4apps.chatshop.core.design.theme.AppTheme

/**
 * Карточка SYSTEM-события заказа: иконка, заголовок, пояснение и опциональное
 * встроенное действие. Компактные события оператора остаются плашками.
 */
@Composable
fun SystemEventCard(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    tone: StatusBadgeTone,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    isActionEnabled: Boolean = true,
    onAction: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    val iconContainer = when (tone) {
        StatusBadgeTone.ACCENT -> AppTheme.colors.accentSoft
        StatusBadgeTone.NEUTRAL -> AppTheme.colors.disabledContainer
        StatusBadgeTone.ERROR -> AppTheme.colors.error.copy(alpha = 0.1f)
    }
    val iconTint = when (tone) {
        StatusBadgeTone.ACCENT -> AppTheme.colors.accent
        StatusBadgeTone.NEUTRAL -> AppTheme.colors.textSecondary
        StatusBadgeTone.ERROR -> AppTheme.colors.error
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = shape)
            .background(color = AppTheme.colors.background, shape = shape)
            .border(width = 1.dp, color = AppTheme.colors.divider, shape = shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = iconContainer, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = AppTheme.typography.body.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = AppTheme.colors.textPrimary,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }
        }
        if (actionLabel != null && onAction != null) {
            AppInlinePrimaryButton(
                text = actionLabel,
                onClick = onAction,
                enabled = isActionEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
