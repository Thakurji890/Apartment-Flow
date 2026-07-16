with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "r") as f:
    content = f.read()

target = '    val currencySymbol = currencyPref.substringAfter("(").substringBefore(")")'
replacement = """    val currencySymbol = currencyPref.substringAfter("(").substringBefore(")")

    if (showUpiConfirmation) {
        AlertDialog(
            onDismissRequest = { showUpiConfirmation = false },
            title = { Text("Payment Confirmation") },
            text = { Text("Did the payment go through?\\n\\nApartmentFlow does not verify the payment; clicking Yes will record it as a manual settlement.") },
            confirmButton = {
                Button(onClick = {
                    showUpiConfirmation = false
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onSettle(selectedDebtorId, selectedCreditorId, amount)
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpiConfirmation = false }) {
                    Text("No")
                }
            }
        )
    }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "w") as f:
        f.write(content)
        print("Patched Dialog!")
else:
    print("Target not found!")
