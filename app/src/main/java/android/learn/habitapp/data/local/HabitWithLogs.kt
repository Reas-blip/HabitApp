package android.learn.habitapp.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class HabitWithLogs (
   @Embedded val habit: HabitEntity,
   @Relation(
      parentColumn = "id",
      entityColumn = "habitId"
   )
   val logs: List<HabitLogsEntity>
)