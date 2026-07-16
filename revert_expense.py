with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "r") as f:
    text = f.read()

text = text.replace(".background(MaterialTheme.colorScheme.surface)\n            .verticalScroll(rememberScrollState()),", ".background(MaterialTheme.colorScheme.surface),")
with open("app/src/main/java/com/example/ui/components/ExpenseEntryForm.kt", "w") as f:
    f.write(text)
