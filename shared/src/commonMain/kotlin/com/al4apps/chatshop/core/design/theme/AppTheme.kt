package com.al4apps.chatshop.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf { AppColors() }
val LocalAppTypography = staticCompositionLocalOf { AppTypography() }
val LocalAppShapes = staticCompositionLocalOf { AppShapes() }

/** Точка доступа к дизайн-системе: `AppTheme.colors`, `AppTheme.typography`, `AppTheme.shapes`. */
object AppTheme {

    val colors: AppColors
        @Composable get() = LocalAppColors.current

    val typography: AppTypography
        @Composable get() = LocalAppTypography.current

    val shapes: AppShapes
        @Composable get() = LocalAppShapes.current
}

/**
 * Тема приложения. Оборачивает [MaterialTheme], чтобы стандартные M3-компоненты
 * (курсоры, ripple, выделение) соответствовали палитре.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = AppColors()
    val typography = AppTypography()
    val shapes = AppShapes()

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
        LocalAppShapes provides shapes,
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = colors.accent,
                onPrimary = colors.onAccent,
                background = colors.background,
                onBackground = colors.textPrimary,
                surface = colors.background,
                onSurface = colors.textPrimary,
                surfaceVariant = colors.surface,
                onSurfaceVariant = colors.textSecondary,
                outline = colors.divider,
                error = colors.error,
                onError = colors.onError,
            ),
            content = content,
        )
    }
}
