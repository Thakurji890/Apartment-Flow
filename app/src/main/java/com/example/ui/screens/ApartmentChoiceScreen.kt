package com.example.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.ApartmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentChoiceScreen(
    viewModel: ApartmentViewModel,
    prefilledInviteCode: String = "",
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.error.collectAsState()

    var showCreateForm by remember { mutableStateOf(false) }
    var showJoinForm by remember { mutableStateOf(prefilledInviteCode.isNotEmpty()) }

    var apartmentNameInput by remember { mutableStateOf("") }
    var inviteCodeInput by remember { mutableStateOf(prefilledInviteCode) }
    var localError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    // Intercept system Back button predictably on sub-forms
    BackHandler(enabled = showCreateForm || showJoinForm) {
        showCreateForm = false
        showJoinForm = false
        localError = null
    }

    // Auto-trigger join if prefilled code was provided and we land on this screen
    LaunchedEffect(prefilledInviteCode) {
        if (prefilledInviteCode.isNotEmpty()) {
            inviteCodeInput = prefilledInviteCode
            showJoinForm = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("apartment_choice_screen"),
        topBar = {
            if (!showCreateForm && !showJoinForm) {
                TopAppBar(
                    title = { },
                    actions = {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("account_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Account Options"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.testTag("account_dropdown_menu")
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sign Out") },
                                    onClick = {
                                        showMenu = false
                                        scope.launch {
                                            viewModel.leaveApartment()
                                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                        }
                                    },
                                    modifier = Modifier.testTag("menu_sign_out")
                                )
                                DropdownMenuItem(
                                    text = { Text("Switch Account") },
                                    onClick = {
                                        showMenu = false
                                        scope.launch {
                                            viewModel.leaveApartment()
                                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                            try {
                                                val credentialManager = androidx.credentials.CredentialManager.create(context)
                                                credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
                                            } catch (e: Exception) {
                                                Log.e("ApartmentChoiceScreen", "Error clearing credential state", e)
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("menu_switch_account")
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 500.dp)
                .testTag("choice_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // If in a sub-form, show a clean Back button
                if (showCreateForm || showJoinForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = {
                                showCreateForm = false
                                showJoinForm = false
                                localError = null
                            },
                            modifier = Modifier.testTag("choice_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Apartment Icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = if (showCreateForm) stringResource(R.string.apt_choice_create_title) else if (showJoinForm) stringResource(R.string.apt_choice_join_title) else stringResource(R.string.apt_choice_welcome),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (showCreateForm) {
                        stringResource(R.string.apt_choice_create_desc)
                    } else if (showJoinForm) {
                        stringResource(R.string.apt_choice_join_desc)
                    } else {
                        stringResource(R.string.apt_choice_default_desc)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Error Displays
                val activeError = localError ?: errorMsg
                AnimatedVisibility(visible = activeError != null) {
                    activeError?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    when {
                        showCreateForm -> {
                            // CREATE FORM
                            OutlinedTextField(
                                value = apartmentNameInput,
                                onValueChange = {
                                    apartmentNameInput = it
                                    localError = null
                                },
                                label = { Text(stringResource(R.string.apt_name_label)) },
                                placeholder = { Text(stringResource(R.string.apt_choice_name_hint)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_apt_name_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done,
                                    capitalization = KeyboardCapitalization.Words
                                )
                            )

                            Button(
                                onClick = {
                                    val name = apartmentNameInput.trim()
                                    if (name.isEmpty()) {
                                        localError = context.getString(R.string.apt_choice_enter_name)
                                        return@Button
                                    }
                                    localError = null
                                    viewModel.createApartment(name) { success, error ->
                                        if (success) {
                                            Toast.makeText(context, context.getString(R.string.apt_choice_create_success), Toast.LENGTH_SHORT).show()
                                            onSuccess()
                                        } else {
                                            localError = error ?: context.getString(R.string.apt_choice_create_failed)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("create_apt_submit_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.apt_choice_create_button), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        showJoinForm -> {
                            // JOIN FORM
                            OutlinedTextField(
                                value = inviteCodeInput,
                                onValueChange = {
                                    inviteCodeInput = it.uppercase()
                                    localError = null
                                },
                                label = { Text(stringResource(R.string.apt_choice_invite_code_label)) },
                                placeholder = { Text(stringResource(R.string.apt_choice_invite_code_hint)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("join_apt_code_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done,
                                    capitalization = KeyboardCapitalization.Characters
                                )
                            )

                            Button(
                                onClick = {
                                    val code = inviteCodeInput.trim()
                                    if (code.length != 6) {
                                        localError = context.getString(R.string.apt_choice_code_length_error)
                                        return@Button
                                    }
                                    localError = null
                                    viewModel.joinApartment(code) { success, error ->
                                        if (success) {
                                            Toast.makeText(context, context.getString(R.string.apt_choice_join_success), Toast.LENGTH_SHORT).show()
                                            onSuccess()
                                        } else {
                                            localError = error ?: context.getString(R.string.apt_choice_join_failed)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("join_apt_submit_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.apt_choice_join_button), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        else -> {
                            // INITIAL OPTIONS PANEL
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Create Option Card
                                Card(
                                    onClick = { showCreateForm = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("choice_option_create"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(20.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddBusiness,
                                            contentDescription = "Create Icon",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.apt_choice_create_new_card),
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = stringResource(R.string.apt_choice_create_new_sub),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowRight,
                                            contentDescription = "Arrow",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Join Option Card
                                Card(
                                    onClick = { showJoinForm = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("choice_option_join"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(20.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GroupAdd,
                                            contentDescription = "Join Icon",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.apt_choice_join_card),
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = stringResource(R.string.apt_choice_join_sub),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowRight,
                                            contentDescription = "Arrow",
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
