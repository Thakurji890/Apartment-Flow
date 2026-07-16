import re

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

pattern = r"                    // Demo / Guest Mode Bypass Button.*?                    \}"

text = re.sub(pattern, "", text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
