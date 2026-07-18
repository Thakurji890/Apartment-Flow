with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

target = """                                        errorMessage = \"${context.getString(R.string.login_google_error)}
$errorMsg\""""

replacement = """                                        errorMessage = "${context.getString(R.string.login_google_error)}\\n$errorMsg\""""

text = text.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
