package com.example.feature.apartment.domain.usecase

import com.example.feature.apartment.domain.model.Apartment
import com.example.feature.apartment.domain.repository.ApartmentRepository
import javax.inject.Inject

class CreateApartmentUseCase @Inject constructor(
    private val repository: ApartmentRepository
) {
    suspend operator fun invoke(apartment: Apartment) = repository.createApartment(apartment)
}
