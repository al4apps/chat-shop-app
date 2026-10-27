package com.al4apps.chatshop.features.chats.firstorder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.dialog_record_cancel
import chatshop.shared.generated.resources.dialog_record_stop
import chatshop.shared.generated.resources.first_order_record
import chatshop.shared.generated.resources.first_order_subtitle
import chatshop.shared.generated.resources.first_order_title
import chatshop.shared.generated.resources.new_order_title
import com.al4apps.chatshop.core.design.icons.AppIcons
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppIconButton
import com.al4apps.chatshop.core.design.widgets.AppTopBar
import com.al4apps.chatshop.core.design.widgets.MessageInputBar
import com.al4apps.chatshop.core.ui.resolve
import com.al4apps.chatshop.core.util.formatSeconds
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
            if (uiState.isRecording) {
                RecordingControls(
                    seconds = uiState.recordingSeconds,
                    onStop = component::onRecordStop,
                    onCancel = component::onRecordCancel,
                    modifier = Modifier.padding(top = 32.dp),
                )
            } else {
                RecordButton(
                    enabled = uiState.canRecord && !uiState.isSubmitting,
                    onClick = component::onRecordStart,
                    modifier = Modifier.padding(top = 40.dp),
                )
            }
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
            modifier = Modifier.imePadding(),
        )
    }
}

@Composable
private fun RecordButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (enabled) AppTheme.colors.accent else AppTheme.colors.disabledContainer
    Box(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(color = color)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Mic,
            contentDescription = stringResource(Res.string.first_order_record),
            tint = if (enabled) AppTheme.colors.onAccent else AppTheme.colors.disabledContent,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun RecordingControls(
    seconds: Int,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = formatSeconds(seconds),
            style = AppTheme.typography.h2,
            color = AppTheme.colors.textPrimary,
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.error)
                .clickable(onClick = onStop),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Stop,
                contentDescription = stringResource(Res.string.dialog_record_stop),
                tint = AppTheme.colors.onError,
                modifier = Modifier.size(40.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIconButton(
                icon = AppIcons.Close,
                contentDescription = stringResource(Res.string.dialog_record_cancel),
                onClick = onCancel,
                tint = AppTheme.colors.textSecondary,
            )
            Text(
                text = stringResource(Res.string.dialog_record_cancel),
                style = AppTheme.typography.body,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}
