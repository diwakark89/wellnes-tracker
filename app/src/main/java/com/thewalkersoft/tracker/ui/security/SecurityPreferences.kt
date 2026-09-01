package com.thewalkersoft.tracker.ui.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SecurityPreferences {
    val isBiometricEnabled: StateFlow<Boolean>
    fun setBiometricEnabled(enabled: Boolean)

    companion object {
        operator fun invoke(context: Context): SecurityPreferences = SecurityPreferencesImpl(context)
    }
}

class SecurityPreferencesImpl(context: Context) : SecurityPreferences {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cycle_tracker_security_prefs", Context.MODE_PRIVATE)

    private val _isBiometricEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false))
    override val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    override fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
    }
}
