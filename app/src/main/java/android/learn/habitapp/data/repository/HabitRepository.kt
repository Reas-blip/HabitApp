package android.learn.habitapp.data.repository

import android.learn.habitapp.data.local.HabitDao
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitLogsEntity
import android.learn.habitapp.data.local.HabitWithLogs
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface HabitRepository {
   suspend fun insertHabit(habit: HabitEntity)

   suspend fun insertHabitLog(habitLog: HabitLogsEntity)
   suspend fun deleteHabitLog(habitId: Int, today: Long)

   suspend fun deleteHabit(habitId: Int)

    suspend fun load(habitId: Int): HabitEntity

   fun getHabitsWithLogs(): Flow<List<HabitWithLogs>>

   fun loadHabitWithLogs(habitId: Int): Flow<HabitWithLogs>


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

   override suspend fun load(habitId: Int): HabitEntity {
     return habitDao.loadHabit(habitId)
   }


   override fun getHabitsWithLogs(): Flow<List<HabitWithLogs>> {
      return habitDao.getHabitsWithLogs()
   }

   override fun loadHabitWithLogs(habitId: Int): Flow<HabitWithLogs> {
      return habitDao.loadHabitWithLogs(habitId)
   }

}



