package com.thewalkersoft.tracker.ui.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cycle_tracker_security_prefs", Context.MODE_PRIVATE)

    private val _isBiometricEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
    }
}
