package com.al4apps.chatshop.features.chats.list

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.core.arch.StateStore
import com.al4apps.chatshop.core.network.onFailure
import com.al4apps.chatshop.core.network.onSuccess
import com.al4apps.chatshop.core.ui.toUiText
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.list.model.ChatListUiState
import com.al4apps.chatshop.features.chats.list.model.ChatListVmState
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DefaultChatListComponent(
    componentContext: ComponentContext,
    private val chatsInteractor: ChatsInteractor,
    private val onDialogSelected: (dialogId: String) -> Unit,
    private val assembler: ChatListAssembler = ChatListAssembler(),
) : ChatListComponent, ComponentContext by componentContext {

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    private val store = StateStore(
        stateKeeper = stateKeeper,
        key = "ChatListVmState",
        serializer = ChatListVmState.serializer(),
        initialState = ::ChatListVmState,
    )

    private val mutableUiState = MutableStateFlow(assembler.assemble(store.value, emptyList()))

    override val uiState: StateFlow<ChatListUiState> = mutableUiState.asStateFlow()

    init {
        scope.launch {
            combine(store.state, chatsInteractor.observeDialogs()) { vmState, dialogs ->
                assembler.assemble(vmState, dialogs)
            }.collect { mutableUiState.value = it }
        }
    }

    override fun onDialogClick(dialogId: String) {
        onDialogSelected(dialogId)
    }

    override fun onNewDialogClick() {
        if (store.value.isCreating) return

        store.update { it.copy(isCreating = true, error = null) }
        scope.launch {
            chatsInteractor.createDialog()
                .onSuccess { dialogId ->
                    store.update { it.copy(isCreating = false) }
                    onDialogSelected(dialogId)
                }
                .onFailure { error ->
                    store.update { it.copy(isCreating = false, error = error.toUiText()) }
                }
        }
    }
}
