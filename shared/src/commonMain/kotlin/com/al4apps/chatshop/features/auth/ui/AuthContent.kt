package com.al4apps.chatshop.features.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.al4apps.chatshop.features.auth.AuthComponent
import com.al4apps.chatshop.features.auth.code.ui.CodeScreen
import com.al4apps.chatshop.features.auth.phone.ui.PhoneScreen
import com.arkivanov.decompose.extensions.compose.stack.Children

@Composable
fun AuthContent(
    component: AuthComponent,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = component.stack,
        modifier = modifier,
    ) { child ->
        when (val instance = child.instance) {
            is AuthComponent.Child.Phone -> PhoneScreen(instance.component)
            is AuthComponent.Child.Code -> CodeScreen(instance.component)
        }
    }
}
