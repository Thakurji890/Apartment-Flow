with open("app/src/main/java/com/example/navigation/Screen.kt", "r") as f:
    content = f.read()

content = content.replace("object FairnessSummary", "object Notifications : Screen(\"notifications\")\n    object FairnessSummary")

with open("app/src/main/java/com/example/navigation/Screen.kt", "w") as f:
    f.write(content)
