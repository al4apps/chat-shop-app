package com.al4apps.chatshop.di

/**
 * Переключатель источника данных, независимый от debug/release.
 *
 * `true` — моки/test mode; `false` — реальный backend.
 * Перед release-сборкой выставить `false` и проверить реальный auth/чаты.
 */
object DevConfig {
    const val TEST_MODE: Boolean = true
}
