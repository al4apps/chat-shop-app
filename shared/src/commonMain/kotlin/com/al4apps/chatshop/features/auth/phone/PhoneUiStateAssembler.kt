package com.al4apps.chatshop.features.auth.phone

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.auth_phone_continue
import com.al4apps.chatshop.core.arch.Assembler
import com.al4apps.chatshop.core.design.model.ButtonUiState
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.features.auth.phone.model.PhoneUiState
import com.al4apps.chatshop.features.auth.phone.model.PhoneVmState

class PhoneUiStateAssembler : Assembler<PhoneVmState, PhoneUiState> {

    override fun assemble(vmState: PhoneVmState): PhoneUiState = PhoneUiState(
        phoneDigits = vmState.phoneDigits,
        isLoading = vmState.isLoading,
        error = vmState.error,
        continueButton = ButtonUiState(
            text = UiText(Res.string.auth_phone_continue),
            enabled = vmState.phoneDigits.length == PhoneVmState.PHONE_LENGTH && !vmState.isLoading,
        ),
    )
}
