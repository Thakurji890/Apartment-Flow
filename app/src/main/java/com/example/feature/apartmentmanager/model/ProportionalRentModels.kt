package com.example.feature.apartmentmanager.model

/**
 * Strategy for proportional rent calculation.
 */
enum class RentSplitStrategy(val label: String, val description: String) {
    SQUARE_FOOTAGE(
        "Room Square Footage",
        "Split rent strictly proportional to each roommate's private bedroom square footage, with common area space shared equally."
    ),
    AMENITY_BASED(
        "Amenities & Perks (Master/Shared Bath)",
        "Adjusts base bedroom share with premium adjustments (e.g. Master Bedroom private bath, walk-in closet, balcony, parking)."
    ),
    INCOME_RATIO(
        "Income / Earnings Ratio",
        "Calculates fair rent shares proportional to each roommate's monthly take-home salary or income."
    ),
    HYBRID_CUSTOM(
        "Custom Room Weights",
        "Assign custom point values or weights per room to calculate tailored rent distribution."
    ),
    EQUAL(
        "Equal Split",
        "Divides the total monthly rent equally across all roommates in the household."
    )
}

data class RoomRentShare(
    val roommateId: String,
    val roommateName: String,
    val roomName: String,
    val calculatedRent: Double,
    val percentageOfTotal: Double,
    val detailNotes: String
)

data class ProportionalRentCalculationResult(
    val totalRent: Double,
    val strategy: RentSplitStrategy,
    val shares: List<RoomRentShare>,
    val commonAreaRent: Double = 0.0,
    val privateRoomsRent: Double = 0.0
)
