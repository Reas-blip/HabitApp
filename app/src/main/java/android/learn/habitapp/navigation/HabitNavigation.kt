package android.learn.habitapp.navigation
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
sealed interface Destination

@Serializable
data object HabitListScreen : Destination

@Serializable
data object ArchivedHabitScreen: Destination
@Serializable
data class HabitDetail(
   val habitId: Int? = null, val currentToken: Long = 0L) : Destination


sealed interface DestinationObject {
   val name: String
   val routeClass: KClass<out Destination>

   data object HabitListObject : DestinationObject {
      override val name = "Dashboard"
      override val routeClass = HabitListScreen::class
   }

   data object ArchivedHabitObject : DestinationObject {
      override val name = "Archived"
      override val routeClass = ArchivedHabitScreen::class
   }
}
