import re

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Fix the duplicate 'Notifications' object if I accidentally put it there earlier.
content = re.sub(r"object Notifications : Screen\(\"notifications\"\)", "", content)

# Find the end of HomeDashboardScreen arguments and add onNavigateToNotifications
old_args = r"onNavigateToChores = \{ navController.navigate\(Screen.ChoreList.createRoute\(apartmentId\)\) \},\s*onNavigateToNotifications = \{ navController.navigate\(Screen.Notifications.route\) \}"
# Remove it first to be safe, or just do a manual replace

content = content.replace("onNavigateToChores = { navController.navigate(Screen.ChoreList.createRoute(apartmentId)) }\n            )", "onNavigateToChores = { navController.navigate(Screen.ChoreList.createRoute(apartmentId)) },\n                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }\n            )")

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
