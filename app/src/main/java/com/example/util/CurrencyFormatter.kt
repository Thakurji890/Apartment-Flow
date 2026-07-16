package com.example.util

import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currencyCode: String): String {
        val symbol = when {
            currencyCode.contains("€") -> "€"
            currencyCode.contains("£") -> "£"
            currencyCode.contains("₹") -> "₹"
            currencyCode.contains("CAD") -> "CA$"
            else -> "$"
        }
        return String.format(Locale.US, "%s%.2f", symbol, amount)
    }
}
