cat << 'INNER' > insert_nav.txt

        composable(
            route = Screen.RecurringBillList.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            // TODO: RecurringBillListScreen
        }

        composable(
            route = Screen.AddRecurringBill.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            // TODO: AddRecurringBillScreen
        }

        composable(
            route = Screen.EditRecurringBill.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("billId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val billId = backStackEntry.arguments?.getString("billId") ?: ""
            // TODO: EditRecurringBillScreen
        }

        composable(
            route = Screen.RecurringBillDetails.route,
            arguments = listOf(
                navArgument("apartmentId") { type = NavType.StringType },
                navArgument("billId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val apartmentId = backStackEntry.arguments?.getString("apartmentId") ?: ""
            val billId = backStackEntry.arguments?.getString("billId") ?: ""
            // TODO: RecurringBillDetailsScreen
        }
INNER
sed -i '/    }/,$d' app/src/main/java/com/example/navigation/AppNavigation.kt
cat insert_nav.txt >> app/src/main/java/com/example/navigation/AppNavigation.kt
echo "    }" >> app/src/main/java/com/example/navigation/AppNavigation.kt
echo "}" >> app/src/main/java/com/example/navigation/AppNavigation.kt
