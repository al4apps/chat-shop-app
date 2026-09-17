package com.al4apps.chatshop.features.chats.dialog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import okio.Path.Companion.toPath
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.dialog_closed_note
import chatshop.shared.generated.resources.message_failed
import chatshop.shared.generated.resources.message_image_deleted
import chatshop.shared.generated.resources.message_sending
import com.al4apps.chatshop.core.design.icons.AppIcons
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppTopBar
import com.al4apps.chatshop.core.design.widgets.MessageInputBar
import com.al4apps.chatshop.core.ui.resolve
import com.al4apps.chatshop.core.util.formatSeconds
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                        onImageClick = component::onImageClick,
                        onVoiceClick = component::onVoiceClick,
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
                    onAttachClick = component::onAttachClick,
                    canRecord = uiState.canRecord,
                    isRecording = uiState.isRecording,
                    recordingSeconds = uiState.recordingSeconds,
                    onRecordStart = component::onRecordStart,
                    onRecordStop = component::onRecordStop,
                    onRecordCancel = component::onRecordCancel,
                )
            }
        }
        uiState.fullscreenImagePath?.let { path ->
            FullscreenImage(path = path, onDismiss = component::onFullscreenDismiss)
        }
    }
}

@Composable
private fun FullscreenImage(
    path: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = path.toPath(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ImageContent(
    item: MessageItemUiState,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AppTheme.shapes.medium
    Box(
        modifier = modifier
            .size(220.dp)
            .clip(shape)
            .background(AppTheme.colors.disabledContainer),
        contentAlignment = Alignment.Center,
    ) {
        when {
            item.isFileDeleted -> Text(
                text = stringResource(Res.string.message_image_deleted),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )

            item.imagePath != null -> AsyncImage(
                model = item.imagePath.toPath(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onImageClick(item.imagePath) },
            )

            else -> CircularProgressIndicator(color = AppTheme.colors.accent)
        }
    }
}

@Composable
private fun VoiceContent(
    item: MessageItemUiState,
    onVoiceClick: (fileId: String, path: String, durationSec: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onAccent = item.isMine
    val contentColor = if (onAccent) AppTheme.colors.onAccent else AppTheme.colors.textPrimary
    val secondaryColor = if (onAccent) {
        AppTheme.colors.onAccent.copy(alpha = 0.7f)
    } else {
        AppTheme.colors.textSecondary
    }
    val ready = item.voicePath != null && !item.isFileDeleted
    val progress = if (item.voiceDurationSec > 0) {
        (item.voicePositionSec.toFloat() / item.voiceDurationSec).coerceIn(0f, 1f)
    } else {
        0f
    }
    val shownSeconds = if (item.isVoicePlaying) item.voicePositionSec else item.voiceDurationSec

    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(contentColor.copy(alpha = 0.15f))
                .clickable(enabled = ready) {
                    onVoiceClick(item.voiceFileId!!, item.voicePath!!, item.voiceDurationSec)
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                item.isFileDeleted -> Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = null,
                    tint = secondaryColor,
                    modifier = Modifier.size(20.dp),
                )

                !ready -> CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )

                item.isVoicePlaying -> Icon(
                    imageVector = AppIcons.Pause,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp),
                )

                else -> Icon(
                    imageVector = AppIcons.Play,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Column(
            modifier = Modifier.width(140.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.25f),
            )
            Text(
                text = formatSeconds(shownSeconds),
                style = AppTheme.typography.caption,
                color = secondaryColor,
            )
        }
    }
}

@Composable
private fun MessageRow(
    item: MessageItemUiState,
    onRetryClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onVoiceClick: (fileId: String, path: String, durationSec: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        item.isSystem -> SystemMessage(item = item, modifier = modifier)
        else -> ChatBubble(
            item = item,
            onRetryClick = onRetryClick,
            onImageClick = onImageClick,
            onVoiceClick = onVoiceClick,
            modifier = modifier,
        )
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
    onImageClick: (String) -> Unit,
    onVoiceClick: (fileId: String, path: String, durationSec: Int) -> Unit,
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
                .padding(
                    horizontal = if (item.isImage) 4.dp else 12.dp,
                    vertical = if (item.isImage) 4.dp else 8.dp,
                ),
        ) {
            when {
                item.isImage -> ImageContent(item = item, onImageClick = onImageClick)
                item.isVoice -> VoiceContent(item = item, onVoiceClick = onVoiceClick)
                else -> Text(
                    text = item.text,
                    style = AppTheme.typography.body,
                    color = if (item.isMine) AppTheme.colors.onAccent else AppTheme.colors.textPrimary,
                )
            }
            Text(
                text = item.time,
                style = AppTheme.typography.caption,
                color = if (item.isMine) {
                    AppTheme.colors.onAccent.copy(alpha = 0.7f)
                } else {
                    AppTheme.colors.textSecondary
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = if (item.isImage) 6.dp else 0.dp),
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
