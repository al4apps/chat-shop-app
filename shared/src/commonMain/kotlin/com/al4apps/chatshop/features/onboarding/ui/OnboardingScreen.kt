package com.al4apps.chatshop.features.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.onboarding_continue
import chatshop.shared.generated.resources.onboarding_design_system
import chatshop.shared.generated.resources.onboarding_stub_title
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppPrimaryButton
import com.al4apps.chatshop.core.design.widgets.AppTextButton
import com.al4apps.chatshop.features.onboarding.OnboardingComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingScreen(
    component: OnboardingComponent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.onboarding_stub_title),
            style = AppTheme.typography.h2,
            color = AppTheme.colors.textPrimary,
        )
        AppPrimaryButton(
            text = stringResource(Res.string.onboarding_continue),
            onClick = component::onContinueClick,
        )
        AppTextButton(
            text = stringResource(Res.string.onboarding_design_system),
            onClick = component::onDesignSystemClick,
        )
    }
}
