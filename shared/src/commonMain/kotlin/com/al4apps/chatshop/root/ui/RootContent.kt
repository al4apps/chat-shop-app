package com.al4apps.chatshop.root.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.al4apps.chatshop.features.auth.ui.AuthScreen
import com.al4apps.chatshop.features.home.ui.HomeScreen
import com.al4apps.chatshop.features.onboarding.ui.OnboardingScreen
import com.al4apps.chatshop.root.RootComponent
import com.arkivanov.decompose.extensions.compose.stack.Children

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = component.stack,
        modifier = modifier,
    ) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.Onboarding -> OnboardingScreen(instance.component)
            is RootComponent.Child.Auth -> AuthScreen(instance.component)
            is RootComponent.Child.Home -> HomeScreen(instance.component)
        }
    }
}
