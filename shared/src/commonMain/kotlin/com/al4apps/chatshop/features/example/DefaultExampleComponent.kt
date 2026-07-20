package com.al4apps.chatshop.features.example

import com.arkivanov.decompose.ComponentContext

class DefaultExampleComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
) : ExampleComponent, ComponentContext by componentContext {

    override fun onBackClick() {
        onBack()
    }
}
