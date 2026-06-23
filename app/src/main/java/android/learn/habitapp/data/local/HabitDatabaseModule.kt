package android.learn.habitapp.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object HabitDatabaseModule {

   @Provides
   @Singleton
   fun provideDatabase(@ApplicationContext context: Context): HabitDatabase {
      return Room.databaseBuilder(
         context,
         HabitDatabase::class.java,
         "habit_database"
      ).build()
   }

   @Provides
   fun provideHabitDao(database: HabitDatabase): HabitDao {
      return database.habitDao()
   }
}
