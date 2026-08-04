package com.example.feature.expensesplit.domain.model

enum class SplitType {
    EQUAL,
    EXACT,
    PERCENTAGE,
    SHARES,
    CUSTOM
}

data class Split(
    val userId: String,
    val amount: Double = 0.0,
    val type: SplitType = SplitType.EQUAL,
    val value: Double? = null // Holds percentage or shares if applicable
)

data class Payment(
    val userId: String,
    val amount: Double
)
