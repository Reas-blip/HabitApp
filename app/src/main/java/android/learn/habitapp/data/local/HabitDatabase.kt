package android.learn.habitapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import javax.inject.Inject
import kotlin.jvm.java


@Database(
   entities = [
      HabitEntity::class,
      HabitLogsEntity::class
   ],
   version = 1
)
abstract class HabitDatabase: RoomDatabase() {
   companion object {
      lateinit var habitDatabase: HabitDatabase

      fun getDatabase(applicationContext: Context): HabitDatabase {
         if (!(::habitDatabase.isInitialized)) {
            habitDatabase = Room.databaseBuilder(
               applicationContext,
               habitDatabase::class.java,
               "habit-db"
            ).build()
         }
         return habitDatabase
      }
   }

   abstract fun habitDao(): HabitDao
}

