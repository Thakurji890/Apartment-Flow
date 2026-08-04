package com.example.feature.apartment.domain.usecase

import com.example.feature.apartment.domain.model.Apartment
import com.example.feature.apartment.domain.repository.ApartmentRepository
import javax.inject.Inject

class GetApartmentUseCase @Inject constructor(
    private val repository: ApartmentRepository
) {
    suspend operator fun invoke(id: String) = repository.getApartment(id)
}
