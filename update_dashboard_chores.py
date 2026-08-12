with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

# Add parameter
if "onNavigateToChores: () -> Unit" not in content:
    content = content.replace(
        "onNavigateToShoppingLists: () -> Unit\n) {",
        "onNavigateToShoppingLists: () -> Unit,\n    onNavigateToChores: () -> Unit\n) {"
    )

import_idx = content.find("import androidx.compose.material.icons.filled.AttachMoney")
if import_idx != -1 and "import androidx.compose.material.icons.filled.CheckCircle" not in content:
    content = content[:import_idx] + "import androidx.compose.material.icons.filled.CheckCircle\n" + content[import_idx:]

# Find "Quick Actions"
quick_actions_end_idx = content.find("ShoppingLists,\n                            modifier = Modifier.weight(1f)\n                        )\n                    }")
if quick_actions_end_idx != -1:
    end_bracket_idx = quick_actions_end_idx + len("ShoppingLists,\n                            modifier = Modifier.weight(1f)\n                        )\n                    }")
    
    append_str = """
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardCard(
                            title = "Chores",
                            icon = Icons.Filled.CheckCircle,
                            onClick = onNavigateToChores,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }"""
    
    content = content[:end_bracket_idx] + append_str + content[end_bracket_idx:]

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
