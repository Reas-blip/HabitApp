package android.learn.habitapp.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope> {

   error("No AnimatedVisibilityScope is provided wrap your composable with CompositionLocalProvider inside")

}

inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
   noinline content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
   composable<T> { backStackEntry ->
      CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
         content(backStackEntry)
      }

   }
}