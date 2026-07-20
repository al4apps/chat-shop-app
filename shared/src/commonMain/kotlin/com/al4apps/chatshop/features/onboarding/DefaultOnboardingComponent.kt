package com.al4apps.chatshop.features.onboarding

import com.arkivanov.decompose.ComponentContext

class DefaultOnboardingComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,
    private val onDesignSystemRequested: () -> Unit,
) : OnboardingComponent, ComponentContext by componentContext {

    override fun onContinueClick() {
        onFinished()
    }

    override fun onDesignSystemClick() {
        onDesignSystemRequested()
    }
}
