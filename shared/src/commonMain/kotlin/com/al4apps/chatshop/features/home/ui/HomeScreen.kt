package com.al4apps.chatshop.features.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.home_dialogs_count
import chatshop.shared.generated.resources.home_stub_title
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.features.home.HomeComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    component: HomeComponent,
    modifier: Modifier = Modifier,
) {
    val dialogsCount by component.dialogsCount.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.home_stub_title),
            style = AppTheme.typography.h2,
            color = AppTheme.colors.textPrimary,
        )
        Text(
            text = stringResource(Res.string.home_dialogs_count, dialogsCount.toString()),
            style = AppTheme.typography.body,
            color = AppTheme.colors.textSecondary,
        )
    }
}
