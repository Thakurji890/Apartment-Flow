import re

with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "r") as f:
    text = f.read()

imports = "import androidx.compose.ui.platform.LocalSoftwareKeyboardController\nimport androidx.compose.ui.platform.LocalFocusManager"
text = text.replace("import androidx.compose.ui.platform.testTag", imports + "\nimport androidx.compose.ui.platform.testTag")

# find ExpenseEntryForm and inject LocalSoftwareKeyboardController
injection = """
    val selectedParticipants = remember { mutableStateListOf<String>() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
"""
text = text.replace("    val selectedParticipants = remember { mutableStateListOf<String>() }", injection)

# Update Cancel Click
cancel_old = """            OutlinedButton(
                onClick = onCancelClick,"""
cancel_new = """            OutlinedButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onCancelClick()
                },"""
text = text.replace(cancel_old, cancel_new)

# Update Save Click
save_old = """                    if (!titleError && !amountError && !dateError && !participantsError) {
                        onSaveClick(title, parsedAmount!!, category, dateStr, payerId, description, selectedParticipants.toList())
                    }"""
save_new = """                    if (!titleError && !amountError && !dateError && !participantsError) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onSaveClick(title, parsedAmount!!, category, dateStr, payerId, description, selectedParticipants.toList())
                    }"""
text = text.replace(save_old, save_new)

with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "w") as f:
    f.write(text)
