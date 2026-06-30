package android.learn.habitapp.navigation

data class HabitSharedElementKey(
   val habitId: Int,
   val token: Long = 0L,
   val type: HabitSharedElementType
)
enum class HabitSharedElementType {
  Background,
   Bounds,
   Emoji,

}