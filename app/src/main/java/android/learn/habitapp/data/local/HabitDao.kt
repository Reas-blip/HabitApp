package android.learn.habitapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface HabitDao {
   @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insertHabit(habit: HabitEntity)

   @Update
   suspend fun updateHabit(habit: HabitEntity)

   @Query("DELETE FROM habits WHERE id = :habitId")
   suspend fun deleteHabit(habitId: Int)

   @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
   suspend fun deleteHabitLog(habitId: Int, date: Long)

   @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insertHabitLog(habitLog: HabitLogsEntity)

   @Query("SELECT * from habits WHERE id = :habitId")
   suspend fun loadHabit(habitId: Int): HabitEntity

   @Transaction
   @Query("SELECT * from habits WHERE isArchived = 0 ORDER BY sortOrder ASC")
   fun getHabitsWithLogs(): Flow<List<HabitWithLogs>>

   @Transaction
   @Query("SELECT * from habits WHERE id = :habitId")
   suspend fun loadHabitWithLogs(habitId: Int): HabitWithLogs

   @Query("UPDATE habits SET isArchived = 1 WHERE id = :habitId")
   suspend fun archiveHabit(habitId: Int)

   @Query("UPDATE habits SET sortOrder = :newOrder WHERE id = :habitId")
   suspend fun updateSortOrder(habitId: Int, newOrder: Int)

   @Transaction
   suspend fun updateSortOrders(orderedIds: List<Int>) {
      orderedIds.forEachIndexed { index, id ->
         updateSortOrder(id, index)
      }
   }
}