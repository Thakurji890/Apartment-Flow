package com.example.feature.apartment.domain.usecase

import com.example.feature.apartment.domain.repository.ApartmentRepository
import javax.inject.Inject

class GetInviteCodeUseCase @Inject constructor(
    private val repository: ApartmentRepository
) {
    suspend operator fun invoke(apartmentId: String) = repository.getActiveInviteCode(apartmentId)
}

class GenerateInviteCodeUseCase @Inject constructor(
    private val repository: ApartmentRepository
) {
    suspend operator fun invoke(apartmentId: String) = repository.generateInviteCode(apartmentId)
}
