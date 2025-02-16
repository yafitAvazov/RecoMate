package com.example.project2.di

import android.content.Context
import androidx.room.Room
import com.example.project2.data.local_db.ItemDao
import com.example.project2.data.local_db.ItemDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ItemDataBase {
        return Room.databaseBuilder(
            context,
            ItemDataBase::class.java,
            "items_db"
        )
            .fallbackToDestructiveMigration() // ✅ הוספת פונקציה לטיפול בשינויים במסד הנתונים
            .build()
    }

    @Provides
    fun provideItemDao(database: ItemDataBase): ItemDao {
        return database.itemsDao()
    }
}
