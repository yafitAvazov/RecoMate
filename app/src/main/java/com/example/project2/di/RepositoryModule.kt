package com.example.project2.di

import com.example.project2.data.local_db.ItemDao
import com.example.project2.data.repository.AuthRepository
import com.example.project2.data.repository.ItemRepository
import com.example.project2.data.repository.firebaseImpl.AuthRepositoryFirebase
import com.example.project2.data.repository.firebaseImpl.ItemRepositoryFirebase
import com.example.project2.data.repository.local.ItemRepositoryLocal
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideItemRepositoryLocal(itemDao: ItemDao): ItemRepositoryLocal {
        return ItemRepositoryLocal(itemDao)
    }

    @Provides
    @Singleton
    fun provideItemRepositoryFirebase(): ItemRepositoryFirebase {
        return ItemRepositoryFirebase()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryFirebase()
    }

    @Provides
    @Singleton
    fun provideItemRepository(
        itemRepositoryLocal: ItemRepositoryLocal,
        itemRepositoryFirebase: ItemRepositoryFirebase
    ): ItemRepository {
        return ItemRepository(itemRepositoryLocal, itemRepositoryFirebase)
    }
}
