package com.al4apps.chatshop.features.auth.domain

import com.al4apps.chatshop.core.network.AppResult

/** Сценарии авторизации: вход по телефону + SMS-коду, выход. */
interface AuthInteractor {

    /**
     * Запрашивает отправку SMS-кода на номер.
     *
     * Поведение: `POST /auth/request-code`. Повторный запрос раньше минуты — `429`.
     *
     * @param phone номер в формате `+7XXXXXXXXXX`.
     * @return успех без данных; ошибки: 400 (невалидный номер), 429 (слишком часто), сеть.
     */
    suspend fun requestCode(phone: String): AppResult<Unit>

    /**
     * Подтверждает вход кодом из SMS.
     *
     * Поведение: `POST /auth/verify` с меткой устройства; при успехе пара токенов
     * атомарно сохраняется в защищённое хранилище и сессия помечается авторизованной —
     * дополнительных действий вызывающему не требуется.
     *
     * @param phone номер, на который запрашивался код.
     * @param code 6-значный код из SMS.
     * @return успех без данных; ошибки: 400 (неверный код), сеть.
     */
    suspend fun verify(phone: String, code: String): AppResult<Unit>

    /**
     * Выход: отзывает refresh-токен на сервере (`POST /auth/logout`), затем чистит
     * локальное хранилище и переводит сессию в Unauthorized независимо от ответа сервера.
     */
    suspend fun logout()
}
