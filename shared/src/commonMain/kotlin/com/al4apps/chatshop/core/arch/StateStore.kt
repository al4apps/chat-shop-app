package com.al4apps.chatshop.core.arch

import com.arkivanov.essenty.statekeeper.StateKeeper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer

/**
 * Хранилище логического состояния компонента (vmState) с переживанием
 * пересоздания процесса/конфигурации через [StateKeeper].
 */
class StateStore<S : Any>(
    stateKeeper: StateKeeper,
    key: String,
    serializer: KSerializer<S>,
    initialState: () -> S,
) {

    private val mutableState = MutableStateFlow(
        stateKeeper.consume(key, serializer) ?: initialState(),
    )

    val state: StateFlow<S> = mutableState.asStateFlow()

    val value: S get() = mutableState.value

    init {
        stateKeeper.register(key, serializer) { mutableState.value }
    }

    fun update(transform: (S) -> S) {
        mutableState.value = transform(mutableState.value)
    }
}
