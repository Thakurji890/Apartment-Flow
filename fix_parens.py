import re

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

text = text.replace("                Spacer(modifier = Modifier.height(8.dp))\n                )\n                // Error message banner", "                Spacer(modifier = Modifier.height(8.dp))\n                // Error message banner")

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
