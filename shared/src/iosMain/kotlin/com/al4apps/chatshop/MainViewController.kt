package com.al4apps.chatshop

import androidx.compose.ui.window.ComposeUIViewController
import com.al4apps.chatshop.core.media.ImagePicker
import com.al4apps.chatshop.core.media.IosImagePicker
import com.al4apps.chatshop.di.initKoin
import com.al4apps.chatshop.root.DefaultRootComponent
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

private var isKoinStarted = false

fun MainViewController(): UIViewController {
    if (!isKoinStarted) {
        isKoinStarted = true
        initKoin()
    }
    val koin = KoinPlatform.getKoin()

    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle = ApplicationLifecycle()),
        sessionManager = koin.get(),
        authInteractor = koin.get(),
        appPreferences = koin.get(),
        syncService = koin.get(),
        chatsInteractor = koin.get(),
        messagesInteractor = koin.get(),
        imagePicker = koin.get(),
        audioRecorder = koin.get(),
        audioPlayer = koin.get(),
    )

    val controller = ComposeUIViewController {
        App(root)
    }
    (koin.get<ImagePicker>() as? IosImagePicker)?.attach(controller)
    return controller
}
