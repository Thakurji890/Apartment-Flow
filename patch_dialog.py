import re

with open("app/src/main/java/com/example/ui/screens/BillsScreen.kt", "r") as f:
    text = f.read()

# Replace AlertDialog with BasicAlertDialog
old_dialog = """    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp),
        content = {"""
new_dialog = """    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp),
        content = {"""
text = text.replace(old_dialog, new_dialog)

with open("app/src/main/java/com/example/ui/screens/BillsScreen.kt", "w") as f:
    f.write(text)
