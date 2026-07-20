package com.al4apps.chatshop.features.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.home_stub_title
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.features.home.HomeComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    component: HomeComponent,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.home_stub_title),
            style = AppTheme.typography.h2,
            color = AppTheme.colors.textPrimary,
        )
    }
}
