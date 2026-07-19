package com.al4apps.chatshop.core.arch

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Скоуп корутин компонента, привязанный к его [Lifecycle]:
 * отменяется автоматически при уничтожении компонента.
 */
class CoroutineFeature private constructor(
    mainContext: CoroutineContext,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = mainContext + SupervisorJob()

    companion object {

        fun attachedTo(
            lifecycle: Lifecycle,
            mainContext: CoroutineContext = Dispatchers.Main.immediate,
        ): CoroutineFeature = CoroutineFeature(mainContext).also { feature ->
            lifecycle.doOnDestroy { feature.cancel() }
        }
    }
}
