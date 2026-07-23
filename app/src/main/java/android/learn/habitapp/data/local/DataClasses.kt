package android.learn.habitapp.data.local

// 1. Frequency Enum


// 2. Timeframe Enum for UI selection
enum class Timeframe {
   WEEK,
   MONTH,
   YEAR,
   DAY
}

data class HabitStatItem(
   val habitId: Int,
   val groupId: String,
   val habitName: String,
   val habitEmoji: String,
   val color: Int?,
   val completionPercent: Int,
   val totalDue: Int,
   val totalCompleted: Int
)

data class OverviewStats(
   val successRatePercent: Int,
   val grandTotalDue: Int,
   val grandTotalCompleted: Int,
   val habitStats: List<HabitStatItem>,
   val missedCount: Int? = null,
   val remainingDue: Int? = null,
)
