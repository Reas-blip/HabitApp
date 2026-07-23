package android.learn.habitapp.ui.theme

import android.os.Build
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography( // Display Styles (Large, prominent text)
   displayLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 57.sp,
      lineHeight = 64.sp,
      letterSpacing = (-0.25).sp
   ),
   displayMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 45.sp,
      lineHeight = 52.sp,
      letterSpacing = 0.sp
   ),
   displaySmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 36.sp,
      lineHeight = 44.sp,
      letterSpacing = 0.sp
   ),

   // Headline Styles (Heading elements)
   headlineLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 32.sp,
      lineHeight = 40.sp,
      letterSpacing = 0.sp
   ),
   headlineMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 28.sp,
      lineHeight = 36.sp,
      letterSpacing = 0.sp
   ),
   headlineSmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 24.sp,
      lineHeight = 32.sp,
      letterSpacing = 0.sp
   ),

   // Title Styles (Sub-headings, top app bars, dialog titles)
   titleLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 22.sp,
      lineHeight = 28.sp,
      letterSpacing = 0.sp
   ),
   titleMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.15.sp
   ),
   titleSmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1.sp
   ),

   // Body Styles (Main paragraph text, list items)
   bodyLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.5.sp
   ),
   bodyMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25.sp
   ),
   bodySmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.4.sp
   ),

   // Label Styles (Buttons, captions, overlines, badges)
   labelLarge = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1.sp
   ),
   labelMedium = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5.sp
   ),
   labelSmall = TextStyle(
      fontFamily = FontFamily.Default,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5.sp
   )
)

private val DarkColorScheme = darkColorScheme(
   primary = Purple80,
   secondary = PurpleGrey80,
   tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
   primary = Color(0xFFB9F6CA),
   onPrimary = Color(0xFF1B5E20),
   primaryContainer = Color(0xFFF3F4F6),
   onPrimaryContainer = Color(0xFF4CAF50),
   surface = Color(0xFFF0F7F1),
   onSurface = Color(0xFFFFFFFF),

   secondary = PurpleGrey40,
   secondaryContainer = Color(0xFF1B5E20),
   onSecondaryContainer = Color.White,
   tertiary = Pink40,
   /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@OptIn(ExperimentalSharedTransitionApi::class)
val CustomSpringTransformSpec = BoundsTransform { _, _ ->
   spring(
      dampingRatio = 0.78f, // Controls the bounce (under 1.0f gives a slight elastic kick)
      stiffness = 450f      // Controls how fast the container accelerates to full size
   )
}

val LocalTransformSpec = compositionLocalOf { CustomSpringTransformSpec }
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
   error("No SharedTransitionScope is provided wrap your composable with CompositionLocalProvider inside")
}

@Composable
fun HabitAppTheme(
   darkTheme: Boolean = isSystemInDarkTheme(),
   // Dynamic color is available on Android 12+
   dynamicColor: Boolean = true,
   content: @Composable () -> Unit
) {
   val colorScheme = LightColorScheme

//      when {
//         dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//         }
//
//         darkTheme -> DarkColorScheme
//         else -> LightColorScheme
//      }
   MaterialTheme(
      colorScheme = colorScheme,
      typography = AppTypography,
      content = {
         SharedTransitionLayout {
            CompositionLocalProvider(
               LocalTransformSpec provides CustomSpringTransformSpec,
               LocalSharedTransitionScope provides this
            ) {
               content()
            }
         }
      }
   )
}