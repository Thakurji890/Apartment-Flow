import re

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = re.sub(r'composable\(\s*composable\(\s*route = Screen.HomeDashboard.route,', r'composable(\n            route = Screen.HomeDashboard.route,', content)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
