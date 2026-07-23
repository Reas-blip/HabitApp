package android.learn.habitapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class FrequencyType {
   DAILY,
   SPECIFIC_DAYS,
   TIMES_PER_WEEK
}

@Entity(tableName = "habits")
data class HabitEntity(
   @PrimaryKey(autoGenerate = true)
   val id: Int = 0,
   val groupId: String = UUID.randomUUID().toString(),
   val name: String,
   val emoji: String,

   val frequencyType: FrequencyType = FrequencyType.DAILY,
   val customDays: String? = null,   // CSV of DayOfWeek names, only used if SPECIFIC_DAYS
   val timesPerWeek: Int? = null,    // only used if TIMES_PER_WEEK
   val reminderTime: String? = null, // stored as "HH:mm", null = no reminder

   val color: Int? = null,
   val sortOrder: Int = 0,
   val isArchived: Boolean = false,
   val isReplaced: Boolean = false,

   val replacedAt: Long? = null,
   val createdAt: Long = System.currentTimeMillis()
)
