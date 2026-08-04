package com.example.feature.apartment.domain.usecase

import com.example.feature.apartment.domain.repository.ApartmentRepository
import javax.inject.Inject

class GetUserApartmentsUseCase @Inject constructor(
    private val repository: ApartmentRepository
) {
    operator fun invoke() = repository.getUserApartments()
}
