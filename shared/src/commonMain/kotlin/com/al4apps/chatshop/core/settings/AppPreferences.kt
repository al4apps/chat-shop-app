package com.al4apps.chatshop.core.settings

/** Нешифрованные настройки приложения (не-секреты: флаги, UI-предпочтения). */
interface AppPreferences {

    /**
     * Показан ли онбординг.
     * Синхронное чтение — используется на старте для выбора стартового экрана.
     */
    fun isOnboardingShown(): Boolean

    /** Помечает онбординг показанным. Дожидается фиксации на диске. */
    suspend fun setOnboardingShown()
}
