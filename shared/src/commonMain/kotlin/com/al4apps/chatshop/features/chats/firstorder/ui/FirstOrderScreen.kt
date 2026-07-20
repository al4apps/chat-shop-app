package com.al4apps.chatshop.features.chats.firstorder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.first_order_stub_body
import chatshop.shared.generated.resources.first_order_stub_title
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.features.chats.firstorder.FirstOrderComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun FirstOrderScreen(
    component: FirstOrderComponent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.first_order_stub_title),
            style = AppTheme.typography.h2,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.first_order_stub_body),
            style = AppTheme.typography.body,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
