package com.example.core.security.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.security.AutoLockTimeout
import com.example.core.security.BiometricStatus
import com.example.core.security.SecurityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsDialog(
    securityManager: SecurityManager,
    onDismiss: () -> Unit,
    onLockNow: () -> Unit
) {
    val isLockEnabled by securityManager.isLockEnabled.collectAsState()
    val biometricRequired by securityManager.biometricRequired.collectAsState()
    val autoLockTimeout by securityManager.autoLockTimeout.collectAsState()
    val biometricStatus = remember { securityManager.checkBiometricStatus() }

    var showChangePinSection by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }
    var confirmPinText by remember { mutableStateOf("") }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }
    var pinSuccessMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "App Lock & Privacy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Protect personal expenses, bill shares, and apartment ledgers on shared household tablets and phones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Master Toggle: App Lock
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLockEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Require App Lock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Locks apartment finances when leaving the app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isLockEnabled,
                            onCheckedChange = { securityManager.setLockEnabled(it) }
                        )
                    }
                }

                if (isLockEnabled) {
                    // Biometric Support Status Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = if (biometricStatus == BiometricStatus.AVAILABLE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "Biometric Hardware Status",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            val statusDesc = when (biometricStatus) {
                                BiometricStatus.AVAILABLE -> "Fingerprint & Face ID enrolled and ready"
                                BiometricStatus.NONE_ENROLLED -> "Biometric hardware detected, but no fingerprints/face enrolled in device settings. PIN fallback active."
                                BiometricStatus.NO_HARDWARE -> "No biometric hardware found on device. 4-digit PIN security active."
                                BiometricStatus.UNAVAILABLE -> "Biometrics currently unavailable. PIN security active."
                            }
                            Text(
                                text = statusDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (biometricStatus == BiometricStatus.AVAILABLE) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Prioritize Biometrics",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Switch(
                                        checked = biometricRequired,
                                        onCheckedChange = { securityManager.setBiometricRequired(it) }
                                    )
                                }
                            }
                        }
                    }

                    // Auto-Lock Timeout Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Auto-Lock Timer",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AutoLockTimeout.entries.forEach { timeout ->
                                val isSelected = autoLockTimeout == timeout
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { securityManager.setAutoLockTimeout(timeout) },
                                    label = {
                                        Text(
                                            text = when (timeout) {
                                                AutoLockTimeout.IMMEDIATE -> "Instant"
                                                AutoLockTimeout.ONE_MINUTE -> "1 min"
                                                AutoLockTimeout.FIVE_MINUTES -> "5 min"
                                            },
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Passcode Configuration
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Security PIN",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { showChangePinSection = !showChangePinSection }) {
                                Text(if (showChangePinSection) "Cancel" else "Change PIN")
                            }
                        }

                        if (!showChangePinSection) {
                            Text(
                                text = "Current PIN: •••• (Default: 1234)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            OutlinedTextField(
                                value = newPinText,
                                onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) newPinText = it },
                                label = { Text("New 4-digit PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = confirmPinText,
                                onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) confirmPinText = it },
                                label = { Text("Confirm PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            pinErrorMessage?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            pinSuccessMessage?.let {
                                Text(text = it, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)
                            }

                            Button(
                                onClick = {
                                    if (newPinText.length != 4) {
                                        pinErrorMessage = "PIN must be exactly 4 digits."
                                        pinSuccessMessage = null
                                    } else if (newPinText != confirmPinText) {
                                        pinErrorMessage = "PINs do not match."
                                        pinSuccessMessage = null
                                    } else {
                                        securityManager.setSecurityPin(newPinText)
                                        pinErrorMessage = null
                                        pinSuccessMessage = "Passcode updated successfully!"
                                        newPinText = ""
                                        confirmPinText = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save New PIN")
                            }
                        }
                    }

                    HorizontalDivider()

                    // Immediate Lock Action
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onLockNow()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock App Now")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
