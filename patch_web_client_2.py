import re

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

error_str = 'errorMessage = "Unexpected credential: ${credential.javaClass.simpleName}"'
replacement = 'errorMessage = "Unexpected credential: ${credential.javaClass.simpleName}" + if (credential is androidx.credentials.CustomCredential) " type: ${credential.type}" else ""'
text = text.replace(error_str, replacement)

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
