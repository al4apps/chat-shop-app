package com.al4apps.chatshop.features.chats.dialog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.dialog_closed_note
import chatshop.shared.generated.resources.message_failed
import chatshop.shared.generated.resources.message_sending
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppTopBar
import com.al4apps.chatshop.core.design.widgets.MessageInputBar
import com.al4apps.chatshop.core.ui.resolve
import com.al4apps.chatshop.features.chats.dialog.DialogComponent
import com.al4apps.chatshop.features.chats.dialog.model.MessageItemUiState
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogScreen(
    component: DialogComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Новое сообщение (index 0 при reverseLayout) — прокрутка к низу ленты.
    LaunchedEffect(uiState.items.firstOrNull()?.localId) {
        if (uiState.items.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        AppTopBar(
            title = uiState.title.resolve(),
            onBackClick = component::onBackClick,
        )
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = uiState.items, key = { it.localId }) { item ->
                MessageRow(
                    item = item,
                    onRetryClick = { component.onRetryClick(item.localId) },
                )
            }
        }
        if (uiState.isInputBlocked) {
            Text(
                text = stringResource(Res.string.dialog_closed_note),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
            )
        } else {
            MessageInputBar(
                text = uiState.inputText,
                isSendEnabled = uiState.isSendEnabled,
                onTextChange = component::onInputChange,
                onSendClick = component::onSendClick,
            )
        }
    }
}

@Composable
private fun MessageRow(
    item: MessageItemUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        item.isSystem -> SystemMessage(item = item, modifier = modifier)
        else -> ChatBubble(item = item, onRetryClick = onRetryClick, modifier = modifier)
    }
}

@Composable
private fun SystemMessage(
    item: MessageItemUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = item.text,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(
                    color = AppTheme.colors.surface,
                    shape = AppTheme.shapes.medium,
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ChatBubble(
    item: MessageItemUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (item.isMine) Alignment.End else Alignment.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (item.isMine) AppTheme.colors.accent else AppTheme.colors.surface,
                    shape = AppTheme.shapes.large,
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = item.text,
                style = AppTheme.typography.body,
                color = if (item.isMine) AppTheme.colors.onAccent else AppTheme.colors.textPrimary,
            )
            Text(
                text = item.time,
                style = AppTheme.typography.caption,
                color = if (item.isMine) {
                    AppTheme.colors.onAccent.copy(alpha = 0.7f)
                } else {
                    AppTheme.colors.textSecondary
                },
                modifier = Modifier.align(Alignment.End),
            )
        }
        when {
            item.isSending -> Text(
                text = stringResource(Res.string.message_sending),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )

            item.isFailed -> Text(
                text = stringResource(Res.string.message_failed),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.error,
                modifier = Modifier.clickable(onClick = onRetryClick),
            )
        }
    }
}
