package android.learn.habitapp.navigation

sealed class HabitSharedElementType {
   data class Background(val id: Int): HabitSharedElementType()
   data class Bounds(val id: Int): HabitSharedElementType()
   data class Emoji(val id: Int): HabitSharedElementType()
   data class Title(val id: Int): HabitSharedElementType()
}