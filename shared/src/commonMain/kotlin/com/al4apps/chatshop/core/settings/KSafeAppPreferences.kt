package com.al4apps.chatshop.core.settings

import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeWriteMode

/** Настройки поверх отдельного KSafe-инстанса `prefs` (без шифрования — Plain). */
class KSafeAppPreferences(
    private val prefs: KSafe,
) : AppPreferences {

    override fun isOnboardingShown(): Boolean = prefs.getDirect(KEY_ONBOARDING_SHOWN, false)

    override suspend fun setOnboardingShown() {
        prefs.put(KEY_ONBOARDING_SHOWN, true, mode = KSafeWriteMode.Plain)
    }

    private companion object {
        const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
    }
}
