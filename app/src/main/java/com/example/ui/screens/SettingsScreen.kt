package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.ApartmentViewModel
import com.google.firebase.auth.FirebaseAuth

data class LanguageOption(val tag: String, val displayName: String)

val languageOptions = listOf(
    LanguageOption("en", "English"),
    LanguageOption("hi", "हिन्दी (Hindi)"),
    LanguageOption("es", "Español")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ApartmentViewModel,
    onNavigateToProfile: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var showLeaveConfirmation by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showEditAptDialog by remember { mutableStateOf(false) }

    var expandedCurrency by remember { mutableStateOf(false) }
    var expandedLanguage by remember { mutableStateOf(false) }
    var expandedTheme by remember { mutableStateOf(false) }

    val userDisplayName = currentUser?.displayName ?: "Guest Roommate"
    val userEmail = currentUser?.email ?: "guest@apartmentflow.com"
    val initials = userDisplayName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .take(2)
        .ifEmpty { "RM" }

    val currentCurrency by viewModel.currency.collectAsState()
    val currentLanguageTag by viewModel.appLanguage.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val currentAptName by viewModel.activeApartmentName.collectAsState()

    val displayLanguage = languageOptions.find { it.tag == currentLanguageTag }?.displayName ?: "English"
    val displayTheme = currentThemeMode.replaceFirstChar { it.uppercase() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.settings_header),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Clickable Profile Card (Navigates to ProfileScreen)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { onNavigateToProfile() }
                    .testTag("settings_profile_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userDisplayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = if (currentUser?.isAnonymous == true) stringResource(R.string.settings_profile_demo_guest) else stringResource(R.string.settings_profile_verified),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.cd_edit_profile),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }

            // Options List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_preferences),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Currency selector card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("settings_currency_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCurrency = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = stringResource(R.string.cd_currency_icon),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_base_currency),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = stringResource(R.string.settings_currency_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentCurrency,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(R.string.cd_dropdown_arrow)
                            )
                        }

                        DropdownMenu(
                            expanded = expandedCurrency,
                            onDismissRequest = { expandedCurrency = false }
                        ) {
                            listOf("USD ($)", "EUR (€)", "GBP (£)", "CAD ($)", "INR (₹)").forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text(curr) },
                                    onClick = {
                                        viewModel.saveCurrency(curr)
                                        expandedCurrency = false
                                    },
                                    modifier = Modifier.testTag("currency_option_${curr.split(" ")[0]}")
                                )
                            }
                        }
                    }
                }

                // Language selection card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("settings_language_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedLanguage = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = stringResource(R.string.cd_language_icon),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_language),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = stringResource(R.string.settings_language_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = displayLanguage,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(R.string.cd_dropdown_arrow)
                            )
                        }

                        DropdownMenu(
                            expanded = expandedLanguage,
                            onDismissRequest = { expandedLanguage = false }
                        ) {
                            languageOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        viewModel.saveLanguage(option.tag)
                                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                                            androidx.core.os.LocaleListCompat.forLanguageTags(option.tag)
                                        )
                                        expandedLanguage = false
                                    },
                                    modifier = Modifier.testTag("language_option_${option.tag}")
                                )
                            }
                        }
                    }
                }

                // Theme selection card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("settings_theme_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedTheme = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessMedium,
                                    contentDescription = stringResource(R.string.cd_theme_icon),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_theme),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = stringResource(R.string.settings_theme_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = displayTheme,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(R.string.cd_dropdown_arrow)
                            )
                        }

                        DropdownMenu(
                            expanded = expandedTheme,
                            onDismissRequest = { expandedTheme = false }
                        ) {
                            listOf("system", "light", "dark").forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        viewModel.saveThemeMode(mode)
                                        expandedTheme = false
                                    },
                                    modifier = Modifier.testTag("theme_option_$mode")
                                )
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.settings_maintenance),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Edit Apartment Name Card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("settings_edit_apt_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEditAptDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F0FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.cd_edit_apt_name),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_edit_apt),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = stringResource(R.string.settings_edit_apt_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.cd_action_arrow),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                // Leave Apartment Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_leave_apartment_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLeaveConfirmation = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF3CD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = stringResource(R.string.cd_leave_icon),
                                    tint = Color(0xFF856404)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_leave),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF856404)
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.settings_leave_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.cd_action_arrow),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                // Log Out Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_logout_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutConfirmation = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFDAD6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = stringResource(R.string.cd_logout_icon),
                                    tint = Color(0xFFBA1A1A)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_logout),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFBA1A1A)
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.settings_logout_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.cd_action_arrow),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                // Info Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_info_title),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = stringResource(R.string.settings_info_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.settings_info_version_template, "2.0.0 (Firebase)"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = stringResource(R.string.settings_info_env_template, "Android SDK 35"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // Leave Apartment Confirmation Dialog
        if (showLeaveConfirmation) {
            AlertDialog(
                onDismissRequest = { showLeaveConfirmation = false },
                title = {
                    Text(
                        text = stringResource(R.string.settings_leave_dialog_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.settings_leave_dialog_text),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.leaveApartment()
                            showLeaveConfirmation = false
                            Toast.makeText(context, context.getString(R.string.settings_toast_left), Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("leave_confirm_button")
                    ) {
                        Text(stringResource(R.string.settings_leave_dialog_confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLeaveConfirmation = false },
                        modifier = Modifier.testTag("leave_cancel_button")
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                title = {
                    Text(
                        text = stringResource(R.string.settings_logout_dialog_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.settings_logout_dialog_text),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            auth.signOut()
                            showLogoutConfirmation = false
                            Toast.makeText(context, context.getString(R.string.settings_toast_logged_out), Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("logout_confirm_button")
                    ) {
                        Text(stringResource(R.string.settings_logout))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutConfirmation = false },
                        modifier = Modifier.testTag("logout_cancel_button")
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        // Edit Apartment Name Dialog
        if (showEditAptDialog) {
            var newAptNameInput by remember { mutableStateOf(currentAptName ?: "") }
            AlertDialog(
                onDismissRequest = { showEditAptDialog = false },
                title = { Text(stringResource(R.string.settings_edit_apt_dialog_title), fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newAptNameInput,
                        onValueChange = { newAptNameInput = it },
                        label = { Text(stringResource(R.string.settings_edit_apt_dialog_label)) },
                        modifier = Modifier.fillMaxWidth().testTag("edit_apt_name_input"),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newAptNameInput.trim().isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.settings_toast_name_empty), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.updateApartmentName(newAptNameInput.trim()) { success, err ->
                                if (success) {
                                    Toast.makeText(context, context.getString(R.string.settings_toast_name_updated), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.settings_toast_name_failed, err ?: ""), Toast.LENGTH_LONG).show()
                                }
                                showEditAptDialog = false
                            }
                        },
                        modifier = Modifier.testTag("edit_apt_name_confirm_button")
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditAptDialog = false },
                        modifier = Modifier.testTag("edit_apt_name_cancel_button")
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    }
}
