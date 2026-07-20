package com.al4apps.chatshop.features.home

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.features.chats.dialog.DefaultDialogComponent
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.firstorder.DefaultFirstOrderComponent
import com.al4apps.chatshop.features.chats.list.DefaultChatListComponent
import com.al4apps.chatshop.services.sync.SyncService
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val chatsInteractor: ChatsInteractor,
    private val syncService: SyncService,
) : HomeComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    override val stack: Value<ChildStack<*, HomeComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Bootstrap,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    init {
        // Bootstrap выполняется только на старте; после пересоздания процесса
        // стек восстановится сам, и активный конфиг будет не Bootstrap.
        if (stack.value.active.instance is HomeComponent.Child.Bootstrap) {
            scope.launch {
                syncService.syncDialogsOnce()
                val target = if (chatsInteractor.hasDialogs()) {
                    Config.ChatList
                } else {
                    Config.FirstOrder
                }
                navigation.replaceAll(target)
            }
        }
    }

    private fun createChild(
        config: Config,
        context: ComponentContext,
    ): HomeComponent.Child = when (config) {
        Config.Bootstrap -> HomeComponent.Child.Bootstrap

        Config.ChatList -> HomeComponent.Child.ChatList(
            DefaultChatListComponent(
                componentContext = context,
                chatsInteractor = chatsInteractor,
                onDialogSelected = { dialogId -> navigation.pushNew(Config.Dialog(dialogId)) },
            ),
        )

        Config.FirstOrder -> HomeComponent.Child.FirstOrder(
            DefaultFirstOrderComponent(componentContext = context),
        )

        is Config.Dialog -> HomeComponent.Child.Dialog(
            DefaultDialogComponent(
                componentContext = context,
                dialogId = config.dialogId,
                onBack = navigation::pop,
            ),
        )
    }

    @Serializable
    private sealed interface Config {

        @Serializable
        data object Bootstrap : Config

        @Serializable
        data object ChatList : Config

        @Serializable
        data object FirstOrder : Config

        @Serializable
        data class Dialog(val dialogId: String) : Config
    }
}
