package com.al4apps.chatshop.di

import com.al4apps.chatshop.core.db.DatabaseFactory
import com.al4apps.chatshop.core.media.AudioRecorder
import com.al4apps.chatshop.core.media.FileStore
import com.al4apps.chatshop.core.media.ImagePicker
import com.al4apps.chatshop.core.media.IosAudioRecorder
import com.al4apps.chatshop.core.media.IosImagePicker
import com.al4apps.chatshop.core.network.ApiConfig
import eu.anifantakis.lib.ksafe.KSafe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single(named(KSAFE_VAULT)) { KSafe(fileName = "vault") }
    single(named(KSAFE_PREFS)) { KSafe(fileName = "prefs") }
    // iOS-симулятор обращается к хост-машине напрямую через localhost.
    single { ApiConfig(baseUrl = "http://localhost:8080/api/v1") }
    single<HttpClientEngine> { Darwin.create() }
    single { DatabaseFactory() }
    single { FileStore() }
    single<ImagePicker> { IosImagePicker() }
    single<AudioRecorder> { IosAudioRecorder() }
}
