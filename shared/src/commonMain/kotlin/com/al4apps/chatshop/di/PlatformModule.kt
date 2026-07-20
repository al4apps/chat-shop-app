package com.al4apps.chatshop.di

import org.koin.core.module.Module

/**
 * Платформенные зависимости, создаваемые на краю приложения:
 * KSafe (хранилище), ApiConfig (dev-хост платформы), Ktor-engine.
 */
expect val platformModule: Module
