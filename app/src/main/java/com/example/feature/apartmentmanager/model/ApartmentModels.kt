package com.example.feature.apartmentmanager.model

data class ApartmentRoommate(
    val id: String,
    val name: String,
    val notes: String = "",
    val isAdmin: Boolean = false,
    val colorHex: Long = 0xFF006A6AL
)

data class ApartmentExpense(
    val id: String,
    val date: String,
    val item: String,
    val amount: Double,
    val paidByRoommateId: String,
    val sharedByRoommateIds: List<String>,
    val notes: String = ""
) {
    val sharingCount: Int
        get() = sharedByRoommateIds.size

    val shareEach: Double
        get() = if (sharingCount > 0) amount / sharingCount else 0.0
}

data class ApartmentSettlement(
    val id: String,
    val date: String,
    val fromRoommateId: String,
    val toRoommateId: String,
    val amount: Double,
    val note: String = ""
)

data class ApartmentProfile(
    val name: String = "Apartment Flat 402",
    val flatNumber: String = "Flat 402",
    val currencySymbol: String = "₹",
    val inviteCode: String = "APT402"
)

data class RoommateBalanceSummary(
    val roommate: ApartmentRoommate,
    val totalPaidGroceries: Double,
    val totalOwedShare: Double,
    val paidToOthers: Double,
    val receivedFromOthers: Double,
    val netBalance: Double
) {
    val status: String
        get() = when {
            netBalance > 0.005 -> "Is owed money"
            netBalance < -0.005 -> "Owes money"
            else -> "Settled"
        }
}

data class DebtTransfer(
    val fromRoommate: ApartmentRoommate,
    val toRoommate: ApartmentRoommate,
    val amount: Double
)
