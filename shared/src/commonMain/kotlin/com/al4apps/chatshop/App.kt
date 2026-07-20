package com.al4apps.chatshop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.al4apps.chatshop.core.design.theme.AppTheme
import com.al4apps.chatshop.root.RootComponent
import com.al4apps.chatshop.root.ui.RootContent

@Composable
fun App(root: RootComponent) {
    AppTheme {
        RootContent(
            component = root,
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
        )
    }
}
