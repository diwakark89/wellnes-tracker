package com.thewalkersoft.tracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.thewalkersoft.tracker.ui.navigation.MainAppNavigation
import com.thewalkersoft.tracker.ui.security.BiometricAuthHelper
import com.thewalkersoft.tracker.ui.theme.CycleTrackerTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as TrackerApplication).container
        val securityPreferences = appContainer.securityPreferences

        setContent {
            CycleTrackerTheme {
                val isBiometricEnabled by securityPreferences.isBiometricEnabled.collectAsState()
                var isUnlocked by remember { mutableStateOf(!isBiometricEnabled) }
                var authErrorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(isBiometricEnabled) {
                    if (isBiometricEnabled && !isUnlocked) {
                        BiometricAuthHelper.showBiometricPrompt(
                            activity = this@MainActivity,
                            onSuccess = {
                                isUnlocked = true
                                authErrorMessage = null
                            },
                            onError = { error ->
                                authErrorMessage = error
                            }
                        )
                    } else if (!isBiometricEnabled) {
                        isUnlocked = true
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isUnlocked) {
                        MainAppNavigation()
                    } else {
                        // Biometric Lock Screen
                        LockScreen(
                            errorMessage = authErrorMessage,
                            onUnlockClick = {
                                BiometricAuthHelper.showBiometricPrompt(
                                    activity = this@MainActivity,
                                    onSuccess = {
                                        isUnlocked = true
                                        authErrorMessage = null
                                    },
                                    onError = { error ->
                                        authErrorMessage = error
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LockScreen(
    errorMessage: String?,
    onUnlockClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Cycle Tracker Locked",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Biometric protection is enabled to keep your health data private.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onUnlockClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock with Biometrics", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}