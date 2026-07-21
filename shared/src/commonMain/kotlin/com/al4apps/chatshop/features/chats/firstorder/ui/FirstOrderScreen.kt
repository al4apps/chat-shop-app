package com.al4apps.chatshop.features.chats.firstorder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.first_order_record
import chatshop.shared.generated.resources.first_order_subtitle
import chatshop.shared.generated.resources.first_order_title
import chatshop.shared.generated.resources.new_order_title
import com.al4apps.chatshop.core.design.icons.AppIcons
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppTopBar
import com.al4apps.chatshop.core.design.widgets.MessageInputBar
import com.al4apps.chatshop.core.ui.resolve
import com.al4apps.chatshop.features.chats.firstorder.FirstOrderComponent
import org.jetbrains.compose.resources.stringResource

@Composable
fun FirstOrderScreen(
    component: FirstOrderComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        if (component.hasBack) {
            AppTopBar(
                title = stringResource(Res.string.new_order_title),
                onBackClick = component::onBackClick,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.first_order_title),
                style = AppTheme.typography.h1,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.first_order_subtitle),
                style = AppTheme.typography.body,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
            // Кнопка неактивна до этапа 10 (аудио); onRecordClick появится тогда же.
            RecordButton(modifier = Modifier.padding(top = 40.dp))
        }
        uiState.error?.let { error ->
            Text(
                text = error.resolve(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        MessageInputBar(
            text = uiState.inputText,
            isSendEnabled = uiState.isSendEnabled,
            onTextChange = component::onInputChange,
            onSendClick = component::onSendClick,
        )
    }
}

@Composable
private fun RecordButton(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .background(color = AppTheme.colors.accent, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Mic,
            contentDescription = stringResource(Res.string.first_order_record),
            tint = AppTheme.colors.onAccent,
            modifier = Modifier.size(40.dp),
        )
    }
}
