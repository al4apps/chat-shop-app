package com.al4apps.chatshop.di

import kotlin.test.Test
import kotlin.test.assertEquals

class AppConfigTest {

    @Test
    fun keepsTestModeAndUrl() {
        val config = AppConfig(
            testMode = true,
            apiBaseUrl = "http://10.0.2.2:8080/api/v1",
        )

        assertEquals(true, config.testMode)
        assertEquals("http://10.0.2.2:8080/api/v1", config.apiBaseUrl)
    }

    @Test
    fun keepsRealModeAndProductionUrl() {
        val config = AppConfig(
            testMode = false,
            apiBaseUrl = "https://api.example.invalid/api/v1",
        )

        assertEquals(false, config.testMode)
        assertEquals("https://api.example.invalid/api/v1", config.apiBaseUrl)
    }
}
