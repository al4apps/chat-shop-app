package com.al4apps.chatshop.features.chats.firstorder

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.core.arch.StateStore
import com.al4apps.chatshop.core.media.AudioRecorder
import com.al4apps.chatshop.core.media.VoiceRecordingController
import com.al4apps.chatshop.core.network.onFailure
import com.al4apps.chatshop.core.network.onSuccess
import com.al4apps.chatshop.core.ui.toUiText
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.domain.MessagesInteractor
import com.al4apps.chatshop.features.chats.firstorder.model.FirstOrderUiState
import com.al4apps.chatshop.features.chats.firstorder.model.FirstOrderVmState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DefaultFirstOrderComponent(
    componentContext: ComponentContext,
    private val chatsInteractor: ChatsInteractor,
    private val messagesInteractor: MessagesInteractor,
    audioRecorder: AudioRecorder,
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

    private val recordingController = VoiceRecordingController(
        recorder = audioRecorder,
        scope = scope,
        onCompleted = { audio -> submit { dialogId -> messagesInteractor.sendVoice(dialogId, audio) } },
    )

    private val mutableUiState = MutableStateFlow(
        assembler.assemble(store.value, recordingController.state.value, recordingController.isAvailable),
    )

    override val uiState: StateFlow<FirstOrderUiState> = mutableUiState.asStateFlow()

    init {
        lifecycle.doOnDestroy { recordingController.cancel() }
        scope.launch {
            combine(store.state, recordingController.state) { vmState, recording ->
                assembler.assemble(vmState, recording, recordingController.isAvailable)
            }.collect { mutableUiState.value = it }
        }
    }

    override fun onInputChange(text: String) {
        store.update { it.copy(inputText = text, error = null) }
    }

    override fun onSendClick() {
        val text = store.value.inputText.trim()
        if (text.isEmpty()) return
        submit { dialogId -> messagesInteractor.sendText(dialogId, text) }
    }

    override fun onRecordStart() {
        recordingController.start()
    }

    override fun onRecordStop() {
        recordingController.stop()
    }

    override fun onRecordCancel() {
        recordingController.cancel()
    }

    override fun onBackClick() {
        onBack?.invoke()
    }

    /** Создаёт диалог, кладёт сообщение через [send] и переходит в диалог. */
    private fun submit(send: suspend (dialogId: String) -> Unit) {
        if (store.value.isSubmitting) return
        store.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            chatsInteractor.createDialog()
                .onSuccess { dialogId ->
                    send(dialogId)
                    onOrderStarted(dialogId)
                }
                .onFailure { error ->
                    store.update { it.copy(isSubmitting = false, error = error.toUiText()) }
                }
        }
    }
}
