package com.example.feature.apartmentmanager.domain

import com.example.feature.apartmentmanager.model.ProportionalRentCalculationResult
import com.example.feature.apartmentmanager.model.RentSplitStrategy
import com.example.feature.apartmentmanager.model.RoomRentShare
import kotlin.math.roundToInt

/**
 * Domain engine for calculating proportional rent splits based on:
 * 1. Room Square Footage (Private sq ft proportion + equal common area split)
 * 2. Amenities & Perks (Master bed with en-suite bath, balcony, parking perks vs shared bath)
 * 3. Income / Earnings Ratio (Proportional to monthly income)
 * 4. Custom Room Weights
 */
object ProportionalRentCalculatorEngine {

    data class SquareFootageInput(
        val roommateId: String,
        val roommateName: String,
        val bedroomName: String,
        val bedroomSqFt: Double
    )

    data class AmenityInput(
        val roommateId: String,
        val roommateName: String,
        val bedroomName: String,
        val basePoints: Double = 100.0,
        val hasPrivateEnsuiteBath: Boolean = false, // +25%
        val hasBalconyPatio: Boolean = false,       // +10%
        val hasWalkInCloset: Boolean = false,       // +10%
        val hasDedicatedParking: Boolean = false,    // +15%
        val hasBetterNaturalLight: Boolean = false   // +5%
    ) {
        val totalScore: Double
            get() {
                var score = basePoints
                if (hasPrivateEnsuiteBath) score += 25.0
                if (hasBalconyPatio) score += 10.0
                if (hasWalkInCloset) score += 10.0
                if (hasDedicatedParking) score += 15.0
                if (hasBetterNaturalLight) score += 5.0
                return score
            }
    }

    data class IncomeRatioInput(
        val roommateId: String,
        val roommateName: String,
        val monthlyIncome: Double
    )

    data class CustomWeightInput(
        val roommateId: String,
        val roommateName: String,
        val roomName: String,
        val weight: Double
    )

    /**
     * Calculate rent based on square footage.
     * commonAreaPercent: portion of total rent assigned to shared living/kitchen space (e.g. 30%).
     * That portion is split equally, while the remaining (70%) is split proportional to bedroom sq ft.
     */
    fun calculateBySquareFootage(
        totalRent: Double,
        inputs: List<SquareFootageInput>,
        commonAreaPercent: Double = 25.0
    ): ProportionalRentCalculationResult {
        if (inputs.isEmpty() || totalRent <= 0.0) {
            return ProportionalRentCalculationResult(totalRent, RentSplitStrategy.SQUARE_FOOTAGE, emptyList())
        }

        val totalSqFt = inputs.sumOf { it.bedroomSqFt }
        val commonPortion = (totalRent * (commonAreaPercent.coerceIn(0.0, 80.0) / 100.0))
        val privatePortion = totalRent - commonPortion
        val commonPerPerson = if (inputs.isNotEmpty()) commonPortion / inputs.size else 0.0

        val shares = inputs.map { input ->
            val ratio = if (totalSqFt > 0) input.bedroomSqFt / totalSqFt else 1.0 / inputs.size
            val privateRent = privatePortion * ratio
            val totalShare = (privateRent + commonPerPerson)
            val rounded = (totalShare * 100.0).roundToInt() / 100.0
            val percent = if (totalRent > 0) (rounded / totalRent) * 100.0 else 0.0

            RoomRentShare(
                roommateId = input.roommateId,
                roommateName = input.roommateName,
                roomName = input.bedroomName,
                calculatedRent = rounded,
                percentageOfTotal = (percent * 10.0).roundToInt() / 10.0,
                detailNotes = "${input.bedroomSqFt.roundToInt()} sq ft (${(ratio * 100).roundToInt()}% private room) + equal common area"
            )
        }

        return ProportionalRentCalculationResult(
            totalRent = totalRent,
            strategy = RentSplitStrategy.SQUARE_FOOTAGE,
            shares = shares,
            commonAreaRent = (commonPortion * 100.0).roundToInt() / 100.0,
            privateRoomsRent = (privatePortion * 100.0).roundToInt() / 100.0
        )
    }

    /**
     * Calculate rent based on amenities (Master bedroom vs Shared bath, balcony, etc.)
     */
    fun calculateByAmenities(
        totalRent: Double,
        inputs: List<AmenityInput>
    ): ProportionalRentCalculationResult {
        if (inputs.isEmpty() || totalRent <= 0.0) {
            return ProportionalRentCalculationResult(totalRent, RentSplitStrategy.AMENITY_BASED, emptyList())
        }

        val totalPoints = inputs.sumOf { it.totalScore }

        val shares = inputs.map { input ->
            val ratio = if (totalPoints > 0) input.totalScore / totalPoints else 1.0 / inputs.size
            val rentShare = totalRent * ratio
            val rounded = (rentShare * 100.0).roundToInt() / 100.0
            val percent = if (totalRent > 0) (rounded / totalRent) * 100.0 else 0.0

            val perks = mutableListOf<String>()
            if (input.hasPrivateEnsuiteBath) perks.add("Private Bath") else perks.add("Shared Bath")
            if (input.hasBalconyPatio) perks.add("Balcony")
            if (input.hasWalkInCloset) perks.add("Walk-in Closet")
            if (input.hasDedicatedParking) perks.add("Parking")
            if (input.hasBetterNaturalLight) perks.add("Sunny View")

            RoomRentShare(
                roommateId = input.roommateId,
                roommateName = input.roommateName,
                roomName = input.bedroomName,
                calculatedRent = rounded,
                percentageOfTotal = (percent * 10.0).roundToInt() / 10.0,
                detailNotes = perks.joinToString(", ")
            )
        }

        return ProportionalRentCalculationResult(
            totalRent = totalRent,
            strategy = RentSplitStrategy.AMENITY_BASED,
            shares = shares
        )
    }

    /**
     * Calculate rent proportional to income ratio.
     */
    fun calculateByIncomeRatio(
        totalRent: Double,
        inputs: List<IncomeRatioInput>
    ): ProportionalRentCalculationResult {
        if (inputs.isEmpty() || totalRent <= 0.0) {
            return ProportionalRentCalculationResult(totalRent, RentSplitStrategy.INCOME_RATIO, emptyList())
        }

        val totalIncome = inputs.sumOf { it.monthlyIncome }

        val shares = inputs.map { input ->
            val ratio = if (totalIncome > 0) input.monthlyIncome / totalIncome else 1.0 / inputs.size
            val rentShare = totalRent * ratio
            val rounded = (rentShare * 100.0).roundToInt() / 100.0
            val percent = if (totalRent > 0) (rounded / totalRent) * 100.0 else 0.0

            RoomRentShare(
                roommateId = input.roommateId,
                roommateName = input.roommateName,
                roomName = "Income-based",
                calculatedRent = rounded,
                percentageOfTotal = (percent * 10.0).roundToInt() / 10.0,
                detailNotes = "Monthly Income: ${"%.0f".format(input.monthlyIncome)} (${(ratio * 100).roundToInt()}% of house income)"
            )
        }

        return ProportionalRentCalculationResult(
            totalRent = totalRent,
            strategy = RentSplitStrategy.INCOME_RATIO,
            shares = shares
        )
    }

    /**
     * Calculate rent based on custom weights.
     */
    fun calculateByCustomWeights(
        totalRent: Double,
        inputs: List<CustomWeightInput>
    ): ProportionalRentCalculationResult {
        if (inputs.isEmpty() || totalRent <= 0.0) {
            return ProportionalRentCalculationResult(totalRent, RentSplitStrategy.HYBRID_CUSTOM, emptyList())
        }

        val totalWeight = inputs.sumOf { it.weight.coerceAtLeast(0.1) }

        val shares = inputs.map { input ->
            val ratio = input.weight.coerceAtLeast(0.1) / totalWeight
            val rentShare = totalRent * ratio
            val rounded = (rentShare * 100.0).roundToInt() / 100.0
            val percent = if (totalRent > 0) (rounded / totalRent) * 100.0 else 0.0

            RoomRentShare(
                roommateId = input.roommateId,
                roommateName = input.roommateName,
                roomName = input.roomName,
                calculatedRent = rounded,
                percentageOfTotal = (percent * 10.0).roundToInt() / 10.0,
                detailNotes = "Weight: ${input.weight} / $totalWeight"
            )
        }

        return ProportionalRentCalculationResult(
            totalRent = totalRent,
            strategy = RentSplitStrategy.HYBRID_CUSTOM,
            shares = shares
        )
    }
}
