package com.al4apps.chatshop.features.chats.firstorder

import com.al4apps.chatshop.core.arch.Assembler
import com.al4apps.chatshop.features.chats.firstorder.model.FirstOrderUiState
import com.al4apps.chatshop.features.chats.firstorder.model.FirstOrderVmState

class FirstOrderAssembler : Assembler<FirstOrderVmState, FirstOrderUiState> {

    override fun assemble(vmState: FirstOrderVmState): FirstOrderUiState = FirstOrderUiState(
        inputText = vmState.inputText,
        isSendEnabled = vmState.inputText.isNotBlank() && !vmState.isSubmitting,
        isSubmitting = vmState.isSubmitting,
        error = vmState.error,
    )
}
