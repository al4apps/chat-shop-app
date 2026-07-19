package com.al4apps.chatshop.features.home

import com.arkivanov.decompose.ComponentContext

class DefaultHomeComponent(
    componentContext: ComponentContext,
) : HomeComponent, ComponentContext by componentContext
