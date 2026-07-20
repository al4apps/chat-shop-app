package com.al4apps.chatshop.core.session

/** Состояние пользовательской сессии. */
sealed interface SessionState {

    data object Authorized : SessionState

    data object Unauthorized : SessionState
}
