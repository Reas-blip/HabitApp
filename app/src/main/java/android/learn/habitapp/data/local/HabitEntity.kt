package android.learn.habitapp.data.local

import android.learn.habitapp.ui.HabitUiState
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "habits")
data class HabitEntity (
   @PrimaryKey(autoGenerate = true)
   val id: Int = 0,
   val name: String,
   val emoji: String,
   val createdAt: Long = System.currentTimeMillis()

) {
   fun getStartOfTodayTimestamp(): Long {
   return LocalDate.now(ZoneId.systemDefault())
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant()
      .toEpochMilli()
}
   fun HabitEntity.toUiState(): HabitUiState {
      return HabitUiState(
         this.id,
         this.name,
         this.emoji,
        false
      )

   }
}