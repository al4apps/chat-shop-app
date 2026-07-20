package com.al4apps.chatshop.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Единственный владелец состояния сессии. Создаётся один на приложение (Koin singleton).
 *
 * Источники переходов:
 * - старт приложения — [SessionState] по наличию токенов в [TokenStorage];
 * - успешный verify — [onAuthorized];
 * - невосстановимый 401 (refresh умер) — [onSessionExpired];
 * - выход пользователя — [onLoggedOut].
 */
class SessionManager(
    private val tokenStorage: TokenStorage,
) {

    private val mutableState = MutableStateFlow(initialState())

    val state: StateFlow<SessionState> = mutableState.asStateFlow()

    val isAuthorized: Boolean get() = mutableState.value == SessionState.Authorized

    /**
     * Помечает сессию авторизованной. Токены к этому моменту
     * уже сохранены вызывающей стороной.
     */
    fun onAuthorized() {
        mutableState.value = SessionState.Authorized
    }

    /** Сессия умерла (refresh невозможен): чистит токены и переводит в Unauthorized. */
    suspend fun onSessionExpired() {
        tokenStorage.clear()
        mutableState.value = SessionState.Unauthorized
    }

    /** Пользователь вышел: токены уже отозваны/удалены вызывающей стороной. */
    fun onLoggedOut() {
        mutableState.value = SessionState.Unauthorized
    }

    private fun initialState(): SessionState = if (tokenStorage.readInitial() != null) {
        SessionState.Authorized
    } else {
        SessionState.Unauthorized
    }
}
