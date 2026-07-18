with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

target = """                                        val result = credentialManager.getCredential(context, request)"""

replacement = """                                        var activityContext = context
                                        while (activityContext is android.content.ContextWrapper && activityContext !is android.app.Activity) {
                                            activityContext = activityContext.baseContext
                                        }
                                        val result = credentialManager.getCredential(activityContext, request)"""

text = text.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
