import re
with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

# Fix signature
content = content.replace("onNavigateToChores: () -> Unit,\n    onNavigateToNotifications: () -> Unit\n) {\n    Row(", "onNavigateToChores: () -> Unit\n) {\n    Row(")

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
