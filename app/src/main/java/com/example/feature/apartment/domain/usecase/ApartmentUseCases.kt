package com.example.feature.apartment.domain.usecase

import javax.inject.Inject

data class ApartmentUseCases @Inject constructor(
    val checkHasApartment: CheckHasApartmentUseCase,
    val createApartment: CreateApartmentUseCase,
    val joinApartment: JoinApartmentUseCase,
    val getUserApartments: GetUserApartmentsUseCase,
    val getApartment: GetApartmentUseCase,
    val getApartmentMembers: GetApartmentMembersUseCase,
    val getInviteCode: GetInviteCodeUseCase,
    val generateInviteCode: GenerateInviteCodeUseCase
)
