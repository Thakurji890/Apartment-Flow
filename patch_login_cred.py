import re

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

pattern = r"                                        if \(credential is GoogleIdTokenCredential\) \{\n                                            val firebaseCredential = GoogleAuthProvider\.getCredential\(credential\.idToken, null\)"
replacement = """                                        if (credential is androidx.credentials.CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)"""

text = re.sub(pattern, replacement, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
