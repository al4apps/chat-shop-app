package com.al4apps.chatshop.core.session

import eu.anifantakis.lib.ksafe.KSafe

/**
 * Хранилище токенов поверх KSafe (шифрование AES-256-GCM,
 * ключ — в Keystore/Keychain платформы).
 */
class KSafeTokenStorage(
    private val ksafe: KSafe,
) : TokenStorage {

    override fun readInitial(): AuthTokens? = ksafe.getDirect<AuthTokens?>(KEY, null)

    override suspend fun read(): AuthTokens? = ksafe.get<AuthTokens?>(KEY, null)

    override suspend fun save(tokens: AuthTokens) {
        ksafe.put(KEY, tokens)
    }

    override suspend fun clear() {
        ksafe.delete(KEY)
    }

    private companion object {
        const val KEY = "auth_tokens"
    }
}
