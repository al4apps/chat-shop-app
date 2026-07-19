package com.al4apps.chatshop.features.onboarding

import com.arkivanov.decompose.ComponentContext

class DefaultOnboardingComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
) : OnboardingComponent, ComponentContext by componentContext {

    override fun onContinueClick() {
        onFinished()
    }
}
