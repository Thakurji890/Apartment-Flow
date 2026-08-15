import re

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

# Add parameter to HomeDashboardScreen
content = content.replace("onNavigateToNotifications: () -> Unit\n) {", "onNavigateToNotifications: () -> Unit,\n    onNavigateToAnalytics: () -> Unit\n) {")

# Add the Analytics entry in QuickActionsRow? No, let's add a Dashboard card for Analytics.
# Find "item { DashboardSummary(" and insert Analytics summary card before it.
analytics_card = """
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToAnalytics() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("This Month", style = MaterialTheme.typography.labelSmall)
                                Text("₹${String.format(\"%.2f\", state.monthTotal)} spent", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = "View Analytics")
                        }
                    }
                }
                
                item { DashboardSummary("""
content = content.replace("item { DashboardSummary(", analytics_card)

# Need to import Icons.Default.ArrowForward if not already
if "import androidx.compose.material.icons.filled.ArrowForward" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.ArrowForward")

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
