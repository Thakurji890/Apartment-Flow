import re

with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "r") as f:
    text = f.read()

old_effect = """    LaunchedEffect(roommates) {
        if (selectedParticipants.isEmpty()) {
            selectedParticipants.addAll(roommates.map { it.id })
        }
    }"""
new_effect = """    LaunchedEffect(roommates) {
        if (selectedParticipants.isEmpty()) {
            selectedParticipants.addAll(roommates.map { it.id })
        }
        if (payerId.isEmpty() && roommates.isNotEmpty()) {
            payerId = roommates.first().id
        }
    }"""
text = text.replace(old_effect, new_effect)

with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "w") as f:
    f.write(text)
