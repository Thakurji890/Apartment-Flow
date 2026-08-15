with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace("        composable(\n        composable(\n            route = Screen.HomeDashboard.route,", "        composable(\n            route = Screen.HomeDashboard.route,")

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
