package com.al4apps.chatshop.features.chats.dialog

import com.arkivanov.decompose.ComponentContext

class DefaultDialogComponent(
    componentContext: ComponentContext,
    override val dialogId: String,
    private val onBack: () -> Unit,
) : DialogComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        onBack()
    }
}
