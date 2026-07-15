package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.ApartmentViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val pendingInviteCode = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize real repository with contextual preference storage
        com.example.data.ApartmentRepository.getInstance().initialize(applicationContext)

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            val viewModel: ApartmentViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = darkTheme) {
                val codeState by pendingInviteCode.collectAsState()
                MainAppContainer(
                    viewModel = viewModel,
                    pendingInviteCode = codeState,
                    onInviteHandled = { pendingInviteCode.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null) {
            val segments = data.pathSegments
            if (segments.size >= 2 && segments[0] == "join") {
                val code = segments[1]
                if (code.length == 6) {
                    pendingInviteCode.value = code.uppercase()
                }
            }
        }
    }
}

@Composable
fun MainAppContainer(
    viewModel: ApartmentViewModel,
    pendingInviteCode: String?,
    onInviteHandled: () -> Unit
) {
    val context = LocalContext.current

    // Observe Auth state reactively from FirebaseAuth
    var firebaseUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            firebaseUser = auth.currentUser
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    // Observe activeApartmentId from viewmodel
    val activeApartmentId by viewModel.activeApartmentId.collectAsState()

    // 1. AUTHENTICATION SHIELD
    if (firebaseUser == null) {
        LoginScreen(
            onLoginSuccess = {
                firebaseUser = FirebaseAuth.getInstance().currentUser
            }
        )
        return
    }

    // 2. DEEP LINK AUTO-JOIN FLOW
    LaunchedEffect(pendingInviteCode, firebaseUser) {
        if (firebaseUser != null && pendingInviteCode != null) {
            // Attempt auto-joining the scanned household code
            viewModel.joinApartment(pendingInviteCode) { success, error ->
                if (success) {
                    Toast.makeText(context, "Successfully joined apartment group via link!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to join via link: $error", Toast.LENGTH_LONG).show()
                }
                onInviteHandled()
            }
        }
    }

    // 3. APARTMENT BOUNDARY CHECK
    if (activeApartmentId == null) {
        ApartmentChoiceScreen(
            viewModel = viewModel,
            prefilledInviteCode = pendingInviteCode ?: "",
            onSuccess = {
                onInviteHandled()
            }
        )
        return
    }

    // 4. MAIN COHESIVE HOUSEHOLD INTERFACE
    val navController = rememberNavController()
    val roommates by viewModel.roommates.collectAsState()
    val suggestedDebts by viewModel.debts.collectAsState()

    // Global Settlement Dialog trigger
    var showSettleDialog by remember { mutableStateOf(false) }
    var prefilledDebtorId by remember { mutableStateOf("") }
    var prefilledCreditorId by remember { mutableStateOf("") }
    var prefilledAmountStr by remember { mutableStateOf("") }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Home Screen Tab
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(R.string.tab_home),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_home),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (currentRoute == "home") FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_home_tab")
                )

                // Bills Screen Tab
                NavigationBarItem(
                    selected = currentRoute == "bills",
                    onClick = {
                        navController.navigate("bills") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = stringResource(R.string.tab_bills),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_bills),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (currentRoute == "bills") FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_bills_tab")
                )

                // People Screen Tab
                NavigationBarItem(
                    selected = currentRoute == "people",
                    onClick = {
                        navController.navigate("people") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = stringResource(R.string.tab_people),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_people),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (currentRoute == "people") FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_people_tab")
                )

                // Settings Screen Tab
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.tab_settings),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_settings),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (currentRoute == "settings") FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToBills = { navController.navigate("bills") },
                    onNavigateToPeople = { navController.navigate("people") },
                    onAddBillClick = { navController.navigate("bills") },
                    onSettleUpClick = {
                        prefilledDebtorId = roommates.firstOrNull()?.id ?: ""
                        prefilledCreditorId = roommates.getOrNull(1)?.id ?: ""
                        prefilledAmountStr = ""
                        showSettleDialog = true
                    },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToNotifications = { navController.navigate("notifications") }
                )
            }

            composable("notifications") {
                NotificationsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("bills") {
                BillsScreen(
                    viewModel = viewModel,
                    onBackClick = if (navController.previousBackStackEntry != null) {
                        { navController.popBackStack() }
                    } else null
                )
            }

            composable("people") {
                PeopleScreen(
                    viewModel = viewModel,
                    onTriggerSettleUpDirectly = { debtor, creditor, amount ->
                        prefilledDebtorId = debtor
                        prefilledCreditorId = creditor
                        prefilledAmountStr = amount.toString()
                        showSettleDialog = true
                    },
                    onBackClick = if (navController.previousBackStackEntry != null) {
                        { navController.popBackStack() }
                    } else null
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { navController.navigate("profile") },
                    onBackClick = if (navController.previousBackStackEntry != null) {
                        { navController.popBackStack() }
                    } else null
                )
            }

            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // Global Settle Up Dialog Integration
        if (showSettleDialog && roommates.isNotEmpty()) {
            val currentCurrency by viewModel.currency.collectAsState()
            SettleUpDialog(
                roommates = roommates,
                suggestedDebts = suggestedDebts,
                currencyPref = currentCurrency,
                onDismiss = { showSettleDialog = false },
                onSettle = { debtorId, creditorId, amount ->
                    viewModel.settleUp(debtorId, creditorId, amount)
                    showSettleDialog = false
                }
            )
        }
    }
}
