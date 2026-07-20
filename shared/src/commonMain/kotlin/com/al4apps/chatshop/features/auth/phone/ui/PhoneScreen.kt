package com.al4apps.chatshop.features.auth.phone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.auth_phone_placeholder
import chatshop.shared.generated.resources.auth_phone_subtitle
import chatshop.shared.generated.resources.auth_phone_title
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppPrimaryButton
import com.al4apps.chatshop.core.design.widgets.AppTextField
import com.al4apps.chatshop.core.ui.resolve
import com.al4apps.chatshop.features.auth.phone.PhoneComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhoneScreen(
    component: PhoneComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .imePadding()
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(Res.string.auth_phone_title),
            style = AppTheme.typography.h1,
            color = AppTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.auth_phone_subtitle),
            style = AppTheme.typography.body,
            color = AppTheme.colors.textSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        AppTextField(
            value = uiState.phoneDigits,
            onValueChange = component::onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(Res.string.auth_phone_placeholder),
            enabled = !uiState.isLoading,
            isError = uiState.error != null,
            supportingText = uiState.error?.resolve(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = PhoneVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        AppPrimaryButton(
            state = uiState.continueButton,
            onClick = component::onContinueClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
