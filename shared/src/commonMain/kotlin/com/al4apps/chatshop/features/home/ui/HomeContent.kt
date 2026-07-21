package com.al4apps.chatshop.features.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.features.chats.dialog.ui.DialogScreen
import com.al4apps.chatshop.features.chats.firstorder.ui.FirstOrderScreen
import com.al4apps.chatshop.features.chats.list.ui.ChatListScreen
import com.al4apps.chatshop.features.home.HomeComponent
import com.arkivanov.decompose.extensions.compose.stack.Children

@Composable
fun HomeContent(
    component: HomeComponent,
    modifier: Modifier = Modifier,
) {
    Scaffold {
        Children(
            stack = component.stack,
            modifier = modifier.padding(it),
        ) { child ->
            when (val instance = child.instance) {
                is HomeComponent.Child.Bootstrap -> BootstrapContent()
                is HomeComponent.Child.ChatList -> ChatListScreen(instance.component)
                is HomeComponent.Child.FirstOrder -> FirstOrderScreen(instance.component)
                is HomeComponent.Child.Dialog -> DialogScreen(instance.component)
            }
        }
    }
}

@Composable
private fun BootstrapContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AppTheme.colors.accent)
    }
}
