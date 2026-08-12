package com.example.di

import com.example.feature.chore.data.repository.ChoreRepositoryImpl
import com.example.feature.chore.domain.repository.ChoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChoreModule {

    @Binds
    @Singleton
    abstract fun bindChoreRepository(
        choreRepositoryImpl: ChoreRepositoryImpl
    ): ChoreRepository
}
