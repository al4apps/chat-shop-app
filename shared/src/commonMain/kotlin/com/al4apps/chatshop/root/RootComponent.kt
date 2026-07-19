package com.al4apps.chatshop.root

import com.al4apps.chatshop.features.auth.AuthComponent
import com.al4apps.chatshop.features.home.HomeComponent
import com.al4apps.chatshop.features.onboarding.OnboardingComponent
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>

    sealed interface Child {

        class Onboarding(val component: OnboardingComponent) : Child

        class Auth(val component: AuthComponent) : Child

        class Home(val component: HomeComponent) : Child
    }
}
