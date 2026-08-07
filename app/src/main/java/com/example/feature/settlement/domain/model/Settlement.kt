package com.example.feature.settlement.domain.model

enum class SettlementStatus {
    PENDING,
    CONFIRMED,
    REJECTED
}

enum class PaymentMethod {
    CASH,
    UPI,
    BANK_TRANSFER,
    CREDIT_CARD,
    DEBIT_CARD,
    WALLET,
    OTHER
}

data class Settlement(
    val settlementId: String = "",
    val apartmentId: String = "",
    val debtorId: String = "",
    val creditorId: String = "",
    val amount: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val status: SettlementStatus = SettlementStatus.PENDING,
    val note: String = "",
    val receiptUrl: String? = null,
    val receiptId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SettlementReceipt(
    val receiptId: String = "",
    val settlementId: String = "",
    val url: String = "",
    val uploadedBy: String = "",
    val uploadedAt: Long = System.currentTimeMillis()
)
