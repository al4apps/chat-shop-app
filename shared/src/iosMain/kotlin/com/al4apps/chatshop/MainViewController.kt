package com.al4apps.chatshop

import androidx.compose.ui.window.ComposeUIViewController
import com.al4apps.chatshop.di.initKoin
import com.al4apps.chatshop.root.DefaultRootComponent
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import platform.UIKit.UIViewController

private var isKoinStarted = false

fun MainViewController(): UIViewController {
    if (!isKoinStarted) {
        isKoinStarted = true
        initKoin()
    }

    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle = ApplicationLifecycle()),
    )

    return ComposeUIViewController {
        App(root)
    }
}
