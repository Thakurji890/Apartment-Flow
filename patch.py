with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "r") as f:
    content = f.read()

target = '                OutlinedTextField(\n                    value = amountStr,'
replacement = """                val creditor = roommates.find { it.id == selectedCreditorId }
                if (creditor != null && creditor.upiId.isNotBlank()) {
                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount <= 0.0) {
                                amountError = true
                                return@Button
                            }
                            val note = android.net.Uri.encode("ApartmentFlow settlement")
                            val name = android.net.Uri.encode(creditor.name)
                            val uri = android.net.Uri.parse("upi://pay?pa=${creditor.upiId}&pn=$name&am=$amount&cu=INR&tn=$note")
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                                showUpiConfirmation = true
                            } catch (e: android.content.ActivityNotFoundException) {
                                android.widget.Toast.makeText(context, "No UPI app found — try Google Pay, PhonePe, or Paytm", android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Pay via UPI", color = MaterialTheme.colorScheme.onTertiary)
                    }
                }
                OutlinedTextField(
                    value = amountStr,"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "w") as f:
        f.write(content)
        print("Patched!")
else:
    print("Target not found!")
