package com.al4apps.chatshop.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppConfigTest {

    @Test
    fun debugAllowsHttpAndTestMode() {
        val config = AppConfig(
            testMode = true,
            apiBaseUrl = "http://10.0.2.2:8080/api/v1",
        )

        assertEquals(true, config.testMode)
        assertEquals("http://10.0.2.2:8080/api/v1", config.apiBaseUrl)
    }

    @Test
    fun releaseAllowsHttps() {
        val config = AppConfig(
            testMode = false,
            apiBaseUrl = "https://api.example.invalid/api/v1",
        )

        assertEquals(false, config.testMode)
    }

    @Test
    fun releaseRejectsHttp() {
        assertFailsWith<IllegalArgumentException> {
            AppConfig(
                testMode = false,
                apiBaseUrl = "http://api.example.invalid/api/v1",
            )
        }
    }

    @Test
    fun rejectsBlankAndInvalidScheme() {
        assertFailsWith<IllegalArgumentException> {
            AppConfig(testMode = true, apiBaseUrl = "")
        }
        assertFailsWith<IllegalArgumentException> {
            AppConfig(testMode = true, apiBaseUrl = "api.example.invalid")
        }
    }
}
