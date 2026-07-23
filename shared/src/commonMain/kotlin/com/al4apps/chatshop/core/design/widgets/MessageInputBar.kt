package com.al4apps.chatshop.core.design.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.dialog_attach
import chatshop.shared.generated.resources.dialog_input_placeholder
import chatshop.shared.generated.resources.dialog_send
import com.al4apps.chatshop.core.design.icons.AppIcons
import com.al4apps.chatshop.core.design.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Панель ввода сообщения: (скрепка) + поле + кнопка отправки. Общая для диалога и первого заказа. */
@Composable
fun MessageInputBar(
    text: String,
    isSendEnabled: Boolean,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.dialog_input_placeholder),
    onAttachClick: (() -> Unit)? = null,
) {
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
        AppIconButton(
            icon = AppIcons.Send,
            contentDescription = stringResource(Res.string.dialog_send),
            onClick = onSendClick,
            enabled = isSendEnabled,
            tint = AppTheme.colors.accent,
        )
    }
}
