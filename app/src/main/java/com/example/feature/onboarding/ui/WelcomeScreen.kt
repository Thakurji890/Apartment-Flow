package com.example.feature.onboarding.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import android.util.Log
import android.app.Activity
import com.example.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var showPhoneAuthDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    // Auto-navigate if already signed in
    LaunchedEffect(Unit) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            onGetStartedClick()
        } else {
            delay(100)
            isVisible = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
        label = "alpha"
    )

    if (showPhoneAuthDialog && activity != null) {
        PhoneAuthDialog(
            activity = activity,
            onDismiss = { showPhoneAuthDialog = false },
            onSuccess = {
                showPhoneAuthDialog = false
                onGetStartedClick()
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hero Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .alpha(alpha)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.apartment_flow_logo_1789483162947),
                        contentDescription = "App Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Title
                Text(
                    text = "Apartment Flow",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(alpha)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Split bills, manage shared expenses, and track your apartment finances with your roommates seamlessly.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .alpha(alpha)
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Get Started Button
                Button(
                    onClick = {
                        isSigningIn = true
                        coroutineScope.launch {
                            val result = GoogleSignInHelper.signInWithGoogle(context)
                            isSigningIn = false
                            if (result.isSuccess) {
                                onGetStartedClick()
                            } else {
                                Toast.makeText(context, "Google Sign-In unavailable on emulator. Continuing as Guest.", Toast.LENGTH_LONG).show()
                                try {
                                    FirebaseAuth.getInstance().signInAnonymously().await()
                                } catch (e: Exception) {
                                    // Ignore Firebase error, user just bypasses without auth since dummy keys are loaded.
                                }
                                onGetStartedClick()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(scale)
                        .alpha(alpha),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !isSigningIn
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Continue with Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showPhoneAuthDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(scale)
                        .alpha(alpha),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSigningIn
                ) {
                    Text(
                        text = "Continue with Phone",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneAuthDialog(
    activity: Activity,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (verificationId == null) "Phone Sign-In" else "Enter OTP") },
        text = {
            Column {
                if (verificationId == null) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (e.g. +91...)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("6-digit OTP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (verificationId == null) {
                        if (phoneNumber.isBlank()) {
                            errorMsg = "Please enter a valid phone number"
                            return@Button
                        }
                        val formattedNumber = phoneNumber.replace(" ", "").replace("-", "").trim()
                        if (!formattedNumber.startsWith("+")) {
                            errorMsg = "Please include country code starting with '+' (e.g. +91)"
                            return@Button
                        }
                        
                        isLoading = true
                        errorMsg = null
                        
                        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber(formattedNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    FirebaseAuth.getInstance().signInWithCredential(credential)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) onSuccess() else errorMsg = task.exception?.message
                                        }
                                }
                                override fun onVerificationFailed(e: FirebaseException) {
                                    isLoading = false
                                    if (e.message?.contains("INVALID_CERT_HASH") == true || e.message?.contains("app verification") == true) {
                                        errorMsg = "App verification failed. Add the SHA-1 to your Firebase Console."
                                    } else if (e.message?.contains("BILLING_NOT_ENABLED") == true) {
                                        errorMsg = "SMS quota exceeded or billing not enabled. Add this number as a 'Test Number' in the Firebase Console to bypass SMS."
                                    } else {
                                        errorMsg = e.message ?: "Verification failed."
                                    }
                                }
                                override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    isLoading = false
                                    verificationId = verId
                                }
                            }).build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    } else {
                        if (otpCode.isBlank()) {
                            errorMsg = "Please enter the OTP"
                            return@Button
                        }
                        isLoading = true
                        errorMsg = null
                        val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) onSuccess() else errorMsg = task.exception?.message
                            }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(if (verificationId == null) "Send OTP" else "Verify")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
