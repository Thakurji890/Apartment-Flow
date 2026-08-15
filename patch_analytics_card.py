import re

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

analytics_card = """
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToAnalytics() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("View Analytics & Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("See charts, trends, and category breakdowns", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = "View Analytics", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                
                item {
                    MonthlySummaryCard(
"""

content = content.replace("item {\n                    MonthlySummaryCard(", analytics_card.strip())

if "import androidx.compose.material.icons.filled.ArrowForward" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.ArrowForward")

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
