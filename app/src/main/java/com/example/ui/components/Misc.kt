package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun CurrencyDisplay(
    amount: Double,
    currencyCode: String = "USD",
    modifier: Modifier = Modifier,
    isExpense: Boolean? = null
) {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    format.currency = Currency.getInstance(currencyCode)
    
    val color = when (isExpense) {
        true -> ExpenseColor
        false -> IncomeColor
        null -> MaterialTheme.colorScheme.onSurface
    }
    
    Text(
        text = format.format(amount),
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}
