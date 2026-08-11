import re

with open("app/src/main/java/com/example/navigation/Screen.kt", "r") as f:
    content = f.read()

# Add Shopping routes
shopping_routes = """    object ShoppingLists : Screen("shopping_lists/{apartmentId}") {
        fun createRoute(apartmentId: String) = "shopping_lists/$apartmentId"
    }
    object ShoppingListDetails : Screen("shopping_list_details/{apartmentId}/{listId}") {
        fun createRoute(apartmentId: String, listId: String) = "shopping_list_details/$apartmentId/$listId"
    }
    object AddEditShoppingList : Screen("add_edit_shopping_list/{apartmentId}?listId={listId}") {
        fun createRoute(apartmentId: String, listId: String? = null) = 
            "add_edit_shopping_list/$apartmentId" + (listId?.let { "?listId=$it" } ?: "")
    }
    object AddEditShoppingItem : Screen("add_edit_shopping_item/{apartmentId}/{listId}?itemId={itemId}") {
        fun createRoute(apartmentId: String, listId: String, itemId: String? = null) = 
            "add_edit_shopping_item/$apartmentId/$listId" + (itemId?.let { "?itemId=$it" } ?: "")
    }
    object PurchaseItem : Screen("purchase_item/{apartmentId}/{listId}/{itemId}") {
        fun createRoute(apartmentId: String, listId: String, itemId: String) = 
            "purchase_item/$apartmentId/$listId/$itemId"
    }
}"""

content = content.replace("}", shopping_routes)

with open("app/src/main/java/com/example/navigation/Screen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    app_nav_content = f.read()

# HomeDashboard update
app_nav_content = app_nav_content.replace(
    "onNavigateToRecurringBillDetails = { billId -> navController.navigate(Screen.RecurringBillDetails.createRoute(apartmentId, billId)) }",
    "onNavigateToRecurringBillDetails = { billId -> navController.navigate(Screen.RecurringBillDetails.createRoute(apartmentId, billId)) },\n                onNavigateToShoppingLists = { navController.navigate(Screen.ShoppingLists.createRoute(apartmentId)) }"
)

# Add shopping composables
shopping_composables = """
        composable(
            route = Screen.ShoppingLists.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.shopping.presentation.lists.ShoppingListsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToListDetails = { listId -> navController.navigate(Screen.ShoppingListDetails.createRoute(apartmentId, listId)) },
                onNavigateToAddList = { navController.navigate(Screen.AddEditShoppingList.createRoute(apartmentId)) }
            )
        }

        composable(
            route = Screen.ShoppingListDetails.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            com.example.feature.shopping.presentation.details.ShoppingListDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddItem = { _ -> navController.navigate(Screen.AddEditShoppingItem.createRoute(apartmentId, listId)) },
                onNavigateToEditItem = { _, itemId -> navController.navigate(Screen.AddEditShoppingItem.createRoute(apartmentId, listId, itemId)) },
                onNavigateToPurchaseItem = { _, itemId -> navController.navigate(Screen.PurchaseItem.createRoute(apartmentId, listId, itemId)) }
            )
        }

        composable(
            route = Screen.AddEditShoppingList.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            com.example.feature.shopping.presentation.add_edit_list.AddEditShoppingListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditShoppingItem.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            com.example.feature.shopping.presentation.add_edit_item.AddEditShoppingItemScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PurchaseItem.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val listId = backStackEntry.arguments?.getString("listId") ?: ""
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            com.example.feature.shopping.presentation.purchase_item.PurchaseItemScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
"""

app_nav_content = app_nav_content.replace("    }\n}", shopping_composables)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
    f.write(app_nav_content)
