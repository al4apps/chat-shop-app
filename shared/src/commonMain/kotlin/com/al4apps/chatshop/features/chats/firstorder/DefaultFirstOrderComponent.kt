package com.al4apps.chatshop.features.chats.firstorder

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.core.arch.StateStore
import com.al4apps.chatshop.core.network.onFailure
import com.al4apps.chatshop.core.network.onSuccess
import com.al4apps.chatshop.core.ui.toUiText
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.domain.MessagesInteractor
import com.al4apps.chatshop.features.chats.firstorder.model.FirstOrderUiState
import com.al4apps.chatshop.features.chats.firstorder.model.FirstOrderVmState
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultFirstOrderComponent(
    componentContext: ComponentContext,
    private val chatsInteractor: ChatsInteractor,
    private val messagesInteractor: MessagesInteractor,
    private val onOrderStarted: (dialogId: String) -> Unit,
    private val onBack: (() -> Unit)? = null,
    private val assembler: FirstOrderAssembler = FirstOrderAssembler(),
) : FirstOrderComponent, ComponentContext by componentContext {

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    override val hasBack: Boolean = onBack != null

    private val store = StateStore(
        stateKeeper = stateKeeper,
        key = "FirstOrderVmState",
        serializer = FirstOrderVmState.serializer(),
        initialState = ::FirstOrderVmState,
    )

    private val mutableUiState = MutableStateFlow(assembler.assemble(store.value))

    override val uiState: StateFlow<FirstOrderUiState> = mutableUiState.asStateFlow()

    init {
        scope.launch {
            store.state.collect { mutableUiState.value = assembler.assemble(it) }
        }
    }

    override fun onInputChange(text: String) {
        store.update { it.copy(inputText = text, error = null) }
    }

    override fun onSendClick() {
        val text = store.value.inputText.trim()
        if (text.isEmpty() || store.value.isSubmitting) return

        store.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            chatsInteractor.createDialog()
                .onSuccess { dialogId ->
                    messagesInteractor.sendText(dialogId = dialogId, text = text)
                    onOrderStarted(dialogId)
                }
                .onFailure { error ->
                    store.update { it.copy(isSubmitting = false, error = error.toUiText()) }
                }
        }
    }

    override fun onBackClick() {
        onBack?.invoke()
    }

    override fun onRecordClick() {
        // Аудиозапись — этап 10. Пока no-op.
    }
}
