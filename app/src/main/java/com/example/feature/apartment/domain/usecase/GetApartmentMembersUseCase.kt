package com.example.feature.apartment.domain.usecase

import com.example.feature.apartment.domain.repository.ApartmentRepository
import javax.inject.Inject

class GetApartmentMembersUseCase @Inject constructor(
    private val repository: ApartmentRepository
) {
    operator fun invoke(apartmentId: String) = repository.getApartmentMembers(apartmentId)
}
