package com.al4apps.chatshop.di

import com.al4apps.chatshop.core.network.ApiConfig
import eu.anifantakis.lib.ksafe.KSafe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { KSafe(fileName = "vault") }
    // iOS-симулятор обращается к хост-машине напрямую через localhost.
    single { ApiConfig(baseUrl = "http://localhost:8080/api/v1") }
    single<HttpClientEngine> { Darwin.create() }
}
