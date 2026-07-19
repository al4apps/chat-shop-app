package com.al4apps.chatshop.di

import org.koin.core.module.Module
import org.koin.dsl.module

fun appModules(): List<Module> = listOf(
    coreModule,
)

// Наполняется по мере этапов: network, db, session, chats, services.
private val coreModule = module { }
