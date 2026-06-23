package android.learn.habitapp.data.repository

import android.learn.habitapp.data.local.HabitDao
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitLogsEntity
import android.learn.habitapp.data.local.HabitWithLogs
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface HabitRepository {
   suspend fun insertHabit(habit: HabitEntity)

   suspend fun insertHabitLog(habitLog: HabitLogsEntity)
   suspend fun deleteHabitLog(habitId: Int, today: Long)

   suspend fun deleteHabit(habitId: Int)

   fun getHabitWithLogs(): Flow<List<HabitWithLogs>>


}

class HabitRepositoryImpl @Inject constructor (
   private val habitDao: HabitDao
): HabitRepository {
   override suspend fun insertHabit(habit: HabitEntity) {
      habitDao.insertHabit(habit)
   }

   override suspend fun insertHabitLog(habitLog: HabitLogsEntity) {
      habitDao.insertHabitLog(habitLog)
   }

   override suspend fun deleteHabitLog(habitId: Int, today: Long) {
      habitDao.deleteHabitLog(habitId, today)
   }

   override suspend fun deleteHabit(habitId: Int) {
      habitDao.deleteHabit(habitId)
   }


   override fun getHabitWithLogs(): Flow<List<HabitWithLogs>> {
      return habitDao.getHabitsWithLogs()
   }

}



