package com.al4apps.chatshop.features.auth.code.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.auth_code_placeholder
import chatshop.shared.generated.resources.auth_code_title
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppTextButton
import com.al4apps.chatshop.core.design.widgets.AppTextField
import com.al4apps.chatshop.core.design.widgets.AppTopBar
import com.al4apps.chatshop.core.ui.resolve
import com.al4apps.chatshop.features.auth.code.CodeComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun CodeScreen(
    component: CodeComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding(),
    ) {
        AppTopBar(
            title = stringResource(Res.string.auth_code_title),
            onBackClick = component::onBackClick,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = uiState.subtitle.resolve(),
                style = AppTheme.typography.body,
                color = AppTheme.colors.textSecondary,
            )
            Spacer(modifier = Modifier.height(24.dp))
            AppTextField(
                value = uiState.codeDigits,
                onValueChange = component::onCodeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(Res.string.auth_code_placeholder),
                enabled = !uiState.isLoading,
                isError = uiState.error != null,
                supportingText = uiState.error?.resolve(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppTextButton(
                state = uiState.resendButton,
                onClick = component::onResendClick,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
