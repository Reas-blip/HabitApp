package android.learn.habitapp.data.repository

import android.learn.habitapp.data.local.HabitDao
import android.learn.habitapp.data.local.HabitEntity
import android.learn.habitapp.data.local.HabitLogsEntity
import android.learn.habitapp.data.local.HabitPreferences
import android.learn.habitapp.data.local.HabitWithLogs
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface HabitRepository {
   suspend fun insertHabit(habit: HabitEntity)
   suspend fun updateHabit(habit: HabitEntity)
   suspend fun updateSortOrders(idsInOrder: List<Int>)
   suspend fun insertHabitLog(habitLog: HabitLogsEntity)
   suspend fun deleteHabitLog(habitId: Int, today: Long)

   suspend fun deleteHabit(habitId: Int)

   suspend fun load(habitId: Int): HabitEntity

   fun getHabitsWithLogs(): Flow<List<HabitWithLogs>>

   suspend fun loadHabitWithLogs(habitId: Int): HabitWithLogs

   suspend fun archiveHabit(habitId: Int)
   suspend fun updateSortOrder(habitId: Int, newOrder: Int)
   val hasSeenSwipeHint: Flow<Boolean>
   suspend fun setSwipeHintSeen()
}

class HabitRepositoryImpl @Inject constructor(
   private val habitDao: HabitDao,
   private val habitPreferences: HabitPreferences,
) : HabitRepository {
   override suspend fun insertHabit(habit: HabitEntity) {
      habitDao.insertHabit(habit)
   }

   override val hasSeenSwipeHint: Flow<Boolean> = habitPreferences.hasSeenSwipeHint

   override suspend fun setSwipeHintSeen() {
      habitPreferences.setSwipeHintSeen()
   }

   override suspend fun updateSortOrders(idsInOrder: List<Int>) {
      habitDao.updateSortOrders(idsInOrder)
   }

   override suspend fun updateHabit(habit: HabitEntity) {
      habitDao.updateHabit(habit)
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

   override suspend fun loadHabitWithLogs(habitId: Int): HabitWithLogs {
      return habitDao.loadHabitWithLogs(habitId)
   }

   override suspend fun archiveHabit(habitId: Int) {
      habitDao.archiveHabit(habitId)
   }

   override suspend fun updateSortOrder(habitId: Int, newOrder: Int) {
      habitDao.updateSortOrder(habitId, newOrder)
   }
}
