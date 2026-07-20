package com.al4apps.chatshop.core.design.widgets

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import com.al4apps.chatshop.core.design.theme.AppTheme

/** Базовое поле ввода: белый фон, синяя рамка в фокусе. */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        textStyle = AppTheme.typography.body,
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.textSecondary,
                )
            }
        },
        supportingText = supportingText?.let {
            {
                Text(text = it, style = AppTheme.typography.caption)
            }
        },
        shape = AppTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppTheme.colors.textPrimary,
            unfocusedTextColor = AppTheme.colors.textPrimary,
            disabledTextColor = AppTheme.colors.disabledContent,
            focusedBorderColor = AppTheme.colors.accent,
            unfocusedBorderColor = AppTheme.colors.divider,
            disabledBorderColor = AppTheme.colors.divider,
            errorBorderColor = AppTheme.colors.error,
            cursorColor = AppTheme.colors.accent,
            focusedContainerColor = AppTheme.colors.background,
            unfocusedContainerColor = AppTheme.colors.background,
            disabledContainerColor = AppTheme.colors.surface,
            errorContainerColor = AppTheme.colors.background,
        ),
    )
}
