import re

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Add Route object
route_pattern = r"object ChoreList : Screen\(\"chore_list/\{apartmentId\}\"\) \{\s*fun createRoute\(apartmentId: String\) = \"chore_list/\$apartmentId\"\s*\}"
new_route = """object ChoreList : Screen("chore_list/{apartmentId}") {
        fun createRoute(apartmentId: String) = "chore_list/$apartmentId"
    }
    object Notifications : Screen("notifications")"""

content = content.replace(route_pattern, new_route)
if "object Notifications : Screen" not in content:
    # try another way
    content = content.replace("sealed class Screen(val route: String) {", "sealed class Screen(val route: String) {\n    object Notifications : Screen(\"notifications\")")


# Add navigation inside dashboard
dash_nav_pattern = r"onNavigateToChores = \{ navController\.navigate\(Screen\.ChoreList\.createRoute\(apartmentId\)\) \}"
new_dash_nav = """onNavigateToChores = { navController.navigate(Screen.ChoreList.createRoute(apartmentId)) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }"""
content = content.replace(dash_nav_pattern, new_dash_nav)

# Add composable block
composable_pattern = r"composable\(Screen\.JoinApartment\.route\) \{"
new_composable = """composable(Screen.Notifications.route) {
            com.example.feature.notification.presentation.list.NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDeepLink = { /* Implement deeper linking parsing here if needed */ }
            )
        }
        
        composable(Screen.JoinApartment.route) {"""

content = content.replace("composable(Screen.JoinApartment.route) {", new_composable)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

