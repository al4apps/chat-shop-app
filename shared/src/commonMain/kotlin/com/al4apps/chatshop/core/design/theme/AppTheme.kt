package com.al4apps.chatshop.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.manrope_bold
import chatshop.shared.generated.resources.manrope_medium
import chatshop.shared.generated.resources.manrope_regular
import chatshop.shared.generated.resources.manrope_semibold
import org.jetbrains.compose.resources.Font

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
    val manropeFamily = FontFamily(
        Font(Res.font.manrope_regular, FontWeight.Normal),
        Font(Res.font.manrope_medium, FontWeight.Medium),
        Font(Res.font.manrope_semibold, FontWeight.SemiBold),
        Font(Res.font.manrope_bold, FontWeight.Bold),
    )
    val typography = AppTypography(fontFamily = manropeFamily)
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
                primaryContainer = colors.accentSoft,
                onPrimaryContainer = colors.accent,
                secondaryContainer = colors.accentSoft,
                onSecondaryContainer = colors.accent,
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
