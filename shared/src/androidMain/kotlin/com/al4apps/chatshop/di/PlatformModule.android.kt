package com.al4apps.chatshop.di

import com.al4apps.chatshop.core.db.DatabaseFactory
import com.al4apps.chatshop.core.network.ApiConfig
import eu.anifantakis.lib.ksafe.KSafe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single(named(KSAFE_VAULT)) {
        KSafe(context = androidContext().applicationContext, fileName = "vault")
    }
    single(named(KSAFE_PREFS)) {
        KSafe(context = androidContext().applicationContext, fileName = "prefs")
    }
    // Android-эмулятор видит хост-машину как 10.0.2.2.
    single { ApiConfig(baseUrl = "http://10.0.2.2:8080/api/v1") }
    single<HttpClientEngine> { OkHttp.create() }
    single { DatabaseFactory(context = androidContext().applicationContext) }
}
