package com.al4apps.chatshop.root

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.core.media.ImagePicker
import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.core.session.SessionState
import com.al4apps.chatshop.core.settings.AppPreferences
import com.al4apps.chatshop.features.auth.DefaultAuthComponent
import com.al4apps.chatshop.features.auth.domain.AuthInteractor
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.domain.MessagesInteractor
import com.al4apps.chatshop.features.example.DefaultExampleComponent
import com.al4apps.chatshop.features.home.DefaultHomeComponent
import com.al4apps.chatshop.features.onboarding.DefaultOnboardingComponent
import com.al4apps.chatshop.services.sync.SyncService
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.essenty.lifecycle.doOnStop
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val sessionManager: SessionManager,
    private val authInteractor: AuthInteractor,
    private val appPreferences: AppPreferences,
    private val syncService: SyncService,
    private val chatsInteractor: ChatsInteractor,
    private val messagesInteractor: MessagesInteractor,
    private val imagePicker: ImagePicker,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = when {
            sessionManager.isAuthorized -> Config.Home
            appPreferences.isOnboardingShown() -> Config.Auth
            else -> Config.Onboarding
        },
        handleBackButton = true,
        childFactory = ::createChild,
    )

    init {
        // Поллинг активен только на переднем плане (retained lifecycle транслирует
        // resume/stop от Activity / UIApplication на обеих платформах).
        lifecycle.doOnResume { syncService.onAppForeground() }
        lifecycle.doOnStop { syncService.onAppBackground() }

        // Сессия умерла (невосстановимый 401) — возврат на авторизацию с любого экрана.
        scope.launch {
            sessionManager.state
                .drop(1)
                .collect { state ->
                    if (state is SessionState.Unauthorized &&
                        stack.value.active.instance is RootComponent.Child.Home
                    ) {
                        navigation.replaceAll(Config.Auth)
                    }
                }
        }
    }

    private fun createChild(
        config: Config,
        context: ComponentContext,
    ): RootComponent.Child = when (config) {
        Config.Onboarding -> RootComponent.Child.Onboarding(
            DefaultOnboardingComponent(
                componentContext = context,
                appPreferences = appPreferences,
                onFinished = { navigation.replaceAll(Config.Auth) },
            ),
        )

        Config.Auth -> RootComponent.Child.Auth(
            DefaultAuthComponent(
                componentContext = context,
                authInteractor = authInteractor,
                onAuthorized = { navigation.replaceAll(Config.Home) },
            ),
        )

        Config.Home -> RootComponent.Child.Home(
            DefaultHomeComponent(
                componentContext = context,
                chatsInteractor = chatsInteractor,
                messagesInteractor = messagesInteractor,
                syncService = syncService,
                imagePicker = imagePicker,
            ),
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

        // Витрина дизайн-системы (dev-экран; входа из прод-UI нет, вызывается при необходимости).
        @Serializable
        data object Example : Config
    }
}
