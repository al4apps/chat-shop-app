package com.al4apps.chatshop.features.auth

import com.al4apps.chatshop.features.auth.code.DefaultCodeComponent
import com.al4apps.chatshop.features.auth.domain.AuthInteractor
import com.al4apps.chatshop.features.auth.phone.DefaultPhoneComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val authInteractor: AuthInteractor,
    private val onAuthorized: () -> Unit,
) : AuthComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, AuthComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Phone,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(
        config: Config,
        context: ComponentContext,
    ): AuthComponent.Child = when (config) {
        Config.Phone -> AuthComponent.Child.Phone(
            DefaultPhoneComponent(
                componentContext = context,
                authInteractor = authInteractor,
                onCodeRequested = { phone -> navigation.pushNew(Config.Code(phone)) },
            ),
        )

        is Config.Code -> AuthComponent.Child.Code(
            DefaultCodeComponent(
                componentContext = context,
                phone = config.phone,
                authInteractor = authInteractor,
                onBack = navigation::pop,
                onAuthorized = onAuthorized,
            ),
        )
    }

    @Serializable
    private sealed interface Config {

        @Serializable
        data object Phone : Config

        @Serializable
        data class Code(val phone: String) : Config
    }
}
