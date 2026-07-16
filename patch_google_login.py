import re

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

# Remove the name input field entirely
name_input_field = r"                // Display Name Input.*?                OutlinedTextField\(.*?                    singleLine = true.*?                \).*?                Spacer\(modifier = Modifier\.height\(24\.dp\)\)"
text = re.sub(name_input_field, "", text, flags=re.DOTALL)

# Remove the enteredName validation from Google Sign-In button
entered_name_validation = r"                            val enteredName = nameInput\.trim\(\)\n                            if \(enteredName\.isEmpty\(\)\) \{\n                                errorMessage = context\.getString\(R\.string\.login_name_empty_error\)\n                                return@Button\n                            \}"
text = re.sub(entered_name_validation, "", text, flags=re.DOTALL)

# Remove the updateProfile block from Google Sign In success listener
update_profile_block = r"                                                // Update profile if they entered a custom name\n                                                auth\.currentUser\?\.updateProfile\(\n                                                    userProfileChangeRequest \{\n                                                        displayName = nameInput\.trim\(\)\n                                                    \}\n                                                \)\?\.addOnCompleteListener \{\n                                                    isLoading = false\n                                                    onLoginSuccess\(\)\n                                                \} \?: run \{\n                                                    isLoading = false\n                                                    onLoginSuccess\(\)\n                                                \}"

new_success_block = r"""                                                isLoading = false
                                                onLoginSuccess()"""
text = re.sub(update_profile_block, new_success_block, text, flags=re.DOTALL)

# Remove the nameInput variable declaration
text = re.sub(r"    var nameInput by remember \{ mutableStateOf\(\"\"\) \}\n", "", text)

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
