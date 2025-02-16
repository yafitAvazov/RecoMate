package com.example.project2.di

import com.example.project2.data.local_db.ItemDao
import com.example.project2.data.repository.AuthRepository
import com.example.project2.data.repository.ItemRepository
import com.example.project2.data.repository.firebaseImpl.AuthRepositoryFirebase
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
    fun provideItemRepository(itemDao: ItemDao): ItemRepository {
        return ItemRepository(itemDao)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryFirebase() // 🟢 ודאי שזהו ה-AuthRepository בשימוש
    }
}
