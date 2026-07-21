package com.al4apps.chatshop.features.chats.list.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.chat_list_empty_title
import chatshop.shared.generated.resources.chat_list_new_dialog
import chatshop.shared.generated.resources.chat_list_title
import com.al4apps.chatshop.core.design.icons.AppIcons
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.core.design.widgets.AppIconButton
import com.al4apps.chatshop.core.design.widgets.AppTopBar
import com.al4apps.chatshop.core.ui.resolve
import com.al4apps.chatshop.features.chats.list.ChatListComponent
import com.al4apps.chatshop.features.chats.list.model.ChatListItemUiState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatListScreen(
    component: ChatListComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        AppTopBar(
            title = stringResource(Res.string.chat_list_title),
            actions = {
                AppIconButton(
                    icon = AppIcons.Plus,
                    contentDescription = stringResource(Res.string.chat_list_new_dialog),
                    onClick = component::onNewDialogClick,
                    tint = AppTheme.colors.accent,
                )
            },
        )
        if (uiState.isEmpty) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.chat_list_empty_title),
                    style = AppTheme.typography.body,
                    color = AppTheme.colors.textSecondary,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = uiState.items, key = { it.id }) { item ->
                    ChatListItemRow(
                        item = item,
                        onClick = { component.onDialogClick(item.id) },
                    )
                    HorizontalDivider(color = AppTheme.colors.divider)
                }
            }
        }
    }
}

@Composable
private fun ChatListItemRow(
    item: ChatListItemUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title.resolve(),
                style = AppTheme.typography.title,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.subtitle.resolve(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.timestamp,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textSecondary,
        )
    }
}
