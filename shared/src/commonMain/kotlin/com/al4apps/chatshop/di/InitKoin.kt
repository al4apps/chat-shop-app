package com.al4apps.chatshop.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Запускает Koin с конфигурацией, полученной из entrypoint приложения.
 * Режим и base URL не задаются глобальной константой в common-коде.
 */
fun initKoin(
    appConfig: AppConfig,
    platformConfig: (KoinApplication.() -> Unit)? = null,
) {
    startKoin {
        platformConfig?.invoke(this)
        modules(appModules(appConfig))
    }
}
