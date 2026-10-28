package com.al4apps.chatshop.features.chats.dialog

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.core.arch.StateStore
import com.al4apps.chatshop.core.media.AudioPlayer
import com.al4apps.chatshop.core.media.AudioRecorder
import com.al4apps.chatshop.core.media.ImagePicker
import com.al4apps.chatshop.core.media.VoiceRecordingController
import com.al4apps.chatshop.core.network.AppError
import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.ui.toUiText
import com.al4apps.chatshop.features.chats.dialog.model.DialogUiState
import com.al4apps.chatshop.features.chats.dialog.model.DialogVmState
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.domain.MessagesInteractor
import com.al4apps.chatshop.services.sync.SyncService
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
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
    private val imagePicker: ImagePicker,
    audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
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

    private val recordingController = VoiceRecordingController(
        recorder = audioRecorder,
        scope = scope,
        onCompleted = { audio -> messagesInteractor.sendVoice(dialogId, audio) },
    )

    private val mutableUiState = MutableStateFlow(
        assembler.assemble(
            vmState = store.value,
            dialog = null,
            messages = emptyList(),
            recording = recordingController.state.value,
            canRecord = recordingController.isAvailable,
            playback = audioPlayer.state.value,
        ),
    )

    override val uiState: StateFlow<DialogUiState> = mutableUiState.asStateFlow()

    // «Назад» при открытом полноэкранном просмотре закрывает его, а не экран.
    private val fullscreenBackCallback = BackCallback(isEnabled = false) {
        onFullscreenDismiss()
    }

    init {
        backHandler.register(fullscreenBackCallback)

        // Частый поллинг ленты — только пока экран открыт.
        syncService.onDialogOpened(dialogId)
        lifecycle.doOnDestroy {
            recordingController.cancel()
            audioPlayer.stop()
            syncService.onDialogClosed()
        }

        scope.launch {
            combine(
                store.state,
                chatsInteractor.observeDialog(dialogId),
                messagesInteractor.observeMessages(dialogId),
                recordingController.state,
                audioPlayer.state,
            ) { vmState, dialog, messages, recording, playback ->
                assembler.assemble(
                    vmState = vmState,
                    dialog = dialog,
                    messages = messages,
                    recording = recording,
                    canRecord = recordingController.isAvailable,
                    playback = playback,
                )
            }.collect {
                mutableUiState.value = it
                fullscreenBackCallback.isEnabled = it.fullscreenImagePath != null
            }
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

    override fun onAttachClick() {
        scope.launch {
            val image = imagePicker.pickImage() ?: return@launch
            messagesInteractor.sendImage(dialogId = dialogId, image = image)
        }
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

    override fun onVoiceClick(fileId: String, path: String, durationSec: Int) {
        audioPlayer.toggle(fileId, path, durationSec)
    }

    override fun onImageClick(imagePath: String) {
        store.update { it.copy(fullscreenImagePath = imagePath) }
    }

    override fun onFullscreenDismiss() {
        store.update { it.copy(fullscreenImagePath = null) }
    }

    override fun onRetryClick(localId: Long) {
        scope.launch {
            messagesInteractor.retryMessage(localId)
        }
    }

    override fun onConfirmReceipt() {
        val vmState = store.value
        if (vmState.isConfirmingReceipt || vmState.isConfirmActionForbidden) return

        store.update { it.copy(isConfirmingReceipt = true, commandError = null) }
        scope.launch {
            when (val result = chatsInteractor.confirmOrderReceived(dialogId)) {
                is AppResult.Success -> store.update {
                    it.copy(isConfirmingReceipt = false, commandError = null)
                }

                is AppResult.Failure -> {
                    val status = (result.error as? AppError.Api)?.status
                    if (status == 409) {
                        syncService.syncDialogsOnce()
                        syncService.syncMessagesOnce(dialogId)
                    }
                    store.update {
                        it.copy(
                            isConfirmingReceipt = false,
                            commandError = result.error.toUiText(),
                            isConfirmActionForbidden = it.isConfirmActionForbidden || status == 403,
                        )
                    }
                }
            }
        }
    }

    override fun onBackClick() {
        onBack()
    }
}
