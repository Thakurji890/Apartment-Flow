import re

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

# Let's fix the broken login screen by replacing the whole else block of isLoading
pattern = r"                    // Google Sign-In Button.*"

replacement = """                    // Google Sign-In Button
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val webClientId = try {
                                        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                                        if (resId != 0) context.getString(resId) else "266945182431-70r532i6v7o9804jcbna732b1o4h8mvl.apps.googleusercontent.com"
                                    } catch (e: Exception) {
                                        "266945182431-70r532i6v7o9804jcbna732b1o4h8mvl.apps.googleusercontent.com"
                                    }
                                    val googleIdOption = androidx.credentials.GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(webClientId)
                                        .setAutoSelectEnabled(false)
                                        .build()
                                    val request = androidx.credentials.GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()
                                    val result = credentialManager.getCredential(context, request)
                                    val credential = result.credential
                                    
                                    if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
                                        val firebaseCredential = com.google.firebase.auth.GoogleAuthProvider.getCredential(credential.idToken, null)
                                        auth.signInWithCredential(firebaseCredential)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                onLoginSuccess()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = "Firebase sign in failed: ${e.localizedMessage}"
                                            }
                                    } else {
                                        isLoading = false
                                        errorMessage = "Unexpected credential type received"
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = context.getString(com.example.R.string.login_google_error)
                                    android.util.Log.e("LoginScreen", "Google Sign-In Error", e)
                                }
                            }
                        },
                        modifier = androidx.compose.ui.Modifier
                            .androidx.compose.foundation.layout.fillMaxWidth()
                            .androidx.compose.foundation.layout.height(50.dp)
                            .androidx.compose.ui.platform.testTag("login_google_button"),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Login,
                            contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.cd_google_icon),
                            modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.padding(end = 8.dp)
                        )
                        androidx.compose.material3.Text(
                            text = androidx.compose.ui.res.stringResource(com.example.R.string.login_google_signin),
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
"""
text = re.sub(pattern, replacement, text, flags=re.DOTALL)

# Let's remove the nameInput textfield if it is there
name_pattern = r"                // Display Name Input.*?                OutlinedTextField\(.*?                    singleLine = true,.*?                \)"
text = re.sub(name_pattern, "", text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
