package com.al4apps.chatshop.features.auth

import com.al4apps.chatshop.features.auth.code.CodeComponent
import com.al4apps.chatshop.features.auth.phone.PhoneComponent
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

/** Фича авторизации: стек шагов Phone → Code. */
interface AuthComponent {

    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {

        class Phone(val component: PhoneComponent) : Child

        class Code(val component: CodeComponent) : Child
    }
}
