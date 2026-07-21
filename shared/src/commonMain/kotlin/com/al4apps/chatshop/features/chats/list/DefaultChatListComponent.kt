package com.al4apps.chatshop.features.chats.list

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.list.model.ChatListUiState
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DefaultChatListComponent(
    componentContext: ComponentContext,
    chatsInteractor: ChatsInteractor,
    private val onDialogSelected: (dialogId: String) -> Unit,
    private val onNewOrder: () -> Unit,
    private val assembler: ChatListAssembler = ChatListAssembler(),
) : ChatListComponent, ComponentContext by componentContext {

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    override val uiState: StateFlow<ChatListUiState> = chatsInteractor.observeDialogs()
        .map { assembler.assemble(it) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = assembler.assemble(emptyList()),
        )

    override fun onDialogClick(dialogId: String) {
        onDialogSelected(dialogId)
    }

    override fun onNewDialogClick() {
        onNewOrder()
    }
}
