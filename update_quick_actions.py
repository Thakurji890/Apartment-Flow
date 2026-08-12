import re
with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "QuickActionItem(icon = Icons.Default.Settings, label = \"Apartment\", onClick = onApartment)",
    "QuickActionItem(icon = Icons.Default.Settings, label = \"Apartment\", onClick = onApartment)\n        QuickActionItem(icon = Icons.Default.ShoppingCart, label = \"Shop\", onClick = onNavigateToShoppingLists)\n        QuickActionItem(icon = Icons.Default.CheckCircle, label = \"Chores\", onClick = onNavigateToChores)"
)

# Fix missing parameters in QuickActionsRow call
content = content.replace(
    "fun QuickActionsRow(\n    onAddExpense: () -> Unit,\n    onSettleUp: () -> Unit,\n    onViewExpenses: () -> Unit,\n    onApartment: () -> Unit\n)",
    "fun QuickActionsRow(\n    onAddExpense: () -> Unit,\n    onSettleUp: () -> Unit,\n    onViewExpenses: () -> Unit,\n    onApartment: () -> Unit,\n    onNavigateToShoppingLists: () -> Unit,\n    onNavigateToChores: () -> Unit\n)"
)

content = content.replace(
    "QuickActionsRow(\n                        onAddExpense = onNavigateToAddExpense,\n                        onSettleUp = onNavigateToSettlements,\n                        onViewExpenses = onNavigateToExpenses,\n                        onApartment = onNavigateToApartment\n                    )",
    "QuickActionsRow(\n                        onAddExpense = onNavigateToAddExpense,\n                        onSettleUp = onNavigateToSettlements,\n                        onViewExpenses = onNavigateToExpenses,\n                        onApartment = onNavigateToApartment,\n                        onNavigateToShoppingLists = onNavigateToShoppingLists,\n                        onNavigateToChores = onNavigateToChores\n                    )"
)

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
