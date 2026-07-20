package com.al4apps.chatshop.features.chats.dialog.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.dialog_stub_body
import chatshop.shared.generated.resources.dialog_stub_title
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppTopBar
import com.al4apps.chatshop.features.chats.dialog.DialogComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogScreen(
    component: DialogComponent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding(),
    ) {
        AppTopBar(
            title = stringResource(Res.string.dialog_stub_title),
            onBackClick = component::onBackClick,
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.dialog_stub_body),
                style = AppTheme.typography.body,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}
