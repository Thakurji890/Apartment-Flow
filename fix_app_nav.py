with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace(
    "                onNavigateToChores = { navController.navigate(Screen.ChoreList.createRoute(apartmentId)) },\n                onNavigateToChores = { navController.navigate(Screen.ChoreList.createRoute(apartmentId)) }",
    "                onNavigateToChores = { navController.navigate(Screen.ChoreList.createRoute(apartmentId)) }"
)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
