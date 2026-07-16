import re

with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "r") as f:
    text = f.read()

# Fix amount validation
text = text.replace("val parsedAmount = amountStr.toDoubleOrNull()", "val parsedAmount = amountStr.replace(\",\", \".\").toDoubleOrNull()")

# Fix participants check
old_check = """                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedParticipants.add(rm.id)
                                } else {
                                    if (selectedParticipants.size > 1) {
                                        selectedParticipants.remove(rm.id)
                                    }
                                }
                            },"""
new_check = """                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedParticipants.add(rm.id)
                                } else {
                                    selectedParticipants.remove(rm.id)
                                }
                            },"""
text = text.replace(old_check, new_check)

with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "w") as f:
    f.write(text)
