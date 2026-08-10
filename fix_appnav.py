import re

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Fix OnboardingScreen
content = content.replace("onFinishOnboarding = {", "onNavigateToWelcome = {")

# Fix LoginScreen: onNavigateToApartmentSetup -> we need to check if it's there. Actually let's just use `sed` to delete `onNavigateToApartmentSetup` block from LoginScreen
content = re.sub(r'                onNavigateToApartmentSetup = \{\n                    navController.navigate\(Screen.ApartmentSetup.route\) \{\n                        popUpTo\(Screen.Welcome.route\) \{ inclusive = true \}\n                    \}\n                \},\n', '', content)

# Fix ExpenseListScreen: missing apartmentId
content = re.sub(r'ExpenseListScreen\(\n                onNavigateBack = \{ navController.popBackStack\(\) \},\n                onNavigateToAddExpense', r'ExpenseListScreen(\n                apartmentId = apartmentId,\n                onNavigateBack = { navController.popBackStack() },\n                onNavigateToAddExpense', content)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
