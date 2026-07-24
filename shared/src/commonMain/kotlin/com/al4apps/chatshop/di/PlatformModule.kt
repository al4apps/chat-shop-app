package com.al4apps.chatshop.di

import org.koin.core.module.Module

/**
 * Платформенные зависимости, создаваемые на краю приложения:
 * KSafe (хранилища), ApiConfig (dev-хост платформы), Ktor-engine.
 */
expect val platformModule: Module

/** Квалификатор KSafe-инстанса для секретов (токены), шифрованный. */
const val KSAFE_VAULT = "vault"

/** Квалификатор KSafe-инстанса для настроек (не-секреты), Plain-записи. */
const val KSAFE_PREFS = "prefs"

/** Квалификатор HTTP-клиента без Auth (скачивание presigned-файлов). */
const val PLAIN_HTTP_CLIENT = "plainHttpClient"
