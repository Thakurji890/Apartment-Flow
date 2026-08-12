import re

with open("app/src/main/java/com/example/navigation/Screen.kt", "r") as f:
    content = f.read()

# Add Chore routes
chore_routes = """    object ChoreList : Screen("chore_list/{apartmentId}") {
        fun createRoute(apartmentId: String) = "chore_list/$apartmentId"
    }
    object AddEditChore : Screen("add_edit_chore/{apartmentId}?choreId={choreId}") {
        fun createRoute(apartmentId: String, choreId: String? = null) = 
            "add_edit_chore/$apartmentId" + (choreId?.let { "?choreId=$it" } ?: "")
    }
    object ChoreDetails : Screen("chore_details/{apartmentId}/{choreId}") {
        fun createRoute(apartmentId: String, choreId: String) = "chore_details/$apartmentId/$choreId"
    }
    object FairnessSummary : Screen("fairness_summary/{apartmentId}") {
        fun createRoute(apartmentId: String) = "fairness_summary/$apartmentId"
    }
}"""

parts = content.rsplit("}", 1)
content = chore_routes.join(parts)

with open("app/src/main/java/com/example/navigation/Screen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    app_nav_content = f.read()

# HomeDashboard update
app_nav_content = app_nav_content.replace(
    "onNavigateToShoppingLists = { navController.navigate(Screen.ShoppingLists.createRoute(apartmentId)) }",
    "onNavigateToShoppingLists = { navController.navigate(Screen.ShoppingLists.createRoute(apartmentId)) },\n                onNavigateToChores = { navController.navigate(Screen.ChoreList.createRoute(apartmentId)) }"
)

# Add chore composables
chore_composables = """
        composable(
            route = Screen.ChoreList.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.chore.presentation.list.ChoreListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddChore = { navController.navigate(Screen.AddEditChore.createRoute(apartmentId)) },
                onNavigateToChoreDetails = { choreId -> navController.navigate(Screen.ChoreDetails.createRoute(apartmentId, choreId)) },
                onNavigateToSummary = { navController.navigate(Screen.FairnessSummary.createRoute(apartmentId)) }
            )
        }

        composable(
            route = Screen.AddEditChore.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("choreId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.chore.presentation.add_edit.AddEditChoreScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ChoreDetails.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("choreId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.chore.presentation.details.ChoreDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditChore = { choreId -> 
                    navController.navigate(Screen.AddEditChore.createRoute(apartmentId, choreId))
                }
            )
        }

        composable(
            route = Screen.FairnessSummary.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.chore.presentation.summary.FairnessSummaryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
"""

parts = app_nav_content.rsplit("    }\n}", 1)
app_nav_content = chore_composables.join(parts)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
    f.write(app_nav_content)
