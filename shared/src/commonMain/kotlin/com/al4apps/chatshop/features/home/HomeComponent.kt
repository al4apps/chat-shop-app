package com.al4apps.chatshop.features.home

import com.al4apps.chatshop.features.chats.dialog.DialogComponent
import com.al4apps.chatshop.features.chats.firstorder.FirstOrderComponent
import com.al4apps.chatshop.features.chats.list.ChatListComponent
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

/**
 * Контейнер всего после авторизации. Стартовый роутинг: Bootstrap (первичная
 * синхронизация) → список чатов, если диалоги есть, иначе экран первого заказа.
 */
interface HomeComponent {

    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {

        data object Bootstrap : Child

        class ChatList(val component: ChatListComponent) : Child

        class FirstOrder(val component: FirstOrderComponent) : Child

        class Dialog(val component: DialogComponent) : Child
    }
}
