package com.al4apps.chatshop.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Палитра приложения: белый фон, единственный синий акцент,
 * серые — вспомогательные. Светлая тема (тёмная — вне MVP).
 */
@Immutable
data class AppColors(
    val background: Color = Color(0xFFFFFFFF),
    val surface: Color = Color(0xFFF5F7FA),
    val accent: Color = Color(0xFF1A73E8),
    val accentPressed: Color = Color(0xFF1765CC),
    val onAccent: Color = Color(0xFFFFFFFF),
    val textPrimary: Color = Color(0xFF1A1C1E),
    val textSecondary: Color = Color(0xFF6B7280),
    val divider: Color = Color(0xFFE5E7EB),
    val disabledContainer: Color = Color(0xFFEFF1F4),
    val disabledContent: Color = Color(0xFF9CA3AF),
    val error: Color = Color(0xFFDC362E),
    val onError: Color = Color(0xFFFFFFFF),
)
