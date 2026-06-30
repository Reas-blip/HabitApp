package android.learn.habitapp.navigation

data class HabitSharedElementKey(
   val habitId: Int,
   val type: HabitSharedElementType
)
enum class HabitSharedElementType {
  Background,
   Bounds,
   Emoji,

}