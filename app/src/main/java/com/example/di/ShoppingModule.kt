package com.example.di

import com.example.feature.shopping.data.repository.ShoppingRepositoryImpl
import com.example.feature.shopping.domain.repository.ShoppingRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShoppingModule {

    @Provides
    @Singleton
    fun provideShoppingRepository(
        firestore: FirebaseFirestore
    ): ShoppingRepository {
        return ShoppingRepositoryImpl(firestore)
    }
}
