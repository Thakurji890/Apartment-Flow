package com.example.di

import com.example.feature.apartment.data.repository.ApartmentRepositoryImpl
import com.example.feature.apartment.domain.repository.ApartmentRepository
import com.example.feature.apartment.domain.usecase.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApartmentModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideApartmentRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ApartmentRepository {
        return ApartmentRepositoryImpl(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideApartmentUseCases(repository: ApartmentRepository): ApartmentUseCases {
        return ApartmentUseCases(
            checkHasApartment = CheckHasApartmentUseCase(repository),
            createApartment = CreateApartmentUseCase(repository),
            joinApartment = JoinApartmentUseCase(repository),
            getUserApartments = GetUserApartmentsUseCase(repository),
            getApartment = GetApartmentUseCase(repository),
            getApartmentMembers = GetApartmentMembersUseCase(repository),
            getInviteCode = GetInviteCodeUseCase(repository),
            generateInviteCode = GenerateInviteCodeUseCase(repository)
        )
    }
}
