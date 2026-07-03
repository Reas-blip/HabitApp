package android.learn.habitapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject
import kotlin.jvm.java

@TypeConverters(HabitConverters::class)
@Database(
   entities = [
      HabitEntity::class,
      HabitLogsEntity::class
   ],
   version = 2
)
abstract class HabitDatabase : RoomDatabase() {
   companion object {
      lateinit var habitDatabase: HabitDatabase

      val MIGRATION_1_2 = object : Migration(1, 2) {
         override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE habits ADD COLUMN frequencyType TEXT NOT NULL DEFAULT 'DAILY'")
            db.execSQL("ALTER TABLE habits ADD COLUMN customDays TEXT")
            db.execSQL("ALTER TABLE habits ADD COLUMN timesPerWeek INTEGER")
            db.execSQL("ALTER TABLE habits ADD COLUMN reminderTime TEXT")
            db.execSQL("ALTER TABLE habits ADD COLUMN color INTEGER")
            db.execSQL("ALTER TABLE habits ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE habits ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
         }
      }

      fun getDatabase(applicationContext: Context): HabitDatabase {
         if (!(::habitDatabase.isInitialized)) {
            habitDatabase = Room.databaseBuilder(
               applicationContext,
               HabitDatabase::class.java,
               "habit_database"
            )
               .addMigrations(MIGRATION_1_2)
               .build()
         }
         return habitDatabase
      }
   }

   abstract fun habitDao(): HabitDao
}
