package com.al4apps.chatshop.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModules())
    }
}
