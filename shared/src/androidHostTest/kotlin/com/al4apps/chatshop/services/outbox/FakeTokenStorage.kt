package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.core.session.AuthTokens
import com.al4apps.chatshop.core.session.TokenStorage

/** Простейшее хранилище токенов для host-теста. */
class FakeTokenStorage : TokenStorage {

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
