package com.al4apps.chatshop.di

import com.al4apps.chatshop.core.db.DatabaseFactory
import com.al4apps.chatshop.core.network.HttpClientFactory
import com.al4apps.chatshop.core.session.KSafeTokenStorage
import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.core.session.TokenStorage
import com.al4apps.chatshop.core.settings.AppPreferences
import com.al4apps.chatshop.core.settings.KSafeAppPreferences
import com.al4apps.chatshop.db.ChatShopDatabase
import com.al4apps.chatshop.features.auth.data.AuthApi
import com.al4apps.chatshop.features.auth.data.DefaultAuthInteractor
import com.al4apps.chatshop.features.auth.domain.AuthInteractor
import com.al4apps.chatshop.features.chats.data.DefaultDialogsRepository
import com.al4apps.chatshop.features.chats.data.DefaultMessagesRepository
import com.al4apps.chatshop.features.chats.domain.DialogsRepository
import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun appModules(): List<Module> = listOf(
    platformModule,
    coreModule,
    networkModule,
    dbModule,
    authModule,
    chatsModule,
)

private val coreModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    single<TokenStorage> { KSafeTokenStorage(ksafe = get(named(KSAFE_VAULT))) }
    single { SessionManager(tokenStorage = get()) }
    single<AppPreferences> { KSafeAppPreferences(prefs = get(named(KSAFE_PREFS))) }
}

private val networkModule = module {
    single<HttpClient> {
        HttpClientFactory(
            engine = get(),
            config = get(),
            json = get(),
            tokenStorage = get(),
            sessionManager = get(),
        ).create()
    }
}

private val dbModule = module {
    single { ChatShopDatabase(driver = get<DatabaseFactory>().createDriver()) }
}

private val chatsModule = module {
    single<DialogsRepository> { DefaultDialogsRepository(db = get()) }
    single<MessagesRepository> { DefaultMessagesRepository(db = get()) }
}

private val authModule = module {
    single { AuthApi(client = get()) }
    single<AuthInteractor> {
        DefaultAuthInteractor(
            authApi = get(),
            tokenStorage = get(),
            sessionManager = get(),
        )
    }
}
