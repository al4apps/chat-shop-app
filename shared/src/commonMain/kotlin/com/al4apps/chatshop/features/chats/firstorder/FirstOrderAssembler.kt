package com.al4apps.chatshop.features.chats.firstorder

import com.al4apps.chatshop.core.media.VoiceRecordingState
import com.al4apps.chatshop.features.chats.firstorder.model.FirstOrderUiState
import com.al4apps.chatshop.features.chats.firstorder.model.FirstOrderVmState

class FirstOrderAssembler {

    fun assemble(
        vmState: FirstOrderVmState,
        recording: VoiceRecordingState,
        canRecord: Boolean,
    ): FirstOrderUiState = FirstOrderUiState(
        inputText = vmState.inputText,
        isSendEnabled = vmState.inputText.isNotBlank() && !vmState.isSubmitting,
        isSubmitting = vmState.isSubmitting,
        error = vmState.error,
        canRecord = canRecord,
        isRecording = recording.isRecording,
        recordingSeconds = recording.seconds,
    )
}
