package com.al4apps.chatshop.features.chats.list.model

import androidx.compose.runtime.Immutable
import com.al4apps.chatshop.core.ui.UiText
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class ChatListUiState(
    val items: ImmutableList<ChatListItemUiState>,
    val isEmpty: Boolean,
    val isCreating: Boolean,
    val error: UiText?,
)
