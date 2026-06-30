
package android.learn.habitapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
   // 1. Provide default clean fades so shared elements don't clash with harsh slides
   noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
      fadeIn(animationSpec = tween(300))
   },
   noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
      fadeOut(animationSpec = tween(300))
   },
   noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
      fadeIn(animationSpec = tween(300))
   },
   noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
      fadeOut(animationSpec = tween(300))
   },
   noinline content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
   composable<T>(
      enterTransition = enterTransition,
      exitTransition = exitTransition,
      popEnterTransition = popEnterTransition,
      popExitTransition = popExitTransition
   ) { backStackEntry ->
      // 'this' inside composable<T> is an AnimatedContentTransitionScope,
      // which implements AnimatedVisibilityScope
      CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
         content(backStackEntry)
      }
   }
}
