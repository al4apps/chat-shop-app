package com.al4apps.chatshop.features.onboarding

import com.al4apps.chatshop.core.arch.CoroutineFeature
import com.al4apps.chatshop.core.settings.AppPreferences
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.launch

class DefaultOnboardingComponent(
    componentContext: ComponentContext,
    private val appPreferences: AppPreferences,
    private val onFinished: () -> Unit,
) : OnboardingComponent, ComponentContext by componentContext {

    private val scope = CoroutineFeature.attachedTo(lifecycle)

    override fun onFinishClick() {
        scope.launch {
            appPreferences.setOnboardingShown()
            onFinished()
        }
    }
}
