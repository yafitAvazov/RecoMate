package com.example.project2.di

import com.example.project2.data.local_db.ItemDao
import com.example.project2.data.repository.ItemRepository
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
}
