package com.al4apps.chatshop.di

import com.al4apps.chatshop.core.db.DatabaseFactory
import com.al4apps.chatshop.core.network.HttpClientFactory
import com.al4apps.chatshop.core.session.InMemoryTokenStorage
import com.al4apps.chatshop.core.session.KSafeTokenStorage
import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.core.session.TokenStorage
import com.al4apps.chatshop.core.settings.AppPreferences
import com.al4apps.chatshop.core.settings.KSafeAppPreferences
import com.al4apps.chatshop.db.ChatShopDatabase
import com.al4apps.chatshop.features.auth.data.AuthApi
import com.al4apps.chatshop.features.auth.data.DefaultAuthApi
import com.al4apps.chatshop.features.auth.data.DefaultAuthInteractor
import com.al4apps.chatshop.features.auth.data.TestAuthApi
import com.al4apps.chatshop.features.auth.domain.AuthInteractor
import com.al4apps.chatshop.features.chats.data.ChatsApi
import com.al4apps.chatshop.features.chats.data.DefaultChatsApi
import com.al4apps.chatshop.features.chats.data.DefaultChatsInteractor
import com.al4apps.chatshop.features.chats.data.DefaultDialogsRepository
import com.al4apps.chatshop.features.chats.data.DefaultFilesRepository
import com.al4apps.chatshop.features.chats.data.DefaultMessagesInteractor
import com.al4apps.chatshop.features.chats.data.DefaultMessagesRepository
import com.al4apps.chatshop.features.chats.data.test.TestChatsApi
import com.al4apps.chatshop.features.chats.data.test.TestChatsStore
import com.al4apps.chatshop.features.chats.domain.ChatsInteractor
import com.al4apps.chatshop.features.chats.domain.DialogsRepository
import com.al4apps.chatshop.features.chats.domain.FilesRepository
import com.al4apps.chatshop.features.chats.domain.MessagesInteractor
import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import com.al4apps.chatshop.services.files.FileDownloadService
import com.al4apps.chatshop.services.outbox.OutboxService
import com.al4apps.chatshop.services.sync.DefaultSyncApi
import com.al4apps.chatshop.services.sync.SyncApi
import com.al4apps.chatshop.services.sync.SyncService
import com.al4apps.chatshop.services.sync.test.TestSyncApi
import com.al4apps.chatshop.services.testmode.TestAutoReplyService
import com.al4apps.chatshop.services.testmode.TestDataInitializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Собирает граф приложения. Режим (прод/тест) выбирается в DI ([DiConfig.TEST_MODE]):
 * подменяется набор реализаций сервисов, общая логика и модули не меняются.
 */
fun appModules(testMode: Boolean): List<Module> = buildList {
    addAll(commonModules())
    add(if (testMode) testDataModule else productionDataModule)
}

private fun commonModules(): List<Module> = listOf(
    platformModule,
    coreModule,
    networkModule,
    dbModule,
    authModule,
    chatsModule,
    syncModule,
    outboxModule,
    filesModule,
)

private val coreModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
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
    // Плейн-клиент без Auth — для скачивания presigned-файлов (§8: GET без Authorization).
    single<HttpClient>(named(PLAIN_HTTP_CLIENT)) {
        HttpClient(get<HttpClientEngine>()) {
            expectSuccess = false
        }
    }
}

private val dbModule = module {
    single { ChatShopDatabase(driver = get<DatabaseFactory>().createDriver()) }
}

private val authModule = module {
    single<AuthInteractor> {
        DefaultAuthInteractor(
            authApi = get(),
            tokenStorage = get(),
            sessionManager = get(),
        )
    }
}

private val chatsModule = module {
    single<DialogsRepository> { DefaultDialogsRepository(db = get()) }
    single<MessagesRepository> { DefaultMessagesRepository(db = get()) }
    single<FilesRepository> { DefaultFilesRepository(db = get()) }
    single<ChatsInteractor> {
        DefaultChatsInteractor(
            chatsApi = get(),
            dialogsRepository = get(),
        )
    }
    single<MessagesInteractor> {
        DefaultMessagesInteractor(
            messagesRepository = get(),
            fileStore = get(),
        )
    }
}

private val syncModule = module {
    single {
        SyncService(
            syncApi = get(),
            dialogsRepository = get(),
            messagesRepository = get(),
            sessionManager = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}

private val outboxModule = module {
    // createdAtStart: сервис ни в кого не инжектится, но должен работать с запуска.
    single(createdAtStart = true) {
        OutboxService(
            chatsApi = get(),
            messagesRepository = get(),
            filesRepository = get(),
            fileStore = get(),
            sessionManager = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}

private val filesModule = module {
    // createdAtStart: скачиватель ни в кого не инжектится, работает с запуска.
    single(createdAtStart = true) {
        FileDownloadService(
            plainClient = get(named(PLAIN_HTTP_CLIENT)),
            filesRepository = get(),
            fileStore = get(),
            sessionManager = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}

/** Прод: сетевые API и токены в защищённом хранилище. */
private val productionDataModule = module {
    single<TokenStorage> { KSafeTokenStorage(ksafe = get(named(KSAFE_VAULT))) }
    single<AuthApi> { DefaultAuthApi(client = get()) }
    single<ChatsApi> { DefaultChatsApi(client = get()) }
    single<SyncApi> { DefaultSyncApi(client = get()) }
}

/** Тест: мок-API без сети, токены в памяти, чистая БД с мок-данными и авто-ответы. */
private val testDataModule = module {
    single<TokenStorage> { InMemoryTokenStorage() }
    single<AuthApi> { TestAuthApi() }
    single { TestChatsStore() }
    single<ChatsApi> { TestChatsApi(store = get()) }
    single<SyncApi> { TestSyncApi(store = get()) }
    single(createdAtStart = true) {
        TestAutoReplyService(
            store = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
    single(createdAtStart = true) {
        TestDataInitializer(
            db = get(),
            store = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}
