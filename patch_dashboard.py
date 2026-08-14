import re

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

# Add onNavigateToNotifications parameter
content = re.sub(
    r"onNavigateToChores: \(\) -> Unit",
    "onNavigateToChores: () -> Unit,\n    onNavigateToNotifications: () -> Unit",
    content
)

# Replace the greeting/profile row in TopAppBar with the notification icon logic.
# Wait, let's just find TopAppBar and replace its actions.
top_app_bar_pattern = r"TopAppBar\(\s*title = \{\s*Column \{\s*Text\(\s*text = getGreeting\(\),\s*style = MaterialTheme\.typography\.labelMedium,\s*color = MaterialTheme\.colorScheme\.onSurfaceVariant\s*\)\s*Row\(verticalAlignment = Alignment\.CenterVertically\) \{\s*Text\(\s*text = \"Hey, \$\{state\.currentUser\?\.displayName \?: \"There\"\}\",\s*style = MaterialTheme\.typography\.titleLarge,\s*fontWeight = FontWeight\.Bold\s*\)\s*\}\s*\}\s*\)"

replacement = """TopAppBar(
                title = {
                    Column {
                        Text(
                            text = getGreeting(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hey, ${state.currentUser?.displayName ?: "There"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            )"""

content = re.sub(top_app_bar_pattern, replacement, content, flags=re.MULTILINE)

with open("app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt", "w") as f:
    f.write(content)

