package com.al4apps.chatshop.root

import com.al4apps.chatshop.features.auth.DefaultAuthComponent
import com.al4apps.chatshop.features.example.DefaultExampleComponent
import com.al4apps.chatshop.features.home.DefaultHomeComponent
import com.al4apps.chatshop.features.onboarding.DefaultOnboardingComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        // TODO(этапы 2–3): выбор стартового экрана по состоянию сессии и флагу онбординга.
        initialConfiguration = Config.Onboarding,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(
        config: Config,
        context: ComponentContext,
    ): RootComponent.Child = when (config) {
        Config.Onboarding -> RootComponent.Child.Onboarding(
            DefaultOnboardingComponent(
                componentContext = context,
                onFinished = { navigation.replaceAll(Config.Auth) },
                onDesignSystemRequested = { navigation.pushNew(Config.Example) },
            ),
        )

        Config.Auth -> RootComponent.Child.Auth(
            DefaultAuthComponent(
                componentContext = context,
                onAuthorized = { navigation.replaceAll(Config.Home) },
            ),
        )

        Config.Home -> RootComponent.Child.Home(
            DefaultHomeComponent(componentContext = context),
        )

        Config.Example -> RootComponent.Child.Example(
            DefaultExampleComponent(
                componentContext = context,
                onBack = navigation::pop,
            ),
        )
    }

    @Serializable
    private sealed interface Config {

        @Serializable
        data object Onboarding : Config

        @Serializable
        data object Auth : Config

        @Serializable
        data object Home : Config

        // Витрина дизайн-системы (dev-экран, вход с онбординга — временный).
        @Serializable
        data object Example : Config
    }
}
