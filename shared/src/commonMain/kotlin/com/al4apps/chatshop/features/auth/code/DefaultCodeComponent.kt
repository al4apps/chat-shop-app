package com.al4apps.chatshop.features.auth.code

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.core.arch.StateStore
import com.al4apps.chatshop.core.network.onFailure
import com.al4apps.chatshop.core.network.onSuccess
import com.al4apps.chatshop.core.ui.toUiText
import com.al4apps.chatshop.features.auth.code.model.CodeUiState
import com.al4apps.chatshop.features.auth.code.model.CodeVmState
import com.al4apps.chatshop.features.auth.domain.AuthInteractor
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultCodeComponent(
    componentContext: ComponentContext,
    phone: String,
    private val authInteractor: AuthInteractor,
    private val onBack: () -> Unit,
    private val onAuthorized: () -> Unit,
    private val assembler: CodeUiStateAssembler = CodeUiStateAssembler(),
) : CodeComponent, ComponentContext by componentContext {

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    private val store = StateStore(
        stateKeeper = stateKeeper,
        key = "CodeVmState",
        serializer = CodeVmState.serializer(),
        initialState = { CodeVmState(phone = phone) },
    )

    private val mutableUiState = MutableStateFlow(assembler.assemble(store.value))

    override val uiState: StateFlow<CodeUiState> = mutableUiState.asStateFlow()

    init {
        scope.launch {
            store.state.collect { mutableUiState.value = assembler.assemble(it) }
        }
        scope.launch {
            while (true) {
                delay(1_000)
                store.update { vmState ->
                    if (vmState.resendSecondsLeft > 0) {
                        vmState.copy(resendSecondsLeft = vmState.resendSecondsLeft - 1)
                    } else {
                        vmState
                    }
                }
            }
        }
    }

    override fun onCodeChange(input: String) {
        if (store.value.isLoading) return
        val digits = input.filter(Char::isDigit).take(CodeVmState.CODE_LENGTH)
        store.update { it.copy(codeDigits = digits, error = null) }
        if (digits.length == CodeVmState.CODE_LENGTH) {
            submit()
        }
    }

    override fun onResendClick() {
        val vmState = store.value
        if (vmState.isLoading || vmState.resendSecondsLeft > 0) return

        store.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            authInteractor.requestCode(store.value.phone)
                .onSuccess {
                    store.update {
                        it.copy(
                            isLoading = false,
                            codeDigits = "",
                            resendSecondsLeft = CodeVmState.RESEND_COOLDOWN_SECONDS,
                        )
                    }
                }
                .onFailure { error ->
                    store.update {
                        it.copy(
                            isLoading = false,
                            error = error.toUiText(),
                            resendSecondsLeft = CodeVmState.RESEND_COOLDOWN_SECONDS,
                        )
                    }
                }
        }
    }

    override fun onBackClick() {
        onBack()
    }

    private fun submit() {
        store.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val vmState = store.value
            authInteractor.verify(phone = vmState.phone, code = vmState.codeDigits)
                .onSuccess {
                    store.update { it.copy(isLoading = false) }
                    onAuthorized()
                }
                .onFailure { error ->
                    store.update {
                        it.copy(
                            isLoading = false,
                            codeDigits = "",
                            error = error.toUiText(),
                        )
                    }
                }
        }
    }
}
