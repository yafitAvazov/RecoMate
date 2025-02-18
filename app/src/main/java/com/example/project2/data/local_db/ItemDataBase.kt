package com.example.project2.data.local_db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.project2.data.model.Item

@Database(entities = [Item::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class) // ✅ TypeConverters for lists
abstract class ItemDataBase : RoomDatabase() {

    abstract fun itemsDao(): ItemDao

    companion object {

        @Volatile
        private var instance: ItemDataBase? = null

        fun getDatabase(context: Context): ItemDataBase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ItemDataBase::class.java,
                    "items_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // ✅ Added the new migration
                    .build().also { instance = it }
            }
        }

        // Migration from version 1 to 2 (already present)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE review_table ADD COLUMN item_comments TEXT DEFAULT '[]' NOT NULL")
            }
        }

        // 🆕 Migration from version 2 to 3 to handle schema updates
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ensure the new column is added if needed (e.g., `item_rating` or other changes)
                db.execSQL("ALTER TABLE review_table ADD COLUMN item_rating INTEGER DEFAULT 0 NOT NULL")
            }
        }
    }
}


