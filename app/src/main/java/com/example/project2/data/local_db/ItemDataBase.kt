package com.example.project2.data.local_db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.project2.data.model.Item

@Database(entities = [Item::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class) // ✅ הוספת TypeConverters כדי לשמור רשימות
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
                    .addMigrations(MIGRATION_1_2) // ✅ שומר את הנתונים ולא מוחק אותם
                    .build().also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE review_table ADD COLUMN item_comments TEXT DEFAULT '[]' NOT NULL")
            }
        }

    }
}
