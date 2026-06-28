package android.learn.habitapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
   @Insert(onConflict = OnConflictStrategy.REPLACE)
   fun insertHabit(habit: HabitEntity)

   @Query("DELETE FROM habits WHERE id = :habitId")
   fun deleteHabit(habitId: Int)

   @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
   fun deleteHabitLog(habitId: Int, date: Long)

   @Insert(onConflict = OnConflictStrategy.REPLACE)
   fun insertHabitLog(habitLog: HabitLogsEntity)

   @Transaction
   @Query("SELECT * from habits")
   fun getHabitsWithLogs(): Flow<List<HabitWithLogs>>

   @Transaction
   @Query("SELECT * from habits where id =:habitId")
   fun loadHabitWithLogs(habitId: Int): Flow<HabitWithLogs>

   @Query("SELECT * from habits WHERE id = :habitId")
   fun loadHabit(habitId: Int): HabitEntity

}