package com.al4apps.chatshop.features.auth.code

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.auth_code_resend
import chatshop.shared.generated.resources.auth_code_resend_in
import chatshop.shared.generated.resources.auth_code_subtitle
import com.al4apps.chatshop.core.arch.Assembler
import com.al4apps.chatshop.core.design.model.ButtonUiState
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.features.auth.code.model.CodeUiState
import com.al4apps.chatshop.features.auth.code.model.CodeVmState

class CodeUiStateAssembler : Assembler<CodeVmState, CodeUiState> {

    override fun assemble(vmState: CodeVmState): CodeUiState = CodeUiState(
        subtitle = UiText(Res.string.auth_code_subtitle, formatPhone(vmState.phone)),
        codeDigits = vmState.codeDigits,
        isLoading = vmState.isLoading,
        error = vmState.error,
        resendButton = ButtonUiState(
            text = if (vmState.resendSecondsLeft > 0) {
                UiText(Res.string.auth_code_resend_in, vmState.resendSecondsLeft.toString())
            } else {
                UiText(Res.string.auth_code_resend)
            },
            enabled = vmState.resendSecondsLeft == 0 && !vmState.isLoading,
        ),
    )

    /** +992988009090 → +992 988009090. */
    private fun formatPhone(phone: String): String {
        val digits = phone.removePrefix("+992")
        if (digits.length != 9) return phone
        return "+992 $digits"
    }
}
