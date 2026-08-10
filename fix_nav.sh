sed -i 's/\/\/ TODO: RecurringBillListScreen/com.example.feature.recurringbill.presentation.list.RecurringBillListScreen(\n                onNavigateBack = { navController.popBackStack() },\n                onNavigateToAddBill = { navController.navigate(Screen.AddRecurringBill.createRoute(apartmentId)) },\n                onNavigateToBillDetails = { billId -> navController.navigate(Screen.RecurringBillDetails.createRoute(apartmentId, billId)) }\n            )/g' app/src/main/java/com/example/navigation/AppNavigation.kt

sed -i 's/\/\/ TODO: AddRecurringBillScreen/com.example.feature.recurringbill.presentation.add_edit.AddEditRecurringBillScreen(\n                onNavigateBack = { navController.popBackStack() }\n            )/g' app/src/main/java/com/example/navigation/AppNavigation.kt

sed -i 's/\/\/ TODO: EditRecurringBillScreen/com.example.feature.recurringbill.presentation.add_edit.AddEditRecurringBillScreen(\n                onNavigateBack = { navController.popBackStack() }\n            )/g' app/src/main/java/com/example/navigation/AppNavigation.kt

sed -i 's/\/\/ TODO: RecurringBillDetailsScreen/com.example.feature.recurringbill.presentation.details.RecurringBillDetailsScreen(\n                onNavigateBack = { navController.popBackStack() },\n                onNavigateToEdit = { navController.navigate(Screen.EditRecurringBill.createRoute(apartmentId, billId)) }\n            )/g' app/src/main/java/com/example/navigation/AppNavigation.kt

