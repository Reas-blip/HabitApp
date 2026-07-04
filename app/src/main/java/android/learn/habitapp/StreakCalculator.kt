package android.learn.habitapp

// StreakCalculator.kt
fun calculateCurrentStreak(logDates: List<Long>): Int {
   if (logDates.isEmpty()) return 0

   val oneDayMillis = 24 * 60 * 60 * 1000L
   val sortedDesc = logDates.toSortedSet(compareByDescending { it })
   val today = getStartOfTodayTimestamp()

   var expected = today
   if (expected !in sortedDesc) expected -= oneDayMillis // today not done yet, count from yesterday

   var streak = 0
   for (date in sortedDesc) {
      when {
         date == expected -> {
            streak++
            expected -= oneDayMillis
         }
         date < expected -> return streak // gap found
      }
   }
   return streak
}