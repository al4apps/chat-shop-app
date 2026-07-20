package com.al4apps.chatshop.features.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.example_button_disabled
import chatshop.shared.generated.resources.example_button_from_state
import chatshop.shared.generated.resources.example_button_primary
import chatshop.shared.generated.resources.example_button_secondary
import chatshop.shared.generated.resources.example_button_text
import chatshop.shared.generated.resources.example_field_disabled_value
import chatshop.shared.generated.resources.example_field_error_text
import chatshop.shared.generated.resources.example_field_error_value
import chatshop.shared.generated.resources.example_field_placeholder
import chatshop.shared.generated.resources.example_section_buttons
import chatshop.shared.generated.resources.example_section_colors
import chatshop.shared.generated.resources.example_section_fields
import chatshop.shared.generated.resources.example_section_icons
import chatshop.shared.generated.resources.example_section_typography
import chatshop.shared.generated.resources.example_title
import chatshop.shared.generated.resources.example_typo_body
import chatshop.shared.generated.resources.example_typo_caption
import chatshop.shared.generated.resources.example_typo_h1
import chatshop.shared.generated.resources.example_typo_h2
import chatshop.shared.generated.resources.example_typo_title
import com.al4apps.chatshop.core.design.icons.AppIcons
import com.al4apps.chatshop.core.design.model.ButtonUiState
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppIconButton
import com.al4apps.chatshop.core.design.widgets.AppPrimaryButton
import com.al4apps.chatshop.core.design.widgets.AppSecondaryButton
import com.al4apps.chatshop.core.design.widgets.AppTextButton
import com.al4apps.chatshop.core.design.widgets.AppTextField
import com.al4apps.chatshop.core.design.widgets.AppTopBar
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.features.example.ExampleComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExampleScreen(
    component: ExampleComponent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding(),
    ) {
        AppTopBar(
            title = stringResource(Res.string.example_title),
            onBackClick = component::onBackClick,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TypographySection()
            SectionDivider()
            ColorsSection()
            SectionDivider()
            ButtonsSection()
            SectionDivider()
            FieldsSection()
            SectionDivider()
            IconsSection()
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = AppTheme.colors.divider,
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.title,
        color = AppTheme.colors.textSecondary,
    )
}

@Composable
private fun TypographySection() {
    SectionTitle(stringResource(Res.string.example_section_typography))
    Text(
        text = stringResource(Res.string.example_typo_h1),
        style = AppTheme.typography.h1,
        color = AppTheme.colors.textPrimary,
    )
    Text(
        text = stringResource(Res.string.example_typo_h2),
        style = AppTheme.typography.h2,
        color = AppTheme.colors.textPrimary,
    )
    Text(
        text = stringResource(Res.string.example_typo_title),
        style = AppTheme.typography.title,
        color = AppTheme.colors.textPrimary,
    )
    Text(
        text = stringResource(Res.string.example_typo_body),
        style = AppTheme.typography.body,
        color = AppTheme.colors.textPrimary,
    )
    Text(
        text = stringResource(Res.string.example_typo_caption),
        style = AppTheme.typography.caption,
        color = AppTheme.colors.textSecondary,
    )
}

@Composable
private fun ColorsSection() {
    SectionTitle(stringResource(Res.string.example_section_colors))
    val colors = AppTheme.colors
    // Подписи — технические имена токенов палитры, не текст интерфейса.
    val swatches = listOf(
        "accent" to colors.accent,
        "accentPressed" to colors.accentPressed,
        "textPrimary" to colors.textPrimary,
        "textSecondary" to colors.textSecondary,
        "surface" to colors.surface,
        "divider" to colors.divider,
        "disabledContainer" to colors.disabledContainer,
        "error" to colors.error,
    )
    swatches.forEach { (name, color) ->
        ColorSwatchRow(name = name, color = color)
    }
}

@Composable
private fun ColorSwatchRow(
    name: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .size(32.dp)
                .border(
                    width = 1.dp,
                    color = AppTheme.colors.divider,
                    shape = AppTheme.shapes.small,
                )
                .padding(1.dp)
                .background(color = color, shape = AppTheme.shapes.small),
        ) {}
        Text(
            text = name,
            style = AppTheme.typography.body,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun ButtonsSection() {
    SectionTitle(stringResource(Res.string.example_section_buttons))
    AppPrimaryButton(
        text = stringResource(Res.string.example_button_primary),
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
    )
    AppSecondaryButton(
        text = stringResource(Res.string.example_button_secondary),
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
    )
    AppPrimaryButton(
        text = stringResource(Res.string.example_button_disabled),
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
    )
    AppPrimaryButton(
        state = ButtonUiState(
            text = UiText(Res.string.example_button_from_state),
            enabled = true,
        ),
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
    )
    AppTextButton(
        text = stringResource(Res.string.example_button_text),
        onClick = {},
    )
}

@Composable
private fun FieldsSection() {
    SectionTitle(stringResource(Res.string.example_section_fields))
    var value by remember { mutableStateOf("") }
    AppTextField(
        value = value,
        onValueChange = { value = it },
        placeholder = stringResource(Res.string.example_field_placeholder),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    AppTextField(
        value = stringResource(Res.string.example_field_error_value),
        onValueChange = {},
        isError = true,
        supportingText = stringResource(Res.string.example_field_error_text),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    AppTextField(
        value = stringResource(Res.string.example_field_disabled_value),
        onValueChange = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun IconsSection() {
    SectionTitle(stringResource(Res.string.example_section_icons))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppIconButton(
            icon = AppIcons.ArrowBack,
            contentDescription = null,
            onClick = {},
        )
        AppIconButton(
            icon = AppIcons.ArrowBack,
            contentDescription = null,
            onClick = {},
            tint = AppTheme.colors.accent,
        )
        AppIconButton(
            icon = AppIcons.ArrowBack,
            contentDescription = null,
            onClick = {},
            enabled = false,
        )
    }
}
