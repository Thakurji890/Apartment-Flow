import re

with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "a") as f:
    f.write("""                val creditor = roommates.find { it.id == selectedCreditorId }
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
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        amountError = false
                    },
                    label = { Text(stringResource(R.string.settle_up_dialog_amount_transferred_label, currencySymbol)) },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settle_amount_input"),
                    isError = amountError,
                    supportingText = {
                        if (amountError) {
                            if (selectedDebtorId == selectedCreditorId) {
                                Text(stringResource(R.string.settle_up_dialog_same_person_error))
                            } else {
                                Text(stringResource(R.string.settle_up_dialog_invalid_amount_error))
                            }
                        } else {
                            val activeDebtorName = roommates.find { it.id == selectedDebtorId }?.name ?: ""
                            val activeCreditorName = roommates.find { it.id == selectedCreditorId }?.name ?: ""
                            if (selectedDebtorId.isNotEmpty() && selectedCreditorId.isNotEmpty() && selectedDebtorId != selectedCreditorId) {
                                if (directDebt > 0.01) {
                                    Text(stringResource(R.string.settle_up_dialog_direct_debt_owes, activeDebtorName, activeCreditorName, formatCurrency(directDebt)))
                                } else if (directDebt < -0.01) {
                                    Text(stringResource(R.string.settle_up_dialog_direct_debt_owes, activeCreditorName, activeDebtorName, formatCurrency(-directDebt)))
                                } else {
                                    Text(stringResource(R.string.settle_up_dialog_no_direct_debt, activeDebtorName, activeCreditorName))
                                }
                            }
                        }
                    },
                    singleLine = true
                )
            }
        }
    )
}
""")

