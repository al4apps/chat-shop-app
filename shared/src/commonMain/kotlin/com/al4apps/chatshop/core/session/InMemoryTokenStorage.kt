package com.al4apps.chatshop.core.session

/**
 * Хранилище токенов только в памяти — для тестового режима.
 * Не переживает перезапуск процесса (режим живёт один рантайм).
 */
class InMemoryTokenStorage : TokenStorage {

    private var tokens: AuthTokens? = null

    override fun readInitial(): AuthTokens? = tokens

    override suspend fun read(): AuthTokens? = tokens

    override suspend fun save(tokens: AuthTokens) {
        this.tokens = tokens
    }

    override suspend fun clear() {
        tokens = null
    }
}
