package com.al4apps.chatshop.features.home

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.features.chats.domain.DialogsRepository
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DefaultHomeComponent(
    componentContext: ComponentContext,
    dialogsRepository: DialogsRepository,
) : HomeComponent, ComponentContext by componentContext {

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    override val dialogsCount: StateFlow<Int> = dialogsRepository.observeDialogs()
        .map { it.size }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = 0)
}
