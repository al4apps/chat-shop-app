package com.al4apps.chatshop.features.chats.dialog

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.core.arch.StateStore
import com.al4apps.chatshop.features.chats.dialog.model.DialogUiState
import com.al4apps.chatshop.features.chats.dialog.model.DialogVmState
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.domain.MessagesInteractor
import com.al4apps.chatshop.services.sync.SyncService
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DefaultDialogComponent(
    componentContext: ComponentContext,
    private val dialogId: String,
    private val chatsInteractor: ChatsInteractor,
    private val messagesInteractor: MessagesInteractor,
    private val syncService: SyncService,
    private val onBack: () -> Unit,
    private val assembler: DialogAssembler = DialogAssembler(),
) : DialogComponent, ComponentContext by componentContext {

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    private val store = StateStore(
        stateKeeper = stateKeeper,
        key = "DialogVmState",
        serializer = DialogVmState.serializer(),
        initialState = ::DialogVmState,
    )

    private val mutableUiState = MutableStateFlow(
        assembler.assemble(store.value, dialog = null, messages = emptyList()),
    )

    override val uiState: StateFlow<DialogUiState> = mutableUiState.asStateFlow()

    init {
        // Частый поллинг ленты — только пока экран открыт.
        syncService.onDialogOpened(dialogId)
        lifecycle.doOnDestroy { syncService.onDialogClosed() }

        scope.launch {
            combine(
                store.state,
                chatsInteractor.observeDialog(dialogId),
                messagesInteractor.observeMessages(dialogId),
            ) { vmState, dialog, messages ->
                assembler.assemble(vmState, dialog, messages)
            }.collect { mutableUiState.value = it }
        }
    }

    override fun onInputChange(text: String) {
        store.update { it.copy(inputText = text) }
    }

    override fun onSendClick() {
        val text = store.value.inputText.trim()
        if (text.isEmpty()) return

        store.update { it.copy(inputText = "") }
        scope.launch {
            messagesInteractor.sendText(dialogId = dialogId, text = text)
        }
    }

    override fun onRetryClick(localId: Long) {
        scope.launch {
            messagesInteractor.retryMessage(localId)
        }
    }

    override fun onBackClick() {
        onBack()
    }
}
