package com.example.feature.apartment.domain.usecase

import com.example.feature.apartment.domain.repository.ApartmentRepository
import javax.inject.Inject

class CheckHasApartmentUseCase @Inject constructor(
    private val repository: ApartmentRepository
) {
    suspend operator fun invoke() = repository.hasApartment()
}
