import re

with open("app/src/main/java/com/example/navigation/Screen.kt", "r") as f:
    content = f.read()

if "object Analytics : Screen(\"analytics/{apartmentId}\")" not in content:
    content = content.replace("object FairnessSummary", "object Analytics : Screen(\"analytics/{apartmentId}\") {\n        fun createRoute(apartmentId: String) = \"analytics/$apartmentId\"\n    }\n    object FairnessSummary")
    with open("app/src/main/java/com/example/navigation/Screen.kt", "w") as f:
        f.write(content)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Add composable to AppNavigation
if "composable(Screen.Analytics.route" not in content:
    composable_block = """
        composable(
            route = Screen.Analytics.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) {
            com.example.feature.analytics.presentation.AnalyticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
"""
    content = content.replace("composable(\n            route = Screen.HomeDashboard.route", composable_block.strip() + "\n\n        composable(\n            route = Screen.HomeDashboard.route")
    
    # Add navigation parameter to dashboard
    # Find onNavigateToChores
    content = content.replace("onNavigateToChores = { navController.navigate(Screen.ChoreList.createRoute(apartmentId)) },\n                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }", "onNavigateToChores = { navController.navigate(Screen.ChoreList.createRoute(apartmentId)) },\n                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },\n                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.createRoute(apartmentId)) }")
    
    with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
        f.write(content)
