import re

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

# Add parameter
content = content.replace(
    "onNavigateToRecurringBillDetails: (String) -> Unit\n) {",
    "onNavigateToRecurringBillDetails: (String) -> Unit,\n    onNavigateToShoppingLists: () -> Unit\n) {"
)

# Add button / card to navigate to Shopping Lists. We can add it inside Quick Actions or as a new card.
# Let's find "Quick Actions"
quick_actions_replacement = """                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardCard(
                            title = "Expenses",
                            icon = Icons.Filled.AttachMoney,
                            onClick = onNavigateToExpenses,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardCard(
                            title = "Settlements",
                            icon = Icons.Filled.AccountBalanceWallet,
                            onClick = onNavigateToSettlements,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardCard(
                            title = "Recurring Bills",
                            icon = Icons.Filled.DateRange,
                            onClick = onNavigateToRecurringBills,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardCard(
                            title = "Shopping",
                            icon = Icons.Filled.ShoppingCart,
                            onClick = onNavigateToShoppingLists,
                            modifier = Modifier.weight(1f)
                        )
                    }"""

content = re.sub(
    r'                    Text\(\s*text = "Quick Actions",.*?modifier = Modifier\.weight\(1f\)\s*\)\s*\}\s*\}',
    quick_actions_replacement,
    content,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
