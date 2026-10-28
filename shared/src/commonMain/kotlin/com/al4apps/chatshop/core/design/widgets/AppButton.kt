package com.al4apps.chatshop.core.design.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.al4apps.chatshop.core.design.model.ButtonUiState
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.ui.resolve

private val ButtonMinHeight = 52.dp
private val ButtonContentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)

/** Основная кнопка: синяя заливка, белый текст. Одно основное действие на экран. */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonMinHeight),
        enabled = enabled,
        shape = AppTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.accent,
            contentColor = AppTheme.colors.onAccent,
            disabledContainerColor = AppTheme.colors.disabledContainer,
            disabledContentColor = AppTheme.colors.disabledContent,
        ),
        contentPadding = ButtonContentPadding,
    ) {
        Text(text = text, style = AppTheme.typography.button)
    }
}

@Composable
fun AppPrimaryButton(
    state: ButtonUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppPrimaryButton(
        text = state.text.resolve(),
        onClick = onClick,
        modifier = modifier,
        enabled = state.enabled,
    )
}

/** Встроенное действие в карточке: компактная основная кнопка высотой 44. */
@Composable
fun AppInlinePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = AppTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.accent,
            contentColor = AppTheme.colors.onAccent,
            disabledContainerColor = AppTheme.colors.disabledContainer,
            disabledContentColor = AppTheme.colors.disabledContent,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Text(
            text = text,
            style = AppTheme.typography.button.copy(fontSize = 14.sp, lineHeight = 18.sp),
        )
    }
}

/** Второстепенная кнопка: контурная, синий текст. */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonMinHeight),
        enabled = enabled,
        shape = AppTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) AppTheme.colors.accent else AppTheme.colors.divider,
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppTheme.colors.accent,
            disabledContentColor = AppTheme.colors.disabledContent,
        ),
        contentPadding = ButtonContentPadding,
    ) {
        Text(text = text, style = AppTheme.typography.button)
    }
}

@Composable
fun AppSecondaryButton(
    state: ButtonUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppSecondaryButton(
        text = state.text.resolve(),
        onClick = onClick,
        modifier = modifier,
        enabled = state.enabled,
    )
}

/** Текстовая кнопка: без фона и рамки, синий текст. */
@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = AppTheme.shapes.medium,
        colors = ButtonDefaults.textButtonColors(
            contentColor = AppTheme.colors.accent,
            disabledContentColor = AppTheme.colors.disabledContent,
        ),
    ) {
        Text(text = text, style = AppTheme.typography.button)
    }
}

@Composable
fun AppTextButton(
    state: ButtonUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTextButton(
        text = state.text.resolve(),
        onClick = onClick,
        modifier = modifier,
        enabled = state.enabled,
    )
}
