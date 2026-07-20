package com.al4apps.chatshop.features.chats.firstorder

import com.arkivanov.decompose.ComponentContext

class DefaultFirstOrderComponent(
    componentContext: ComponentContext,
) : FirstOrderComponent, ComponentContext by componentContext
