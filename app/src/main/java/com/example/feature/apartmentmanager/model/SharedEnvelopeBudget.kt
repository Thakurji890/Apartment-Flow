package com.example.feature.apartmentmanager.model

/**
 * Standard expense categories for shared households.
 */
enum class ExpenseCategory(val displayName: String, val iconName: String, val defaultColorHex: Long) {
    GROCERIES("Shared Groceries", "ShoppingCart", 0xFF2E7D32L),
    UTILITIES("Utilities & Bills", "Bolt", 0xFFE65100L),
    RENT("Rent & Housing", "Home", 0xFF1565C0L),
    HOUSEHOLD_SUPPLIES("Household Supplies", "CleaningServices", 0xFF6A1B9AL),
    INTERNET_WIFI("Internet & Wi-Fi", "Wifi", 0xFF00838FL),
    ENTERTAINMENT("Entertainment & Subs", "Tv", 0xFFAD1457L),
    MAINTENANCE("Repairs & Maintenance", "Build", 0xFF4E342EL),
    OTHER("Other Miscellaneous", "Category", 0xFF546E7AL);

    companion object {
        fun fromString(value: String?): ExpenseCategory {
            if (value == null) return GROCERIES
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: GROCERIES
        }
    }
}

/**
 * Monthly Shared Envelope Budget cap for a specific category.
 */
data class SharedEnvelopeBudget(
    val id: String,
    val category: ExpenseCategory,
    val monthlyCap: Double,
    val alertThresholdPercent: Int = 85, // e.g. Alert when 85% reached
    val notes: String = ""
) {
    fun calculateProgress(spentAmount: Double): Double {
        if (monthlyCap <= 0.0) return 0.0
        return (spentAmount / monthlyCap).coerceIn(0.0, 2.0) // allow over budget up to 200% for progress indicator
    }

    fun isNearLimit(spentAmount: Double): Boolean {
        if (monthlyCap <= 0.0) return false
        val percent = (spentAmount / monthlyCap) * 100.0
        return percent >= alertThresholdPercent && percent < 100.0
    }

    fun isOverLimit(spentAmount: Double): Boolean {
        return monthlyCap > 0.0 && spentAmount >= monthlyCap
    }

    fun isExceeded(spentAmount: Double): Boolean {
        return isOverLimit(spentAmount)
    }
}
