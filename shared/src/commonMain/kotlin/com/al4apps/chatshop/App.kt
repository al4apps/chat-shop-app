package com.al4apps.chatshop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.al4apps.chatshop.root.RootComponent
import com.al4apps.chatshop.root.ui.RootContent

@Composable
fun App(root: RootComponent) {
    MaterialTheme {
        RootContent(
            component = root,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
