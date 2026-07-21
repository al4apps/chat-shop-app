package com.al4apps.chatshop.features.chats.list.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class ChatListUiState(
    val items: ImmutableList<ChatListItemUiState>,
    val isEmpty: Boolean,
)
