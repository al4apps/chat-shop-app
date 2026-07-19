package com.al4apps.chatshop.features.auth

import com.arkivanov.decompose.ComponentContext

class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val onAuthorized: () -> Unit,
) : AuthComponent, ComponentContext by componentContext {

    override fun onLoginClick() {
        onAuthorized()
    }
}
