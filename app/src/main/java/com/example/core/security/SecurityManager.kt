package com.example.core.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class BiometricStatus {
    AVAILABLE,
    NONE_ENROLLED,
    NO_HARDWARE,
    UNAVAILABLE
}

enum class AutoLockTimeout(val label: String, val millis: Long) {
    IMMEDIATE("Immediately on exit", 0L),
    ONE_MINUTE("After 1 minute", 60_000L),
    FIVE_MINUTES("After 5 minutes", 300_000L)
}

@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("apartment_security_prefs", Context.MODE_PRIVATE)

    private val _isLockEnabled = MutableStateFlow(prefs.getBoolean("lock_enabled", false))
    val isLockEnabled: StateFlow<Boolean> = _isLockEnabled.asStateFlow()

    private val _isAppLocked = MutableStateFlow(prefs.getBoolean("lock_enabled", false))
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _biometricRequired = MutableStateFlow(prefs.getBoolean("biometric_required", true))
    val biometricRequired: StateFlow<Boolean> = _biometricRequired.asStateFlow()

    private val _autoLockTimeout = MutableStateFlow(
        AutoLockTimeout.entries.find { it.millis == prefs.getLong("autolock_timeout", 0L) }
            ?: AutoLockTimeout.IMMEDIATE
    )
    val autoLockTimeout: StateFlow<AutoLockTimeout> = _autoLockTimeout.asStateFlow()

    private var lastPausedTimestamp: Long = 0L

    fun checkBiometricStatus(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
        ) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            else -> BiometricStatus.UNAVAILABLE
        }
    }

    fun isBiometricAvailable(): Boolean {
        return checkBiometricStatus() == BiometricStatus.AVAILABLE
    }

    fun setLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("lock_enabled", enabled).apply()
        _isLockEnabled.value = enabled
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    fun setBiometricRequired(required: Boolean) {
        prefs.edit().putBoolean("biometric_required", required).apply()
        _biometricRequired.value = required
    }

    fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        prefs.edit().putLong("autolock_timeout", timeout.millis).apply()
        _autoLockTimeout.value = timeout
    }

    fun getSecurityPin(): String {
        return prefs.getString("security_pin", "1234") ?: "1234"
    }

    fun setSecurityPin(newPin: String) {
        prefs.edit().putString("security_pin", newPin).apply()
    }

    fun verifyPin(enteredPin: String): Boolean {
        val currentPin = getSecurityPin()
        val isValid = enteredPin == currentPin
        if (isValid) {
            unlockApp()
        }
        return isValid
    }

    fun lockApp() {
        if (_isLockEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun onAppPaused() {
        lastPausedTimestamp = System.currentTimeMillis()
    }

    fun onAppResumed() {
        if (!_isLockEnabled.value) return
        val elapsed = System.currentTimeMillis() - lastPausedTimestamp
        if (lastPausedTimestamp > 0L && elapsed >= _autoLockTimeout.value.millis) {
            _isAppLocked.value = true
        }
    }

    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            onError("Biometric sensor not enrolled or unavailable. Please use PIN.")
            return
        }

        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock ApartmentFlow")
                .setSubtitle("Authenticate to access personal & household ledgers")
                .setDescription("Biometric lock protects sensitive financial data on shared devices")
                .setNegativeButtonText("Use Passcode")
                .build()

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        unlockApp()
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Ignore user cancel error code so we don't flash unnecessary alarms
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && 
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                            errorCode != BiometricPrompt.ERROR_CANCELED) {
                            onError(errString.toString())
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Biometric authentication failed. Please try again or use PIN.")
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError("Could not start biometric authentication: ${e.localizedMessage}")
        }
    }
}
