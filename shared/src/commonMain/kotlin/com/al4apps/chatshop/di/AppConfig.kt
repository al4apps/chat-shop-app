package com.al4apps.chatshop.di

/**
 * Конфигурация приложения, создаваемая на краю (Android BuildConfig / iOS Info.plist).
 *
 * В release запрещён test mode и незашифрованный HTTP. Debug может использовать
 * локальный backend и мок-сервисы.
 */
data class AppConfig(
    val testMode: Boolean,
    val apiBaseUrl: String,
) {
    init {
        require(apiBaseUrl.isNotBlank()) { "apiBaseUrl must not be blank" }
        require(
            apiBaseUrl.startsWith("http://") || apiBaseUrl.startsWith("https://"),
        ) { "apiBaseUrl must start with http:// or https://" }
        require(testMode || apiBaseUrl.startsWith("https://")) {
            "Release apiBaseUrl must use https://"
        }
    }
}
