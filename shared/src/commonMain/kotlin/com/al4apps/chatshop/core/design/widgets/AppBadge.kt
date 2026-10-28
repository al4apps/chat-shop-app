package com.al4apps.chatshop.core.design.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.al4apps.chatshop.core.design.model.StatusBadgeTone
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.core.ui.resolve

/**
 * Компактный статусный бейдж: мягкий accent-фон, accent-текст и capsule-форма.
 * Ширина определяется содержимым; длинный текст обрезается.
 */
@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = AppTheme.colors.accentSoft,
    contentColor: Color = AppTheme.colors.accent,
) {
    Box(
        modifier = modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(percent = 50),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = AppTheme.typography.caption.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Статусный бейдж с тоном из UI-модели; цвета выбираются темой. */
@Composable
fun StatusBadge(
    text: UiText,
    tone: StatusBadgeTone,
    modifier: Modifier = Modifier,
) {
    val (container, content) = when (tone) {
        StatusBadgeTone.ACCENT -> AppTheme.colors.accentSoft to AppTheme.colors.accent
        StatusBadgeTone.NEUTRAL -> AppTheme.colors.disabledContainer to AppTheme.colors.textSecondary
        StatusBadgeTone.ERROR -> AppTheme.colors.disabledContainer to AppTheme.colors.error
    }
    AppBadge(
        text = text.resolve(),
        modifier = modifier,
        containerColor = container,
        contentColor = content,
    )
}
