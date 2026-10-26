package com.al4apps.chatshop.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Палитра приложения v2: белый фон, насыщенный синий акцент,
 * прохладные нейтрали и мягкий тинт для статусов.
 * Светлая тема (тёмная — вне MVP).
 */
@Immutable
data class AppColors(
    val background: Color = Color(0xFFFFFFFF),
    val surface: Color = Color(0xFFF4F7FB),
    val accent: Color = Color(0xFF0B63F6),
    val accentPressed: Color = Color(0xFF084CC4),
    val accentSoft: Color = Color(0xFFE8F1FF),
    val onAccent: Color = Color(0xFFFFFFFF),
    val textPrimary: Color = Color(0xFF101828),
    val textSecondary: Color = Color(0xFF5B6472),
    val divider: Color = Color(0xFFE3E9F2),
    val disabledContainer: Color = Color(0xFFEEF2F7),
    val disabledContent: Color = Color(0xFF98A2B3),
    val error: Color = Color(0xFFE5484D),
    val onError: Color = Color(0xFFFFFFFF),
)
