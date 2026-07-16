import re

with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "r") as f:
    text = f.read()

text = text.replace("roommates.firstOrNull { !it.isGuest }?.id ?: \"\"", "roommates.firstOrNull()?.id ?: \"\"")
text = text.replace("roommates.filter { !it.isGuest }.forEach { rm ->", "roommates.forEach { rm ->")

with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "w") as f:
    f.write(text)
