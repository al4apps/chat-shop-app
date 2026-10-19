package com.al4apps.chatshop.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(platformConfig: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        platformConfig?.invoke(this)
        modules(appModules(testMode = DiConfig.TEST_MODE))
    }
}
