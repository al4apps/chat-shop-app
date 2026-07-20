package com.al4apps.chatshop.features.auth.phone

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.core.arch.StateStore
import com.al4apps.chatshop.core.network.onFailure
import com.al4apps.chatshop.core.network.onSuccess
import com.al4apps.chatshop.core.ui.toUiText
import com.al4apps.chatshop.features.auth.domain.AuthInteractor
import com.al4apps.chatshop.features.auth.phone.model.PhoneUiState
import com.al4apps.chatshop.features.auth.phone.model.PhoneVmState
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultPhoneComponent(
    componentContext: ComponentContext,
    private val authInteractor: AuthInteractor,
    private val onCodeRequested: (phone: String) -> Unit,
    private val assembler: PhoneUiStateAssembler = PhoneUiStateAssembler(),
) : PhoneComponent, ComponentContext by componentContext {

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    private val store = StateStore(
        stateKeeper = stateKeeper,
        key = "PhoneVmState",
        serializer = PhoneVmState.serializer(),
        initialState = ::PhoneVmState,
    )

    private val mutableUiState = MutableStateFlow(assembler.assemble(store.value))

    override val uiState: StateFlow<PhoneUiState> = mutableUiState.asStateFlow()

    init {
        scope.launch {
            store.state.collect { mutableUiState.value = assembler.assemble(it) }
        }
    }

    override fun onPhoneChange(input: String) {
        val digits = input.filter(Char::isDigit).take(PhoneVmState.PHONE_LENGTH)
        store.update { it.copy(phoneDigits = digits, error = null) }
    }

    override fun onContinueClick() {
        val vmState = store.value
        if (vmState.isLoading || vmState.phoneDigits.length != PhoneVmState.PHONE_LENGTH) return

        store.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val phone = fullPhone(vmState.phoneDigits)
            authInteractor.requestCode(phone)
                .onSuccess {
                    store.update { it.copy(isLoading = false) }
                    onCodeRequested(phone)
                }
                .onFailure { error ->
                    store.update { it.copy(isLoading = false, error = error.toUiText()) }
                }
        }
    }

    private fun fullPhone(digits: String): String = "+992$digits"
}
