package com.al4apps.chatshop.core.design.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.dialog_attach
import chatshop.shared.generated.resources.dialog_input_placeholder
import chatshop.shared.generated.resources.dialog_record
import chatshop.shared.generated.resources.dialog_record_cancel
import chatshop.shared.generated.resources.dialog_record_stop
import chatshop.shared.generated.resources.dialog_send
import com.al4apps.chatshop.core.design.icons.AppIcons
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.util.formatSeconds
import org.jetbrains.compose.resources.stringResource

/** Панель ввода сообщения: (скрепка) + поле + отправка/микрофон. Общая для диалога и первого заказа. */
@Composable
fun MessageInputBar(
    text: String,
    isSendEnabled: Boolean,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.dialog_input_placeholder),
    onAttachClick: (() -> Unit)? = null,
    canRecord: Boolean = false,
    isRecording: Boolean = false,
    recordingSeconds: Int = 0,
    onRecordStart: () -> Unit = {},
    onRecordStop: () -> Unit = {},
    onRecordCancel: () -> Unit = {},
) {
    if (isRecording) {
        RecordingBar(
            seconds = recordingSeconds,
            onCancel = onRecordCancel,
            onStop = onRecordStop,
            modifier = modifier,
        )
        return
    }

    val showMic = canRecord && text.isBlank()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onAttachClick != null) {
            AppIconButton(
                icon = AppIcons.Attach,
                contentDescription = stringResource(Res.string.dialog_attach),
                onClick = onAttachClick,
                tint = AppTheme.colors.textSecondary,
            )
        }
        AppTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = placeholder,
        )
        if (showMic) {
            AppIconButton(
                icon = AppIcons.Mic,
                contentDescription = stringResource(Res.string.dialog_record),
                onClick = onRecordStart,
                tint = AppTheme.colors.accent,
            )
        } else {
            AppIconButton(
                icon = AppIcons.Send,
                contentDescription = stringResource(Res.string.dialog_send),
                onClick = onSendClick,
                enabled = isSendEnabled,
                tint = AppTheme.colors.accent,
            )
        }
    }
}

@Composable
private fun RecordingBar(
    seconds: Int,
    onCancel: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppIconButton(
            icon = AppIcons.Close,
            contentDescription = stringResource(Res.string.dialog_record_cancel),
            onClick = onCancel,
            tint = AppTheme.colors.textSecondary,
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RecordingDot()
            Text(
                text = formatSeconds(seconds),
                style = AppTheme.typography.body,
                color = AppTheme.colors.textPrimary,
            )
        }
        AppIconButton(
            icon = AppIcons.Send,
            contentDescription = stringResource(Res.string.dialog_record_stop),
            onClick = onStop,
            tint = AppTheme.colors.accent,
        )
    }
}

@Composable
private fun RecordingDot(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.error),
    )
}
