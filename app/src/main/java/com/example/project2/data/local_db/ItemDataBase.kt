package com.example.project2.data.local_db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.project2.data.model.Item

@Database(entities = [Item::class], version = 6, exportSchema = false) // 🔥 העלאת מספר גרסה
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,MIGRATION_5_6) // ✅ עדכון ה-Migration
                    .fallbackToDestructiveMigration() // 🔥 אם יש בעיה, מבצע מחיקה מלאה
                    .build().also { instance = it }
            }
        }

        // ✅ Migrations שמירת נתונים בין גרסאות
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE review_table ADD COLUMN item_comments TEXT DEFAULT '[]' NOT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE review_table ADD COLUMN isLiked INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE review_table ADD COLUMN isLiked INTEGER NOT NULL DEFAULT 0")
            }
        }
        // ✅ הגירת גרסה 4 ל-5 (אם צריך)
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE review_table ADD COLUMN new_column_name TEXT DEFAULT '' NOT NULL") // 🔥 אם צריך להוסיף עמודות חדשות
            }
        }
        // ✅ הגירת גרסה 4 ל-5 (אם צריך)
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE review_table ADD COLUMN new_column_name TEXT DEFAULT '' NOT NULL") // 🔥 אם צריך להוסיף עמודות חדשות
            }
        }

    }
}
